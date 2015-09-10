package com.themukobeta.mukobeta.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.themukobeta.mukobeta.R;

import java.util.Random;

public class LineChartView extends View {

    private static final int MIN_LINES = 3;
    private static final int MAX_LINES = 8;
    private static final int[] DISTANCES = { 1, 2, 5 };
    private static final float GRAPH_SMOOTHNES = 0.15f;

    private boolean animate = false;
    private int row = 0;

    private Dynamics[] datapoints;
    private Paint paint = new Paint();

    private Runnable animator = new Runnable() {
        @Override
        public void run() {
            boolean needNewFrame = false;
            long now = AnimationUtils.currentAnimationTimeMillis();
            for (Dynamics dynamics : datapoints) {
                dynamics.update(now);
                if (!dynamics.isAtRest()) {
                    needNewFrame = true;
                }
            }
            if (needNewFrame) {
                postDelayed(this, 20);
            }
            if (animate) {
                invalidate();
            }
        }
    };

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets the y data points of the line chart. The data points are assumed to
     * be positive and equally spaced on the x-axis. The line chart will be
     * scaled so that the entire height of the view is used.
     * 
     * @paramdatapoints
     *            y values of the line chart
     */
    public void setChartData(float[] newDatapoints) {
        long now = AnimationUtils.currentAnimationTimeMillis();
        if (datapoints == null || datapoints.length != newDatapoints.length) {
            datapoints = new Dynamics[newDatapoints.length];
            for (int i = 0; i < newDatapoints.length; i++) {
                datapoints[i] = new Dynamics(70f, 0.30f);
                datapoints[i].setPosition(newDatapoints[i], now);
                datapoints[i].setTargetPosition(newDatapoints[i], now);
            }
            invalidate();
        } else {
            for (int i = 0; i < newDatapoints.length; i++) {
                datapoints[i].setTargetPosition(newDatapoints[i], now);
            }
            removeCallbacks(animator);
            post(animator);
        }
    }

    public void setAnimation(boolean value){
        animate = value;
        if (value){
            invalidate();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        setChartData(getData());

        float maxValue = getMax(datapoints);
        //drawBackground(canvas, maxValue);
        drawLineChart(canvas, maxValue);
        incrementRow();
        if (animate) {
            postInvalidateDelayed(800);
        }
    }

    private void drawLineChart(Canvas canvas, float maxValue) {
        Path path = createSmoothPath(maxValue);

        paint.setStyle(Style.FILL);
        paint.setStrokeWidth(4);
        paint.setColor(getResources().getColor(R.color.muko_color));
        paint.setAntiAlias(true);
        paint.setShadowLayer(4, 2, 2, 0x81000000);
        canvas.drawPath(path, paint);
        paint.setShadowLayer(0, 0, 0, 0);
    }

    private Path createSmoothPath(float maxValue) {

        Path path = new Path();
        path.moveTo(getXPos(0), getYPos(datapoints[0].getPosition(), maxValue));
        for (int i = 0; i < datapoints.length - 1; i++) {
            float thisPointX = getXPos(i);
            float thisPointY = getYPos(datapoints[i].getPosition(), maxValue);
            float nextPointX = getXPos(i + 1);
            float nextPointY = getYPos(datapoints[si(i + 1)].getPosition(), maxValue);

            float startdiffX = (nextPointX - getXPos(si(i - 1)));
            float startdiffY = (nextPointY - getYPos(datapoints[si(i - 1)].getPosition(), maxValue));
            float endDiffX = (getXPos(si(i + 2)) - thisPointX);
            float endDiffY = (getYPos(datapoints[si(i + 2)].getPosition(), maxValue) - thisPointY);

            float firstControlX = thisPointX + (GRAPH_SMOOTHNES * startdiffX);
            float firstControlY = thisPointY + (GRAPH_SMOOTHNES * startdiffY);
            float secondControlX = nextPointX - (GRAPH_SMOOTHNES * endDiffX);
            float secondControlY = nextPointY - (GRAPH_SMOOTHNES * endDiffY);

            path.cubicTo(firstControlX, firstControlY, secondControlX, secondControlY, nextPointX,nextPointY);
        }
        return path;
    }

    /**
     * Given an index in datapoints, it will make sure the the returned index is
     * within the array
     * 
     * @param i
     * @return
     */
    private int si(int i) {
        if (i > datapoints.length - 1) {
            return datapoints.length - 1;
        } else if (i < 0) {
            return 0;
        }
        return i;
    }

    private float getMax(Dynamics[] array) {
        float max = array[0].getPosition();
        for (int i = 1; i < array.length; i++) {
            if (array[i].getPosition() > max) {
                max = array[i].getPosition();
            }
        }
        return max;
    }

    private float getYPos(float value, float maxValue) {
        float height = getHeight() - getPaddingTop() - getPaddingBottom();

        // scale it to the view size
        value = (value / maxValue) * height;

        // invert it so that higher values have lower y
        value = height - value;

        // offset it to adjust for padding
        value += getPaddingTop();

        return value;
    }

    private float getXPos(float value) {
        float width = getWidth() - getPaddingLeft() - getPaddingRight();
        float maxValue = datapoints.length - 1;

        // scale it to the view size
        value = (value / maxValue) * width;

        // offset it to adjust for padding
        value += getPaddingLeft();

        return value;
    }

    private float[] getData() {
        Random random = new Random();
        float[] data = new float[8];

      /*  for (int i = 1; i < 7; i++){
            if (i % 2 == 1) {
                previous = random.nextInt((35 - 13) + 1) + 13;
                data[i] = previous;
            }
            else {
                data[i] = (float) ((previous * 0.5) + 5);
            }

        }*/

       /* if (row == 0) {
            data = new float[]{13, 28, 20, 28, 20, 28, 20, 13};
        }
        else {

        }*/

        if (row == 0) {
            for (int i = 1; i < 4; i++) {
                data[i] = random.nextInt((35 - 13) + 1) + 13;
            }
            for (int i = 4; i < 8; i++){
                data[i] = i + 10;
            }
        }
        else if (row == 1){
            for (int i = 1; i < 4; i++) {
                data[i] = i + 13;
            }
            for (int i = 4; i < 8; i++){
                data[i] = random.nextInt((35 - 13) + 1) + 13;
            }
        }
        else if (row == 2){
            for (int i = 1; i < 8; i = i + 2) {
                data[i] = random.nextInt((35 - 13) + 1) + 13;
            }
            for (int i = 2; i < 8; i = i + 2){
                data[i] = i + 13;
            }
        }

        /*for (int i = 2; i < 8; i = i + 3){
            data[i] = random.nextInt((40 - 23) + 1) + 13;
        }*/

        /*for (int i = 3; i < 8; i = i + 3){
            data[i] = random.nextInt((30 - 13) + 1) + 13;
        }*/

        data[0] = 13;
        data[7] = 13;
        return data;
    }

    private void incrementRow(){
        if (row >= 2){
            row = 0;
        }
        else {
            row++;
        }

    }


}
