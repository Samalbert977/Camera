package com.dev.corvo.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Family on 7/21/2017.
 */

public class FrontPreview extends SurfaceView implements SurfaceHolder.Callback {
   private SurfaceHolder mholder;
    private Camera Ncamera;
    FrontPreview(Context context, Camera camera)
    {
        super(context);
        Ncamera = camera;
        mholder = getHolder();
        mholder.addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            Ncamera.setPreviewDisplay(surfaceHolder);
            Ncamera.startPreview();
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (mholder.getSurface()==null)
        {
            return;
        }
        try {
            Ncamera.stopPreview();


        }catch (Exception e){}


        try {

            Ncamera.setPreviewDisplay(surfaceHolder);
            Ncamera.startPreview();


        }catch (Exception e){}
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
