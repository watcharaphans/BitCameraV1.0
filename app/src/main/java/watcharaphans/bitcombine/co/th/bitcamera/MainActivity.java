package watcharaphans.bitcombine.co.th.bitcamera;

import android.Manifest;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Lock;

import watcharaphans.bitcombine.co.th.bitcamera.fragment.MainFragment;
import watcharaphans.bitcombine.co.th.bitcamera.fragment.ScanQrCodeFragment;
import watcharaphans.bitcombine.co.th.bitcamera.service.CountPictures;
import watcharaphans.bitcombine.co.th.bitcamera.service.MyBoundService;
import watcharaphans.bitcombine.co.th.bitcamera.service.UploadFilesService;

public class MainActivity extends AppCompatActivity
        implements MainFragment.MainFragmentListener {

   // private static final String TAG = MainActivity.class.getName();
    private static final String TAG = "BitMainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    public static final String KEY_MESSENGER_INTENT = BuildConfig.APPLICATION_ID + ".KEY_MESSENGER_INTENT";

    public static final String FILENAME_A = "picture_a.jpg";
    public static final String FILENAME_B = "picture_b.jpg";
    public static final String FILENAME_C = "picture_c.jpg";
    public static final String FILENAME_D = "picture_d.jpg";

    int PERMISSION_ALL = 1;
    private String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Camera mCamera = null;
    private IncomingMessageHandler mHandler;
    private Integer PicNum = 0;
    private Integer picBalance = 0;

    StartActivityOnBootReceiver myReceiver;
    public static final String PROGRESS_UPDATE = "progress_update";
    private BroadcastReceiver broadcastReceiver;

    private MyBoundService myBoundService;
    private boolean mIsBound;
    private TextView tv_countPic;
    private ImageView wifiImageView;
    private ServiceConnection mServiceConn;
    public static Boolean statusWifi = false;
    private Boolean statusUpload = false;
    private boolean isRegistered = false;
    PowerManager.WakeLock wakeLock;
    boolean checkFTP = false;
    private int batteryStatus = 0;
    public TextView txtStatusBattery;
    private DevicePolicyManager devicePolicyManager;
    private ActivityManager activityManager;
    private ComponentName compName;
    public static final int RESULT_ENABLE = 11;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        wakeLock.acquire();

        PreferenceManager.setDefaultValues(this,R.xml.preferences, false);
        PreferenceManager.setDefaultValues(this,R.xml.picture_preferences,false);

        if (!hasPermissions(this, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)) {
            // Permission is not granted
            /*ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION
            );*/
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else { // Permission granted.
            main();
        }

        wifiImageView = (ImageView) findViewById(R.id.status_wifi);
        ImageView settingsImageView = (ImageView) findViewById(R.id.imvSettings);
        settingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, PassScreenActivity.class);
                intent.putExtra("key_password", "normal");
                startActivity(intent);
            }
        });

        tv_countPic = (TextView) findViewById(R.id.countPic);

        doBindService();
        mHandler = new IncomingMessageHandler(this);
        myReceiver = new StartActivityOnBootReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(myReceiver, filter);

        isRegistered = true;

        // Register BroadcastReceiver
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);

        txtStatusBattery = (TextView) findViewById(R.id.txtBattery);

        //setWorkingModeVisibility(true);

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
        startActivityForResult(intent, RESULT_ENABLE);

        LinearLayout linearLockScreen = (LinearLayout)findViewById(R.id.linearLockScreen);
        linearLockScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean active = devicePolicyManager.isAdminActive(compName);
                Log.e(TAG,"------> [Click Lock Screen] : "+ active);
                if (active) {
                    devicePolicyManager.lockNow();
                } else {
                    Log.e(TAG,"You need to enable the Admin Device Features"+ active);
                    //Toast.makeText(this, "You need to enable the Admin Device Features", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }  // Main Method

    private void doBindService() {
        mServiceConn = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MyBoundService.LocalBinder binder =
                        (MyBoundService.LocalBinder) service;
                myBoundService = binder.getService();
                mIsBound = true;
                Log.d(TAG, "********* Connect Bound Service");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mIsBound = false;
                Log.d(TAG, "********* Disconnect Bound Service");
            }
        };

        // ผูก activity เข้ากับ bound service
        Intent intentBound =
                new Intent(this, MyBoundService.class);
        bindService(intentBound, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    private void doUnbindService(){
        Log.d(TAG, "Unbind Service");
        if(mIsBound){
            mIsBound = false;
            unbindService(mServiceConn);
            Log.d(TAG, "Unbind Service **Success**");
        }
    }

    public void countPicture(){

        final String pathToWatch = getPublicStorageDir().getAbsolutePath();

        //Log.i(TAG, "---Start count pictures--- " + pathToWatch);

        CountPictures countPictures = new CountPictures(
            new CountPictures.CountPicturesCallback() {

            @Override
            public void onCountFinish(Integer picnum) {

                PicNum = picnum;
                Log.i(TAG, "--MainActivity--onCountFinish : " + picnum);
                tv_countPic.setText(PicNum.toString());
            }

            @Override
            public void onCountFailed() {
                // ไม่ต้องทำอะไร, รอเก็บตก
            }
        }
        );
        countPictures.execute();

    }

    public File getPublicStorageDir() {

        File picDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "BitCamera");
      //  File dir = new File(appDir, qrCodeData.dirName);
        if (picDir.mkdirs()) {
            Log.i(TAG, "Directory " + picDir.getAbsolutePath() + " created successfully");
        } else {
            Log.d(TAG, "Directory already exists or error creating directory: " + picDir.getAbsolutePath());
        }

        return picDir;
    }

    private void registerReceiver() {

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                statusWifi = intent.getBooleanExtra("Key_StatusWifi",false);
                Log.e(TAG,"------ [broadcastReceiver Wifi] == "+statusWifi);

                if(statusWifi == true){

                    Log.d(TAG,"------ Wifi Connect == "+statusWifi);
                    if(statusUpload){
                        Log.d(TAG,"------ Start Bound Service");
                        myBoundService.startPretendLongRunningTask(PicNum);
                    }
                    wifiImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_green));

                } else if(statusWifi == false) {
                    Log.d(TAG,"------ Wifi Disconnect == "+statusWifi);
                    wifiImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_red));
                }

//                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
//                Log.e(TAG,"------ Status Battery == "+ level);

                // Start Check Wifi
//                if(PicNum > 0 && statusWifi == true ){
//                    Log.d(TAG, "Found pic = " + PicNum + " & Wifi Connect!! ");
//
//                }else if(PicNum > 0 && statusWifi == false){
//                    Log.d(TAG, "Found pic = " + PicNum + " & Wifi Disconnect..");
//
//                }else if(PicNum == 0 && statusWifi == false){
//                    Log.d(TAG, "No pic = " + PicNum + " & Wifi Disconnect..");
//                }else{
//                    Log.d(TAG, "Check pic = Failed & Check Wifi = Failed ");
//                }

//                Bundle extras = intent.getExtras();
//                Integer count_complete = extras.getInt("Key_upload");
//
//                Log.d(TAG,"------MainActivity Upload files -----" + count_complete);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("watcharaphans.bitcombine.co.th.bitcamera"));

    }

    private void StartUploadService(){

        Log.d(TAG, "****** Start Thread Upload Service ******* ");

            Log.e(TAG, ">>>>> Start Bound Service picnum ="+PicNum);

            //picBalance = myBoundService.getProgress();

            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {

                @Override
                public void run() {

                    countPicture();
                    picBalance = PicNum;
                    checkFTP = myBoundService.checkConnFTP();

                    batteryStatus = getBatteryPercentage(getBaseContext());
                    Log.d(TAG,"###### Loop checkFTP :"+checkFTP +" ###### PIC :"+ picBalance + "######  Wifi:" + statusWifi +
                    "##### StatusUpload : " + statusUpload + " ##### Batt : "+batteryStatus);
                    txtStatusBattery.setText(Integer.toString(batteryStatus) + "%");

                    // 1) Wifi = OK , FTP = OK , PicNum > 0
                    if(picBalance > 0 && checkFTP == true && statusWifi == true) { //&& checkFTP==true

                        String tmpStr10 = String.valueOf(picBalance);
                        tv_countPic.setText(tmpStr10);
                        //picBalance = myBoundService.getProgress();
                        statusUpload = true;
                        Log.d(TAG, "****** [1] Uploading Service = " + picBalance + " StatusUpload : "
                                + statusUpload + " checkFTP : " + checkFTP);

                        wifiImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_green));
                        handler.postDelayed(this, 3000);

                    // 2) Wifi = OK , FTP = Failed , PicNum > 0
                    }else if(picBalance > 0 && checkFTP == false && statusWifi == true) {
                        Log.d(TAG, "****** [2] Uploading Service = " + picBalance + " StatusUpload : "
                                + statusUpload + " checkFTP : " + checkFTP);
                        wifiImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_yellow));
                        myBoundService.startPretendLongRunningTask(PicNum);
                        handler.postDelayed(this, 3000);

                    // 3) Wifi = Failed , FTP = Failed , PicNum > 0
                    }else if(picBalance > 0 && checkFTP == false && statusWifi == false) {
                        Log.d(TAG, "****** [3] Uploading Service = " + picBalance + " StatusUpload : "
                                + statusUpload + " checkFTP : " + checkFTP);
                        wifiImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_red));
                        myBoundService.startPretendLongRunningTask(PicNum);
                        statusUpload = false;
                        handler.postDelayed(this, 3000);

                        // 4) Pic = 0 , FTP = false , Wifi = true
                    }else if(statusWifi == true && picBalance == 0 && checkFTP == false ){

                        String tmpStr10 = String.valueOf(picBalance);
                        tv_countPic.setText(tmpStr10);
                        //PicNum = picBalance;
                        statusUpload = false;
                        wifiImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_green));
                        Log.d(TAG, "****** [4] Upload Service = " + picBalance+ " StatusUpload : " + statusUpload);
                        //Log.d(TAG,"***>> Check Thread : "+ myBoundService.checkThread());
                        //handler.removeCallbacks(this);
                        handler.postDelayed(this, 3000);

                        // [Start] เช็คว่ามีรูปภาพเพิ่มมาภายหลัง จะทำการ upload ใหม่
                    }else if(statusWifi == true && picBalance > 0 && checkFTP == false &&  statusUpload == false){

                        Log.d(TAG, "****** [Start] Upload Service = " + picBalance+ " StatusUpload : " + statusUpload);
                        if(statusUpload){
                            Log.d(TAG,"------ Start Bound Service");
                            myBoundService.startPretendLongRunningTask(PicNum);
                        }

                        handler.postDelayed(this, 3000);

                        // [5]
                    }else{

                        Log.d(TAG, "****** [5] Upload Service = " + picBalance+ " StatusUpload : " + statusUpload + " Wifi :"+statusWifi);
                        handler.postDelayed(this, 3000);
                    }
                }
            };
            handler.postDelayed(runnable, 3000);

//        }else{
//
//              statusUpload = false;
//              Log.d(TAG, "****** Upload Service connectWifi = " +statusWifi+ " StatusUpload =   " + statusUpload );
//
//        }
    }

    public void getResultFormFragment(String result){
        if(result == "countpic"){
            //TODO--ทำคำสั่ง update ยอดรูปภาพ
            //countPicture();
            // playSound("save");
        }
    }

    private void setObservers(){
        final Handler handler = new Handler();
        final int[] progress = {0};
                Runnable runnable = new Runnable() {
            @Override
            public void run() {

                progress[0]++;
                Log.d(TAG,"--> Bound Service = " +progress[0]);
                //handler.postDelayed(this,3000);

                if(progress[0] != 10){
                    handler.postDelayed(this, 100);
                }else{
                    handler.removeCallbacks(this);
                }
            }
        };
        handler.postDelayed(runnable,3000);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//        registerReceiver(myReceiver, filter);

        Log.d(TAG, "---> MainActivity = onStart " );
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();

// TODO อาจหาวิธีย้ายให้ไปทำตอน setting ที่ SettingsFragment


//        if (btWorkingMode.getVisibility() == View.VISIBLE){
//            btWorkingMode.setVisibility(View.INVISIBLE);
//            Log.d(TAG, "---> MainActivity = Not show  ");
//        }else{
//            btWorkingMode.setVisibility(View.VISIBLE);
//            Log.d(TAG, "---> MainActivity = show ");
//
//        }

        Log.d(TAG, "---> MainActivity = onResume ");
        StartUploadService();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "---> MainActivity = onPause ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "---> MainActivity = onStop ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unbindService(mServiceConn);
        Log.d(TAG, "---> MainActivity = onDestroy");
        Log.d(TAG, "---> MainActivity = doUnbindService()");
        doUnbindService();

        if(broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            Log.d(TAG, "---> MainActivity = unregisterReceiver(broadcastReceiver)");
        }
        if(myReceiver != null){
            unregisterReceiver(myReceiver);
            Log.d(TAG, "---> MainActivity = unregisterReceiver(myReceiver)");
        }
        // screen and CPU will stay awake during this section

        wakeLock.release();
    }

    private void scheduleJob(int jobId) {
        final JobInfo.Builder builder = new JobInfo.Builder(
                jobId,
                new ComponentName(
                        getPackageName(),
                        UploadFilesService.class.getName()
                )
        );

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPersisted(true);
        builder.setPeriodic(60 * 1000);

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler.schedule(builder.build()) <= 0) {
            Toast.makeText(this, "Job schedule ERROR! - id: " + jobId, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Job schedule OK - id: " + jobId, Toast.LENGTH_LONG).show();
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void main() {

        countPicture();
        registerReceiver();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.contentFragmentMain, new ScanQrCodeFragment())
                .commit();


        //startLoopCountPic();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    main();
                } else {
                    // permission denied
                    String msg = "แอพไม่สามารถทำงานได้ เพราะไม่ได้รับอนุญาตให้เข้าถึงกล้อง";
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(msg)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
                            .show();
                }
            }
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

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void setToolbarVisibility(int visibility) {
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.toolbarMain);
        mainToolbar.setVisibility(visibility);
    }

    private static final int REQUEST_CODE_FILENAME_A = 1;
    private static final int REQUEST_CODE_FILENAME_C = 2;
    private static final int REQUEST_CODE_FILENAME_D = 3;
    private static final int REQUEST_CODE_FILENAME_B = 4;

    @Override
    public void onClickCameraImageA() {
        Intent intent;
        intent = new Intent(this, TouchActivity.class);
        intent.putExtra("filename", FILENAME_A);
        startActivityForResult(intent, REQUEST_CODE_FILENAME_A);
    }

    @Override
    public void onClickCameraImageB() {
        Intent intent;
        intent = new Intent(this, TouchActivity.class);
        intent.putExtra("filename", FILENAME_B);
        startActivityForResult(intent, REQUEST_CODE_FILENAME_B);
    }

    @Override
    public void onClickCameraImageC() {
        Intent intent;
        intent = new Intent(this, TouchActivity.class);
        intent.putExtra("filename", FILENAME_C);
        startActivityForResult(intent, REQUEST_CODE_FILENAME_C);
    }

    @Override
    public void onClickCameraImageD() {
        Intent intent;
        intent = new Intent(this, TouchActivity.class);
        intent.putExtra("filename", FILENAME_D);
        startActivityForResult(intent, REQUEST_CODE_FILENAME_D);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "---> MainActivity = resultCode :: " + resultCode);
        Log.e(TAG, "---> MainActivity = requestCode :: " + requestCode);

        switch (requestCode) {

            case REQUEST_CODE_FILENAME_A:
                //todo:
                if (resultCode == RESULT_OK) {
                    File imageFile = new File(getFilesDir(), FILENAME_A);
                    Log.d(TAG, "---> Image A = "+imageFile);
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    Log.d(TAG, "---> bitmap A = "+bitmap);
                    int width=960;
                    int height=720;
                    Bitmap resizedbitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                    MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag("main_fragment");
                    fragment.setImageViewA(resizedbitmap);
                }

                break;

            case REQUEST_CODE_FILENAME_B:
                //todo:
                if (resultCode == RESULT_OK) {
                    File imageFile = new File(getFilesDir(), FILENAME_B);
                    Log.d(TAG, "---> Image B = "+imageFile);
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    Log.d(TAG, "---> bitmap B = "+bitmap);
                    int width=960;
                    int height=720;
                    Bitmap resizedbitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                    MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag("main_fragment");
                    fragment.setImageViewB(resizedbitmap);
                }

                break;

            case REQUEST_CODE_FILENAME_C:
                //todo:
                if (resultCode == RESULT_OK) {
                    File imageFile = new File(getFilesDir(), FILENAME_C);
                    Log.d(TAG, "---> Image C = "+imageFile);
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    Log.d(TAG, "---> bitmap C = "+bitmap);
                    int width=960;
                    int height=720;
                    Bitmap resizedbitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                    MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag("main_fragment");
                    fragment.setImageViewC(resizedbitmap);
                }

                break;
            case REQUEST_CODE_FILENAME_D:
                //todo:
                if (resultCode == RESULT_OK) {
                    File imageFile = new File(getFilesDir(), FILENAME_D);
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    int width=960;
                    int height=720;
                    Bitmap resizedbitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                    MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag("main_fragment");
                    fragment.setImageViewD(resizedbitmap);
                }
//                else if(resultCode == RESULT_CANCELED){
//                    Intent intent;
//                    intent = new Intent(this, TouchActivity.class);
//                    intent.putExtra("filename", FILENAME_D);
//                    startActivityForResult(intent, REQUEST_CODE_FILENAME_D);
//                }
                break;
        }
    }


    private static class IncomingMessageHandler extends Handler {

        // Prevent possible leaks with a weak reference.
        private WeakReference<MainActivity> mActivity;

        IncomingMessageHandler(MainActivity activity) {
            super(/* default looper */);
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mActivity.get();
            if (mainActivity == null) {
                // Activity is no longer available, exit.
                return;
            }
            //final TextView testTextView = (TextView) mainActivity.findViewById(R.id.test_text_view);
            Message m;
        }
    }

    public void setWorkingModeVisibility(boolean status){

        SharedPreferences prefsWorkMode;
        TextView txtWorkingMode = (TextView) findViewById(R.id.txtWorkingMode);

        prefsWorkMode = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        final String workingMode = prefsWorkMode.getString("working_mode", "mode-in");

        Log.d(TAG, "---> Set Working Mode  = "+workingMode);
        if(status){
            txtWorkingMode.setVisibility(View.VISIBLE);
            switch(workingMode) {
                case "mode-in" :
                    txtWorkingMode.setText("IN");
                    txtWorkingMode.setTextColor(Color.parseColor("#ffffff")); //"#29cc00"
                    txtWorkingMode.setBackgroundColor(Color.parseColor("#008000"));
                    break;
                case "mode-out" :
                    txtWorkingMode.setText("OUT");
                    txtWorkingMode.setTextColor(Color.parseColor("#ffffff")); //#ff0000
                    txtWorkingMode.setBackgroundColor(Color.parseColor("#FF0000"));
                    break;
            }
        }else{
            txtWorkingMode.setVisibility(View.INVISIBLE);
        }
    }

    private void playSound(String str) {

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

    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }
}  // Main class
