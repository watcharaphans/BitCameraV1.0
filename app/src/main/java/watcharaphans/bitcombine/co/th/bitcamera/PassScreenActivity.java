package watcharaphans.bitcombine.co.th.bitcamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class PassScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private static String PASS_CODE = null;
    private static final String TAG = "bitPassScreenActivity";
    private TextView mPassCodeTextView;
    private TextView mLableTitle;
    private SharedPreferences mPreference;
    private int statusChangePwd;
    private String mKey_password = null;
    private String newPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_screen);

        mPassCodeTextView = findViewById(R.id.pass_code_text_view);
        mLableTitle = findViewById(R.id.label_text_view);

        findViewById(R.id.digit_1_button).setOnClickListener(this);
        findViewById(R.id.digit_2_button).setOnClickListener(this);
        findViewById(R.id.digit_3_button).setOnClickListener(this);
        findViewById(R.id.digit_4_button).setOnClickListener(this);
        findViewById(R.id.digit_5_button).setOnClickListener(this);
        findViewById(R.id.digit_6_button).setOnClickListener(this);
        findViewById(R.id.digit_7_button).setOnClickListener(this);
        findViewById(R.id.digit_8_button).setOnClickListener(this);
        findViewById(R.id.digit_9_button).setOnClickListener(this);
        findViewById(R.id.digit_0_button).setOnClickListener(this);

        Button backSpaceButton = findViewById(R.id.back_space_button);
        backSpaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound("button");
                String currentInput = mPassCodeTextView.getText().toString();
                if (currentInput.length() > 0) {
                    // ลบตัวขวาสุดทิ้ง
                    String newInput = currentInput.substring(0, currentInput.length() - 1);
                    mPassCodeTextView.setText(newInput);
                }
            }
        });
        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                playSound("button");
                finish();
            }
        });

        mPreference = PreferenceManager.getDefaultSharedPreferences(this);
        PASS_CODE = mPreference.getString("password","3412");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mKey_password = bundle.getString("key_password");
            //Toast.makeText(this, "Name : " + pathPicture, Toast.LENGTH_SHORT).show();

            switch (mKey_password) {
                case "normal":
                    mLableTitle.setText("กรุณาป้อนรหัสผ่าน");
                    statusChangePwd = 1;
                    break;
                case "change_password":
                    mLableTitle.setText("กรุณาป้อนรหัสผ่านใหม่");
                    statusChangePwd = 2;
                    break;
                case "retry_password":
                    mLableTitle.setText("ยืนยันรหัสผ่านใหม่");
                    statusChangePwd = 3;
                    break;
            }
            Log.i(TAG, "key intent --> " + mKey_password);

        }else{
            Log.i(TAG, "key intent --> none");
        }
    }

    // logic ที่ใช้ตรวจสอบว่ารหัสผ่านถูกต้องหรือไม่
    private boolean isPassCodeValid(String newInput) {
        Log.i(TAG, "isPassCodeValid " + PASS_CODE +"--> "+newInput);
        return PASS_CODE.equals(newInput);
    }

    private void putPreference(String pwd){
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putString("pref_pwd",pwd);

        Toast.makeText(
                PassScreenActivity.this,
                "บันทึกรหัสผ่านใหม่",
                Toast.LENGTH_SHORT
        ).show();

    }

    @Override
    public void onClick(View v) {
        playSound("button");
        String currentInput = mPassCodeTextView.getText().toString();
        if (currentInput.length() < 4) {
            // ใช้ตัวเลขที่เป็น text บนปุ่ม
            String digit = ((Button) v).getText().toString();
            String newInput = currentInput + digit;
            mPassCodeTextView.setText(newInput);

            if (newInput.length() == 4) {

                Log.d(TAG, "---> "+ mKey_password + "..........." + statusChangePwd);

                switch (statusChangePwd) {
                    case 1:
                        Log.d(TAG, "---> "+ mKey_password + "...........normal");
                        if (isPassCodeValid(newInput)) {

                            //Dialog แจ้งรหัสผ่านถูกต้อง
//                    new AlertDialog.Builder(PassScreenActivity.this)
//                            .setMessage("รหัสผ่านถูกต้อง")
//                            .setPositiveButton("OK", null)
//                            .show();
                            Log.d(TAG, "---> " + mKey_password + "...........success");

                            Toast.makeText(
                                    PassScreenActivity.this,
                                    "รหัสผ่านถูกต้อง",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent = new Intent(PassScreenActivity.this, SettingsActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(
                                    PassScreenActivity.this,
                                    newInput + " ยังไม่ถูก กรุณาลองใหม่",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                        break;
                    case 2:

                        mLableTitle.setText("ยืนยันรหัสผ่านใหม่");
                        mKey_password = "retry_password";
                        statusChangePwd = 3;
                        newPassword = null;
                        newPassword = newInput;
                        mPassCodeTextView.setText("");
                        Log.d(TAG, "---> "+ mKey_password + "...........change password "+newPassword);

                        break;
                    case 3:
                        Log.d(TAG, "---> "+ mKey_password + "...........check newpassword " +newPassword + " # input : "+newInput);

                        if(newPassword.equals(newInput)) {
                            Log.d(TAG, "---> "+ mKey_password + "...........Success");
                            SharedPreferences.Editor editor = mPreference.edit();
                            editor.putString("password", newPassword);
                            editor.apply();
                            mPassCodeTextView.setText("");
                            Toast.makeText(
                                    PassScreenActivity.this,
                                    "บันทึกรหัสผ่านใหม่",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();

                        }else {
                            Log.d(TAG, "---> "+ mKey_password + "...........retry password");
                            Toast.makeText(
                                    PassScreenActivity.this,
                                    newInput + " ยังไม่ถูก กรุณาลองใหม่",
                                    Toast.LENGTH_SHORT
                            ).show();
                            mPassCodeTextView.setText("");
                        }
                        break;
                }

            }
        }
    }

    private void playSound(String str){

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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Log.d(TAG, "...........onBackPressed");
//        Toast.makeText(
//                PassScreenActivity.this,
//                "กลับหน้าแรก",
//                Toast.LENGTH_SHORT
//        ).show();
        finish();
    }
}
