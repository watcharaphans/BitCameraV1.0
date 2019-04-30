package watcharaphans.bitcombine.co.th.bitcamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

public class PreviewPicturesActivity extends AppCompatActivity {

    private static final String TAG = PreviewPicturesActivity.class.getName();
    private static final int REQUEST_CODE_FILENAME_A = 1;
    private static final int REQUEST_CODE_FILENAME_B = 2;
    private static final int REQUEST_CODE_FILENAME_C = 3;
    private static final int REQUEST_CODE_FILENAME_D = 4;

    private ImageView cameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_pictures);

        cameraView = (ImageView) findViewById(R.id.imgView);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String pathPicture = bundle.getString("pathPicture");
            //Toast.makeText(this, "Name : " + pathPicture, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "get Image Preview --> " + pathPicture);
            setImagePreview(pathPicture);
        }

    }

    public void setImagePreview(final String imagePreview) {

        Log.i(TAG, "Set Image Preview --> " + imagePreview);
        String imagePreview2 = imagePreview;
        File imageFile = new File(imagePreview2);
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        //Bitmap resizedbitmap = Bitmap.createScaledBitmap(bitmap, 800, 600, true);

        Log.i(TAG, "Set Image bitmap --> " + bitmap);

        cameraView.setImageBitmap(bitmap);

        LinearLayout btn_capdone = (LinearLayout) findViewById(R.id.linearCapDone);
        btn_capdone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back to MainFragment
                //playSound("button");
                Log.i(TAG, "--> Capture Done!!!");
                setResult(RESULT_OK);
                finish();
            }
        });

        LinearLayout btn_capAgain = (LinearLayout) findViewById(R.id.linearCapAgain);
        btn_capAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Back to Open Camera
                //playSound("button");
                Log.i(TAG, "--> Back Capture...");
                setResult(RESULT_CANCELED);
                finish();
            }
        });

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
            case "save":
                mp = MediaPlayer.create(this, R.raw._save_done);
                mp.start();
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
