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

/*
Copyright 2018 samalbert977

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
