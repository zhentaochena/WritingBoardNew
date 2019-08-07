package com.example.writingboard.view.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;

public class MultiWriting {
    private SparseArray<Path> pathArray = new SparseArray<>(); //多点书写记录的path
    private SparseArray<PointF> pointArray = new SparseArray<>(); //多点书写记录的最后点的
    private WritingState state = WritingState.getInstance();

    public void onTouchEvent(MotionEvent event, Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.STROKE);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pathArray.clear();
            case MotionEvent.ACTION_POINTER_DOWN:
                start(event);
                break;
            case MotionEvent.ACTION_MOVE:
                move(event, canvas, paint);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                end(event, paint);
                break;
        }

    }

    private void start(MotionEvent event) {
        float x = event.getX(event.getActionIndex());
        float y = event.getY(event.getActionIndex());
        int index = event.getPointerId(event.getActionIndex());
        pathArray.put(index, new Path());
        pathArray.get(index).moveTo(x, y);
        pointArray.put(index, new PointF(x, y));
    }

    private void move(MotionEvent event, Canvas canvas, Paint paint) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            int index = event.getPointerId(i);
            float x = event.getX(i);
            float y = event.getY(i);
            Log.d("手指追踪", index + "");
            float dx = Math.abs(x - pointArray.get(index).x);
            float dy = Math.abs(y - pointArray.get(index).y);
            if (dx >= 4 || dy >= 4) {
                pathArray.get(index).quadTo(pointArray.get(index).x, pointArray.get(index).y,
                        (x + pointArray.get(index).x) / 2, (y + pointArray.get(index).y) / 2);
                canvas.drawPath(pathArray.get(index), paint);
                pointArray.get(index).x = x;
                pointArray.get(index).y = y;
            }
        }
    }

    private void end(MotionEvent event, Paint paint) {
        int index = event.getPointerId(event.getActionIndex());
        WritingStep step = new WritingStep(pathArray.get(index), paint);
        step.updatePoints();
        state.getSteps().add(step);
        pathArray.remove(index);
        pointArray.remove(index);
    }

    public void draw(Canvas canvas, Paint paint){
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < pathArray.size(); i++) {
            int k = pathArray.keyAt(i);
            canvas.drawPath(pathArray.get(k), paint);
        }
    }
}
