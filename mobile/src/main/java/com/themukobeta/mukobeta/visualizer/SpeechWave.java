package com.themukobeta.mukobeta.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.themukobeta.mukobeta.R;

import java.util.Random;

/**
 * Created by abhi on 27/3/15.
 */
public class SpeechWave extends View{
    Paint paint;

    Random g;

    float X, Y;
    int compression = 10, compression_limit = 10, compression_rate = 5;
    float i, old_x, old_y, new_x, new_y;
    boolean done =false;
    int timer = 50;
    boolean alternate = false;
    int yes;


    public void setYes(int yes) {
        this.yes = yes;
        if (yes < 10 && yes > 5){
            //compression = 60;
            compression_rate = 15;
            compression_limit = 100;
        }
        else if (yes > 0 && yes < 5) {
            //compression = 30;
            compression_rate = 10;
            compression_limit = 50;
        }
        else {
            compression_rate = 5;
            compression_limit = 10;
        }
        alternate = true;
        postInvalidateDelayed(500);
    }

    public SpeechWave (Context context, AttributeSet attrs)
    {
        super(context,attrs);
        g = new Random ();
        paint = new Paint();
        paint.setColor (Color.TRANSPARENT);
    }



    @Override
    protected void onDraw (Canvas c)
    {
        super.onDraw (c);
        int w = c.getWidth ();

        int h = c.getHeight();

        Paint newpaint = new Paint();
        newpaint.setColor(Color.TRANSPARENT);
        //c.drawRect(0,0,getWidth(),getHeight(),paint);
        c.drawPaint (newpaint);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(3);

        paint.setStyle(Paint.Style.STROKE);

        paint.setColor (getResources().getColor(R.color.muko_color));

        //yes = new Random().nextInt(9 - 1 + 1) + 1;

        old_x = (float)0.0;

        old_y = (float)Math.sin(old_x/180.0*Math.PI);

        int limit = 1200;
        float angle = 1000;
        int delayed = 50;

        if (yes < 10 && yes > 5){
            limit = 1650;
            angle = 1500;
            delayed = 20;
        }
        else if (yes > 0 && yes < 5) {
            limit = 1350;
            angle = 1200;
            delayed = 30;
        }

        if (compression <= -compression_limit){
            done = true;
        }
        else if (compression >= compression_limit){
            done = false;
        }

        if (!done) {
            compression = compression - compression_rate;
            //timer= timer + 3;
        }else if (done){
            compression = compression + compression_rate;
            //timer= timer - 5;
        }





        for (i = 10; i <= limit; i = i + 10)
        {
            new_x = i;
            new_y = (float) Math.sin(new_x / 180.0 * Math.PI);
           /* if (i == 180){
                c.drawLine((float) (old_x / 360.0 * w), 100 + 90 * old_y, (float) (new_x / 360.0 * w), 100 + 90 * new_y, paint);
            }*/
            //else {
                c.drawLine((float) (old_x / angle * w), 100 + compression * old_y, (float) (new_x / angle * w), 100 + compression * new_y, paint);
            //}
            old_x = new_x;
            old_y = new_y;
        }


        //Log.i("onDraw","delay: " + compression_rate);
        if (!alternate) {
            postInvalidateDelayed(50);
        }

        alternate = false;

        /*
        old_x = (float)0.0;

        old_y = (float)Math.cos(2*old_x/180.0*Math.PI);



        for (i = 10; i <= 360; i = i + 10)

        {

            new_x = i;

            new_y = (float)Math.cos(2*new_x/180.0*Math.PI);



            c.drawLine ((float)(old_x/360.0*w), 300 + 90*old_y, (float)(new_x/360.0*w), 300 + 90*new_y, paint);



            old_x = new_x;

            old_y = new_y;

        }*/



    }
}
