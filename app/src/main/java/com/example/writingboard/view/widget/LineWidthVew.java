package com.example.writingboard.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

public class LineWidthVew extends View {

    private Paint paint;
    private WritingState state = WritingState.getInstance();
    private PointF start;
    private PointF end;

    public LineWidthVew(Context context) {
        this(context, null);
    }

    public LineWidthVew(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineWidthVew(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        updatePaint();
    }

    public void updatePaint() {
        paint = new Paint();
        paint.setColor(state.getPenColor());
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(state.getPenSize());
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setPaintSize(int size) {
        paint.setStrokeWidth(size);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        start = new PointF(100, h / 2.0f);
        end = new PointF(w - 100, h / 2.0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(start.x, start.y, end.x, end.y, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), 120);
    }
}
