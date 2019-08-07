package com.example.writingboard.view.widget;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class PathPoint implements Serializable {
    public float x;
    public float y;
    public boolean isRemove = false;

    public PathPoint() {}

    public PathPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PathPoint(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    public final void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public final void set(android.graphics.PointF p) {
        this.x = p.x;
        this.y = p.y;
    }

    public final void negate() {
        x = -x;
        y = -y;
    }

    public final void offset(float dx, float dy) {
        x += dx;
        y += dy;
    }


    public final boolean equals(float x, float y) {
        return this.x == x && this.y == y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PathPoint pointF = (PathPoint) o;

        if (Float.compare(pointF.x, x) != 0) return false;
        return Float.compare(pointF.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }


    @NotNull
    @Override
    public String toString() {
        return "PointF(" + x + ", " + y + ")";
    }

}

