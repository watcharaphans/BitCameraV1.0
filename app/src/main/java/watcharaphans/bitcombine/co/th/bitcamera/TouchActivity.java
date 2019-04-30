package watcharaphans.bitcombine.co.th.bitcamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

import watcharaphans.bitcombine.co.th.bitcamera.fragment.MainFragment;
import watcharaphans.bitcombine.co.th.bitcamera.fragment.ScanQrCodeFragment;
import watcharaphans.bitcombine.co.th.bitcamera.touch2focus.CameraPreview;
import watcharaphans.bitcombine.co.th.bitcamera.touch2focus.DrawingView;
import watcharaphans.bitcombine.co.th.bitcamera.touch2focus.PreviewSurfaceView;

@SuppressWarnings("deprecation")
public class TouchActivity extends AppCompatActivity
        implements CameraPreview.CameraPreviewCallback,
                   View.OnClickListener {

    private static final String TAG = TouchActivity.class.getName();

    private PreviewSurfaceView camView;
    private CameraPreview cameraPreview;
    private DrawingView drawingView;
    private Button flashButton;
    private Button backButton;
    private SharedPreferences prefs;
    private String fileName;
    //private QrCodeData qrCodeData;
    //private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch);

        Intent intent = getIntent();
        fileName = intent.getStringExtra("filename");

        camView = (PreviewSurfaceView) findViewById(R.id.preview_surface);
        SurfaceHolder camHolder = camView.getHolder();

        cameraPreview = new CameraPreview(this, this, fileName);
        camHolder.addCallback(cameraPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        camView.setListener(cameraPreview);
        //cameraPreview.changeExposureComp(-currentAlphaAngle);
        drawingView = (DrawingView) findViewById(R.id.drawing_surface);
        camView.setDrawingView(drawingView);

        ImageView takePhotoImageView = (ImageView) findViewById(R.id.imvTakePhoto);
        takePhotoImageView.setOnClickListener(this);

        flashButton = (Button) findViewById(R.id.btnFlash);
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //playSound("button");
                PopupMenu popup = new PopupMenu(TouchActivity.this, flashButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.flash_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_flash_on:
                                setFlash("on");
                                break;
                            case R.id.action_flash_off:
                                setFlash("off");
                                break;
                            case R.id.action_flash_auto:
                                setFlash("auto");
                                break;
                        }
                        return true;
                    }
                });
                popup.show(); //showing popup menu
            }
        });

        backButton = (Button) findViewById(R.id.btnBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "---> Press Back");
                finish();
            }
        });

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String flashValue = prefs.getString("flash_mode", "auto");
        final String resValue = prefs.getString("image_resolution", "640x480");

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setFlash(flashValue);
            }
        }, 200);

        Log.i(TAG, "Flash value --> " + flashValue);
        Log.i(TAG, "Resolution value --> " + resValue);
    }

    private void setFlash(String mode) {

        switch (mode) {
            case "on":
                cameraPreview.setFlashMode(CameraPreview.FLASH_MODE_ON);
                flashButton.setCompoundDrawablesWithIntrinsicBounds(
                        0, R.drawable.ic_flash_on_white, 0, 0);
                break;
            case "off":
                cameraPreview.setFlashMode(CameraPreview.FLASH_MODE_OFF);
                flashButton.setCompoundDrawablesWithIntrinsicBounds(
                        0, R.drawable.ic_flash_off_white, 0, 0);
                break;
            case "auto":
                cameraPreview.setFlashMode(CameraPreview.FLASH_MODE_AUTO);
                flashButton.setCompoundDrawablesWithIntrinsicBounds(
                        0, R.drawable.ic_flash_auto_white, 0, 0);
                break;
        }
    }

    @Override
    public void onCameraFocus(boolean isInitial) {
        if (isInitial) {
            camView.drawInitialFocusRect();
        } else {
            camView.drawFocusRect();
        }
    }

    @Override
    public void onPictureSaved(String pathPicture) {
        //Intent intent = new Intent();

        //setResult(RESULT_OK);
        final boolean view_pic = prefs.getBoolean("view_pic", true);
        Log.i(TAG, "----> Capture Finish Preview : " + view_pic);

        //check ON/OFF Preview Pictures
        if(view_pic){

            switch (fileName) {
                case "picture_a.jpg":
                    Intent intentA = new Intent(this, PreviewPicturesActivity.class);
                    intentA.putExtra("pathPicture", pathPicture);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    startActivityForResult(intentA, REQUEST_CODE_FILENAME_A);
                    Log.i(TAG, "----> Open Preview A");
                    //finish();
                    break;
                case "picture_b.jpg":
                    Intent intentB = new Intent(this, PreviewPicturesActivity.class);
                    intentB.putExtra("pathPicture", pathPicture);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    startActivityForResult(intentB, REQUEST_CODE_FILENAME_B);
                    Log.i(TAG, "----> Open Preview B");
                    //finish();
                    break;
                case "picture_c.jpg":
                    Intent intentC = new Intent(this, PreviewPicturesActivity.class);
                    intentC.putExtra("pathPicture", pathPicture);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    startActivityForResult(intentC, REQUEST_CODE_FILENAME_C);
                    Log.i(TAG, "----> Open Preview C");
                    //finish();
                    break;
                case "picture_d.jpg":
                    Intent intentD = new Intent(this, PreviewPicturesActivity.class);
                    intentD.putExtra("pathPicture", pathPicture);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    startActivityForResult(intentD, REQUEST_CODE_FILENAME_D);
                    Log.i(TAG, "----> Open Preview D");
                    //finish();
                    break;
            }

        }else{
            setResult(RESULT_OK);
            Log.i(TAG, "----> Back to MainFragment");
            finish();
        }
    }

    @Override
    public void onHello() {
    }

    @Override
    public void onClick(View view) {

        int viewId = view.getId();
        switch (viewId) {
            case R.id.imvTakePhoto:
                Log.i(TAG, "----> Press Capture");
                playSound("shutter");
                cameraPreview.takePicture();
                break;
//            case R.id.btnBack:
//                Log.i(TAG, "----> Press back");
//                //finish();
//                break;
        }
    }

    private void playSound(String str){

        MediaPlayer mp;
        switch (str) {
            case "shutter":
                mp = MediaPlayer.create(this, R.raw._shutter);
                mp.start();
                break;
            case "button":
                mp = MediaPlayer.create(this, R.raw._button);
                mp.start();
                break;

        }

    }

    private static final int REQUEST_CODE_FILENAME_A = 1;
    private static final int REQUEST_CODE_FILENAME_C = 2;
    private static final int REQUEST_CODE_FILENAME_D = 3;
    private static final int REQUEST_CODE_FILENAME_B = 4;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "----> requestCode == " + requestCode);
        Log.i(TAG, "----> resultCode == " + resultCode);

        switch (requestCode) {

            case REQUEST_CODE_FILENAME_A:

                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "----> Camera A RESULT ==  OK!!!!");
                    setResult(RESULT_OK);
                    finish();
                }else if(resultCode == RESULT_CANCELED){
                    Log.i(TAG, "----> Camera A RESULT ==  Cancel!!!!");
                }

                break;

            case REQUEST_CODE_FILENAME_B:

                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "----> Camera B RESULT ==  OK!!!!");
                    setResult(RESULT_OK);
                    finish();
                }else if(resultCode == RESULT_CANCELED){
                    Log.i(TAG, "----> Camera B RESULT ==  Cancel!!!!");
                }

                break;

            case REQUEST_CODE_FILENAME_C:

                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "----> Camera C RESULT ==  OK!!!!");
                    setResult(RESULT_OK);
                    finish();
                }else if(resultCode == RESULT_CANCELED){
                    Log.i(TAG, "----> Camera C RESULT ==  Cancel!!!!");
                }

                break;
            case REQUEST_CODE_FILENAME_D:

                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "----> Camera D RESULT ==  OK!!!!");
                    setResult(RESULT_OK);
                    finish();
                }else if(resultCode == RESULT_CANCELED){
                    Log.i(TAG, "----> Camera D RESULT ==  Cancel!!!!");
                }

                break;
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Enables regular immersive mode.
            // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
            // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }else{
            // Enables regular immersive mode.
            // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
            // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
//                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

}
