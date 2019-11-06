package com.w3engineers.mesh.ui.diagram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
/*
 *  ****************************************************************************
 *  * Created by : Md Tariqul Islam on 8/29/2019 at 1:57 PM.
 *  * Email : tariqul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md Tariqul Islam on 8/29/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */


public class LineDraw extends View {
    private Paint paint;
    private View viewA;
    private View viewB;

    List<Line> lineList;


    public LineDraw(Context context) {
        super(context);
    }

    public void clearLineList(){
        lineList.clear();
    }
    public LineDraw(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5);

        lineList = new ArrayList<>();
    }

    public void drawLine(View viewA, View viewB, boolean isConnected) {
        this.viewA = viewA;
        this.viewB = viewB;
        if (viewA == null || viewB == null)
            return;

        float centreX1 = viewA.getX() + viewA.getWidth() / 2;
        float centreY1 = viewA.getY() + viewA.getHeight() / 2;

        float centreX2 = viewB.getX() + viewB.getWidth() / 2;
        float centreY2 = viewB.getY() + viewB.getHeight() / 2;

        Line line = new Line(centreX1, centreY1, centreX2, centreY2, isConnected);
        lineList.add(line);

        Log.d("DiagarmTest", "point: " + line.toString());

       // invalidate();
    }

    public void drawLine(float x1, float y1, float x2, float y2, boolean isConnected) {

        Line line = new Line(x1, y1, x2, y2, isConnected);
        lineList.add(line);

        Log.d("DiagarmTest", "point: " + line.toString());

        // invalidate();
    }

    public void draw() {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.MULTIPLY);

        for (Line line : lineList) {
            paint.setColor(line.isConnected ? Color.rgb(230, 0, 0) : Color.LTGRAY);
            canvas.drawLine(line.getViewAX(), line.getViewAY(), line.getViewBX(), line.getViewBY(), paint);
        }

       /* float centreX1 = viewA.getX() + viewA.getWidth() / 2;
        float centreY1 = viewA.getY() + viewA.getHeight() / 2;

        float centreX2 = viewB.getX() + viewB.getWidth() / 2;
        float centreY2 = viewB.getY() + viewB.getHeight() / 2;*/

        //canvas.drawLine(centreX1, centreY1, centreX2, centreY2, paint);
    }

    private class Line {
        float viewAX;
        float viewAY;
        float viewBX;
        float viewBY;
        boolean isConnected;

        public Line(float viewAX, float viewAY, float viewBX, float viewBY, boolean isConnected) {
            this.viewAX = viewAX;
            this.viewAY = viewAY;
            this.viewBX = viewBX;
            this.viewBY = viewBY;
            this.isConnected = isConnected;
        }

        public float getViewAX() {
            return viewAX;
        }

        public void setViewAX(float viewAX) {
            this.viewAX = viewAX;
        }

        public float getViewAY() {
            return viewAY;
        }

        public void setViewAY(float viewAY) {
            this.viewAY = viewAY;
        }

        public float getViewBX() {
            return viewBX;
        }

        public void setViewBX(float viewBX) {
            this.viewBX = viewBX;
        }

        public float getViewBY() {
            return viewBY;
        }

        public void setViewBY(float viewBY) {
            this.viewBY = viewBY;
        }

        @Override
        public String toString() {
            return "Line{" +
                    "viewAX=" + viewAX +
                    ", viewAY=" + viewAY +
                    ", viewBX=" + viewBX +
                    ", viewBY=" + viewBY +
                    '}';
        }
    }
}
