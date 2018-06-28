package com.dev.corvo.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
