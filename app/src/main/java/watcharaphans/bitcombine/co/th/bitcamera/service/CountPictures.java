package watcharaphans.bitcombine.co.th.bitcamera.service;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class CountPictures extends AsyncTask<String, Void, Integer> {

    private final String TAG = CountPictures.class.getName();

    private String fileSrc;
    private String filePic;
    private String fileDest;
    private String pathDest;
    private CountPicturesCallback callback;

    public CountPictures(CountPicturesCallback callback ) {

        //Start AsyncTask
//        this.pathDest = "_visitors/" + dirDate;
//        this.fileDest = "_visitors/" + dirDate + "/" + filePic;
//        this.fileSrc = filePath + "/"+ filePic;
//        this.filePic = filePic;
        this.callback = callback;

    }

    public interface CountPicturesCallback {
        public void onCountFinish(Integer picnum);
        public void onCountFailed();
    }

    protected Integer doInBackground(String... params)   {

        Log.d(TAG, "Thread Start  ");
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "BitCamera");
        File[] files = directory.listFiles();
        Log.d(TAG, "Thread List Folder = " + files.length);

        boolean success = false;
        int num = 0;

        try {
            //while (true){
                Log.d(TAG, "************* Start Loop **************");

                if (files != null && files.length > 0) {
                    for (int i = 0; i < files.length; i++) {

                        if (files[i].isDirectory()) {

                            File filePic = new File(String.valueOf(files[i]));
                            File[] filePic2 = filePic.listFiles();

                            Log.i(TAG, "FileName Dir 1 Size :" + filePic2.length + "---"+files[i]);
                            for (int run = 0; run < filePic2.length; run++) {
                                num++;
                                Log.d(TAG, "----> " + num + " FileName Dir 2 :" + filePic2[run]);
                            }
                        } else {
                            if (files[i].getName().contains(".png") || files[i].getName().contains(".jpg")
                                    || files[i].getName().contains(".jpeg")
                                    || files[i].getName().contains(".gif"))
                            {
                                Log.d(TAG, Integer.toString(i) + ") Thread FileName JPG :" + files[i]);
                                // picArrList.add(files[i].toString());
                            }
                        }
                    }
                }
        } catch (Exception e) {
            Log.d(TAG, "Error: could not connect to host " + "192.168.2.211");
        }
        Log.d(TAG, "---> total picture =  " + num);

        return num;
        //todo:
    }

    protected void onProgressUpdate(Integer... values) {

    }

    protected void onPostExecute(Integer picnum)  {

        callback.onCountFinish(picnum);
//        if (success) {
//            callback.onCountFinish(picnum);
//        } else {
//            callback.onCountFailed();
//        }
    }
}
