package com.dev.corvo.camera;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Handler;

import com.dev.corvo.camera.setting.MypreferenceFragment;

import static java.lang.Math.abs;

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


public class CameraMain extends AppCompatActivity implements SensorEventListener, SeekBar.OnSeekBarChangeListener {

    private static final int CAMERA_PERMISSION = 1;
    private static boolean UPDATE_THUMBNAIL_NOW = false;
    private static boolean HAS_PERMISSIONS = false;
    //private boolean HIGH_SATURATED_IMAGES = false;
    private boolean IF_ZOOM_EXECUTED = false;
    private boolean IS_FRONT_CAMERA = false;
    private static final String Ratio_4_3 = "1.3";
    private static final String Ratio_Tolerance_4_3 = "1.4";
    private boolean IS_WB_MENU_VISIBLE = false; //this variable is use to check the visibility of the WhiteBalance Menu on Screen
    private boolean IS_CE_MENU_VISIBLE = false; //this variable is use to check the visibility of the ColorEffect Menu on Screen
    private boolean IS_TIMER_MENU_VISIBLE = false; //this variable is use to check the visibility of the Timer Menu on Screen
    public Camera mCamera = null;
    private HandlerThread bgThread = null;
    private Handler handler;
    private FrameLayout Preview_Frame_16_9, Preview_Frame_4_3;
    /*Preview_Frame is used to add camera preview
    * Frame is used to draw focus rectangle on the screen*/
    private Camera.Parameters p;
    private SharedPreferences sharedpreference, setting;
    /*sharedpreference is app's default preferences and
    * setting is the variable of PreferenceManager of settings activity */
    private static Camera.Size[] Resolutions;
    private String path;
    private String LatestImage = null;
    private ListView WB_List, CE_List, Timer_list;
    private RelativeLayout Menu_Container, mView;
    private ImageView Gallery;
    private CountDownTimer Timer = null;
    private static int TimeDelay = 0;
    private TextView TimerScreen;
    private int rotation, picRotation;
    private int temp, MinExp, MaxExp;
    private static int RearCamera, FrontCamera;
    private float mDistance;
    private int OpenedCamera;
    private ExifInterface exifInterface;
    private ImageButton flash, shutter, SwitchButton, WhiteBalance, ColorEffect, TimerButton, Hdr;
    private SeekBar seekBar, exposurebar;
    private Vibrator v;
    private File mediaStorageDir;
    private android.location.Location location;
    private static CustomView customView;
    private float x, y;
    private Sensor accelerometer;
    private SensorManager sensorManager;
    private String Compare;
    private LocationClass locationClass = null;
    private Bitmap thumbnail = null;
    private String ISO = null,DeviceName = null,ShutterSpeed = null,FocalLength = null,Aperture = null;
    private Keys keys = new Keys();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,/* 1*/
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,/* 2*/
                    Manifest.permission.READ_EXTERNAL_STORAGE,/* 3*/
                    Manifest.permission.ACCESS_FINE_LOCATION,/* 4*/
                    Manifest.permission.ACCESS_COARSE_LOCATION,/* 5*/
                    Manifest.permission.VIBRATE /* 6*/
            }, CAMERA_PERMISSION);
        } else {
            HAS_PERMISSIONS = true;
            MainActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED
                        && grantResults[1] != PackageManager.PERMISSION_GRANTED
                        && grantResults[2] != PackageManager.PERMISSION_GRANTED
                        && grantResults[3] != PackageManager.PERMISSION_GRANTED
                        && grantResults[4] != PackageManager.PERMISSION_GRANTED
                        && grantResults[5] != PackageManager.PERMISSION_GRANTED) {
                    this.finish();
                } else {
                    HAS_PERMISSIONS = true;
                    MainActivity();
                }
        }
    }

    private void MainActivity() {
        //Initializing UI
        Preview_Frame_16_9 = (FrameLayout) findViewById(R.id.surface);
        Preview_Frame_4_3 = (FrameLayout) findViewById(R.id.surface2);
        //Initializing PANEL 1
        shutter = (ImageButton) findViewById(R.id.shutter);
        Gallery = (ImageView) findViewById(R.id.gallery);
        SwitchButton = (ImageButton) findViewById(R.id.switchCamera);
        //Initializing PANEL 2
        flash = (ImageButton) findViewById(R.id.flash);
        Hdr = (ImageButton) findViewById(R.id.HDR);
        ColorEffect = (ImageButton) findViewById(R.id.Color_effect);
        WhiteBalance = (ImageButton) findViewById(R.id.WhiteBalance);
        TimerButton = (ImageButton) findViewById(R.id.TimerIcon);
        //Initializing PANEL 3
        seekBar = (SeekBar) findViewById(R.id.SeekBar);
        //END
        WB_List = (ListView) findViewById(R.id.Wb_List);
        CE_List = (ListView) findViewById(R.id.CE_List);
        Timer_list = (ListView) findViewById(R.id.Timer_List);
        TimerScreen = (TextView) findViewById(R.id.Timer);
        Menu_Container = (RelativeLayout) findViewById(R.id.Menu_container);
        seekBar.setOnSeekBarChangeListener(this);
        //Panel 4
        exposurebar = (SeekBar) findViewById(R.id.exposurebar);
        //End of Initializing UI;
        v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        sharedpreference = getSharedPreferences("MyPref", MODE_PRIVATE);
        setting = PreferenceManager.getDefaultSharedPreferences(this);
        mView = (RelativeLayout) findViewById(R.id.VIEW);
        customView = new CustomView(this);
        mView.addView(customView);
        InitializeSetOnClickListeners();

        int cameraNum = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraNum; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                RearCamera = i;
                MypreferenceFragment.RearCam = RearCamera;

            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                FrontCamera = i;
                MypreferenceFragment.FrontCam = FrontCamera;
            }
        }
        OpenedCamera = RearCamera;
    }


    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean b, Camera camera) {
            if (b) {
                if(setting.getBoolean(keys.SOUND_PREFERENCE_KEY,true)){
                    MediaPlayer mediaPlayer = MediaPlayer.create(CameraMain.this,R.raw.focussound);
                    mediaPlayer.start();}

                customView.DrawRect(x,y,true,IS_FRONT_CAMERA);
                SetCameraParameters();
                p.setFocusAreas(null);
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                SetCameraParameters();

            }

        }
    };

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x_axis = sensorEvent.values[0];
        float y_axis = sensorEvent.values[1];

        // total = (x_axis+y_axis+z_axis+45)%360;
        if (x_axis <= -7.9 && x_axis >= -9.55) {
            rotation = 3;
        } else if (x_axis <= 8.1 && x_axis >= -7.8 && y_axis >= 4.7) {
            rotation = 0;
        } else if (x_axis >= 8.2 && x_axis <= 10.1) {
            rotation = 1;
        }
        UIRotation();
        if(UPDATE_THUMBNAIL_NOW)
            Update_Thumbnail();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        /* TODO AUTO GENERATED  STUB */
    }

    //THIS METHOD INVOKE TAKE PICTURE METHOD ON CLICKING SHUTTER IMAGE BUTTON
    public void TakePic(View view) {
        //Menu_Container.removeAllViews();
        //Menu_Container.setVisibility(View.INVISIBLE);
        Timer = new CountDownTimer(TimeDelay, 1000) {
            @Override
            public void onTick(long l) {
                if (TimeDelay != 0) {
                    TimerScreen.setVisibility(View.VISIBLE);
                    TimerScreen.setText(String.valueOf(l / 1000));
                    if(setting.getBoolean(keys.TIMER_SOUND_KEY,true)){
                    MediaPlayer beep = MediaPlayer.create(CameraMain.this,R.raw.beep);
                    beep.start();}
                }
            }

            @Override
            public void onFinish() {
                temp = rotation;
                TimerScreen.setVisibility(View.INVISIBLE);
                mCamera.takePicture(null, null, mPicture);
                if(setting.getBoolean(keys.SOUND_PREFERENCE_KEY,true)){
                MediaPlayer mediaPlayer = MediaPlayer.create(CameraMain.this,R.raw.shuttersound);
                mediaPlayer.start();}

            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
            if(HAS_PERMISSIONS) {
                ReleaseCamera();
                if (Timer != null)
                    Timer.cancel();
                CloseBgThread();
                sensorManager.unregisterListener(this, accelerometer);
                TurnOffLocation();
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
            if(HAS_PERMISSIONS) {
                if (ViewConfiguration.get(this).hasPermanentMenuKey()) {
                    HideActionBar();
                } else {
                    HideNavigationAndActionBar();
                }
                if (mCamera == null)
                    SetupCamera(OpenedCamera);
                OpenBgThread();
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                if (setting.getBoolean(keys.LOCATION_KEY, false))
                    TurnOnLocation();
                else
                    TurnOffLocation();
                UpdateImage();
            }
    }

    @NonNull
    private Boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }


    //Getting camera Instance
    private static Camera getCameraInstance(int ID) {
        Camera c = null;
        try {
            if (ID == RearCamera) {
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            } else if (ID == FrontCamera) {
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    //Clicking and saving the captured image with anonymous class
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            customView.SimulateFlashRect(customView.getWidth(), customView.getHeight());
            mCamera.stopPreview();
            mCamera.startPreview();
            Gallery.setImageResource(R.mipmap.loadingimage);
            SaveImageInBackground(data);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    GetExifData(data);
                }
            });
            }

        };


    public void openGallery(View view) {
        if (LatestImage != null) {
            File temp = new File(LatestImage);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(temp),"image/*");
            startActivity(i);
        } else {
            ShowToastNotification("Click some Photos First", Toast.LENGTH_LONG);
        }
    }

    public void ExifInterface() {
        try {
            exifInterface = new ExifInterface(path);
        /*else statement is used to rotate front camera pictures*/
            if (temp == 0) {
                if (OpenedCamera == RearCamera)
                    exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
                else
                    exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
            } else if (temp == 3) {
                if (OpenedCamera == RearCamera) {
                    exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
                } else
                    exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
            }
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME, new SimpleDateFormat("dd/MM/yyy").format(new Date()));
            if(FocalLength!=null)
                exifInterface.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, FocalLength);
            if(DeviceName!=null)
                exifInterface.setAttribute(ExifInterface.TAG_MODEL, DeviceName);
            if(ShutterSpeed!=null)
                exifInterface.setAttribute(ExifInterface.TAG_SHUTTER_SPEED_VALUE,ShutterSpeed);
            if(ISO!=null)
                exifInterface.setAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS,ISO);
            if(Aperture!=null)
                exifInterface.setAttribute(ExifInterface.TAG_F_NUMBER,Aperture);
            if (setting.getBoolean(keys.LOCATION_KEY, false)){
                if (locationClass != null) {
                    String LONGITUDE = getGeoCoordinates(location,"Longitude");
                    String LATITUDE = getGeoCoordinates(location,"latitude");
                    exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, LONGITUDE);//locationToDMS(locationClass.getLatitude())
                    exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, location.getLongitude() > 0 ? "N" : "S");
                    exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, LATITUDE);//locationToDMS(locationClass.getLongitude())
                    exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, location.getLatitude() > 0 ? "E" : "W");
                }}
            exifInterface.saveAttributes();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }


    private void UpdateImage() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
        int update = 0;
        mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/", "CameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                //Log.d("MyCameraApp", "failed to create directory");
                ShowToastNotification("Failed to create folder", Toast.LENGTH_LONG);
            }
        } else {
            String Root = Environment.getExternalStorageDirectory().toString() + "/DCIM/CameraApp/";
            File f = new File(Root);
            File[] file = f.listFiles();
            File LastModified;
            if (file.length != 0) {
                LastModified = file[0];
                final List<String> FileList = new ArrayList<>();
                for (int i = 0; i < file.length; i++) {
                    FileList.add(i, file[i].getName());
                    if (LastModified.lastModified() < file[i].lastModified() && file[i].getName().contains(".jpg")) {
                        update = i;  //update is a int variable that points to index number of latest image
                        LastModified = file[i];
                    }
                }
                LatestImage = Root + FileList.get(update);
                try {
                    exifInterface = new ExifInterface(LatestImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                picRotation = getPicRotation(Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION)));
                Bitmap bitmap = BitmapFactory.decodeFile(LatestImage);
                thumbnail = Bitmap.createScaledBitmap(bitmap, 80, 80, false);
                if (thumbnail != null)
                    UPDATE_THUMBNAIL_NOW = true;
            } else {
                UPDATE_THUMBNAIL_NOW = false;
            }

        }
            }
        });
    }


    public void OpenSettings(View view) {
        Intent Settings = new Intent(this, setting.class);
        startActivity(Settings);
    }

    private void LoadPreferences() {
        /* "Rear_Cam_Image_Size" is the Key of Preferred Resolution of Rear(Back) Camera
         * "Front_Cam_Image_Size" is the Key of Preferred Resolution of Front Camera
         * "High_Saturated_Image" is the key for option in Settings whether to save High Saturated Image or not
         * */
        String RearFacing = setting.getString(keys.REAR_CAMERA_RESOLUTION_KEY, "");
        boolean CustomRes = setting.getBoolean(keys.CUSTOM_RESOLUTION_KEY,false);
        String CustomRearFacing = setting.getString(keys.CUSTOM_IMAGE_SIZE_KEY,"");
        String FrontFacing = setting.getString(keys.FRONT_CAMERA_RESOLUTION_KEY, "");
        if (OpenedCamera == RearCamera) {
            if(!CustomRes)
                DefaultResolutionPref(RearFacing);
            else
                CustomResolutionPref(CustomRearFacing);

        } else if (OpenedCamera == FrontCamera) {
            for (int i = 0; i < MypreferenceFragment.CameraResolutions.length; i++) {
                if (FrontFacing.equalsIgnoreCase(MypreferenceFragment.CameraResolutions[i])) {
                    p.setPictureSize(Resolutions[i].width, Resolutions[i].height);
                    p.setPreviewSize(Resolutions[i].width, Resolutions[i].height);
                    float result = ((float) Resolutions[i].width / (float) Resolutions[i].height);
                    Compare = String.format("%.1f", result);
                    break;
                } else if (i == MypreferenceFragment.CameraResolutions.length - 1 && !FrontFacing.equalsIgnoreCase(MypreferenceFragment.CameraResolutions[i])) {   //Loading Highest Resolution of Camera by Default at App FirstRun
                    int Length = Resolutions.length - 1;
                    p.setPictureSize(Resolutions[Length].width, Resolutions[Length].height);
                    p.setPreviewSize(Resolutions[Length].width, Resolutions[Length].height);
                    float result = ((float) Resolutions[i].width / (float) Resolutions[i].height);
                    Compare = String.format("%.1f", result);
                }
            }
        }
    }

    private void CustomResolutionPref(String customRearFacing) {
        float result;
            switch (customRearFacing)
            {
                case "16 MP (4:3)":
                    p.setPictureSize(4619,3464);
                    result = (float) 4619 / (float) 3464;
                    Compare = String.format("%.1f",result);
                    break;
                case "18 MP (4:3)":
                    p.setPictureSize(4899,3674);
                    result = (float) 4899 / (float) 3674;
                    Compare = String.format("%.1f",result);
                    break;
                case "21 MP (4:3)":
                    p.setPictureSize(5292,3969);
                    result = (float) 5292 / (float) 3969;
                    Compare = String.format("%.1f",result);
                    break;
            }
    }

    private void OpenBgThread() {
        bgThread = new HandlerThread("Image Saving Thread");
        bgThread.start();
        handler = new Handler(bgThread.getLooper());
    }

    private void CloseBgThread() {
        if(bgThread!=null) {
            bgThread.quitSafely();
            try {
                bgThread.join();
                bgThread = null;
                handler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void UIRotation() {
        float rotate_by;
        switch (rotation) {
            case 0:
                rotate_by = 270.0f;
                Rotate_UI_By(rotate_by);
                break;
            case 3:
                rotate_by = 180.0f;
                Rotate_UI_By(rotate_by);
                break;
            case 1:
                rotate_by = 0.0f;
                Rotate_UI_By(rotate_by);
                break;
        }
    }

    public void ChangeFlashState(View view) {
        if (IsFlashSupported())
            ChangeState();
        else
            flash.setImageResource(R.mipmap.flash_off);

    }

    private void ChangeState() {
        /* Here in Flash preferences :
        * 0 -> AutoFlash
        * 1 -> Flash ON
        * 2-> Flash OFF
        * */
        flash = (ImageButton) findViewById(R.id.flash);
        SharedPreferences.Editor editor = sharedpreference.edit();
        if (!sharedpreference.getBoolean(keys.HDR_KEY, false)) {

            if (sharedpreference.getInt(keys.FLASH_KEY, 0) == 0) {
                editor.putInt(keys.FLASH_KEY, 1);
                editor.apply();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                flash.setImageResource(R.mipmap.flash_on);
            } else if (sharedpreference.getInt(keys.FLASH_KEY, 0) == 1) {
                flash.setImageResource(R.mipmap.flash_off);
                editor.putInt(keys.FLASH_KEY, 2);
                editor.commit();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            } else if (sharedpreference.getInt(keys.FLASH_KEY, 0) == 2) {
                flash.setImageResource(R.mipmap.flash_auto);
                editor.putInt(keys.FLASH_KEY, 0);
                editor.commit();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }
            SetCameraParameters();
        } else {
            flash.setImageResource(R.mipmap.flash_off);
        }
    }

    private void LoadFlashState() {
        if (OpenedCamera == RearCamera) {
            if (!sharedpreference.getBoolean(keys.HDR_KEY, false)) {
                if (sharedpreference.getInt(keys.FLASH_KEY, 0) == 0) {
                    flash.setImageResource(R.mipmap.flash_auto);
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                } else if (sharedpreference.getInt(keys.FLASH_KEY, 0) == 1) {
                    flash.setImageResource(R.mipmap.flash_on);
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                } else if (sharedpreference.getInt(keys.FLASH_KEY, 0) == 2) {
                    flash.setImageResource(R.mipmap.flash_off);
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
            } else {
                flash.setImageResource(R.mipmap.flash_off);
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
        } else {
            flash.setImageResource(R.mipmap.flash_off);
        }

    }

    public void SwitchCamera() {
        if (OpenedCamera == RearCamera) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            SetupCamera(FrontCamera);
        } else if (OpenedCamera == FrontCamera) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            SetupCamera(RearCamera);
        }
    }

    public void SetupCamera(int CameraID) {
        if (CameraID == RearCamera) {
            mCamera = getCameraInstance(RearCamera);
            RearPreview mPreview = new RearPreview(this, mCamera);
            p = mCamera.getParameters();
            SupportedResolutions();
            LoadPreferences();
            LoadHDRState();
            LoadFlashState();
            p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            SetCameraParameters();
            if (p.isZoomSupported())
                seekBar.setMax(p.getMaxZoom());
            if (Compare.equalsIgnoreCase(Ratio_4_3) || Compare.equalsIgnoreCase(Ratio_Tolerance_4_3)) {
                Preview_Frame_16_9.removeAllViews();
                Preview_Frame_16_9.setVisibility(View.INVISIBLE);;
                Preview_Frame_4_3.removeAllViews();
                Preview_Frame_4_3.setVisibility(View.VISIBLE);
                Preview_Frame_4_3.addView(mPreview);
            } else/* if (Compare.equalsIgnoreCase(Ratio_16_9) || Compare.equalsIgnoreCase(Ratio_Tolerance_16_9)) */{
                Preview_Frame_4_3.removeAllViews();
                Preview_Frame_4_3.setVisibility(View.INVISIBLE);
                Preview_Frame_16_9.removeAllViews();
                Preview_Frame_16_9.setVisibility(View.VISIBLE);
                Preview_Frame_16_9.addView(mPreview);
            }
            //ShowToastNotification(String.valueOf(mPreview.lenght),Toast.LENGTH_LONG);
            MypreferenceFragment.CamNUm = RearCamera;
            OpenedCamera = RearCamera;
            IS_FRONT_CAMERA = false;
        }
        else if (CameraID == FrontCamera)
        {
            mCamera = getCameraInstance(FrontCamera);
            FrontPreview frontPreview = new FrontPreview(this, mCamera);
            p = mCamera.getParameters();
            SupportedResolutions();
            LoadPreferences();
            LoadHDRState();
            LoadFlashState();
            SetCameraParameters();
            if (p.isZoomSupported())
                seekBar.setMax(p.getMaxZoom());
            if (Compare.equalsIgnoreCase(Ratio_4_3) || Compare.equalsIgnoreCase(Ratio_Tolerance_4_3)) {
                Preview_Frame_16_9.removeAllViews();
                Preview_Frame_16_9.setVisibility(View.INVISIBLE);
                Preview_Frame_4_3.removeAllViews();
                Preview_Frame_4_3.setVisibility(View.VISIBLE);
                Preview_Frame_4_3.addView(frontPreview);
            } else/* if (Compare.equalsIgnoreCase(Ratio_16_9) || Compare.equalsIgnoreCase(Ratio_Tolerance_16_9))*/ {
                Preview_Frame_4_3.removeAllViews();
                Preview_Frame_4_3.setVisibility(View.INVISIBLE);
                Preview_Frame_16_9.removeAllViews();
                Preview_Frame_16_9.setVisibility(View.VISIBLE);
                Preview_Frame_16_9.addView(frontPreview);
            }
            MypreferenceFragment.CamNUm = FrontCamera;
            OpenedCamera = FrontCamera;
            IS_FRONT_CAMERA = true;
        }
        ExposureBar();
        seekBar.setProgress(p.getZoom());
        exposurebar.setProgress(MaxExp-Math.abs(p.getExposureCompensation()));
        customView.ClearCanvas();
    }

    private void HideNavigationAndActionBar() {
        View view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void HideActionBar() {
        View view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public void SupportedResolutions() {
        List<Camera.Size> Res = mCamera.getParameters().getSupportedPictureSizes();
        MypreferenceFragment.CameraResolutions = new String[Res.size()];
        MypreferenceFragment.EntryValues = new String[Res.size()];
        Resolutions = new Camera.Size[Res.size()];
        float result;
        float Width, Height;
        for (int i = 0; i < Res.size(); i++) {
            Resolutions[i] = Res.get(i);
            float total = Resolutions[i].width * Resolutions[i].height;
            total /= 1024000;
            String.format("%.2f", total);
            Width = (float) Resolutions[i].width;
            Height = (float) Resolutions[i].height;
            result = Width / Height;
            String Compare = String.format("%.1f", result);
            if (Compare.equalsIgnoreCase(Ratio_4_3) || Compare.equalsIgnoreCase(Ratio_Tolerance_4_3) || Compare.equalsIgnoreCase("1.2")) {
                MypreferenceFragment.EntryValues[i] = String.valueOf(total) + " MP (4:3)";
            } else {
                MypreferenceFragment.EntryValues[i] = String.valueOf(total) + " MP (16:9)";
            }
                MypreferenceFragment.CameraResolutions[i] = String.valueOf(total);

        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    return VolumeKeyAction(event);
                }

            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    return VolumeKeyAction(event);
                }
            default:
                return super.dispatchKeyEvent(event);

        }
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, int progress, boolean FromUser) {
        if (FromUser && seekBar.getId()==R.id.SeekBar) {
            p.setZoom(progress);
            SetCameraParameters();
        }
        seekBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //TODO Auto Generated STUB!
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                seekBar.setVisibility(View.INVISIBLE);
            }
        },3000);
    }

    public void ZoomCamera(float newDistance) {
        int MaxZoom = p.getMaxZoom();
        int CurrentZoom = p.getZoom();
        if (newDistance > mDistance) {  //Zoom IN
            if (CurrentZoom < MaxZoom) {
                CurrentZoom++;
            }
        } else if (newDistance < mDistance) {
            if (CurrentZoom > 0) {
                CurrentZoom--;
            }
        }
        mDistance = newDistance;
        p.setZoom(CurrentZoom);
        SetCameraParameters();
        seekBar.setProgress(CurrentZoom);
    }

    private void ZoomCamera(KeyEvent keyEvent)
    {
        int CurrentZoom = p.getZoom();
        int MaxZoom = p.getMaxZoom();
        int action = keyEvent.getAction();
        int KeyCode = keyEvent.getKeyCode();
        switch (KeyCode)
        {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(action == KeyEvent.ACTION_DOWN)
                    if(CurrentZoom < MaxZoom)
                        CurrentZoom++;
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(action == KeyEvent.ACTION_DOWN)
                    if(CurrentZoom > 0)
                        CurrentZoom--;
                break;
        }
        p.setZoom(CurrentZoom);
        SetCameraParameters();
        seekBar.setProgress(CurrentZoom);
    }


    public float getFingerSpacing(MotionEvent event) {
        //Determine the spacing between two fingers

        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    public void TapToFocus(float x,float y) {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            Rect focusRect = CalculateFocusRect(x,y);
            p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(focusRect, 1000));
            p.setFocusAreas(meteringAreas);
            mCamera.autoFocus(mAutoFocusTakePictureCallback);
        }
    }

    private String[] SupportedFocusModes() {
        List<String> FocusModes;
        FocusModes = p.getSupportedFocusModes();
        String[] SupportedFocusMode = new String[FocusModes.size()];
        for (int i = 0; i < FocusModes.size(); i++) {
            SupportedFocusMode[i] = FocusModes.get(i);

        }
        return SupportedFocusMode;
    }

    private void SetCameraParameters() {
        try {
            mCamera.setParameters(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void LoadHDRState() {
        if (!sharedpreference.getBoolean(keys.HDR_KEY, false)) {
            Hdr.setImageResource(R.mipmap.hdr_off);
        } else {
            Hdr.setImageResource(R.mipmap.hdr_on);
            p.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
        }
    }

    private void CreateWBMenu() {
        ArrayList<String> wb_Array_list = new ArrayList<>();
        wb_Array_list.add(Camera.Parameters.WHITE_BALANCE_AUTO);
        wb_Array_list.add(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
        wb_Array_list.add(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
        wb_Array_list.add(Camera.Parameters.WHITE_BALANCE_FLUORESCENT);
        wb_Array_list.add(Camera.Parameters.WHITE_BALANCE_INCANDESCENT);
        wb_Array_list.add(Camera.Parameters.WHITE_BALANCE_SHADE);
        wb_Array_list.add(Camera.Parameters.WHITE_BALANCE_TWILIGHT);
        wb_Array_list.add(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT);
        ArrayAdapter<String> wb_Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wb_Array_list);
        WB_List.setAdapter(wb_Adapter);

    }

    private void CreateColorEffectMenu() {
        ArrayList<String> CE_array_list = new ArrayList<>();
        CE_array_list.add(Camera.Parameters.EFFECT_NONE);
        CE_array_list.add(Camera.Parameters.EFFECT_AQUA);
        CE_array_list.add(Camera.Parameters.EFFECT_BLACKBOARD);
        CE_array_list.add(Camera.Parameters.EFFECT_MONO);
        CE_array_list.add(Camera.Parameters.EFFECT_NEGATIVE);
        CE_array_list.add(Camera.Parameters.EFFECT_POSTERIZE);
        CE_array_list.add(Camera.Parameters.EFFECT_SEPIA);
        CE_array_list.add(Camera.Parameters.EFFECT_SOLARIZE);
        CE_array_list.add(Camera.Parameters.EFFECT_WHITEBOARD);
        ArrayAdapter<String> CE_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, CE_array_list);
        CE_List.setAdapter(CE_adapter);

    }

    private void Rotate_UI_By(float Degree) {
        Gallery.setRotation(Degree+picRotation);
        flash.setRotation(Degree);
        SwitchButton.setRotation(Degree);
        shutter.setRotation(Degree);
        WhiteBalance.setRotation(Degree);
        ColorEffect.setRotation(Degree);
        Hdr.setRotation(Degree);
        WB_List.setRotation(Degree);
        CE_List.setRotation(Degree);
        TimerScreen.setRotation(Degree);
        Timer_list.setRotation(Degree);
        TimerButton.setRotation(Degree);
    }

    private void ShowToastNotification(String Message, int Duration) {
        Toast.makeText(this, Message, Duration).show();
    }

    private void CreateTimerMenu() {
        ArrayList<String> TimerList = new ArrayList<>();
        TimerList.add("Off");
        TimerList.add("3");
        TimerList.add("5");
        TimerList.add("7");
        ArrayAdapter<String> TimerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TimerList);
        Timer_list.setAdapter(TimerAdapter);
    }

    private void ReleaseCamera()
    {
        if(mCamera!=null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private Rect CalculateFocusRect(float x , float y)
    {
        int left,top,right,bottom;
        left = (int) x-50;
        top = (int) y+50;
        right = (int) x+50;
        bottom = (int) y-50;

        return new Rect(left,top,right,bottom);
    }

    @org.jetbrains.annotations.Contract(pure = true)
    private int getPicRotation(int ExifRotation)
    {
        switch (ExifRotation)
        {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            default:
                return 0;
        }

    }

    private void TurnOnLocation()
    {
        locationClass = new LocationClass(this);
            locationClass.store_location = true;
            locationClass.setupLocationListener();

    }

    private void TurnOffLocation()
    {
        if(locationClass !=null)
        {
            locationClass.store_location = false;
            locationClass.freeLocationListeners();
            locationClass = null;
        }
    }

    private boolean IsFlashSupported()
    {
        List<String> FlashModes = p.getSupportedFlashModes();
        return !(FlashModes.isEmpty() || FlashModes.size() == 1 && FlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF) || FlashModes.size()==0);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void InitializeSetOnClickListeners()
    {
        //Tap to focus code
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return HandleMotionEvent(motionEvent);
            }
        });
        shutter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if(action == MotionEvent.ACTION_DOWN)
                {
                    shutter.setImageResource(R.mipmap.capturex);
                }
                if(action == MotionEvent.ACTION_UP)
                {
                    shutter.setImageResource(R.mipmap.capture2);
                }
                return false;
            }
        });

        SwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlipIcon(view);
                //SwitchCamera();
            }
        });

        exposurebar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    int Exposure = i - Math.abs(MinExp);
                    p.setExposureCompensation(Exposure);
                    SetCameraParameters();
                    TimerScreen.setVisibility(View.VISIBLE);
                    TimerScreen.setText(String.valueOf(Exposure).concat(" Ev"));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TimerScreen.setVisibility(View.INVISIBLE);
                    }
                },1000);
            }
        });
    }

    private boolean HandleMotionEvent(MotionEvent motionEvent)
    {
        int action = motionEvent.getAction();
        x = motionEvent.getX();
        y = motionEvent.getY();
        if (motionEvent.getPointerCount() > 1) {
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDistance = getFingerSpacing(motionEvent);
            } else if (action == MotionEvent.ACTION_MOVE) {
                IF_ZOOM_EXECUTED = true;
                float newDistance = getFingerSpacing(motionEvent);
                ZoomCamera(newDistance);

            }
            return true;
        } else if (action == MotionEvent.ACTION_UP && motionEvent.getPointerCount() == 1) {
            if (!IF_ZOOM_EXECUTED) {
                customView.DrawRect(x,y,false,IS_FRONT_CAMERA);
                if(!IS_FRONT_CAMERA)
                TapToFocus(x,y);
            } else
                IF_ZOOM_EXECUTED = false;
            return true;
        }
        else
            return true;
    }

    public void onClickWhiteBalance(View view)
    {
        if (!sharedpreference.getBoolean("HDR", false)) {
            if (!IS_WB_MENU_VISIBLE) {
                IS_WB_MENU_VISIBLE = true;
                IS_CE_MENU_VISIBLE = false;
                IS_TIMER_MENU_VISIBLE = false;
                Menu_Container.removeAllViews();
                Menu_Container.setVisibility(View.VISIBLE);
                Menu_Container.addView(WB_List);
                CreateWBMenu();

                WB_List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long ID) {
                        String Item = String.valueOf(WB_List.getItemAtPosition(position));
                        p.setWhiteBalance(Item);
                        p.setColorEffect(Camera.Parameters.EFFECT_NONE);
                        SetCameraParameters();
                        Menu_Container.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                Menu_Container.removeAllViews();
                Menu_Container.setVisibility(View.INVISIBLE);
                IS_WB_MENU_VISIBLE = false;
            }
        } else {
            ShowToastNotification("First Turn HDR mode OFF", Toast.LENGTH_SHORT);
        }
    }
    public void onClickColorEffect(View view)
    {
        if (!sharedpreference.getBoolean("HDR", false)) {
            if (!IS_CE_MENU_VISIBLE) {
                IS_CE_MENU_VISIBLE = true;
                IS_WB_MENU_VISIBLE = false;
                IS_TIMER_MENU_VISIBLE = false;
                Menu_Container.removeAllViews();
                Menu_Container.setVisibility(View.VISIBLE);
                Menu_Container.addView(CE_List);
                CreateColorEffectMenu();
                CE_List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long Id) {
                        String Item = String.valueOf(CE_List.getItemAtPosition(position));
                        p.setColorEffect(Item);
                        p.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
                        SetCameraParameters();
                        Menu_Container.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                Menu_Container.removeAllViews();
                Menu_Container.setVisibility(View.INVISIBLE);
                IS_CE_MENU_VISIBLE = false;
            }
        } else {
            ShowToastNotification("First turn HDR mode OFF", Toast.LENGTH_SHORT);
        }
    }
    public void onClickTimer(View view)
    {
        if (!IS_TIMER_MENU_VISIBLE) {
            IS_TIMER_MENU_VISIBLE = true;
            IS_CE_MENU_VISIBLE = false;
            IS_WB_MENU_VISIBLE = false;
            Menu_Container.removeAllViews();
            Menu_Container.setVisibility(View.VISIBLE);
            Menu_Container.addView(Timer_list);
            CreateTimerMenu();
            Timer_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long Id) {
                    switch (position) {
                        case 0:
                            TimeDelay = 0;
                            break;
                        case 1:
                            TimeDelay = 3000;
                            break;
                        case 2:
                            TimeDelay = 5000;
                            break;
                        case 3:
                            TimeDelay = 7000;
                            break;
                        default:
                            TimeDelay = 0;
                    }
                    Menu_Container.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            Menu_Container.removeAllViews();
            Menu_Container.setVisibility(View.INVISIBLE);
            IS_TIMER_MENU_VISIBLE = false;
        }
    }
    public void onClickHdr(View view)
    {
        SharedPreferences.Editor editor = sharedpreference.edit();
        if (Menu_Container.getVisibility() == View.INVISIBLE) {
            if (sharedpreference.getBoolean("HDR", false)) {
                Hdr.setImageResource(R.mipmap.hdr_off);
                editor.putBoolean("HDR", false);
                editor.apply();
                p.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
                p.setColorEffect(Camera.Parameters.EFFECT_NONE);
                SetCameraParameters();
                LoadFlashState();

            } else if (!sharedpreference.getBoolean("HDR", false)) {
                Hdr.setImageResource(R.mipmap.hdr_on);
                editor.putBoolean("HDR", true);
                editor.commit();
                p.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                p.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
                flash.setImageResource(R.mipmap.flash_off);
                SetCameraParameters();
            }
        }
    }

    private void SaveImageInBackground(final byte[] data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                ShowToastNotification("Failed to create folder", Toast.LENGTH_LONG);
            }
        } else {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "CAM" + timeStamp + ".jpg";
            File pictureFile = new File(mediaStorageDir.getPath() + File.separator + fileName);
            Bitmap Image = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (setting.getString(keys.ENHANCED_IMAGES_KEY,"N")!="N")
            {
                Bitmap bitmapResult = Bitmap.createBitmap(Image.getWidth(), Image.getHeight(), Bitmap.Config.ARGB_8888);
                Paint paint = new Paint();
                Canvas canvasResult = new Canvas(bitmapResult);
                ColorMatrix colorMatrix = new ColorMatrix();
                if(setting.getString(keys.ENHANCED_IMAGES_KEY,"N").equals("S") ||
                        setting.getString(keys.ENHANCED_IMAGES_KEY,"N").equals("SC"))
                {
                    colorMatrix.setSaturation(1.6f);
                }
                if(setting.getString(keys.ENHANCED_IMAGES_KEY,"N").equals("C") ||
                        setting.getString(keys.ENHANCED_IMAGES_KEY,"N").equals("SC")) {
                    float scale = 1.5f;
                    float translate = (-.5f * scale + .5f) * 255.f;
                    float[] Contrast = new float[]{
                            scale, 0, 0, 0, translate,
                            0, scale, 0, 0, translate,
                            0, 0, scale, 0, translate,
                            0, 0, 0, 1, 0};
                    colorMatrix.set(Contrast);
                }
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
                paint.setColorFilter(filter);
                canvasResult.drawBitmap(Image, 0, 0, paint);
                if(OpenedCamera == FrontCamera)
                    bitmapResult = MirrorImage(bitmapResult);
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    bitmapResult.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    if(OpenedCamera == FrontCamera)
                        Image = MirrorImage(Image);
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    Image.compress(Bitmap.CompressFormat.JPEG,90,fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    ShowToastNotification("File not found: ", Toast.LENGTH_LONG);
                } catch (IOException e) {
                    ShowToastNotification("Error accessing file: ", Toast.LENGTH_LONG);
                }
            }
            if (locationClass != null)
                location = locationClass.getLocation();
            try {
                path = pictureFile.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ExifInterface();
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(pictureFile));
            sendBroadcast(mediaScanIntent);
            UpdateImage();//Refreshing the gallery and Loading Latest Image Captured
            v.vibrate(80);
        }
        }
        });//End of New Runnable
    }//End of SaveImageInBackground() method

    private void Update_Thumbnail()
    {
        if(thumbnail!=null)
            Gallery.setImageBitmap(thumbnail);
        else
            Gallery.setImageResource(R.mipmap.galleryicon);

        UPDATE_THUMBNAIL_NOW = false;
    }

    Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
                SwitchCamera();
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    private void FlipIcon(View V)
    {
        ObjectAnimator FlipAnimator;
        if(rotation == 0)
            FlipAnimator = ObjectAnimator.ofFloat(V,"rotationX",0f,180f);
        else
            FlipAnimator = ObjectAnimator.ofFloat(V,"rotationY",180f,0f);
        FlipAnimator.addListener(animatorListener);
        FlipAnimator.setDuration(2000);
        FlipAnimator.start();
    }

    private void DefaultResolutionPref(String RearFacing)
    {
        for (int i = 0; i < MypreferenceFragment.CameraResolutions.length; i++) {
            if (RearFacing.equalsIgnoreCase(MypreferenceFragment.CameraResolutions[i])) {
                p.setPictureSize(Resolutions[i].width, Resolutions[i].height);
                p.setPreviewSize(Resolutions[i].width, Resolutions[i].height);
                float result = ((float) Resolutions[i].width / (float) Resolutions[i].height);
                Compare = String.format("%.1f", result);
                break;
            } else if (i == MypreferenceFragment.CameraResolutions.length - 1 && !RearFacing.equalsIgnoreCase(MypreferenceFragment.CameraResolutions[i])) {   //Loading Highest Resolution of Camera by Default at App FirstRun
                p.setPictureSize(Resolutions[i].width, Resolutions[i].height);
                p.setPreviewSize(Resolutions[i].width, Resolutions[i].height);
                float result = ((float) Resolutions[i].width / (float) Resolutions[i].height);
                Compare = String.format("%.1f", result);
            }
        }
    }

    private void ExposureBar()
    {
        MaxExp = p.getMaxExposureCompensation();
        MinExp = p.getMinExposureCompensation();
        exposurebar.setMax(MaxExp+Math.abs(MinExp));
    }

    public String getGeoCoordinates(android.location.Location location , String value) {
        String[] degMinSec;
        if (location == null) return "0/1,0/1,0/1000";
        // You can adapt this to latitude very easily by passing locationClass.getLatitude()
        if(value.equals("Longitude")){
        degMinSec = android.location.Location.convert(location.getLongitude(), android.location.Location.FORMAT_SECONDS).split(":");
            return degMinSec[0] + "," + degMinSec[1] + "," + degMinSec[2] ;}
        else{
            degMinSec = android.location.Location.convert(location.getLatitude(), android.location.Location.FORMAT_SECONDS).split(":");
        return degMinSec[0] + "," + degMinSec[1] + "," + degMinSec[2] ;}
        //return degMinSec[0] + "/1,";
    }

   private Bitmap MirrorImage(Bitmap image)
   {
       Matrix matrix = new Matrix();
       matrix.preScale(-1.0f,1.0f);
       return Bitmap.createBitmap(image,0,0,image.getWidth(),image.getHeight(),matrix,true);
   }

   private boolean VolumeKeyAction(KeyEvent event)
   {
       String key = setting.getString("VolumeKey","");
       switch (key)
       {
           case "T":
               TakePic(null);
               return true;
           case "Z":
               ZoomCamera(event);
               return true;
           case "C":
               return false;
           default:
               return false;

       }
   }

   private void GetExifData(final byte[] array){
       File Temp = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"Temp.jpg");
       FileOutputStream fileOutputStream = null;
       try {
           fileOutputStream = new FileOutputStream(Temp);
           fileOutputStream.write(array);
           fileOutputStream.flush();
           fileOutputStream.close();
       } catch (IOException e) {
           e.printStackTrace();
       }

       String FilePath = Temp.getAbsolutePath();
       try {
           ExifInterface exifInterface1 = new ExifInterface(FilePath);
           ISO = exifInterface1.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS);
           DeviceName = Build.MODEL;
           ShutterSpeed = exifInterface1.getAttribute(ExifInterface.TAG_APERTURE_VALUE);
           FocalLength = exifInterface1.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
           Aperture = exifInterface1.getAttribute(ExifInterface.TAG_F_NUMBER);
       } catch (IOException e) {
           e.printStackTrace();
       }

       Temp.delete();
   }
}
