package com.example.writingboard.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SurfaceBoard extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Paint paint; //画笔
    private Paint eraserPaint; //板擦画笔
    private Paint selectPaint; //圈选画笔
    private Paint markPaint; //标记画笔
    private Path path;
    private WritingStep curStep; //当前笔迹信息
    private Canvas canvas;
    private final WritingState writingState = WritingState.getInstance();
    private volatile boolean isEraser = false;
    private volatile boolean isSelect = false;

    private Path selectRectPath; //圈选框的path
    private RectF selectRect; //圈选的外接矩形，方便判断触点

    private float scale = 0;  //记录旧的缩放标记，判断是放大还是缩小

    //标记被圈选的笔迹是否在操作中
    private volatile boolean isOp = false;

    private volatile boolean isRoam = false; //标记是否处于漫游功能
    private SurfaceHolder surfaceHolder;
    private volatile boolean isDrawing = true;
    private volatile float lastX, lastY;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isDrawing = true;
                new Thread(this).start();
                onDown(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                onDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(event);
                break;
            case MotionEvent.ACTION_UP:
                onUp(event);
                isDrawing = false;
                break;
        }
        lastX = x;
        lastY = y;
        return true;
    }

    public SurfaceBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        drawToCanvas();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isDrawing = false;
    }

    @Override
    public void run() {
        while (isDrawing) {
            drawToCanvas();
        }
    }

    /**
     * 画笔初始化
     */
    private void init() {

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

        path = new Path();
        update();

        //初始化板擦
        eraserPaint = new Paint();
        eraserPaint.setColor(Color.BLACK);
        eraserPaint.setAntiAlias(true);
        eraserPaint.setDither(true);
        eraserPaint.setStyle(Paint.Style.FILL);

        //初始化圈选画笔
        selectPaint = new Paint();
        selectPaint.setColor(Color.RED);
        selectPaint.setStrokeWidth(4);
        selectPaint.setAntiAlias(true);
        selectPaint.setDither(true);
        selectPaint.setStyle(Paint.Style.STROKE);

        //初始化标记画笔
        markPaint = new Paint();
        markPaint.setColor(Color.RED);
        markPaint.setStrokeWidth(10);
        markPaint.setAntiAlias(true);
        markPaint.setDither(true);
        markPaint.setStrokeJoin(Paint.Join.ROUND);
        markPaint.setStrokeCap(Paint.Cap.ROUND);
        markPaint.setStyle(Paint.Style.STROKE);

    }

    /**
     * 更新画笔
     */
    public void update() {
        paint = new Paint();
        paint.setColor(writingState.getPenColor());
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(writingState.getPenSize());
        paint.setStyle(Paint.Style.STROKE);
    }

    private void drawToCanvas(){
        try {
            canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);
                repaintAll(canvas);
                if (isEraser) {
                    canvas.drawRect(lastX - 50, lastY - 50, lastX + 50,
                            lastY + 50, eraserPaint);
                } else {
                    if (path != null && !isRoam) {
                        if (!isSelect)
                            canvas.drawPath(path, paint);
                        else
                            canvas.drawPath(path, selectPaint);
                    }
                }
            }
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void onDown(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (event.getSize() > 0.03 && !isSelect) {
            isEraser = true;
        }
        if (isSelect) {
            if (selectRect != null && selectRect.contains(x, y)) {
                isOp = true;
            } else {
                isOp = false;
                selectRect = new RectF();
                selectRectPath = new Path();
            }
        } else {
            Log.d("手指按下的数量", event.getPointerCount() + "个");
            //如果不在圈选状态下，二指以上可触发漫游功能。
            if (event.getPointerCount() > 1) {
                isRoam = true;
            }
        }
        if (!isEraser) {
            if (!isOp) {
                path = new Path();
                path.moveTo(x, y);
                if (!isSelect) {
                    curStep = new WritingStep(path, paint);
                }
            }
        }

    }

    private void onMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (isEraser) {
            eraseALine(x, y);
        } else {
            if (Math.abs(x - lastX) >= 4 || Math.abs(y - lastY) >= 4) {
                List<PointF> points = new ArrayList<>();
                for (int i = 0; i < event.getPointerCount(); i++) {
                    points.add(new PointF(event.getX(i), event.getY(i)));
                }
                float d = touchDistance(points);
                if (isOp) moveSelectPath(x - lastX, y - lastY, d, getMidPoint(points));
                else if (isRoam) {
                    transformCanvas(x - lastX, y - lastY, d, getMidPoint(points));
                    curStep = null;
                }
                if (path != null) {
                    path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2);
                }

            }
        }

    }

    private void onUp(MotionEvent event) {
        if (!isEraser) {
            Log.d("选择和操作", isSelect ? "选择了" : "没选择");
            if (isSelect) {
                Log.d("选择和操作", isOp ? "操作中" : "未操作");
                if (event.getPointerId(event.getActionIndex()) == 0) {
                    if (!isOp) {
                        selectStep();
                    } else {
                        isOp = false;
                    }
                }
                writingState.updateSteps();
            } else {
                if (isRoam) {
                    isRoam = false;
                } else {
                    if (path != null && curStep != null) {
                        curStep.setPaint(paint);
                        curStep.setPath(path);
                        synchronized (writingState.getSteps()) {
                            writingState.getSteps().add(curStep);
                        }
                        curStep = null;
                    }
                }
            }
            path = null;
        } else {
            isEraser = false;
        }
    }



    /**
     * 擦除一条线
     *
     * @param x 触点x
     * @param y 触点y
     */
    private void eraseALine(float x, float y) {
        synchronized (writingState.getSteps()) {
            Iterator<WritingStep> it = writingState.getSteps().iterator();
            while (it.hasNext()) {
                WritingStep step = it.next();
                if (step.inArea(x, y)) {
                    it.remove();
                }
            }
        }
    }

    /**
     * 重绘所有笔迹
     */
    private void repaintAll(Canvas canvas) {

        if (isSelect) {
            if (selectPaint != null && selectRectPath != null) {
                canvas.drawPath(selectRectPath, selectPaint);
            }
        }


        synchronized (writingState.getSelectBitmap()) {
            for (BitmapStep step : writingState.getSelectBitmap()){
                markPaint.setStrokeWidth(10);
                canvas.drawPath(step.getPath(), markPaint);
            }
        }

        synchronized (writingState.getBitmapSteps()) {
            for (BitmapStep step : writingState.getBitmapSteps()) {
                canvas.drawBitmap(step.getBitmap(), step.getMatrix(), markPaint);
            }
        }

        synchronized (writingState.getSelectedSteps()) {
            for (WritingStep step : writingState.getSelectedSteps()){
                markPaint.setStrokeWidth(step.getPaint().getStrokeWidth() + 5);
                canvas.drawPath(step.getPath(), markPaint);
            }
        }

        synchronized (writingState.getSteps()){
            for (WritingStep step : writingState.getSteps()) {
                canvas.drawPath(step.getPath(), step.getPaint());
            }
        }
    }

    /**
     * 计算多个点之间的最大距离
     *
     * @param points 点集
     * @return 距离
     */
    private float touchDistance(List<PointF> points) {
        if (points.size() <= 1) return 0;
        float max = 0;
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                float d = calDistance(points.get(i), points.get(j));
                if (d > max) {
                    max = d;
                }
            }
        }
        return max;
    }

    /**
     * 计算两点之间的浮点距离
     *
     * @param p1 点1
     * @param p2 点2
     * @return 距离
     */
    private float calDistance(PointF p1, PointF p2) {
        float dx = p1.x - p2.x;
        float dy = p1.y - p2.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 圈选笔迹
     */
    private void selectStep() {

        Path selectPath = new Path();
        writingState.getSelectedSteps().clear();
        writingState.getSelectBitmap().clear();

        for (WritingStep step : writingState.getSteps()) {
            Path temp = new Path();
            temp.addPath(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                temp.op(step.getPath(), Path.Op.INTERSECT);
                if (!temp.isEmpty()) {
                    selectPath.addPath(step.getPath());
                    writingState.getSelectedSteps().add(step);
                }
            }
        }

        for (BitmapStep step : writingState.getBitmapSteps()) {
            Path temp = new Path();
            temp.addPath(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                temp.op(step.getPath(), Path.Op.INTERSECT);
                if (!temp.isEmpty()) {
                    selectPath.addPath(step.getPath());
                    writingState.getSelectBitmap().add(step);
                }
            }
        }

        if (!selectPath.isEmpty()) {
            //计算圈选path总边界
            selectRect = new RectF();
            selectPath.computeBounds(selectRect, true);
            selectRectPath = new Path();
            expandRect(selectRect);
            selectRectPath.addRect(selectRect, Path.Direction.CCW);
        }
    }

    /**
     * 扩大圈选框
     *
     * @param r 圈选框
     */
    private void expandRect(RectF r) {
        int e = 40;
        r.left -= e;
        r.right += e;
        r.top -= e;
        r.bottom += e;
    }

    private void moveSelectPath(float dx, float dy, float d, PointF mid) {
        float s = 1;
        if (d != 0) {
            float dif = d - scale;

            if (Math.abs(dif) > 8) {
                if (dif > 0) {
                    s = 1.02f;
                } else {
                    s = 0.98f;
                }
                scale = d;
            }
        }

        //笔迹转换
        for (WritingStep step : writingState.getSelectedSteps()) {
            Matrix matrix = new Matrix();
            matrix.postTranslate(dx, dy);
            if (mid != null) {
                matrix.postScale(s, s, mid.x, mid.y);
            }
            step.getPath().transform(matrix);
        }

        //图片转换
        for (BitmapStep step : writingState.getSelectBitmap()) {
            Matrix matrix = new Matrix();
            matrix.postTranslate(dx, dy);
            if (mid != null) {
                matrix.postScale(s, s, mid.x, mid.y);
            }
            step.getPath().transform(matrix);

            step.getMatrix().postTranslate(dx, dy);
            if (mid != null)
                step.getMatrix().postScale(s, s, mid.x, mid.y);
        }


        //绘制圈选的最小外接矩形
        selectRect = new RectF();
        Matrix m = new Matrix();
        selectRectPath.computeBounds(selectRect, true);
        m.postTranslate(dx, dy);
        if (mid != null) {
            m.postScale(s, s, mid.x, mid.y);
        }
        selectRectPath.transform(m);

    }

    /**
     * 移动和缩放画布
     *
     * @param dx  变换后的y坐标
     * @param dy  变换后的x坐标
     * @param d   缩放标记
     * @param mid 手指中心点
     */
    private void transformCanvas(float dx, float dy, float d, PointF mid) {
        float s = 1;
        if (d != 0) {
            float dif = d - scale;
            Log.d("缩放的差", dif + "");
            if (Math.abs(dif) >= 10) {
                if (dif > 0) {
                    s = 1.02f;
                } else {
                    s = 0.98f;
                }
                scale = d;
            }
        }

        if (mid != null) {
            for (WritingStep step : writingState.getSteps()) {
                Matrix matrix = new Matrix();
                matrix.setTranslate(dx, dy);
                matrix.preScale(s, s, mid.x, mid.y);
                step.updatePoints();
                step.getPath().transform(matrix);
            }

            for (BitmapStep step : writingState.getBitmapSteps()) {
                Matrix matrix = new Matrix();
                matrix.setTranslate(dx, dy);
                matrix.preScale(s, s, mid.x, mid.y);
                step.getMatrix().postTranslate(dx, dy);
                step.getMatrix().postScale(s, s, mid.x, mid.y);
                step.getPath().transform(matrix);
            }
        }
    }

    /**
     * 获取所有触点的中间点
     *
     * @param points 触点集合
     * @return 中间点
     */
    private PointF getMidPoint(List<PointF> points) {
        if (points.size() <= 1) return null;
        float max = 0;
        PointF ps = null;
        PointF pe = null;
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                float d = calDistance(points.get(i), points.get(j));
                if (d > max) {
                    ps = points.get(i);
                    pe = points.get(j);
                    max = d;
                }
            }
        }
        if (ps != null && pe != null) {
            return new PointF((ps.x + pe.x) / 2, (ps.y + pe.y) / 2);
        } else return null;
    }

    public Paint getPaint() {
        return paint;
    }

    public boolean isSelect(){
        return isSelect;
    }

    public void setSelect(boolean s) {
        isSelect = s;
    }

    public void exitSelect() {
        isSelect = false;
        isOp = false;
        selectRect = null;
        selectRectPath = null;
        writingState.getSelectBitmap().clear();;
        writingState.getSelectedSteps().clear();
        drawToCanvas();
    }

    public void addBitmap(Bitmap bitmap){
        writingState.getBitmapSteps().add(new BitmapStep(bitmap, new Matrix()));
        drawToCanvas();
    }
}
