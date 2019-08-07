package com.example.writingboard.view.widget;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 保存笔迹信息的单例
 */
public class WritingState {
    private static volatile WritingState instance = null;

    private int penColor;
    private int penSize;

    private List<WritingStep> steps = new ArrayList<>();
    private List<WritingStep> deleteSteps = new ArrayList<>();
    private List<WritingStep> selectedSteps = new ArrayList<>();
    private List<BitmapStep> bitmapSteps = new ArrayList<>();
    private List<BitmapStep> selectBitmap = new ArrayList<>();
    private List<WritingStep> splitSteps = new ArrayList<>();
    private WritingState(){
        penColor = Color.BLACK;
        penSize = 10;
    }

    public static WritingState getInstance() {
        if (instance == null) {
            synchronized (WritingState.class) {
                if (instance == null) {
                    instance = new WritingState();
                }
            }
        }
        return instance;
    }


    public int getPenColor() {
        return penColor;
    }

    public void setPenColor(int penColor) {
        this.penColor = penColor;
    }

    public int getPenSize() {
        return penSize;
    }

    public void setPenSize(int penSize) {
        this.penSize = penSize;
    }

    public List<WritingStep> getSteps() {
        return steps;
    }

    public void setSteps(Vector<WritingStep> steps) {
        this.steps = steps;
    }

    public List<WritingStep> getDeleteSteps() {
        return deleteSteps;
    }

    public void setDeleteSteps(List<WritingStep> deleteSteps) {
        this.deleteSteps = deleteSteps;
    }

    public List<WritingStep> getSelectedSteps() {
        return selectedSteps;
    }

    public void setSelectedSteps(List<WritingStep> selectedSteps) {
        this.selectedSteps = selectedSteps;
    }

    public void updateSteps() {
        for (WritingStep step: steps) {
            step.updatePoints();
        }
    }

    public List<BitmapStep> getBitmapSteps() {
        return bitmapSteps;
    }

    public void setBitmapSteps(List<BitmapStep> bitmapSteps) {
        this.bitmapSteps = bitmapSteps;
    }

    public List<BitmapStep> getSelectBitmap() {
        return selectBitmap;
    }

    public void setSelectBitmap(List<BitmapStep> selectBitmap) {
        this.selectBitmap = selectBitmap;
    }

    public List<WritingStep> getSplitSteps() {
        return splitSteps;
    }

    public void setSplitSteps(List<WritingStep> splitSteps) {
        this.splitSteps = splitSteps;
    }

    public void draw(Canvas canvas, Paint paint) {

        for (WritingStep step : steps) {
            canvas.drawPath(step.getPath(), paint);
        }
    }
}
