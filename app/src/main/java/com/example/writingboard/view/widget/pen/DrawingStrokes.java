package com.example.writingboard.view.widget.pen;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.example.writingboard.view.widget.PathPoint;
import com.example.writingboard.view.widget.WritingState;
import com.example.writingboard.view.widget.WritingStep;

import java.util.Vector;

public class DrawingStrokes {
    private final static String TAG = DrawingStrokes.class.getSimpleName();
    public Paint drawingPaint;
    public Vector<TimePoint> timePoints = new Vector<>();
    public Vector<TimePoint> pointList;

    public Path strokesPath;
    public float lastLineX;
    public float lastLineY;

    public float lastX1;
    public float lastY1;
    public float lastX2;
    public float lastY2;
    public Bitmap drawingBitmap;
    public Canvas drawingCanvas;


    public TimePoint lastTop= new TimePoint();
    public TimePoint lastBottom=new TimePoint();
    public boolean isDown = false,isUp = false;
    public float lastK = 0;
    public View strokeView;
    public WritingState writingState;
    public WritingStep curStep;



    public DrawCurve drawCurve;

    public DrawingState state = DrawingState.NO_STATE;

    public float lastWidth;
    private float maxWidth;

    public DrawingStrokes(){
        writingState = WritingState.getInstance();
        this.strokesPath = new Path();
        pointList = new Vector<>();
    }

    public DrawingStrokes(View strokeView){
        writingState = WritingState.getInstance();
        this.strokeView = strokeView;
        this.strokesPath = new Path();
        pointList = new Vector<>();
    }
    public void setSize(Paint paint, Canvas canvas, Bitmap bitmap){
        this.drawingPaint = new Paint(paint);
        drawingCanvas = canvas;
        drawingBitmap = bitmap;
    }

    public void updatePaint(Paint paint) {
        drawingPaint = new Paint(paint);
    }



    /**
     * 获取宽度
     * @param pressure 压感值
     * @param widthDelta 宽度差
     * @return 宽度
     */
    private float strokeWidth(float pressure, float widthDelta){
        float width = Math.min(maxWidth   , (0.1f * (1 + pressure * (maxWidth * 10 - 1) )))
                * 0.9f + lastWidth * 0.1f;
        if(width> lastWidth)
            return Math.min(width  , lastWidth + widthDelta);
        else
            return Math.max(width , lastWidth - widthDelta);
    }

    public void setMaxWidth(float maxWidth){
        this.maxWidth = maxWidth;
    }

    public float getMaxWidth() {
        return maxWidth;
    }

    public void addPoint(TimePoint timePoint,float pressure){
        pointList.add(timePoint);
        if(pointList.size() > 3){
            Curve curve = new Curve(pointList.get(0),
                    pointList.get(1), pointList.get(2), pointList.get(3));
            float velocity = curve.point3.velocityFrom(curve.point2);
            float newWidth = strokeWidth(pressure, getWidthDelta(curve, velocity)) ;
            newWidth = Float.isNaN(newWidth) ? lastWidth : newWidth;
            if(curStep != null) {
                curStep.getStroke()
                        .addOriginPoint(new TimePoint(timePoint.x, timePoint.y));
                curStep.getStroke()
                        .addOriginWidth(newWidth);
            }
            if(isUp){
                if (curStep != null) {
                    curStep.getStroke()
                            .addOriginPoint(new TimePoint(timePoint.x, timePoint.y));
                    curStep.getStroke()
                            .addOriginWidth(newWidth);
                }
            }
            if (drawCurve == null) {
                drawCurve = new DrawCurve(curve, lastWidth, newWidth, drawingCanvas, drawingPaint);
                drawCurve.initLastPoint(lastTop, lastBottom);
            } else {
                drawCurve.updateData(lastWidth, newWidth, curve, drawingPaint);
            }
            drawCurve.draw(this);
            pointList.remove(0);
        }
    }

    private double mult(float x1, float y1, float x2, float y2, float x3, float y3){
        return (x1 - x3)*(y2 - y3) - (x2 - x3)*(y1 - y3);
    }
    boolean intersect(float x1, float y1, float x2, float y2, float x3, float y3,
                      float x4, float y4){
        if(Math.max(x1,x2)< Math.min(x3,x4)){
            return  false;
        }
        if(Math.max(y1,y2)< Math.min(y3,y4)){
            return false;
        }
        if(Math.max(x3,x4)< Math.min(x1,x2)){
            return false;
        }
        if(Math.max(y3,y4)< Math.min(y1,y2)){
            return false;
        }
        if(mult(x3,y3,x2,y2,x1,y1)*mult(x2,y2,x4,y4,x1,y1)<0){
            return false;
        }
        return !(mult(x1, y1, x4, y4, x3, y3) * mult(x4, y4, x2, y2, x3, y3) < 0);
    }

    public float calculateDegree(float x1,float y1,float x2,float y2,float x3,float y3){
        float b = (float) Math.sqrt((x1 - x2)*( x1 - x2) + (y1 - y2) * (y1 - y2));
        float c = (float) Math.sqrt((x2 - x3)*( x2 - x3) +
                (y2 - y3) * (y2 - y3));
        float a = (float) Math.sqrt((x1 - x3)*( x1 - x3) +
                (y1 - y3) * (y1 - y3));
        if(c==0||b==0) return 0;
        float sum = (b * b + c * c - a * a)/(2*b*c);
        float degree =(float) Math.acos(sum) * 180 / (float) Math.PI;
        Log.i(TAG, "degree : " + degree);
        if(Float.isNaN(degree)) degree = 0;
        return  degree;
    }


    public void moveTo(float x,float y,float pressure){
        strokesPath = new Path();
        timePoints.clear();
        pointList.clear();
        isDown = true;
        isUp = false;
        lastX2 = x;
        lastY2 = y;
        lastX1 = x;
        lastY1 = y;
        lastWidth = Math.min(maxWidth  , 0.1f * (1 + (pressure+0.2f) * (maxWidth * 10 - 1) ));
        lastK = 0;
        curStep = new WritingStep(new Stroke(), drawingPaint);
        addPoint(new TimePoint(x, y), pressure);
        addPoint(new TimePoint(x, y), pressure);
        curStep.getStroke().addOriginPoint(new TimePoint(x, y));
        curStep.getStroke().addOriginPoint(new TimePoint(x,y));
        curStep.getStroke().addOriginWidth(lastWidth);
        curStep.getStroke().addOriginWidth(lastWidth);
    }

    public void lineTo(float x,float y,float pressure,boolean isUp){
        if (isUp) {
            addPoint(new TimePoint(x, y), pressure);
            this.isUp = true;
            addPoint(new TimePoint(x, y), pressure);
            for(int i = timePoints.size() - 1; i>=0; i--){
                strokesPath.lineTo(timePoints.elementAt(i).getX(), timePoints.elementAt(i).getY());
                curStep.getStroke().addPoint(new TimePoint(timePoints.elementAt(i).getX(),
                        timePoints.elementAt(i).getY()));
            }
            timePoints.clear();
            curStep.setPath(strokesPath);
            writingState.getSteps().add(curStep);
            curStep = null;

        } else {
            addPoint(new TimePoint(x, y), pressure);
        }

    }

    public void draw(Canvas canvas, Paint paint) {
        Log.d("进入绘制","Yes");
        canvas.drawBitmap(drawingBitmap, 0, 0, paint);
    }


    private float getWidthDelta(Curve c, float v) {
        float dw;
        //通过速度获得步长
        if (v > 3) {
            c.steps = 4;
            dw = 3.0f;
        } else if (v > 2) {
            c.steps = 3;
            dw = 2.0f;
        } else if (v > 1) {
            c.steps = 3;
            dw = 1.0f;
        } else if (v > 0.5) {
            c.steps = 2;
            dw = 0.8f;
        } else if (v > 0.2) {
            c.steps = 2;
            dw = 0.6f;
        } else if (v > 0.1) {
            c.steps = 1;
            dw = 0.3f;
        } else {
            c.steps = 1;
            dw = 0.2f;
        }
        return dw;

    }


    public enum DrawingState{
        NO_STATE,
        X_ADD_Y_DEC,
        X_ADD_Y_SAM,
        X_DEC_Y_ADD,
        X_DEC_Y_DEC,
        X_DEC_Y_SAM,
        X_SAM_Y_ADD,
        X_SAM_Y_DEC,
        X_SAM_Y_SAM,
    }

}
