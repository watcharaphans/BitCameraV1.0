package watcharaphans.bitcombine.co.th.bitcamera.fragment;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

import watcharaphans.bitcombine.co.th.bitcamera.R;
import watcharaphans.bitcombine.co.th.bitcamera.service.CountPictures;

public class AboutFragment extends Fragment {

    private static final String TAG = "AboutFragment";
    TextView txtAppVer;
    TextView txtAndroidVer;
    TextView txtDisplaySize;
    TextView txtBrand;
    TextView txtSerial;
    TextView txtSizeMem;
    TextView txtNameWifi;
    TextView txtIPMobile;
    TextView txtSubnet;
    TextView txtGateway;
    TextView txtMacAddress;
    TextView txtResPicture;
    TextView txtQualityPicture;
    TextView txtCountPic;
    int numPic;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_system, container, false);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String version;
        int versionCode;
        String strAndroidVersion = Build.VERSION.RELEASE;
        int strAndroidApi = Build.VERSION.SDK_INT;
        countPictures();

        txtAppVer = (TextView) view.findViewById(R.id.txtAppVer);

        txtAndroidVer = (TextView) view.findViewById(R.id.txtAndroidVer);
        txtAndroidVer.setText("Android Version : " + strAndroidVersion + " (API " + strAndroidApi + ")");

        txtDisplaySize = (TextView) view.findViewById(R.id.txtDisplaySize);
        txtBrand = (TextView) view.findViewById(R.id.txtBrand);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightDS = displayMetrics.heightPixels;
        int widthDS = displayMetrics.widthPixels;
        txtDisplaySize.setText("Display : " + widthDS + " x " + heightDS);
        Log.d(TAG, "Display : " + widthDS + " x " + heightDS);

        txtSerial = (TextView) view.findViewById(R.id.txtSerial);
        txtSizeMem = (TextView) view.findViewById(R.id.txtSizeMem);
        txtNameWifi = (TextView) view.findViewById(R.id.txtNameWifi);
        txtIPMobile = (TextView) view.findViewById(R.id.txtIPMobile);
        txtSubnet = (TextView) view.findViewById(R.id.txtSubnet);
        txtGateway = (TextView) view.findViewById(R.id.txtGateway);
        txtMacAddress = (TextView) view.findViewById(R.id.txtMacAddress);
        txtResPicture = (TextView) view.findViewById(R.id.txtResPicture);
        txtQualityPicture = (TextView) view.findViewById(R.id.txtQualityPicture);
        txtCountPic = (TextView) view.findViewById(R.id.txtCountPic);

        if (strAndroidApi >= Build.VERSION_CODES.O) {

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            Log.d(TAG, "SERIAL: " + Build.getSerial() + "\n" +
                    "MODEL: " + Build.MODEL + "\n" +
                    "ID: " + Build.ID + "\n" +
                    "Manufacture: " + Build.MANUFACTURER + "\n" +
                    "Brand: " + Build.BRAND + "\n" +
                    "Type: " + Build.TYPE + "\n" +
                    "User: " + Build.USER + "\n" +
                    "BASE: " + Build.VERSION_CODES.BASE + "\n" +
                    "INCREMENTAL: " + Build.VERSION.INCREMENTAL + "\n" +
                    "SDK:  " + Build.VERSION.SDK + "\n" +
                    "BOARD: " + Build.BOARD + "\n" +
                    "BRAND: " + Build.BRAND + "\n" +
                    "HOST: " + Build.HOST + "\n" +
                    "FINGERPRINT: "+Build.FINGERPRINT + "\n" +
                    "Version Code: " + Build.VERSION.RELEASE);
            txtBrand.setText("BRAND : " + Build.BRAND);
        }else{

            Log.d(TAG, "SERIAL: " + Build.SERIAL + "\n" +
                    "MODEL: " + Build.MODEL + "\n" +
                    "ID: " + Build.ID + "\n" +
                    "Manufacture: " + Build.MANUFACTURER + "\n" +
                    "Brand: " + Build.BRAND + "\n" +
                    "Type: " + Build.TYPE + "\n" +
                    "User: " + Build.USER + "\n" +
                    "BASE: " + Build.VERSION_CODES.BASE + "\n" +
                    "INCREMENTAL: " + Build.VERSION.INCREMENTAL + "\n" +
                    "SDK:  " + Build.VERSION.SDK + "\n" +
                    "BOARD: " + Build.BOARD + "\n" +
                    "BRAND: " + Build.BRAND + "\n" +
                    "HOST: " + Build.HOST + "\n" +
                    "FINGERPRINT: "+Build.FINGERPRINT + "\n" +
                    "Version Code: " + Build.VERSION.RELEASE);
            txtBrand.setText("BRAND : " + Build.BRAND);
        }

        // Get Version App
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = pInfo.versionName;
            versionCode = pInfo.versionCode;

            txtAppVer.setText("BitCamera Version : "+version );

            Log.d(TAG, "Version Name : "+version + "\n Version Code : "+versionCode);
        }catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "PackageManager Catch : "+e.toString());
        }

        txtSerial.setText("Serial : " +getSerialNumber());
        Log.d(TAG, "SERIAL : " + getSerialNumber());


        ConnectivityManager connManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        NetworkInfo.State mobileInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        if (wifiInfo == NetworkInfo.State.CONNECTED ) {
            Log.d(TAG, "Wifi Connect!!");

            WifiManager wifiMan = (WifiManager) getContext().getApplicationContext().getSystemService(getContext().WIFI_SERVICE);
            WifiInfo wifiInf = wifiMan.getConnectionInfo();
            String macAddr = wifiInf.getMacAddress();
            String ipAddr = Formatter.formatIpAddress(wifiMan.getConnectionInfo().getIpAddress());

            DhcpInfo dhcp = wifiMan.getDhcpInfo();
            String mask = intToIP(dhcp.netmask);

            Log.d(TAG,"SSID :" + wifiInf.getSSID());
            Log.d(TAG, "MAC : " + macAddr);
            Log.d(TAG, "IP : " + ipAddr);

            txtNameWifi.setText("SSID : " + wifiInf.getSSID());
            txtIPMobile.setText("IP Address : " + ipAddr);
            txtSubnet.setText("Subnet : " + mask);
            txtGateway.setText("Gateway : "+ pingGateWayInWifi(getContext()));
            txtMacAddress.setText("Mac : " + getMacAddr());

            //Log.d(TAG, "Wifi info : "+wifiInf + "\n Mac : "+macAddr);

        } else if (wifiInfo == NetworkInfo.State.DISCONNECTED || mobileInfo == NetworkInfo.State.DISCONNECTED) {
            Log.d(TAG, "Disconnect..");
            txtNameWifi.setText("SSID : ไม่ได้เชื่อมต่อ Wifi" );
            txtIPMobile.setText("IP Address : none ");
            txtSubnet.setText("Subnet : none");
            txtGateway.setText("Gateway : none");
            txtMacAddress.setText("Mac : none ");

        }

//        Log.d(TAG, "Memory Info : "+ printMemoryInfo(getActivity()));
//        Log.d(TAG, "Avail Memory : "+ getAvailMemory(getActivity()));

        double totalInternalValue = getTotalInternalMemorySize();
        double freeInternalValue = getAvailableInternalMemorySize();
        double usedInternalValue = totalInternalValue - freeInternalValue;
        int percentInteranl = (int) (( usedInternalValue * 100) / totalInternalValue);
//        Log.d(TAG,"Total Internal : "+ formatSize(totalInternalValue) );
//        Log.d(TAG,"Free Internal : "+ formatSize(freeInternalValue) );
//        Log.d(TAG,"Use Internal : "+ formatSize(usedInternalValue) + " " + percentInteranl + "%");

        txtSizeMem.setText("Storage : " + formatSize(usedInternalValue) + "/"+formatSize(totalInternalValue) + " ("+percentInteranl+"%)");

//        String externalMemoryTitle = "External Memory Information";
//        long totalExternalValue = getTotalExternalMemorySize();
//        long freeExternalValue = getAvailableExternalMemorySize();
//        long usedExternalValue = totalExternalValue - freeExternalValue;
//
//        Log.e(TAG,"Total External : "+ formatSize(totalExternalValue) );
//        Log.e(TAG,"Free External : "+ formatSize(freeExternalValue) );
//        Log.e(TAG,"Use External : "+ formatSize(usedExternalValue));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String picRes = prefs.getString("image_resolution", "640x480");
        String picQua = prefs.getString("image_quality","95");

        txtResPicture.setText("Picture Resolution : "+picRes);
        txtQualityPicture.setText("Picture Quality : "+picQua+"%");
        Log.e(TAG,"Use External : "+ numPic);
        txtCountPic.setText("จำนวนรูปภาพในเครื่อง : " + numPic + " รูป");

    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return 0;
        }
    }

    public static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getDataDirectory();//getExternalStorageDirectory()
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return 0;
        }
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static String formatSize(double size) {
        String suffix = null;
        String strMem = null;
        NumberFormat numberFormt;
        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            numberFormt = NumberFormat.getInstance();
            numberFormt.setMaximumFractionDigits(1);
            strMem = numberFormt.format(size);
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
                numberFormt = NumberFormat.getInstance();
                numberFormt.setMaximumFractionDigits(1);
                strMem = numberFormt.format(size);
                if (size >= 1024) {
                    suffix = "GB";
                    size /= 1024;
                    numberFormt = NumberFormat.getInstance();
                    numberFormt.setMaximumFractionDigits(1);
                    strMem = numberFormt.format(size);
                }
            }
        }
        // การใส่ comma
//        StringBuilder resultBuffer = new StringBuilder(Long.toString((long) size));
////
////        int commaOffset = resultBuffer.length() - 3;
////        while (commaOffset > 0) {
////            resultBuffer.insert(commaOffset, ',');
////            commaOffset -= 3;
////        }
//
//
        StringBuilder resultBuffer = new StringBuilder(strMem);
        if (suffix != null) resultBuffer.append(suffix);
        Log.e(TAG,"resultBuffer : "+ resultBuffer);

        return resultBuffer.toString();
    }

    public String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release +")";
    }

    public static ActivityManager.MemoryInfo getMemoryInfo(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi;
    }

    public static ActivityManager.MemoryInfo printMemoryInfo(Context context) {
        ActivityManager.MemoryInfo mi = getMemoryInfo(context);

        StringBuilder sb = new StringBuilder();
        sb.append("_______  Memory :   ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            sb.append("\ntotalMem        :").append(mi.totalMem);
        }
        sb.append("\navailMem        :").append(mi.availMem);
        sb.append("\nlowMemory       :").append(mi.lowMemory);
        sb.append("\nthreshold       :").append(mi.threshold);
        Log.i(TAG, sb.toString());

        return mi;
    }

    public static String getSerialNumber() {
        String serialNumber;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            serialNumber = (String) get.invoke(c, "gsm.sn1");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ril.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ro.serialno");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "sys.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = Build.SERIAL;

            // If none of the methods above worked
            if (serialNumber.equals(""))
                serialNumber = null;
        } catch (Exception e) {
            e.printStackTrace();
            serialNumber = null;
        }

        return serialNumber;
    }

    public static String pingGateWayInWifi(Context context) {
        String gateWay = null;
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return "wifiManager not found";
        }
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo != null) {
            int tmp = dhcpInfo.gateway;
            gateWay = String.format("%d.%d.%d.%d", (tmp & 0xff), (tmp >> 8 & 0xff),
                    (tmp >> 16 & 0xff), (tmp >> 24 & 0xff));
        }
        return gateWay;
    }

//    public static String GetSubnetMask_WIFI() {
//
//        WifiManager wifiManager = (WifiManager) Settings.Global.getMainActivity()
//                .getSystemService(Context.WIFI_SERVICE);
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//
//        DhcpInfo dhcp = wifiManager.getDhcpInfo();
//        String mask = intToIP(dhcp.netmask);
//
//        return mask;
//    }

    private static String intToIP(int ipAddress) {
        String ret = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));

        return ret;
    }

    private void countPictures(){

        CountPictures countPictures = new CountPictures(
                new CountPictures.CountPicturesCallback() {

                    @Override
                    public void onCountFinish(Integer picnum) {

                        numPic = picnum;
                        Log.d(TAG, "Count Finish : " + picnum);
                        txtCountPic.setText("จำนวนรูปภาพในเครื่อง : " + numPic + " รูป");
                    }

                    @Override
                    public void onCountFailed() {
                        Log.d(TAG, "Count Failed");
                        // ไม่ต้องทำอะไร, รอเก็บตก
                    }
                }
        );
        countPictures.execute();
        Log.d(TAG, "Count End : " + numPic);
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                String strMac = res1.toString().toUpperCase();
                return strMac;
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

}
