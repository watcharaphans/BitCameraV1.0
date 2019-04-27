//https://stackoverflow.com/questions/17968132/set-the-camera-focus-area-in-android

package watcharaphans.bitcombine.co.th.bitcamera.touch2focus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraPreview implements SurfaceHolder.Callback {

    private Context mContext;
    private CameraPreviewCallback callback;
    private String fileName;
    private Camera mCamera = null;
    //public Camera.Parameters params;
    private SurfaceHolder sHolder;
    public List<Camera.Size> supportedSizes;
    private Integer resWidth;
    private Integer resHeight;
    public int isCamOpen = 0;
    public boolean isSizeSupported = false;
    private Handler handler;
    private String pathPicture;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mCamera == null) return;

            mCamera.cancelAutoFocus();
            mCamera.stopPreview();

            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            final List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(new Rect(-100, -100, 100, 100), 1000);
            focusList.add(focusArea);
            params.setFocusAreas(focusList);
            params.setMeteringAreas(focusList);
            //params.setJpegQuality(50);
            mCamera.setParameters(params);
            mCamera.startPreview();
//            int quality = params.getJpegQuality();
//            Log.d(TAG,"--Get Quality setting : " + quality);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mCamera == null) return;

                    mCamera.autoFocus(new AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean b, Camera camera) {
                            callback.onCameraFocus(true);
                        }
                    });
                }
            }, 200); //200 millisecond delay
        }
    };

    private final static String TAG = "CameraPreview";

    public CameraPreview(Context context, CameraPreviewCallback callback, String fileName) {
        this.mContext = context;
        this.callback = callback;
        this.fileName = fileName;
    }

    private int openCamera() {
        if (isCamOpen == 1) {
            releaseCamera();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final String resValue = prefs.getString("image_resolution", "640x480");
        final String strsettingQuality = prefs.getString("image_quality", "90");
        String[] settingRes = resValue.split("x");
        resWidth = Integer.parseInt(settingRes[0]);
        resHeight = Integer.parseInt(settingRes[1]);

        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);

        if (mCamera == null) {
            return -1;
        }

        Camera.Parameters params = mCamera.getParameters();

        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        Camera.Size selected = sizes.get(0);
        Log.i(TAG, "OpenCamera --> Open Width:" + resWidth + ", Height: " + resHeight);
        params.setPreviewSize(resWidth, resHeight);
        Log.i(TAG, "OpenCamera --> setPreviewSize = Width:" + resWidth + ", Height: " + resHeight);

        //TODO กรณี android Version 5 ลงไปจะเปิดกล้องไม่ได้
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        Log.i(TAG, "OpenCamera --> SetFocusMode");

        final List<Camera.Area> focusList = new ArrayList<Camera.Area>();
        Camera.Area focusArea = new Camera.Area(new Rect(-100, -100, 100, 100), 1000);
        focusList.add(focusArea);
        params.setFocusAreas(focusList);
        Log.i(TAG, "OpenCamera --> setFocusAreas");
        params.setMeteringAreas(focusList);
        Log.i(TAG, "OpenCamera --> setMeteringAreas");
        params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        mCamera.setParameters(params);
        Log.i(TAG, "OpenCamera --> setParameters");
        mCamera.startPreview();

        // สั่งให้กล้อง auto-focus หลังจาก delay 200ms
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCamera.autoFocus(new AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {
                        callback.onCameraFocus(true);
                    }
                });
            }
        }, 200); //200 millisecond delay

        try {
            mCamera.setPreviewDisplay(sHolder);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
            return -1;
        }
        isCamOpen = 1;
        return isCamOpen;
    }

    public int isCamOpen() {
        return isCamOpen;
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        isCamOpen = 0;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        sHolder = holder;
        isCamOpen = openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        setCameraDisplayOrientation();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    /**
     * Called from PreviewSurfaceView to set touch focus.
     *
     * @param - Rect - new area for auto focus
     */
    public void doTouchFocus(final Rect tfocusRect) {
        Log.i(TAG, "TouchFocus");
        try {
            final List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
            focusList.add(focusArea);

            Camera.Parameters para = mCamera.getParameters();
            para.setFocusAreas(focusList);
            para.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            para.setMeteringAreas(focusList);
            mCamera.setParameters(para);
            mCamera.autoFocus(new AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    callback.onCameraFocus(false);
                }
            });

            if (handler != null) {
                handler.removeCallbacks(runnable);
            }
            handler = new Handler();
            handler.postDelayed(runnable, 3000);

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Unable to autofocus");
        }
    }

    public void setCameraDisplayOrientation() {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, info);

        int rotation = ((WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

    private Bitmap imageFile2;

    public void takePicture() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final String resValue = prefs.getString("image_resolution", "640x480");
        final String strsettingQuality = prefs.getString("image_quality", "90");
        final int intSettingQuality = Integer.parseInt(strsettingQuality);
        //String text="abc-01-001";
        String[] settingRes = resValue.split("x");
//        int resWidth = Integer.parseInt(settingRes[0]);
//        int resHeight = Integer.parseInt(settingRes[1]);


        Log.d(TAG,"takePicture ----Photo setting : " + resWidth + " X " + resHeight );
        final Camera.Parameters params = mCamera.getParameters();
//        List<Camera.Size> sizes = params.getSupportedPictureSizes();
//        Camera.Size selected = sizes.get(0);
        //params.setPictureSize(selected.width, selected.height);
        params.setJpegQuality(intSettingQuality);
//        int quality = params.getJpegQuality();
        Log.d(TAG,"----Get Quality setting : " + intSettingQuality);
        params.setPictureSize(resWidth, resHeight);
        //params.setPictureSize(1024, 768);
        //params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        Log.d(TAG,"----Set Picture Size : " + intSettingQuality);
        mCamera.setParameters(params);
        Log.d(TAG,"----Set Parameters");
        /*mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                MySurfaceView.TakePictureCallback callback = new MySurfaceView.TakePictureCallback();

            }
        });*/
        mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                try {
                    File imageFile = new File(mContext.getFilesDir(), fileName);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }

                    FileOutputStream fos = new FileOutputStream(imageFile);


                    pathPicture = imageFile.toString();
                    Log.d(TAG,"----Photo Save Path : " + pathPicture);
//                    // NEWLY ADDED CODE STARTS HERE [ stamp date time
//                    Canvas canvas = new Canvas(imageFile2);
//
//                    Paint paint = new Paint();
//                    paint.setColor(Color.WHITE); // Text Color
//                    paint.setTextSize(12); // Text Size
//                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
//                    // some more settings...
//
//                    canvas.drawBitmap(imageFile2, 0, 0, paint);
//                    canvas.drawText("Testing...", 10, 10, paint);
//                    // NEWLY ADDED CODE ENDS HERE ]


                    fos.write(data);
                    fos.flush();
                    fos.close();

                   // String pathImage = imageFile.toString();
                   // ResizeImages(pathImage,pathImage);
                   // updateMediaScanner(mContext, imageFile);

                   // Toast.makeText(mContext, "บันทึกภาพแล้ว", Toast.LENGTH_SHORT).show();

                    callback.onPictureSaved(pathPicture);
                } catch (FileNotFoundException e) {
                    Log.d(TAG,"----FileNotFoundException "+ e);
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.d(TAG,"----IOException "+ e);
                    e.printStackTrace();
                }

                //camera.startPreview();
            }
        });
    }
// Code Show picture in Gallery
    public void updateMediaScanner(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        context.sendBroadcast(intent);
    }

    public static void ResizeImages(String sPath, String sTo) throws IOException {

        Bitmap photo = BitmapFactory.decodeFile(sPath);
        //photo = Bitmap.createScaledBitmap(photo, 300, 300, false);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File f = new File(sTo);
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();

        File file =  new File(sPath);
        file.delete();

    }

    public File getPublicStorageDir(String dirName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), dirName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    public static final int FLASH_MODE_ON = 0;
    public static final int FLASH_MODE_OFF = 1;
    public static final int FLASH_MODE_AUTO = 2;

    public void setFlashMode(int mode) {
        Camera.Parameters params = mCamera.getParameters();
        switch (mode) {
            case FLASH_MODE_ON:
                params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                break;
            case FLASH_MODE_OFF:
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
            case FLASH_MODE_AUTO:
                params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
        }
        mCamera.setParameters(params);
    }

    public interface CameraPreviewCallback {
        public void onCameraFocus(boolean isInitial);
        public void onPictureSaved(String pathPicture);
        public void onHello();
    }
}