package watcharaphans.bitcombine.co.th.bitcamera.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import watcharaphans.bitcombine.co.th.bitcamera.MainActivity;
import watcharaphans.bitcombine.co.th.bitcamera.R;
import watcharaphans.bitcombine.co.th.bitcamera.model.QrCodeData;
import watcharaphans.bitcombine.co.th.bitcamera.service.UploadTask;
import watcharaphans.bitcombine.co.th.bitcamera.utility.MyConstant;

import static android.app.Activity.RESULT_OK;
import static watcharaphans.bitcombine.co.th.bitcamera.MainActivity.FILENAME_A;
import static watcharaphans.bitcombine.co.th.bitcamera.MainActivity.FILENAME_B;
import static watcharaphans.bitcombine.co.th.bitcamera.MainActivity.FILENAME_C;
import static watcharaphans.bitcombine.co.th.bitcamera.MainActivity.FILENAME_D;

public class MainFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = MainFragment.class.getName();

    private String resultQRString;
    private ImageView cameraAImageView, cameraBImageView, cameraCImageView, cameraDImageView;
    private ImageView cameraAImageNotAllow, cameraBImageNotAllow, cameraCImageNotAllow, cameraDImageNotAllow;
    private TextView txtNameCamA, txtNameCamB, txtNameCamC, txtNameCamD;
    private Uri cameraCUri, cameraDUri;
    private File[] cameraFile = new File[2];
    private File resizeCameraCFile;
    private boolean cameraCABoolean = false, cameraDABoolean = false;

    private String qrCode;
    private String qrDate;
    private QrCodeData qrCodeData;
    private String pathSavePic;
    private Boolean cameraAActive, cameraBActive, cameraCActive, cameraDActive;

    private MainActivity activity;

    //Uri =  path  mี่เก็บค่าต่างๆ
    public static MainFragment takePhotoInstance(String qrCode) {
        MainFragment mainFragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Result", qrCode);
        mainFragment.setArguments(bundle);
        return mainFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.qrCode = bundle.getString("Result");
            this.qrCodeData = new QrCodeData(this.qrCode, getContext());

        }
        monitorDirectoryChange();
    }

    private FileObserver observer;

    private void monitorDirectoryChange() {

        // check รูปในโฟลเดอร์
        final String pathToWatch = getPublicStorageDir().getAbsolutePath();
        observer = new FileObserver(pathToWatch) {
            @Override
            public void onEvent(int event, final String file) {
                //if(event == FileObserver.CREATE && !file.equals(".probe")){ // check if its a "create" and not equal to .probe because thats created every time camera is launched
                //}
                if (event == FileObserver.CREATE) {

                    String msg = "File created [" + pathToWatch + "/ *** /" + file + "]";
                    Log.d(TAG, msg);

                    final String filePath = pathToWatch + "/" + file;
                    final String fileDir = pathToWatch;
                    final String filePic = file;
                    final Context context = getActivity();

                    Log.i(TAG, "Before UploadTask execute.");
                    UploadTask uploadTask = new UploadTask(
                            fileDir, filePic, qrDate, context,
                            new UploadTask.UploadTaskCallback() {
                                @Override
                                public void onUploadSuccess() {

                                    //Toast.makeText(getApplicationContext(), "ส่งรูปภาพสำเร็จ..", Toast.LENGTH_SHORT).show();
                                    // ลบไฟล์ทิ้งไป
                                    //sendToast("Send Pictures Sucess");
                                    Log.i(TAG, "Deleting Files....");
                                    File file = new File(filePath);
                                    if (file.delete()) {
                                        Log.i(TAG, "Delete file successfully");
                                    } else {
                                        Log.e(TAG, "Error deleting file");
                                    }
                                }

                                @Override
                                public void onUploadFailed() {
                                    // ไม่ต้องทำอะไร, รอเก็บตก
                                    Log.d(TAG, "************* Upload pictures Failed!!! ************** ");
                                 }
                            }
                    );
                    uploadTask.execute();
                }else if(event == FileObserver.MODIFY){
                    //Log.d(TAG, "FileObserve Event MODIFY..." + pathToWatch);
                }else if(event == FileObserver.DELETE){
                    //Log.d(TAG, "FileObserve Event DELETE..." + pathToWatch);
                }else if(event == FileObserver.MOVED_TO){
                    //Log.d(TAG, "FileObserve Event MOVED_TO..." + pathToWatch);
                }else if(event == FileObserver.MOVED_FROM){
                    //Log.d(TAG, "FileObserve Event MOVED_FROM..." + pathToWatch);
                }else{
                    //Log.d(TAG, "FileObserve Not Event Create..." + pathToWatch);
                }
            }
        };
        observer.startWatching(); //START OBSERVING
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).setToolbarVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_take_photo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView = (TextView) view.findViewById(R.id.txtResult);
        String styledText = "<B><font color='blue'>" + this.qrCodeData.id + "</font></B>" + "<br>" + this.qrCodeData.licenseCar
                + "<br>" + this.qrCodeData.remark;
        textView.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);

        qrDate = this.qrCodeData.dateString + this.qrCodeData.machineNumber;
        Log.i(TAG, "DATA From QRCode Date --> " + qrDate);

        String statusWork = "";
        String colorMode = "";
        switch(this.qrCodeData.WorkingMode) {
            case "mode-in" :
                statusWork = "IN"; //#008000
                colorMode = "#008000";
                break;
            case "mode-out" :
                statusWork = "OUT"; //#FF0000
                colorMode = "#FF0000";
                break;
        }

        String nameMonth = this.qrCodeData.dateString.substring(2, 4);
        int numberMonth = Integer.parseInt(nameMonth);
        String arr[] = {"ม.ค.", "ก.พ.", "มี.ค." ,"เม.ย.", "พ.ค." , "มิ.ย.", "ก.ค.", "ส.ค.",
        "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."};
        int numberMonth2 = numberMonth - 1;
        String NameOfMonth = arr[numberMonth2];

        String DateIn = this.qrCodeData.dateString.substring(4, 6) + "/"
                + nameMonth + "/20" + this.qrCodeData.dateString.substring(0, 2);

        String TimeIn = this.qrCodeData.timeString.substring(0, 2) + ":"
                + this.qrCodeData.timeString.substring(2, 4);
        String DateTimeIn = "<font color='" + colorMode + "'>"+statusWork+ "<br>" +DateIn + " " + TimeIn +"</font>"; //<font color='#000000'>

        TextView textView2 = (TextView) view.findViewById(R.id.txtResult2);
        textView2.setText(Html.fromHtml(DateTimeIn), TextView.BufferType.SPANNABLE); //DateTimeIn

        txtNameCamA = (TextView) view.findViewById(R.id.txtCameraA);
        txtNameCamA.setText("กดถ่าย\nกล้อง A");
        txtNameCamB = (TextView) view.findViewById(R.id.txtCameraB);
        txtNameCamB.setText("กดถ่าย\nกล้อง B");
        txtNameCamC = (TextView) view.findViewById(R.id.txtCameraC);
        txtNameCamC.setText("กดถ่าย\nกล้อง C");
        txtNameCamD = (TextView) view.findViewById(R.id.txtCameraD);
        txtNameCamD.setText("กดถ่าย\nกล้อง D");

        cameraAImageView = (ImageView) view.findViewById(R.id.imvCameraA);
        cameraAImageView.setTag(false);

        cameraBImageView = (ImageView) view.findViewById(R.id.imvCameraB);
        cameraBImageView.setTag(false);

        cameraCImageView = (ImageView) view.findViewById(R.id.imvCameraC);
        cameraCImageView.setTag(false);

        cameraDImageView = (ImageView) view.findViewById(R.id.imvCameraD);
        cameraDImageView.setTag(false);

        Button cancelButton = (Button) view.findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(this);

        Button saveButton = (Button) view.findViewById(R.id.btnSave);
        saveButton.setOnClickListener(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        cameraAActive = prefs.getBoolean("cameraA", false);
        cameraBActive = prefs.getBoolean("cameraB", false);
        cameraCActive = prefs.getBoolean("cameraC", false);
        cameraDActive = prefs.getBoolean("cameraD", false);

//        Log.i(TAG, "GET CONFIG CameraAActive --> " + cameraAActive);
//        Log.i(TAG, "GET CONFIG CameraBActive --> " + cameraBActive);
//        Log.i(TAG, "GET CONFIG CameraCActive --> " + cameraCActive);
//        Log.i(TAG, "GET CONFIG CameraDActive --> " + cameraDActive);

        cameraAImageNotAllow = (ImageView) view.findViewById(R.id.imvCameraAnotAllow);
        cameraBImageNotAllow = (ImageView) view.findViewById(R.id.imvCameraBnotAllow);
        cameraCImageNotAllow = (ImageView) view.findViewById(R.id.imvCameraCnotAllow);
        cameraDImageNotAllow = (ImageView) view.findViewById(R.id.imvCameraDnotAllow);

        if(cameraAActive){
            cameraAImageNotAllow.setVisibility(view.INVISIBLE);
            cameraAImageView.setOnClickListener(this);
        }else{
            cameraAImageNotAllow.setVisibility(view.VISIBLE);
        }

        if(cameraBActive){
            cameraBImageNotAllow.setVisibility(view.INVISIBLE);
            cameraBImageView.setOnClickListener(this);
        }else{
            cameraBImageNotAllow.setVisibility(view.VISIBLE);
        }

        if(cameraCActive){
            cameraCImageNotAllow.setVisibility(view.INVISIBLE);
            cameraCImageView.setOnClickListener(this);
        }else{
            cameraCImageNotAllow.setVisibility(view.VISIBLE);
        }

        if(cameraDActive){
            cameraDImageNotAllow.setVisibility(view.INVISIBLE);
            cameraDImageView.setOnClickListener(this);
        }else{
            cameraDImageNotAllow.setVisibility(view.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Create Directory
        //createFiles();
        activity.setWorkingModeVisibility(false);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        Intent intent;
        switch (viewId) {
            case R.id.imvCameraA:
                mListener.onClickCameraImageA();
                break;
            case R.id.imvCameraB:
                mListener.onClickCameraImageB();
                break;
            case R.id.imvCameraC:
                mListener.onClickCameraImageC();
                break;
            case R.id.imvCameraD:
                mListener.onClickCameraImageD();
                break;
            case R.id.btnCancel:
                getActivity()
                        .getSupportFragmentManager()
                        .popBackStack();
                break;
            case R.id.btnSave:
                playSound("save");
                //TODO check config ก่อนว่า มีเปิดกล้องอะไรบ้าง
                boolean imageAReady = (Boolean) cameraAImageView.getTag();
                boolean imageBReady = (Boolean) cameraBImageView.getTag();
                boolean imageCReady = (Boolean) cameraCImageView.getTag();
                boolean imageDReady = (Boolean) cameraDImageView.getTag();

                if(imageAReady){

                    File dir = getPublicStorageDir();
                    File srcFileA = new File(getContext().getFilesDir(), FILENAME_A);
                    File dstFileA = new File(dir, qrCodeData.fileNameA);
                    Log.i(TAG, "SRC A --> " + srcFileA.getAbsolutePath());
                    Log.i(TAG, "DST A --> " + dstFileA.getAbsolutePath());
                    //boolean moveFileCResult = srcFileA.renameTo(dstFileC);
                    try {
                        fileCopy(srcFileA, dstFileA);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                if(imageBReady){

                    File dir = getPublicStorageDir();
                    File srcFileB = new File(getContext().getFilesDir(), FILENAME_B);
                    File dstFileB = new File(dir, qrCodeData.fileNameB);
                    Log.i(TAG, "SRC B --> " + srcFileB.getAbsolutePath());
                    Log.i(TAG, "DST B --> " + dstFileB.getAbsolutePath());
                    //boolean moveFileCResult = srcFileA.renameTo(dstFileC);
                    try {
                        fileCopy(srcFileB, dstFileB);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                if(imageCReady){

                    File dir = getPublicStorageDir();

                    File srcFileC = new File(getContext().getFilesDir(), FILENAME_C);
                    File dstFileC = new File(dir, qrCodeData.fileNameC);
                    Log.i(TAG, "SRC C --> " + srcFileC.getAbsolutePath());
                    Log.i(TAG, "DST C --> " + dstFileC.getAbsolutePath());
                    //boolean moveFileCResult = srcFileC.renameTo(dstFileC);
                    try {
                        fileCopy(srcFileC, dstFileC);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(imageDReady){

                    File dir = getPublicStorageDir();

                    File srcFileD = new File(getContext().getFilesDir(), FILENAME_D);
                    File dstFileD = new File(dir, qrCodeData.fileNameD);
                    Log.i(TAG, "SRC D --> " + srcFileD.getAbsolutePath());
                    Log.i(TAG, "DST D --> " + dstFileD.getAbsolutePath());
                    //boolean moveFileDResult = srcFileD.renameTo(dstFileD);
                    try {
                        fileCopy(srcFileD, dstFileD);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

//                if (imageAReady || imageBReady || imageCReady || imageDReady) {
//                    //uploadPhotoToServer();
//                    File dir = getPublicStorageDir();
//
//                    File srcFileC = new File(getContext().getFilesDir(), FILENAME_C);
//                    File dstFileC = new File(dir, qrCodeData.fileNameC);
//                    Log.i(TAG, "SRC C --> " + srcFileC.getAbsolutePath());
//                    Log.i(TAG, "DST C --> " + dstFileC.getAbsolutePath());
//                    //boolean moveFileCResult = srcFileC.renameTo(dstFileC);
//                    try {
//                        fileCopy(srcFileC, dstFileC);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    File srcFileD = new File(getContext().getFilesDir(), FILENAME_D);
//                    File dstFileD = new File(dir, qrCodeData.fileNameD);
//                    Log.i(TAG, "SRC D --> " + srcFileD.getAbsolutePath());
//                    Log.i(TAG, "DST D --> " + dstFileD.getAbsolutePath());
//                    //boolean moveFileDResult = srcFileD.renameTo(dstFileD);
//                    try {
//                        fileCopy(srcFileD, dstFileD);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                } else {
//                    Toast.makeText(getActivity(), "กรุณาถ่ายรูปให้ครบก่อนบันทึก", Toast.LENGTH_SHORT).show();
//                }
                getActivity()
                        .getSupportFragmentManager()
                        .popBackStack();

                break;
        }
    }

    private void playSound(String str){

        MediaPlayer mp;
        switch (str) {
            case "shutter":
                mp = MediaPlayer.create(getActivity(), R.raw._shutter);
                mp.start();
                break;
            case "button":
                mp = MediaPlayer.create(getActivity(), R.raw._button);
                mp.start();
                break;
            case "save":
                mp = MediaPlayer.create(getActivity(), R.raw._save_done);
                mp.start();
                break;

        }

    }

    public void fileCopy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    public File getPublicStorageDir() {
        // Get the directory for the user's public pictures directory.
        //Log.i(TAG, "DIR --> " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());

//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
//        final String resValue = prefs.getString("image_resolution", "640x480");

        //TODO check mound micro sd card and set Path save pictures
        //Environment.getExternalStorageDirectory()
//        String state = Environment.getExternalStorageState();
//        Log.d(TAG,"Check Mound Sd card : "+ state);
//
//        if (!(state.equals(Environment.MEDIA_MOUNTED))) {
//            Log.d(TAG,"There is no any sd card");
//
//        } else {
//
//            File fileSd = Environment.getExternalStorageDirectory();
//            Log.d(TAG,"Sd card available " + fileSd);
//        }

        File appDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "BitCamera");
       // File appDir = new File(Environment.getExternalStorageDirectory());
        File dir = new File(appDir, qrCodeData.dirName);
        if (dir.mkdirs()) {
            Log.i(TAG, "Directory " + dir.getAbsolutePath() + " created successfully");
        } else {
            Log.e(TAG, "Directory already exists or error creating directory: " + dir.getAbsolutePath());
        }
        return dir;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }  //  onActivity Result

    public static void ResizeImages(String sPath, String sTo) throws IOException {
        Bitmap photo = BitmapFactory.decodeFile(sPath);
        photo = Bitmap.createScaledBitmap(photo, 300, 300, false);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File f = new File(sTo);
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();

        File file = new File(sPath);
        file.delete();
    }

    public void setImageViewA(Bitmap bitmap) {
        Log.d(TAG,"***Set Image on view A --> "+ bitmap);

        cameraAImageView.setImageBitmap(bitmap);
        cameraAImageView.setTag(true);
        txtNameCamA.setText("");
    }

    public void setImageViewB(Bitmap bitmap) {
        Log.d(TAG,"***Set Image on view B --> "+ bitmap);

        cameraBImageView.setImageBitmap(bitmap);
        cameraBImageView.setTag(true);
        txtNameCamB.setText("");
    }

    public void setImageViewC(Bitmap bitmap) {
        Log.d(TAG,"***Set Image on view C --> "+ bitmap);

        cameraCImageView.setImageBitmap(bitmap);
        cameraCImageView.setTag(true);
        txtNameCamC.setText("");
    }

    public void setImageViewD(Bitmap bitmap) {
        Log.d(TAG,"***Set Image on view D --> "+bitmap);
        cameraDImageView.setImageBitmap(bitmap);
        cameraDImageView.setTag(true);
        txtNameCamD.setText("");
    }


    private MainFragmentListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (MainFragmentListener) context;

        // Fragment ผูกเข้ากับ Activity
        activity = (MainActivity) getActivity();
  //      activity.setWorkingModeVisibility(false); // ซ่อนแทบ mode IN/OUT
    }

    public interface MainFragmentListener {
        public void onClickCameraImageA();
        public void onClickCameraImageB();
        public void onClickCameraImageC();
        public void onClickCameraImageD();
    }

}
