package com.dev.corvo.camera;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Family on 8/23/2017.
 */

public class CustomView extends SurfaceView {
    SurfaceHolder holder;
    Paint paint;
    Canvas canvas;

    public CustomView(Context context) {
        super(context);
        holder = getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
    }

    public void DrawRect(float x , float y , boolean FOCUS , boolean IS_FRONT_CAMERA)
    {
        paint.setStrokeWidth(2);
        this.invalidate();
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                if (FOCUS || IS_FRONT_CAMERA) {
                    if(IS_FRONT_CAMERA) {
                        paint.setColor(Color.BLUE);
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        canvas.drawColor(Color.TRANSPARENT);
                        canvas.drawRect(x-50,y+50,x+50,y-50,paint);
                        holder.unlockCanvasAndPost(canvas);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ClearCanvas();
                            }
                        }, 1000);
                    }
                    else {
                        paint.setColor(Color.GREEN);
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        canvas.drawColor(Color.TRANSPARENT);
                        canvas.drawRect(x - 50, y + 50, x + 50, y - 50, paint);
                        holder.unlockCanvasAndPost(canvas);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ClearCanvas();
                            }
                        }, 1000);
                    }
                } else {

                        paint.setColor(Color.RED);
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        canvas.drawColor(Color.TRANSPARENT);
                        canvas.drawRect(x-50,y+50,x+50,y-50,paint);
                        holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    protected void ClearCanvas()
    {
        Canvas canvas1 = holder.lockCanvas();
        if (canvas1 != null) {
            canvas1.drawColor(0, PorterDuff.Mode.CLEAR);
            holder.unlockCanvasAndPost(canvas1);
        }
    }

    public void SimulateFlashRect(float width , float height) {
        invalidate();
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(10);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.drawColor(Color.TRANSPARENT);
                canvas.drawRect(CreateRect((int)width,(int)height),paint);
                holder.unlockCanvasAndPost(canvas);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ClearCanvas();
                    }
                }, 10);
            }
        }
    }

    private Rect CreateRect(int width, int height)
    {
        int y1,x1;
        y1 = (height/2)-height;
        x1 = (width/2)-width;
        return new Rect((width/2)-x1,(height/2)+y1,(width/2)+x1,(height/2)-y1);
    }

}
