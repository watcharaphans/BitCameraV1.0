package watcharaphans.bitcombine.co.th.bitcamera.fragment;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import watcharaphans.bitcombine.co.th.bitcamera.MainActivity;
import watcharaphans.bitcombine.co.th.bitcamera.R;

public class ScanQrCodeFragment extends Fragment implements ZXingScannerView.ResultHandler{

    private String TAG = ScanQrCodeFragment.class.getName();
    private ZXingScannerView zXingScannerView;
    private String resultString;
    private MainActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
        activity.setWorkingModeVisibility(true);
        Log.d(TAG, "onAttach...");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).setToolbarVisibility(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        zXingScannerView = new ZXingScannerView(getActivity());
        return zXingScannerView;
    }

    // todo: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx ทดสอบ hardcode
//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        //todo: ส่วนนี้คือการ bypass การสแกน QR, อย่าลืมลบส่วนนี้ทิ้ง
//        zXingScannerView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getActivity()
//                        .getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.contentFragmentMain,
//                                MainFragment.takePhotoInstance("|nnfzz!6!<khkhzzzzKJOLYzzmfmnlnznfkgleznm"),
//                                "main_fragment")
//                        .addToBackStack(null)
//                        .commit();
//            }
//        });
//    }
    // todo: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

    @Override
    public void onResume() {
        super.onResume();
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();
        Log.d(TAG, "Start Camera QR Code");
        activity.setWorkingModeVisibility(true);
        activity.getResultFormFragment("countpic");

        //TODO พบบัคกรณีพักหน้าจอแล้วกลับมาเข้า app ใหม่
        //activity.setWorkingModeVisibility(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        zXingScannerView.stopCamera();
        Log.d(TAG, "onPause...");
    }

    @Override
    public void handleResult(Result result) {
        resultString = result.getText().trim();
        Log.d(TAG, "Result 200ms ---> " + resultString);
        if (!resultString.isEmpty()) {

            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFragmentMain, MainFragment.takePhotoInstance(resultString), "main_fragment")
                    .addToBackStack(null)
                    .commit();
        }

        /*Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                zXingScannerView.resumeCameraPreview(ScanQrCodeFragment.this);
            }
        }, 2000);*/

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == Activity.RESULT_OK) {
            if(data != null) {
                String value = data.getStringExtra("update_pic");
                if(value != null) {
                    Log.v(TAG, "Data passed from Child fragment = " + value);
                }
            }
        }
    }
} //Main Class
