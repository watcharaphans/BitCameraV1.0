package watcharaphans.bitcombine.co.th.bitcamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class StartActivityOnBootReceiver extends BroadcastReceiver {

    private Boolean StatusWifi;
    private static final String TAG = StartActivityOnBootReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo.State wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
                NetworkInfo.State mobileInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
                if (wifiInfo == NetworkInfo.State.CONNECTED ) {
                    Log.e(TAG, "Wifi Connect!!");
                    StatusWifi = true;
                } else if (wifiInfo == NetworkInfo.State.DISCONNECTED || mobileInfo == NetworkInfo.State.DISCONNECTED) {
                    Log.e(TAG, "Disconnect..");
                    StatusWifi = false;
                }
            }
        }else{

            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            boolean isConnected = wifi != null && wifi.isConnectedOrConnecting();

            if (isConnected) {
                Log.d(TAG, "Wifi Connect!! YES");
                StatusWifi = true;
            } else {
                Log.d(TAG, "Disconnect.. NO");
                StatusWifi = false;
            }

//            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
//            Log.v(TAG, "Android < O");
//            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())
//                    && WifiManager.WIFI_STATE_ENABLED == wifiState) {
//                Log.v(TAG, "Wifi is now ON..");
//                Toast.makeText(context,"Wifi is ON..",Toast.LENGTH_LONG).show();
//                StatusWifi = true;
//
//            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())
//                    && WifiManager.WIFI_STATE_DISABLED == wifiState){
//                Log.v(TAG, "Wifi is now OFF");
//                Toast.makeText(context,"Wifi is OFF",Toast.LENGTH_LONG).show();
//                StatusWifi =false;
//            }
        }

        Log.e(TAG, "====> Status Wifi = " + StatusWifi);
        //Log.e(TAG, "====> Intent.getExtra = " + intent.getExtras());

        if (intent.getExtras() != null && StatusWifi != null) {
            Intent in = new Intent("watcharaphans.bitcombine.co.th.bitcamera");
            Bundle extras = new Bundle();
            extras.putBoolean("Key_StatusWifi",StatusWifi );
            in.putExtras(extras);
            context.sendBroadcast(in);
            Log.e(TAG, "====> sendBroadcast : " + StatusWifi);
        }

    }

}
