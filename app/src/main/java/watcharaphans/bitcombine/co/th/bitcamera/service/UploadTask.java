package watcharaphans.bitcombine.co.th.bitcamera.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;

import watcharaphans.bitcombine.co.th.bitcamera.MainActivity;
import watcharaphans.bitcombine.co.th.bitcamera.fragment.MainFragment;
import watcharaphans.bitcombine.co.th.bitcamera.utility.MyConstant;

public class UploadTask extends AsyncTask<String, Void, Boolean> {

    private String fileSrc;
    private String filePic;
    private String fileDest;
    private String pathDest, pathTemp;
    private UploadTaskCallback callback;
    boolean status;
    private Context ctx;

    MyConstant myConstant = new MyConstant();
    private static final String TAG = "UploadTask";
    public org.apache.commons.net.ftp.FTPClient mFTPClient = null;
    public String settingIP;
    public boolean success;

    public UploadTask(String filePath, String filePic, String dirDate, Context context, UploadTaskCallback callback) {

        //Start AsyncTask
        this.pathDest = "_visitors/temp_images/" + dirDate;
        this.fileDest = "_visitors/temp_images/" + dirDate + "/" + filePic;
        this.fileSrc = filePath + "/"+ filePic;
        this.callback = callback;
        this.filePic = filePic;
        this.ctx = context;
        pathTemp = "_visitors/temp_images/";
        success = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        // TODO default = 192.168.1.2
        settingIP = prefs.getString("edit_ip", "192.168.2.222");

    }

    @Override
    protected Boolean doInBackground(String... params) {
        //todo:
        // คำสั่ง upload ไฟล์

        Log.d(TAG, "Start AsyncTask UploadTasks..." + settingIP);
        try {
            mFTPClient = new org.apache.commons.net.ftp.FTPClient();

            // connecting to the host
            mFTPClient.connect(settingIP, 21);

            // now check the reply code, if positive mean connection success
            if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {

                // login using username & password
                status = mFTPClient.login("root", "solokey");
                mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                mFTPClient.enterLocalPassiveMode();

                Log.d(TAG, "Connect to host "+settingIP+" ===> Success : " + status );
                Log.d(TAG, "Path Picture ===> " + fileSrc + " ***** " + fileDest);
                Log.d(TAG, "Create Path Temp ===> " + pathTemp + " and " + pathDest);
                ftpMakeDirectory(pathTemp);
                ftpMakeDirectory(pathDest);
               // Log.d(TAG, "Path Text ===> " + Environment.getExternalStorageDirectory() + "/TAGFtp/" + TEMP_FILENAME +" **** "+ TEMP_FILENAME );
                status = ftpUpload(fileSrc,fileDest,"/");


                        if (status == true) {
                            success = true;
                            Log.d(TAG, "Upload success");
                        } else {
                            success = false;
                            Log.d(TAG, "Upload failed");
                        }

                mFTPClient.logout();
                mFTPClient.disconnect();

                return status;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error: could not connect to host " + settingIP);
            success = false;
        }

        return success;
    }

    public boolean ftpMakeDirectory(String new_dir_path) {
        try {
            boolean status = mFTPClient.makeDirectory(new_dir_path);
            return status;
        } catch (Exception e) {
            Log.d(TAG, "Error: could not create new directory named "
                    + new_dir_path);
        }

        return false;
    }

    public boolean ftpUpload(String srcFilePath, String desFileName,
                             String desDirectory) {
        boolean status = false;
        try {
            FileInputStream srcFileStream = new FileInputStream(srcFilePath);

            // change working directory to the destination directory
            // if (ftpChangeDirectory(desDirectory)) {
            status = mFTPClient.storeFile(desFileName, srcFileStream);
            // }
            Log.d(TAG, "Upload files ===> Success");
            srcFileStream.close();

            return status;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "upload failed: " + e);
        }

        return status;
    }

    @Override
    protected void onPreExecute() {
     //   Log.i(TAG, "onPreExecute...");
    }

    @Override
    protected void onPostExecute(Boolean success) {
        Log.i(TAG, "onPostExecute...");
        if (success) {
            Toast.makeText(ctx, "ส่งรูปภาพสำเร็จ", Toast.LENGTH_SHORT).show();
            callback.onUploadSuccess();
        } else {
            Toast.makeText(ctx, "เกิดข้อผิดพลาด! ส่งรูปภาพไม่ได้", Toast.LENGTH_SHORT).show();
            callback.onUploadFailed();
        }
    }

    public interface UploadTaskCallback {
        public void onUploadSuccess();
        public void onUploadFailed();

    }

}
