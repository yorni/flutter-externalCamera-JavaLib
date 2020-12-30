package com.example.javaimpl;

//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.widget.ImageView;
//
//import android.os.Environment;
//
//import android.hardware.usb.UsbDevice;
//import android.os.Looper;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Surface;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
////import com.jiangdg.usbcamera.UVCCameraHelper;
////import com.jiangdg.usbcamera.utils.FileUtils;
////import com.serenegiant.usb.CameraDialog;
////import com.serenegiant.usb.USBMonitor;
////import com.serenegiant.usb.common.AbstractUVCCameraHandler;
////import com.serenegiant.usb.widget.CameraViewInterface;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.io.File;
//import java.nio.file.Files;
//import java.util.Date;
//
//
//
//public class CameraActivity extends AppCompatActivity {
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.camera_activity);
//    }
//
//}
//






//package com.dopo.usbcamera;

        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.widget.ImageView;

        import android.os.Environment;

        import android.hardware.usb.UsbDevice;
        import android.os.Looper;
        import androidx.appcompat.app.AlertDialog;
        import androidx.appcompat.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Surface;
        import android.view.View;
        import android.widget.Button;
        import android.widget.Toast;

        import com.jiangdg.usbcamera.UVCCameraHelper;
        import com.jiangdg.usbcamera.utils.FileUtils;
        import com.serenegiant.usb.CameraDialog;
        import com.serenegiant.usb.USBMonitor;
        import com.serenegiant.usb.common.AbstractUVCCameraHandler;
        import com.serenegiant.usb.widget.CameraViewInterface;

        import java.io.File;
        import java.nio.file.Files;
        import java.util.Date;

public class CameraActivity extends AppCompatActivity implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {

    private static final String TAG = "Debug";

    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private AlertDialog mDialog;
    private Button mButtonToMakeShot = null;

    private boolean isRequest;
    private boolean isPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        // step.1 initialize UVCCameraHelper
        //mUVCCameraView = (CameraViewInterface) mTextureView;
        mUVCCameraView = (CameraViewInterface) findViewById(R.id.camera_view);
        mUVCCameraView.setCallback(this);
        mCameraHelper = UVCCameraHelper.getInstance();
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);
        mButtonToMakeShot =findViewById(R.id.button);

        mCameraHelper.setOnPreviewFrameListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
            @Override
            public void onPreviewResult(byte[] nv21Yuv) {
                //showShortMsg(" usb camera callback"+nv21Yuv.length);
//                File dir = Environment.getDataDirectory();   //获取data目录
//                //Environment.getExternalStorageDirectory(); // 获取SD卡目录
//                File outFile=new File(dir,"/data/com.alex.com.alex.livertmppushsdk.demo/text"+nv21Yuv.length+".txt");
            }
        });
        View text = findViewById(R.id.text);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraHelper.startCameraFoucs();
            }
        });
        mButtonToMakeShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showShortMsg("clicked button");
                //File outputDir = getCacheDir();
                //showShortMsg(outputDir.getPath());
                String picPath = UVCCameraHelper.ROOT_PATH + "test_"+System.currentTimeMillis()
                        + UVCCameraHelper.SUFFIX_JPEG;
                mCameraHelper.capturePicture(picPath, new AbstractUVCCameraHandler.OnCaptureListener() {

                    @Override
                    public void onCaptureResult(String path) {
                        Log.i(TAG,"save path：" + path);

                    }
                });

            }
        });

    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showShortMsg("取消操作");
        }
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface cameraViewInterface, Surface surface) {
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
            isPreview = true;
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface cameraViewInterface, Surface surface, int i, int i1) {

    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface cameraViewInterface, Surface surface) {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
            isPreview = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // step.2 register USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // step.3 unregister USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileUtils.releaseFile();
        // step.4 release uvc camera resources
        if (mCameraHelper != null) {
            mCameraHelper.release();
        }
    }

    private void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            if (mCameraHelper == null || mCameraHelper.getUsbDeviceCount() == 0) {
                showShortMsg("check no usb camera");
                return;
            }
            // request open permission
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    mCameraHelper.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                showShortMsg(device.getDeviceName() + " is out");
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showShortMsg("fail to connect,please check resolution params");
                isPreview = false;
            } else {
                isPreview = true;
                showShortMsg("connecting");
                // initialize seekbar
                // need to wait UVCCamera initialize over
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Looper.prepare();
                        if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
//                            mSeekBrightness.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_BRIGHTNESS));
//                            mSeekContrast.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_CONTRAST));
                        }
                        Looper.loop();
                    }
                }).start();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            showShortMsg("disconnecting");
        }
    };
}