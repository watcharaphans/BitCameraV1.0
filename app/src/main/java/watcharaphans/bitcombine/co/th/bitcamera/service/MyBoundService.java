package watcharaphans.bitcombine.co.th.bitcamera.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;

import watcharaphans.bitcombine.co.th.bitcamera.Notification.NotificationHelper;

public class MyBoundService extends Service {

    private static final String TAG = "MyBoundService";
    private Handler mHandler;
    private int mProgress, mMaxValue;
    private Boolean mIsPaused, mCheckConnFtp;

    boolean uploadComplete;
    private static final int NOTI_SECONDARY1 = 1200;
    private NotificationHelper noti = null;
    private int totalPictures;
    private int finishPictures;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private static final int DELAY = 3000;
    private boolean finish = false;

    private MyThread thread;

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public void resetTask() {
        mProgress = 0;
    }

    public class LocalBinder extends Binder {
        public MyBoundService getService(){

            return MyBoundService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        mProgress = 0;
        mIsPaused = true;
        mMaxValue = 0;
        mCheckConnFtp = false;
        noti = new NotificationHelper(this);

    }

    public String getCurrentThaiDate(){
        String[] thaiMonths = {"มกราคม","กุมภาพันธ์"};
        Calendar calendar = Calendar.getInstance();
        int d = calendar.get(Calendar.DAY_OF_MONTH);
        int m = calendar.get(Calendar.MONTH);
        int y = calendar.get(Calendar.YEAR) + 543;
        String thaidate = d + "/" + m + "/" + y;
        return  thaidate;
    }

    public int getProgress(){
        return mProgress;
    }

    public void pausePretendLongRunningTask(){
        mIsPaused = true;
    }

    public void startPretendLongRunningTask(final int countPic){

        thread = new MyThread();
        totalPictures = countPic;
        if(!thread.isAlive()){
            Log.d(TAG, "*** Start Thread Upload files..." + thread.isAlive());
            thread.start();
        }else{
            Log.d(TAG, "*** Start Thread Upload files..." + thread.isAlive());
        }
//        final Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
////                if(mProgress >= mMaxValue){
//////                    Log.d(TAG, "run: removing callbacks" + mProgress + " " +mMaxValue);
//////                    mHandler.removeCallbacks(this); // remove callbacks from runnable
//////                    pausePretendLongRunningTask();
//////                }
//////                else{
//////                    Log.d(TAG, "run: progress: " + mProgress);
//////                    mProgress += 10; // increment the progress
//////                    mHandler.postDelayed(this, 1000); // continue incrementing
//////                }
//
//                int num = 0;
//                FTPUpload = new FTPClient();
//                totalPictures = countPic;
//                mProgress = countPic;
//                finishPictures = totalPictures;
//                uploadComplete = false;
//
//                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                // TODO default = 192.168.1.2
//                final String settingIP = prefs.getString("edit_ip", "192.168.2.222");
//
//                Log.d(TAG, "*** Bound Start *** total : " + totalPictures + " IP = " +settingIP);
//                File directory = new File(Environment.getExternalStoragePublicDirectory(
//                        Environment.DIRECTORY_PICTURES), "BitCamera");
//                File[] files = directory.listFiles();
//
//                Log.d(TAG, "Bound List Pictures Size: " + files.length);
//                Boolean countPic = true;
//                while (countPic){
//
//                    if (files != null && files.length > 0) {
//
//                        for (int i = 0; i < files.length; i++) {
//
//                            if (files[i].isDirectory()) {
//                                // Log.d(TAG, "FileName Dir 2 :" + files[i]);
//                                File filePic = new File(String.valueOf(files[i]));
//                                File[] filePic2 = filePic.listFiles();
//
//                                // Log.i(TAG, "FileName Dir 1 Size :" + filePic2.length + "---"+files[i]);
//
//                                int fileSize = filePic2.length;
//                                long total = 0;
//                                for (int run = 0; run < filePic2.length; run++) {
//                                    num++;
//                                    Log.d(TAG, "1.----> FTP " + num + " FileName Dir 2 :" + filePic2[run]);
//
//                                    String subpath =filePic2[run].toString();
//                                    String[] split1 = subpath.split("BitCamera");
//                                    String firstSubString = split1[0];
//                                    String secondSubString = split1[1];
//
//                                    String s2 = secondSubString;
//                                    String[] split2 = s2.split("/");
//                                    String DirPic = split2[1];
//                                    String NamePic = split2[2];
//                                    String DestPathPic = "_visitors/" + DirPic + "/" + NamePic;
//                                    String DestPath = "_visitors/" + DirPic;
//
//                                    try {
//
//                                        FTPUpload.connect(settingIP, 21);
//                                        boolean status = false;
//                                        if (FTPReply.isPositiveCompletion(FTPUpload.getReplyCode())) {
//                                            // login using username & password
//                                            FTPUpload.login("root", "solokey");
//
//                                            total = num;
//                                            finishPictures--;
//
//                                            int progress = (int) ((double) (total * 100) / (double) totalPictures);
//                                            //int progress = (int) ((double) (total * 100) / (double) fileSize);
//
//                                            Log.d(TAG,"2.----> Upload " + total + " --> " + progress);
////                              //              updateNotification(progress);
////
////                                            FTPUpload.setFileType(FTP.BINARY_FILE_TYPE);
////                                            FTPUpload.enterLocalPassiveMode();
////                                            ftpMakeDirService(DestPath);
////                                            // Log.d(TAG, "Path Text ===> " + Environment.getExternalStorageDirectory() + "/TAGFtp/" + TEMP_FILENAME +" **** "+ TEMP_FILENAME );
////                                            status = ftpUploadService(subpath,DestPathPic,"/");
////
////                                            if (status == true) {
////
////                                                Log.d(TAG, "Upload success");
////                                                mProgress--;
////                                            } else {
////
////                                                uploadComplete = false;
////                                                Log.d(TAG, "Upload failed");
////                                            }
//
//                                            // Log.d(TAG, "Path Text ===> " + Environment.getExternalStorageDirectory() + "/TAGFtp/" + TEMP_FILENAME +" **** "+ TEMP_FILENAME );
//                                        }else{
//                                            Log.d(TAG, "Error: Disconnect to host " + settingIP);
//                                            uploadComplete = false;
//                                          //  stopSelf();
//                                        }
//
//                                    }catch (Exception e) {
//                                        Log.d(TAG, "Error: Cannot Login to host " + settingIP);
//                                        uploadComplete = false;
//                                       // stopSelf();
//                                    }
//                                }
//                            } else {
//                                if (files[i].getName().contains(".png") || files[i].getName().contains(".jpg")
//                                        || files[i].getName().contains(".jpeg")
//                                        || files[i].getName().contains(".gif"))
//                                {
//                                    Log.d(TAG, Integer.toString(i) + ") Thread FileName JPG :" + files[i]);
//                                    // picArrList.add(files[i].toString());
//                                    uploadComplete = false;
//                                }
//                            }
//                        }
//                    }
//
//                    countPic = false;
//                    uploadComplete = true;
//                   // onDownloadComplete(uploadComplete);
//
//                    if (finish){
//                        stopSelf();
//                        return;
//                    }
//                }
//            }
//        };
//        mHandler.postDelayed(runnable, 1000);
    }
//
//    public boolean ftpMakeDirService(String new_dir_path) {
//        try {
//            boolean status = FTPUpload.makeDirectory(new_dir_path);
//            return status;
//        } catch (Exception e) {
//            Log.d(TAG, "Error: could not create new directory named "
//                    + new_dir_path);
//        }
//
//        return false;
//    }
//
//    public boolean ftpUploadService(String srcFilePath, String desFileName,
//                                    String desDirectory) {
//        boolean status = false;
//        try {
//            FileInputStream srcFileStream = new FileInputStream(srcFilePath);
//
//            status = FTPUpload.storeFile(desFileName, srcFileStream);
//
//            File file = new File(srcFilePath);
//            Log.d(TAG, srcFilePath+ "Upload files ===> Success");
//            if (file.delete()) {
//                Log.i(TAG, "Delete file successfully");
//            } else {
//                Log.e(TAG, "Error deleting file");
//            }
//
//            srcFileStream.close();
//
//            return status;
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.d(TAG, "upload failed: " + e);
//        }
//
//        return status;
//    }

    private class MyThread extends Thread{

        private static final String TAG = "MyBoundService";
        private static final int DELAY = 3000;
        //private int num = 0;
        private boolean finish = false;
        public org.apache.commons.net.ftp.FTPClient FTPUpload = null;


        public void run(){

            finishPictures = totalPictures;
            mProgress = finishPictures;
            uploadComplete = false;

            String subpath = null;
            String NamePic = null;
            String DestPathPic = null;
            String DestPath = null;
            String tempPath = "/_visitors/temp_images/";

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            // TODO default = 192.168.1.2
            final String settingIP = prefs.getString("edit_ip", "192.168.2.222");

            Log.d(TAG, "***** [1] Thread Start *** total : " + totalPictures + " IP = " +settingIP);

            long total = 0;

            int num = 0;
            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "BitCamera");
            File[] files = directory.listFiles();

            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {

                    if (files[i].isDirectory()) {
                        // Log.d(TAG, "FileName Dir 2 :" + files[i]);
                        File filePic = new File(String.valueOf(files[i]));
                        File[] filePic2 = filePic.listFiles();

                        // Log.i(TAG, "FileName Dir 1 Size :" + filePic2.length + "---"+files[i]);

                        int fileSize = filePic2.length;

                        for (int run = 0; run < filePic2.length; run++) {

                            //Log.d(TAG, "1.----> Dir: " + num + " File:" + filePic2[run]);

                            subpath = filePic2[run].toString();
                            String[] split1 = subpath.split("BitCamera");
                            String firstSubString = split1[0];
                            String secondSubString = split1[1];

                            String s2 = secondSubString;
                            String[] split2 = s2.split("/");
                            String DirPic = split2[1];
                            NamePic = split2[2];
                            DestPathPic = "_visitors/temp_images/" + DirPic + "/" + NamePic;
                            DestPath = "_visitors/temp_images/" + DirPic;

                            //Log.d(TAG, "*** FTP Service Num :"  + num + " path :" + subpath + " ---> " + DestPathPic);
                            num++;

                            ///////////////////////////////////////

                            // --------ใช้ FTPClient ต้องอยู่ใน try--------
                            try {

                                FTPUpload = new org.apache.commons.net.ftp.FTPClient();
                                //FTPUpload.setConnectTimeout(5000); // timeout 5 s
                                FTPUpload.setConnectTimeout(10000);
                                FTPUpload.setDefaultTimeout(10000);

                                //Log.d(TAG, " ###Start connect to Host");

                                boolean status = false;
                                FTPUpload.connect(settingIP, 21);// port

                                // login using username & password
                                mCheckConnFtp = FTPUpload.login("root", "solokey");

                                if(!mCheckConnFtp){
                                    FTPUpload.logout();
                                    mCheckConnFtp =false;
                                }
                                Log.d(TAG, "***** [2] Connect to host " + settingIP + " = " + mCheckConnFtp);

                                if (!FTPReply.isPositiveCompletion(FTPUpload.getReplyCode())) {
                                    Log.d(TAG, "***** Disconnect to host " + settingIP + " = " + mCheckConnFtp);
                                    FTPUpload.disconnect();
                                }
//                        total = num;
//
//                        int progress = (int) ((double) (total * 100) / (double) totalPictures);
                                //int progress = (int) ((double) (total * 100) / (double) fileSize);

                                if(mCheckConnFtp == true){

                                    //FTPUpload.setFileType(FTP.BINARY_FILE_TYPE);
                                    FTPUpload.enterLocalPassiveMode();

//                    //get system name
//                    Log.d(TAG, "Remote system is " + FTPUpload.getSystemType());
//                    //get current directory
//                    Log.d(TAG, "Current directory is " + FTPUpload.printWorkingDirectory());

                                    ftpMakeDirService(tempPath);
                                    ftpMakeDirService(DestPath);
                                    status = ftpUploadService(subpath, DestPathPic,"/");

                                    Log.d(TAG, " ###FTP files to host " + settingIP + " = " + subpath + " --> "+DestPathPic);
                                    if (status == true) {
                                        mCheckConnFtp = true;
                                        Log.d(TAG, "***** [3] ----> Upload success");
                                        mProgress--;
                                    } else {
                                        mCheckConnFtp = false;
                                        Log.d(TAG, "***** [3] ----> Upload failed");
                                        stopSelf();
                                        return;

                                    }
                                }else{
                                    Log.d(TAG, "***** Error: Cannot login to host " + settingIP);
                                    mCheckConnFtp = false;
                                    stopSelf();
                                    return;
                                }

                                FTPUpload.logout();
                                FTPUpload.disconnect();

                            } catch (Exception e) {
                                Log.d(TAG, "***** Error: could not connect to host " + settingIP + e);
                                mCheckConnFtp = false;

                                stopSelf();
                                return;
                            }// Try Connect FTP

                            ////////////////////////////////////////

                        }// Loop For
                    } else { // If check directory
                        if (files[i].getName().contains(".png") || files[i].getName().contains(".jpg")
                                || files[i].getName().contains(".jpeg")
                                || files[i].getName().contains(".gif")) {
                            Log.d(TAG, Integer.toString(i) + ") Thread FileName JPG :" + files[i]);
                            // picArrList.add(files[i].toString());
                        }
                    }// End if check directory
                }//End for loop
            }// end if files != null

            uploadComplete = true;
            finish = true;


            if (finish){
                Log.d(TAG, "******* Thread Finish *******");
                stopSelf();
                return;
            }

        }// run

        public boolean ftpMakeDirService(String new_dir_path) {
            try {
                boolean status = FTPUpload.makeDirectory(new_dir_path);
                mCheckConnFtp = true;
                return status;
            } catch (Exception e) {
                mCheckConnFtp = false;
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
                FTPUpload.setFileType(FTPClient.BINARY_FILE_TYPE);
                status = FTPUpload.storeFile(desFileName, srcFileStream);

                if(status){
                    File file = new File(srcFilePath);
                    Log.d(TAG, srcFilePath+ "***Upload files ===> Success");
                    if (file.delete()) {
                        totalPictures = mProgress;
                        Log.i(TAG, "Delete file successfully = " + mProgress);

                    } else {
                        Log.e(TAG, "Error deleting file");
                        stopSelf();
                    }
                    srcFileStream.close();
                    return status;
                }else {
                    Log.d(TAG, "***Upload failed ");
                    return status;
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "***Upload failed: " + e);
            }
            return status;
        }

        private void listPathPictures(){

            int num = 0;
            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "BitCamera");
            File[] files = directory.listFiles();

            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {

                    if (files[i].isDirectory()) {
                        // Log.d(TAG, "FileName Dir 2 :" + files[i]);
                        File filePic = new File(String.valueOf(files[i]));
                        File[] filePic2 = filePic.listFiles();

                        // Log.i(TAG, "FileName Dir 1 Size :" + filePic2.length + "---"+files[i]);

                        int fileSize = filePic2.length;

                        for (int run = 0; run < filePic2.length; run++) {
                            num++;
                            //Log.d(TAG, "1.----> Dir: " + num + " File:" + filePic2[run]);

                            String subpath = filePic2[run].toString();
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

                        }// Loop For
                    } else { // If check directory
                        if (files[i].getName().contains(".png") || files[i].getName().contains(".jpg")
                                || files[i].getName().contains(".jpeg")
                                || files[i].getName().contains(".gif")) {
                            Log.d(TAG, Integer.toString(i) + ") Thread FileName JPG :" + files[i]);
                            // picArrList.add(files[i].toString());
                        }
                    }// End if check directory
                }//End for loop
            }// end if files != null

            Log.d(TAG, "******************** List Pictures Size: " + totalPictures);
        }

    }// My Thread

    public int countPictureBalance(){
        int picBalance = 0;
        return picBalance;
    }

    public boolean checkConnFTP(){
        return mCheckConnFtp;
    }

    public boolean checkThread(){
        boolean checkThread = thread.isAlive();
        Log.d(TAG,"------Check Thread : " + checkThread);
        return checkThread;
    }

    private void HeadsUpNotification(){
        Notification.Builder nb = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nb = noti.getNotification2("แจ้งเตือน", "ทำการส่งรูปต่อ ");
            if (nb != null) {
                noti.notify(NOTI_SECONDARY1, nb);
            }
        }else {
            noti.showHeadsUpNotification();
        }
    }

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
    }

    @Override
    public void onDestroy() {
    }
}
