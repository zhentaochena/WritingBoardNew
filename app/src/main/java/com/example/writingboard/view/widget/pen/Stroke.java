package com.example.writingboard.view.widget.pen;

import android.graphics.Path;

import java.util.Vector;

public class Stroke{
        private Path stroke;
        private Vector<TimePoint> points; //拟合后的几个点
        private Vector<TimePoint> originPoints;//最原始的几个点
        private Vector<Float> originWidth;

        Stroke(){
            this.stroke = new Path();
            this.points = new Vector<>();
            originWidth = new Vector<>();
            originPoints = new Vector<>();
        }
        public Path getStrokePath(){
            return  stroke;
        }
        public void setStroke(Path path){
            stroke = new Path(path);
        }
        public Vector<TimePoint> getOriginPoints(){
            return originPoints;
        }
        public void setOriginPoints(Vector<TimePoint> timePoints){
            this.originPoints = timePoints;
        }
        public void setOriginWidth(Vector<Float> originWidth){
            this.originWidth = originWidth;
        }
        public void addOriginPoint(TimePoint myPoints){
            originPoints.add(myPoints);
        }
        public Vector<Float> getOriginWidth(){
            return originWidth;
        }
        public void addOriginWidth(float width){
            originWidth.add(width);
        }
        public void addPoint(TimePoint point){
            points.add(point);
        }
        public Vector<TimePoint> getPoints(){
            return points;
        }
    }
