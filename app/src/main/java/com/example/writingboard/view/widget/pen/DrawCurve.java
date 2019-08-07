package com.example.writingboard.view.widget.pen;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.example.writingboard.view.widget.WritingState;
import com.example.writingboard.view.widget.WritingStep;

public class DrawCurve {
    private int curveIndex = 2;
    private float startWidth;
    private float endWidth;
    private Curve curve;
    protected Canvas canvas;
    private Paint curvePaint;
    private TimePoint lastTop, lastBottom;
    private Path curvePath;

    public DrawCurve(Curve curve, float startWidth, float endWidth, Canvas canvas, Paint curvePaint) {
        this.curve = curve;
        this.startWidth = startWidth;
        this.endWidth = endWidth;
        this.canvas = canvas;
        this.curvePaint = curvePaint;
        this.curvePath = new Path();
    }

    public void updateData(float startWidth, float endWidth, Curve curve, Paint paint) {
        this.curve = curve;
        this.startWidth = startWidth;
        this.endWidth = endWidth;
        curvePaint = new Paint(paint);
    }

    public void initLastPoint(TimePoint lastTop, TimePoint lastBottom) {
        this.lastTop = lastTop;
        this.lastBottom = lastBottom;
    }

    public void draw(DrawingStrokes drawingStrokes) {
        //获得笔在两点间不同宽度的差值
        int drawSteps = (int) Math.floor(curve.length());
        if (drawingStrokes.isUp) {
            if (drawSteps > 2)
                curveIndex = (drawSteps - 2) / 2;
            else curveIndex = 1;
            if (curveIndex < 1) curveIndex = 1;
            if (drawSteps == 0) drawSteps = 2;
        } else if (drawingStrokes.isDown) {
            curveIndex = 1;
            if (drawSteps == 0) drawSteps = 2;
        } else {
            calCurveIndex(drawSteps);
        }
        float widthDelta = endWidth - startWidth;
        //两点间实际轨迹距离
        float k;
        TimePoint  pA = new TimePoint(), pB = new TimePoint(),
                pC = new TimePoint(), pD = new TimePoint();

        if (drawSteps == 0) {
            drawSteps = 1;
        }

        for (int i = 0, num = 1; i < drawSteps; i += curveIndex, num++) {
            curvePath.reset();

            // 根据控制点计算触点
            float t = (float) (i) / drawSteps;
            float x = getPointByCtrl(t, curve.point1.x,curve.point2.x,curve.point3.x,curve.point4.x);
            float y = getPointByCtrl(t, curve.point1.y,curve.point2.y,curve.point3.y,curve.point4.y);
            float currentWidth = startWidth + t * widthDelta;
            if (!drawingStrokes.isUp)
                if (Math.abs(t * widthDelta) > 0.2f * num) {
                    if (t * widthDelta > 0)
                        currentWidth = startWidth + 0.2f * num;
                    else currentWidth = startWidth - 0.2f * num;
                }
            DrawingStrokes.DrawingState currentState = getState(x, y, drawingStrokes);
            k = calTrapezoidPoints(x, y, drawingStrokes, currentWidth, pA,pB,pC,pD);
            if (drawingStrokes.isDown) {
                onDown(drawingStrokes, pA, pB, pC, pD);
            } else {
                onMove(drawingStrokes, x, y, k, pA, pB, pC, pD, currentState);
            }
            if (drawingStrokes.isUp && i >= drawSteps - curveIndex) {
                onUp(drawingStrokes, pC, pD, currentWidth +
                        (currentWidth > 10 ? currentWidth * 2 : 0));
            }
            lastTop.x = pC.x;
            lastTop.y = pC.y;
            lastBottom.x = pD.x;
            lastBottom.y = pD.y;
            drawingStrokes.lastWidth = currentWidth;
            drawingStrokes.lastX2 = drawingStrokes.lastX1;
            drawingStrokes.lastY2 = drawingStrokes.lastY1;
            drawingStrokes.lastX1 = x;
            drawingStrokes.lastY1 = y;
            drawingStrokes.lastK = k;
            drawingStrokes.state = currentState;
        }
    }

    private DrawingStrokes.DrawingState getState(float x, float y, DrawingStrokes drawingStrokes) {
        float numX = x - drawingStrokes.lastX1;
        float numY = y - drawingStrokes.lastY1;
        if (numX > 0 && numY < 0) return DrawingStrokes.DrawingState.X_ADD_Y_DEC;
        if (numX > 0 && numY == 0) return DrawingStrokes.DrawingState.X_ADD_Y_SAM;
        if (numX < 0 && numY > 0) return DrawingStrokes.DrawingState.X_DEC_Y_ADD;
        if (numX < 0 && numY < 0) return DrawingStrokes.DrawingState.X_DEC_Y_DEC;
        if (numX < 0 && numY == 0) return DrawingStrokes.DrawingState.X_DEC_Y_SAM;
        if (numX == 0 && numY > 0) return DrawingStrokes.DrawingState.X_SAM_Y_ADD;
        if (numX == 0 && numY < 0) return DrawingStrokes.DrawingState.X_SAM_Y_DEC;
        if (numX == 0 && numY == 0) return DrawingStrokes.DrawingState.X_SAM_Y_SAM;
        return DrawingStrokes.DrawingState.NO_STATE;
    }

    /**
     * 第一次接触屏幕
     */
    private void onDown(DrawingStrokes drawingStrokes, TimePoint pA, TimePoint pB,
                        TimePoint pC, TimePoint pD) {
        //起点  需要算AB
        //算出矩形的四个点
        float k = 0;
        TimePoint A, B, C, D;
        if (pA.x != pB.x) {
            k = (pA.y - pB.y) / (pA.x - pB.x);
            A = new TimePoint((drawingStrokes.lastWidth) * (-k) / (float) Math.sqrt(k * k + 1) + pA.x,
                    (drawingStrokes.lastWidth) / (float) Math.sqrt(k * k + 1) + pA.y);
            B = new TimePoint((-drawingStrokes.lastWidth) * (-k) / (float) Math.sqrt(k * k + 1) + pA.x,
                    (-drawingStrokes.lastWidth) / (float) Math.sqrt(k * k + 1) + pA.y);
            //当前点的上下端点C,D
            C = new TimePoint((drawingStrokes.lastWidth) * (-k) / (float) Math.sqrt(k * k + 1) + pB.x,
                    (drawingStrokes.lastWidth) / (float) Math.sqrt(k * k + 1) + pB.y);
            D = new TimePoint((-drawingStrokes.lastWidth) * (-k) / (float) Math.sqrt(k * k + 1) + pB.x,
                    (-drawingStrokes.lastWidth) / (float) Math.sqrt(k * k + 1) + pB.y);

        } else {
            A = new TimePoint(drawingStrokes.lastWidth + pA.x,
                    pA.y);
            B = new TimePoint(-drawingStrokes.lastWidth + pA.x,
                    pA.y);
            C = new TimePoint(drawingStrokes.lastWidth + pB.x,
                    pB.y);
            D = new TimePoint(-drawingStrokes.lastWidth / 2 + pB.x,
                    pB.y);
        }
        TimePoint centerAC = new TimePoint((A.x + C.x) / 2, (A.y + C.y) / 2);
        TimePoint centerBD = new TimePoint((B.x + D.x) / 2, (B.y + D.y) / 2);
        boolean isAC;
        if (pA.x != pB.x) {
            float b = pA.y - k * pA.x;
            isAC = (centerAC.y - k * centerAC.x - b) * (drawingStrokes.pointList.get(3).y
                    - k * drawingStrokes.pointList.get(3).x - b) <= 0;

        } else {
            isAC = (centerAC.y - pA.y) * (drawingStrokes.pointList.get(3).y - pA.y) <= 0;
        }


        curvePath.moveTo(pB.x, pB.y);
        if (isAC)
            curvePath.quadTo(centerAC.x, centerAC.y, pA.x, pA.y);
        else curvePath.quadTo(centerBD.x, centerBD.y, pA.x, pA.y);
        curvePath.lineTo(pC.x, pC.y);
        curvePath.lineTo(pD.x, pD.y);
        curvePath.lineTo(pB.x, pB.y);
        canvas.drawPath(curvePath, curvePaint);
        curvePath.reset();


        drawingStrokes.isDown = false;

        drawingStrokes.strokesPath.moveTo(pB.x, pB.y);
        if (isAC) {
            drawingStrokes.strokesPath.quadTo(centerAC.x, centerAC.y, pA.x, pA.y);
            drawingStrokes.curStep.getStroke()
                    .addPoint(new TimePoint(centerAC.x, centerAC.y));
        } else {
            drawingStrokes.strokesPath.quadTo(centerBD.x, centerBD.y, pA.x, pA.y);
            drawingStrokes.curStep.getStroke()
                    .addPoint(new TimePoint(centerBD.x, centerBD.y));
        }

        drawingStrokes.strokesPath.lineTo(pC.x, pC.y);
        drawingStrokes.lastLineX = pC.x;
        drawingStrokes.lastLineY = pC.y;
        drawingStrokes.timePoints.add(new TimePoint(pB.x, pB.y));
        drawingStrokes.timePoints.add(new TimePoint(pD.x, pD.y));
    }

    private void onUp(DrawingStrokes drawingStrokes,
                      TimePoint pC, TimePoint pD, float currentWidth) {
        drawingStrokes.isUp = false;
        TimePoint A, B, C, D;
        float k = 0;
        if (pC.x != pD.x) {
            k = (pC.y - pD.y) / (pC.x - pD.x);
            A = new TimePoint((currentWidth) * (-k) / (float) Math.sqrt(k * k + 1) + pC.x,
                    (currentWidth) / (float) Math.sqrt(k * k + 1) + pC.y);
            B = new TimePoint((-currentWidth) * (-k) / (float) Math.sqrt(k * k + 1) + pD.x,
                    (-currentWidth) / (float) Math.sqrt(k * k + 1) + pD.y);
            //当前点的上下端点c,d
            C = new TimePoint((currentWidth) * (-k) / (float) Math.sqrt(k * k + 1) + pC.x,
                    (currentWidth) / (float) Math.sqrt(k * k + 1) + pC.y);
            D = new TimePoint((-currentWidth) * (-k) / (float) Math.sqrt(k * k + 1) + pD.x,
                    (-currentWidth) / (float) Math.sqrt(k * k + 1) + pD.y);

        } else {
            A = new TimePoint(currentWidth + pC.x,
                    pC.y);
            B = new TimePoint(-currentWidth + pD.x,
                    pD.y);
            C = new TimePoint(currentWidth + pC.x,
                    pC.y);
            D = new TimePoint(-currentWidth + pD.x,
                    pD.y);
        }
        TimePoint centerAC = new TimePoint((A.x + C.x) / 2, (A.y + C.y) / 2);
        TimePoint centerBD = new TimePoint((B.x + D.x) / 2, (B.y + D.y) / 2);
        boolean isAC;
        if (pC.x != pD.x) {
            float b = pC.y - k * pC.x;
            isAC = (centerAC.y - k * centerAC.x - b) * (drawingStrokes.lastY1 - k * drawingStrokes.lastX1 - b) <= 0;
        } else {
            isAC = (centerAC.y - pC.y) * (drawingStrokes.lastY1 - pC.y) <= 0;
        }
        curvePath.moveTo(pC.x, pC.y);
        if (isAC) {
            curvePath.quadTo(centerAC.x, centerAC.y, pD.x, pD.y);
            if (drawingStrokes.lastLineX == pC.x && drawingStrokes.lastLineY == pC.y) {
                drawingStrokes.strokesPath.quadTo(centerAC.x, centerAC.y, pD.x, pD.y);
            } else {
                drawingStrokes.strokesPath.quadTo(centerAC.x, centerAC.y, pC.x, pC.y);
            }
        } else {
            curvePath.quadTo(centerBD.x, centerBD.y, pD.x, pD.y);
            if (drawingStrokes.lastLineX == pC.x && drawingStrokes.lastLineY == pC.y) {
                drawingStrokes.strokesPath.quadTo(centerBD.x, centerBD.y, pD.x, pD.y);
            } else {
                drawingStrokes.strokesPath.quadTo(centerBD.x, centerBD.y, pC.x, pC.y);
            }
        }
        curvePath.lineTo(pC.x, pC.y);
        canvas.drawPath(curvePath, curvePaint);
        curvePath.reset();
    }

    private float getPointByCtrl(float t, float p1, float p2, float p3, float p4) {
        float tt = t * t;
        float ttt = tt * t;
        float u = 1 - t;
        float uu = u * u;
        float uuu = uu * u;
        float t1 = -3 * ttt + 3 * tt + 3 * t + 1;
        float t2 = 3 * ttt - 6 * tt + 4;
        float result = uuu * p1 / 6.0f;
        result += t2 * p2 / 6.0f;
        result += t1 * p3 / 6.0f;
        result += ttt * p4 / 6.0f;
        return result;
    }

    private void calCurveIndex(int drawSteps) {
        if (drawSteps > 100) curveIndex = 40;
        else if (drawSteps > 80) curveIndex = 35;
        else if (drawSteps > 70) curveIndex = 30;
        else if (drawSteps > 60) curveIndex = 25;
        else if (drawSteps > 50) curveIndex = 20;
        else if (drawSteps > 40) curveIndex = 15;
        else if (drawSteps > 30) curveIndex = 13;
        else if (drawSteps > 20) curveIndex = 9;
        else if (drawSteps > 10) curveIndex = 7;
        else if (drawSteps >= 4) curveIndex = 3;
        else curveIndex = 1;
    }

    private float calTrapezoidPoints(float x, float y, DrawingStrokes drawingStrokes, float currentWidth,
                                    TimePoint pA, TimePoint pB,
                                    TimePoint pC, TimePoint pD) {
        float k = 0;
        if (x != drawingStrokes.lastX1) {
            k = (y - drawingStrokes.lastY1) / (x - drawingStrokes.lastX1);
            //上个点的上下端点A,B
            //计算公式：k(Ay - dy) = dx - Ax
            //(Ax - dx)平方 + (Ay - dy)平方 = w平方
            //Ay = w / ((k2 + 1))开平方 + dy
            pA.set((drawingStrokes.lastWidth / 2) * (-k)
                    / (float) Math.sqrt(k * k + 1) + drawingStrokes.lastX1,
                    (drawingStrokes.lastWidth / 2) / (float) Math.sqrt(k * k + 1)
                            + drawingStrokes.lastY1);
            pB.set((-drawingStrokes.lastWidth / 2) * (-k)
                    / (float) Math.sqrt(k * k + 1) + drawingStrokes.lastX1,
                    (-drawingStrokes.lastWidth / 2)
                            / (float) Math.sqrt(k * k + 1) + drawingStrokes.lastY1);
            //当前点的上下端点C,D
            pC.set((currentWidth / 2) * (-k) / (float) Math.sqrt(k * k + 1) + x,
                    (currentWidth / 2) / (float) Math.sqrt(k * k + 1) + y);
            pD.set((-currentWidth / 2) * (-k) / (float) Math.sqrt(k * k + 1) + x,
                    (-currentWidth / 2) / (float) Math.sqrt(k * k + 1) + y);

        } else {
            pA.set(drawingStrokes.lastWidth / 2 + drawingStrokes.lastX1,
                    drawingStrokes.lastY1);
            pB.set(-drawingStrokes.lastWidth / 2 + drawingStrokes.lastX1,
                    drawingStrokes.lastY1);
            pC.set(currentWidth / 2 + x,
                    y);
            pD.set(-currentWidth / 2 + x,
                    y);
        }
        return k;
    }

    private void onMove(DrawingStrokes drawingStrokes, float x, float y, float k, TimePoint pA,
                        TimePoint pB, TimePoint pC, TimePoint pD,
                        DrawingStrokes.DrawingState currentState) {
        //相交为180
        if (!((drawingStrokes.lastX2 == drawingStrokes.lastX1 && drawingStrokes.lastX1 == x)
                || (drawingStrokes.lastX2 != drawingStrokes.lastX1 && drawingStrokes.lastX1
                != x && (k == drawingStrokes.lastK || -k == drawingStrokes.lastK)))) {
            //判断外端点画弧
            float degreeA = drawingStrokes.calculateDegree(drawingStrokes.lastX1, drawingStrokes.lastY1,
                    drawingStrokes.lastX2, drawingStrokes.lastY2, pA.x, pA.y);
            float degreeB = drawingStrokes.calculateDegree(drawingStrokes.lastX1, drawingStrokes.lastY1,
                    drawingStrokes.lastX2, drawingStrokes.lastY2, pB.x, pB.y);
            float degreeLT = drawingStrokes.calculateDegree(drawingStrokes.lastX1, drawingStrokes.lastY1,
                    x, y, lastTop.x, lastTop.y);
            float degreeLB = drawingStrokes.calculateDegree(drawingStrokes.lastX1, drawingStrokes.lastY1,
                    x, y, lastBottom.x, lastBottom.y);
            //谁大谁是外端点
            if ((degreeA >= degreeB && degreeLT >= degreeLB) || (degreeA <= degreeB && degreeLT <= degreeLB)) {

                //填充
                curvePath.moveTo(pA.x, pA.y);
                curvePath.lineTo(lastTop.x, lastTop.y);
                curvePath.lineTo(lastBottom.x, lastBottom.y);
                curvePath.lineTo(pB.x, pB.y);
                curvePath.lineTo(pA.x, pA.y);
                canvas.drawPath(curvePath, curvePaint);
                curvePath.reset();

                if (drawingStrokes.lastLineX == lastTop.x && drawingStrokes.lastLineY == lastTop.y) {
                    drawingStrokes.strokesPath.lineTo(pA.x, pA.y);
                    drawingStrokes.timePoints.add(new TimePoint(pB.x, pB.y));
                    drawingStrokes.lastLineX = pA.x;
                    drawingStrokes.lastLineY = pA.y;
                } else {
                    drawingStrokes.strokesPath.lineTo(pB.x, pB.y);
                    drawingStrokes.timePoints.add(new TimePoint(pA.x, pA.y));
                    drawingStrokes.lastLineX = pB.x;
                    drawingStrokes.lastLineY = pB.y;
                }
            } else {
                if (drawingStrokes.intersect(pA.x, pA.y, lastBottom.x, lastBottom.y, x, y, drawingStrokes.lastX1, drawingStrokes.lastY1)
                        || drawingStrokes.intersect(pA.x, pA.y, lastBottom.x, lastBottom.y, drawingStrokes.lastX2, drawingStrokes.lastY2,
                        drawingStrokes.lastX1, drawingStrokes.lastY1)) {
                    //转弯了
                    if (drawingStrokes.state != DrawingStrokes.DrawingState.NO_STATE
                            && drawingStrokes.state != currentState) {

                        curvePath.moveTo(pA.x, pA.y);
                        curvePath.lineTo(lastBottom.x, lastBottom.y);
                        curvePath.lineTo(lastTop.x, lastTop.y);
                        curvePath.lineTo(pB.x, pB.y);
                        curvePath.lineTo(pA.x, pA.y);
                        canvas.drawPath(curvePath, curvePaint);
                        curvePath.reset();

                        if (drawingStrokes.lastLineX == lastBottom.x && drawingStrokes.lastLineY == lastBottom.y) {
                            drawingStrokes.strokesPath.lineTo(pA.x, pA.y);
                            drawingStrokes.timePoints.add(new TimePoint(pB.x, pB.y));
                            drawingStrokes.lastLineX = pA.x;
                            drawingStrokes.lastLineY = pA.y;
                        } else {
                            drawingStrokes.strokesPath.lineTo(pB.x, pB.y);
                            drawingStrokes.timePoints.add(new TimePoint(pA.x, pA.y));
                            drawingStrokes.lastLineX = pB.x;
                            drawingStrokes.lastLineY = pB.y;
                        }
                    } else {

                        curvePath.moveTo(pA.x, pA.y);
                        curvePath.lineTo(lastTop.x, lastTop.y);
                        curvePath.lineTo(lastBottom.x, lastBottom.y);
                        curvePath.lineTo(pB.x, pB.y);
                        curvePath.lineTo(pA.x, pA.y);
                        canvas.drawPath(curvePath, curvePaint);
                        curvePath.reset();

                        if (drawingStrokes.lastLineX == lastTop.x && drawingStrokes.lastLineY == lastTop.y) {
                            drawingStrokes.strokesPath.lineTo(pA.x, pA.y);
                            drawingStrokes.timePoints.add(new TimePoint(pB.x, pB.y));
                            drawingStrokes.lastLineX = pA.x;
                            drawingStrokes.lastLineY = pA.y;
                        } else {
                            drawingStrokes.strokesPath.lineTo(pB.x, pB.y);
                            drawingStrokes.timePoints.add(new TimePoint(pA.x, pA.y));
                            drawingStrokes.lastLineX = pB.x;
                            drawingStrokes.lastLineY = pB.y;
                        }
                    }
                } else {

                    //填充
                    curvePath.moveTo(pA.x, pA.y);
                    curvePath.lineTo(lastBottom.x, lastBottom.y);
                    curvePath.lineTo(lastTop.x, lastTop.y);
                    curvePath.lineTo(pB.x, pB.y);
                    curvePath.lineTo(pA.x, pA.y);
                    canvas.drawPath(curvePath, curvePaint);
                    curvePath.reset();

                    if (drawingStrokes.lastLineX == lastBottom.x && drawingStrokes.lastLineY == lastBottom.y) {
                        drawingStrokes.strokesPath.lineTo(pA.x, pA.y);
                        drawingStrokes.timePoints.add(new TimePoint(pB.x, pB.y));
                        drawingStrokes.lastLineX = pA.x;
                        drawingStrokes.lastLineY = pA.y;
                    } else {
                        drawingStrokes.strokesPath.lineTo(pB.x, pB.y);
                        drawingStrokes.timePoints.add(new TimePoint(pA.x, pA.y));
                        drawingStrokes.lastLineX = pB.x;
                        drawingStrokes.lastLineY = pB.y;
                    }
                }
            }
        }

        //填充
        curvePath.moveTo(pA.x, pA.y);
        curvePath.lineTo(pC.x, pC.y);
        curvePath.lineTo(pD.x, pD.y);
        curvePath.lineTo(pB.x, pB.y);
        curvePath.lineTo(pA.x, pA.y);
        canvas.drawPath(curvePath, curvePaint);
        curvePath.reset();

        if (drawingStrokes.lastLineX == pA.x && drawingStrokes.lastLineY == pA.y) {
            drawingStrokes.strokesPath.lineTo(pC.x, pC.y);

            drawingStrokes.timePoints.add(new TimePoint(pD.x, pD.y));
            drawingStrokes.lastLineX = pC.x;
            drawingStrokes.lastLineY = pC.y;
        } else {
            drawingStrokes.strokesPath.lineTo(pD.x, pD.y);

            drawingStrokes.timePoints.add(new TimePoint(pC.x, pC.y));
            drawingStrokes.lastLineX = pD.x;
            drawingStrokes.lastLineY = pD.y;
        }
    }
}
