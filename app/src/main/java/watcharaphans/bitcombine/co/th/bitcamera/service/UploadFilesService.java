package watcharaphans.bitcombine.co.th.bitcamera.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;

import watcharaphans.bitcombine.co.th.bitcamera.MainActivity;
import watcharaphans.bitcombine.co.th.bitcamera.Notification.NotificationHelper;
import watcharaphans.bitcombine.co.th.bitcamera.R;

import static watcharaphans.bitcombine.co.th.bitcamera.App.CHANNEL_ID;
import static watcharaphans.bitcombine.co.th.bitcamera.MainActivity.KEY_MESSENGER_INTENT;

// class for check pictures and FTP All pictures in memory to Server. When Wifi connect
// คลาสสำหรับ เช็คไฟล์รูปภาพที่ค้างอยู่ และจัดการส่ง FTP ไปที่ Server ทั้งหมดเมื่อต่อ Wifi ได้
public class UploadFilesService extends Service {

    private MyThread thread;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    boolean uploadComplete;
    private static final int NOTI_SECONDARY1 = 1200;
   // private NotificationHelper noti = null;
    private NotificationHelper noti = null;
    private int totalPictures;
    private int finishPictures;
    private Context mContext;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        thread = new MyThread();
        noti = new NotificationHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.mContext = getApplicationContext();
        this.totalPictures = intent.getIntExtra(KEY_MESSENGER_INTENT,0);

        Toast.makeText(this,"Service Start..",
                Toast.LENGTH_LONG).show();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("id", "an", NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription("no sound");
            notificationChannel.setSound(null, null);
            notificationChannel.enableLights(false);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationBuilder = new NotificationCompat.Builder(this, "id")
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle("Upload")
                .setContentText("Upload รูปภาพ")
                .setDefaults(0)
                .setAutoCancel(true);
        notificationManager.notify(0, notificationBuilder.build());

        if(!thread.isAlive()){
            //Log.d(TAG, "Start Thread Upload files...");
            thread.start();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        Toast.makeText(this, "Service Stopped..",
                Toast.LENGTH_LONG).show();

        Log.d("UploadFilesService", "************* Service Stopped.. **************");

        Notification.Builder nb = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nb = noti.getNotification2("แจ้งเตือน", "ทำการส่งรูปต่อ ");
            if (nb != null) {
                noti.notify(NOTI_SECONDARY1, nb);
            }
        }else {
            noti.showHeadsUpNotification();
        }

        thread.finish = true;

    }

    private class MyThread extends Thread{

        private static final String TAG = "UploadFilesService";
        private static final int DELAY = 3000;
        //private int num = 0;
        private boolean finish = false;
        public FTPClient FTPUpload = null;


        public void run(){

            int num = 0;
            FTPUpload = new FTPClient();
            finishPictures = totalPictures;
            uploadComplete = false;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            // TODO default = 192.168.1.2
            final String settingIP = prefs.getString("edit_ip", "192.168.2.222");

            Log.d(TAG, "*** Thread Start *** total : " + totalPictures + " IP = " +settingIP);
            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "BitCamera");
            File[] files = directory.listFiles();

            Log.d(TAG, "Thread List Pictures Size: " + files.length);
            Boolean countPic = true;
            while (countPic){

                if (files != null && files.length > 0) {

                    for (int i = 0; i < files.length; i++) {

                        if (files[i].isDirectory()) {

                            File filePic = new File(String.valueOf(files[i]));
                            File[] filePic2 = filePic.listFiles();

                            int fileSize = filePic2.length;
                            long total = 0;
                            for (int run = 0; run < filePic2.length; run++) {
                                num++;
                                Log.d(TAG, "1.----> FTP " + num + " FileName Dir 2 :" + filePic2[run]);
                                String subpath =filePic2[run].toString();
                                String[] split1 = subpath.split("BitCamera");
                                String firstSubString = split1[0];
                                String secondSubString = split1[1];

                                String s2 = secondSubString;
                                String[] split2 = s2.split("/");
                                String DirPic = split2[1];
                                String NamePic = split2[2];
                                String DestPathPic = "_visitors/" + DirPic + "/" + NamePic;
                                String DestPath = "_visitors/" + DirPic;

                               // Log.d(TAG, "*** FTP Service " + subpath + " ---> " + DestPathPic);
                                // ใช้ FTPClient ต้องอยู่ใน try

                                try {

                                    FTPUpload.connect(settingIP, 21);
                                    boolean status = false;
                                    if (FTPReply.isPositiveCompletion(FTPUpload.getReplyCode())) {
                                        // login using username & password
                                        FTPUpload.login("root", "solokey");

                                        total = num;
                                        finishPictures--;

                                        int progress = (int) ((double) (total * 100) / (double) totalPictures);
                                        //int progress = (int) ((double) (total * 100) / (double) fileSize);

                                        Log.d(TAG,"2.----> Upload " + total + " --> " + progress);
                                        updateNotification(progress);

                                        FTPUpload.setFileType(FTP.BINARY_FILE_TYPE);
                                        FTPUpload.enterLocalPassiveMode();
                                        ftpMakeDirService(DestPath);
                                        // Log.d(TAG, "Path Text ===> " + Environment.getExternalStorageDirectory() + "/TAGFtp/" + TEMP_FILENAME +" **** "+ TEMP_FILENAME );
                                        status = ftpUploadService(subpath,DestPathPic,"/");

                                        if (status == true) {

                                            Log.d(TAG, "3.----> Upload success");
                                        } else {

                                            uploadComplete = false;
                                            Log.d(TAG, "3.----> Upload failed");
                                        }

                                        // Log.d(TAG, "Path Text ===> " + Environment.getExternalStorageDirectory() + "/TAGFtp/" + TEMP_FILENAME +" **** "+ TEMP_FILENAME );
                                    }else{
                                        Log.d(TAG, "Error: Disconnect to host " + "192.168.2.211");
                                        uploadComplete = false;
                                        stopSelf();
                                    }

                                }catch (Exception e) {
                                    Log.d(TAG, "Error: could not connect to host " + "192.168.2.211");
                                    uploadComplete = false;
                                    stopSelf();
                                }// End Try connect
                            }
                        } else {
                            if (files[i].getName().contains(".png") || files[i].getName().contains(".jpg")
                                    || files[i].getName().contains(".jpeg")
                                    || files[i].getName().contains(".gif"))
                            {
                                Log.d(TAG, Integer.toString(i) + ") Thread FileName JPG :" + files[i]);
                                // picArrList.add(files[i].toString());
                                uploadComplete = false;
                            }
                        }
                    }

                }// End Loop check folder

                countPic = false;
                uploadComplete = true;
                //onDownloadComplete(uploadComplete);

                if (finish){
                    stopSelf();
                    return;
                }
            }// end while

        }// run

            public boolean ftpMakeDirService(String new_dir_path) {
                try {
                    boolean status = FTPUpload.makeDirectory(new_dir_path);
                    return status;
                } catch (Exception e) {
                    Log.d(TAG, "Error: could not create new directory named "
                            + new_dir_path);
                }
                return false;
            }

            public boolean ftpUploadService(String srcFilePath, String desFileName,
                    String desDirectory) {
                boolean status = false;
                try {
                    FileInputStream srcFileStream = new FileInputStream(srcFilePath);

                    status = FTPUpload.storeFile(desFileName, srcFileStream);

                    File file = new File(srcFilePath);
                    Log.d(TAG, srcFilePath+ "Upload files ===> Success");
                    if (file.delete()) {
                        Log.i(TAG, "Delete file successfully");
                    } else {
                        Log.e(TAG, "Error deleting file");
                    }
                    srcFileStream.close();

                    return status;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "upload failed: " + e);
                }
                return status;
            }

        }// My Thread

    private void updateNotification(int currentProgress) {

        notificationBuilder.setProgress(100, currentProgress, false);
        notificationBuilder.setContentText("กำลังส่งรูปภาพ : " + currentProgress + "%");
        notificationManager.notify(0, notificationBuilder.build());

    }

    final void onDownloadComplete(boolean downloadComplete) {
        sendProgressUpdate(finishPictures);

        notificationManager.cancel(0);
        notificationBuilder.setProgress(0, 0, false);
        notificationBuilder.setContentText("ส่งรูปภาพสำเร็จทั้งหมด = " + totalPictures);
        notificationManager.notify(0, notificationBuilder.build());

    }

    private void sendProgressUpdate(Integer uploadComplete) {

        Log.d("UploadFilesService", "sendProgressUpdate : " + uploadComplete);
//        Intent intent = new Intent(this, MainActivity.class);
//
//        intent.putExtra("uploadComplete", uploadComplete);

//        Intent in = new Intent("watcharaphans.bitcombine.co.th.bitcamera");
//        Bundle extras = new Bundle();
//        extras.putInt("Key_upload",uploadComplete );
//        in.putExtras(extras);
//        this.sendBroadcast(in);

//        Intent intent = new Intent();
//        intent.setAction("watcharaphans.bitcombine.co.th.bitcamera");
//        intent.putExtra("Key_upload",uploadComplete);
       // sendBroadcast(intent);

 //       startActivity(intent);

//        Intent broadCastIntent = new Intent();
//        broadCastIntent.setAction("watcharaphans.bitcombine.co.th.bitcamera");
//        broadCastIntent.putExtra("uploadComplete", uploaComplete);
//        sendBroadcast(broadCastIntent);
        // LocalBroadcastManager.getInstance(BackgroundNotificationService.this).sendBroadcast(intent);

    }

}// Class UploadFilesService
