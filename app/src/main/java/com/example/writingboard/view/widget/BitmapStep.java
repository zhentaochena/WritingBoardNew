package com.example.writingboard.view.widget;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;

public class BitmapStep {
    private Bitmap bitmap; //图片
    private Matrix matrix; //变换矩阵
    private Path path; //记录图片范围

    public BitmapStep(Bitmap bitmap, Matrix matrix) {
        this.bitmap = bitmap;
        this.matrix = matrix;
        this.path = new Path();
        path.addRect(new RectF(0, 0, bitmap.getWidth(),
                bitmap.getHeight()), Path.Direction.CCW);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
