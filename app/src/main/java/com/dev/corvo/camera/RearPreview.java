package com.dev.corvo.camera;

import android.content.Context;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;

import android.preference.PreferenceManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Family on 7/1/2017.
 */

    public class RearPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    RearPreview(Context context, Camera camera)
    {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.setKeepScreenOn(true);
        mHolder.addCallback(this);

    }



    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {

        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            }
            catch(Exception e){e.printStackTrace();}

        }



    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, final int i1, final int i2) {
            if (mHolder.getSurface()==null)
            {
                return;
            }
            try {
                    mCamera.stopPreview();


            }catch (Exception e){e.printStackTrace();}


        try {

                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();


        }catch (Exception e){e.printStackTrace();}

    }



    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //mCamera.release();
    }

}


