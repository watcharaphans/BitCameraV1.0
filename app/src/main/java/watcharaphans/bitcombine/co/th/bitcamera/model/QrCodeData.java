package watcharaphans.bitcombine.co.th.bitcamera.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class QrCodeData {

    private static final String TAG = QrCodeData.class.getName();

    public String id;
    public String licenseCar;
    public String remark;
    public String machineNumber;
    public String dirName;
    public String fileNameA;
    public String fileNameB;
    public String fileNameC;
    public String fileNameD;

    public String dateString;
    public String timeString;
    public String WorkingMode;

    protected Context context;

//    private Context context;
//
////save the context recievied via constructor in a local variable
//
//    public QrCodeData(Context context){
//        this.context=context;
//    }

    public QrCodeData(String qrCode, Context context) {
        //        รับค่า String ที่ Decode แล้ว
        this.context = context.getApplicationContext();

        String QRcode_Convert = "";

        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean cameraAActive = prefs.getBoolean("cameraA", false);
        final boolean cameraBActive = prefs.getBoolean("cameraB", false);
        final boolean cameraCActive = prefs.getBoolean("cameraC", false);
        final boolean cameraDActive = prefs.getBoolean("cameraD", false);

        final String workingMode = prefs.getString("working_mode", "mode-in");

        Log.d(TAG, "Setting cameraBActive ===> " + cameraBActive);
        Log.d(TAG, "Setting cameraCActive ===> " + cameraCActive);
        Log.d(TAG, "Setting cameraDActive ===> " + cameraDActive);
        Log.d(TAG, "Setting Working Mode ===> " + workingMode);

//        เช็คข้อมูลตัวแรกเท่ากับเครื่องหมาย | หรือไม่
        if (qrCode.charAt(0) == '|') {
            int check_FontThai = 0;
//            วน loop เพื่อทำการ Decode Qrcode
            for (int i = 0; i < qrCode.length(); ++i) {
                if (qrCode.charAt(i) == '!') {
                    i++;
                    char CharDecimal = qrCode.charAt(i);
                    int ValueASCII = (int) CharDecimal;

                    char char_decode = (char) (ValueASCII + 3536);
                    if (ValueASCII == '}') {
                        QRcode_Convert += "ะ";
                    } else {
                        QRcode_Convert += char_decode;
                    }
                } else {
                    //กรณี QRcode ที่เข้ามาเป็นเครื่องหมาย | ให้คืนค่าว่างกลับไป
                    if (qrCode.charAt(i) == '|') {
                        QRcode_Convert += "";
                    } else {
                        char CharDecimal = qrCode.charAt(i);
                        int ValueASCII = (int) CharDecimal;
                        //เช็คว่า เป็นตัวอักษรไทยหรือไม่
                        if (ValueASCII <= 0)
                            check_FontThai = 1;

                        if (check_FontThai == 1) {
                            QRcode_Convert += qrCode.charAt(i);
                        } else {
                            char char_decode = (char) (158 - ValueASCII);
                            if (ValueASCII == '>') {
                                //char_decode
                                QRcode_Convert += ">";
                            } else if (ValueASCII == ' ') {
                                QRcode_Convert += " ";
                            } else {
                                QRcode_Convert += char_decode;
                            }
                        }
                    }
                }
            }
        }
    // รองรับ QRCode ที่ยาวเกิน 10 ตัวอักษร เปลี่ยน == 10 เป็น >= 10
        if (QRcode_Convert.split("\\$", -1).length - 1 >= 10) {
            String[] data = QRcode_Convert.split("\\$");

            this.id = data[0];
            this.licenseCar = data[2];
            this.machineNumber = data[10];
            this.remark = data[6];
            this.dirName = data[8] + data[10];

            switch(workingMode) {
                case "mode-in" :
                    this.fileNameA = data[9] + data[10] + "A.jpg";
                    this.fileNameB = data[9] + data[10] + "B.jpg";
                    this.fileNameC = data[9] + data[10] + "C.jpg";
                    this.fileNameD = data[9] + data[10] + "D.jpg";
                    this.WorkingMode = "mode-in";
                    break;
                case "mode-out" :
                    this.fileNameA = data[9] + data[10] + "A_OUT.jpg";
                    this.fileNameB = data[9] + data[10] + "B_OUT.jpg";
                    this.fileNameC = data[9] + data[10] + "C_OUT.jpg";
                    this.fileNameD = data[9] + data[10] + "D_OUT.jpg";
                    this.WorkingMode = "mode-out";
                    break;
            }

            this.dateString = data[8];
            this.timeString = data[9];

            Log.d(TAG, "Date-Time ===> " + this.dateString + "-" + this.timeString );
            Log.d(TAG, "id ===> " + this.id);
            Log.d(TAG, "license car ===> " + this.licenseCar);
            Log.d(TAG, "machine number ===> " + this.machineNumber);
            Log.d(TAG, "remark ===> " + this.remark);
            Log.d(TAG, "directory name ===> " + this.dirName);
            Log.d(TAG, "file name [A] ===> " + this.fileNameA);
            Log.d(TAG, "file name [B] ===> " + this.fileNameB);
            Log.d(TAG, "file name [C] ===> " + this.fileNameC);
            Log.d(TAG, "file name [D] ===> " + this.fileNameD);
        }
    }
}
