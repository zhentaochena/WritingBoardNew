package com.example.writingboard.view.widget;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;

import com.example.writingboard.view.widget.pen.Stroke;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存每一步的path和paint信息
 */
public class WritingStep{
    private Paint paint;
    private Path path;
    private ArrayList<PathPoint> points = null; //笔迹经过的点集
    public static final int DENSITY = 1; //点集密度
    private Stroke stroke;

    public WritingStep(Path path, Paint paint){
        this.paint = new Paint(paint);
        this.path = path;
    }

    public WritingStep(Stroke stroke, Paint paint) {
        this.stroke = stroke;
        this.paint = paint;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
        updatePoints();
    }

    public void setPoints(ArrayList<PathPoint> points) {
        this.points = points;
    }

    public boolean inArea(float x, float y){
        for (PathPoint point : getPoints()) {
            if (x - 50 < point.x && x + 50 > point.x && y - 50 < point.y && y + 50 > point.y) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过板擦的范围更新点集
     */
    public boolean updatePoints() {
        points = new ArrayList<>();
        PathMeasure pm = new PathMeasure(path, false);
        float length = pm.getLength();
        float distance = 0f;

        float[] aCoordinates = new float[2];

        while (distance < length) {
            pm.getPosTan(distance, aCoordinates, null);
            points.add(new PathPoint(aCoordinates[0],
                    aCoordinates[1]));
            distance = distance + DENSITY;
        }
        return points.size() > 10;
    }

    /**
     * 通过path获取经过的点集
     * @return 点集
     */
    public List<PathPoint> getPoints() {
        return points;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }
}
