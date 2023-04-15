package com.kdu.bubble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.kdu.bubble.bus.UserData;
import com.kdu.bubble.bus.getoff.SetDestination;
import com.kdu.bubble.bus.ride.SetRideBus;
import com.kdu.bubble.voice.Command;

public class MainActivity extends AppCompatActivity {

    private Activity mainActivity = this;
    private Command command;
    public static Vibrator vibrator;
    private Button sttButton;
    private static Context context;
    private String key = "DehyBWi1YgSSR2wKJMIKUPjlAv09zlwMMw8%2BOZk0UC%2F52Tn7z2fSb3G%2BMA3T9c0UN0Xam2pXAbgVEHvOn9h63g%3D%3D";
    public static UserData userData = new UserData();
    public static SetRideBus rideBus;
    public static SetDestination setDestination;
    public static TextView startStTextView;
    public static TextView startStIdTextView;
    public static TextView busTextView;
    public static TextView destStTextView;
    public static TextView destStIdTextView;
    public static Animation anim;

    private final String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
    };

    private final ActivityResultLauncher<String[]> requestPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            (results) -> {
                for (Boolean granted : results.values()) {
                    if (!granted) {
                        Toast.makeText(this, "권한이 없으면 앱을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                }

                speechCurrentStation();
            });


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);
        command = new Command(mainActivity, key);
        rideBus = new SetRideBus(this, key);
        setDestination = new SetDestination(this, key);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        startStTextView = findViewById(R.id.depStTextView);
        startStIdTextView = findViewById(R.id.depStIdTextView);
        busTextView = findViewById(R.id.rideBusTextView);
        destStTextView = findViewById(R.id.arrStTextView);
        destStIdTextView = findViewById(R.id.arrStIdTextView);

        // 애니메이션 설정
        anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(100);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(20);

        // STT버튼 OnTouch 이벤트 (색상변경)
        sttButton = findViewById(R.id.STTButton);
        sttButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                command.getCommand();
            }
        });

        sttButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        sttButton.setBackgroundResource(R.drawable.sttbutton_clicked);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        sttButton.setBackgroundResource(R.drawable.sttbutton);
                        break;
                    }
                }
                return false;
            }
        });

        if (!checkPermissions()) {
            requestPermissions.launch(permissions);
        } else {
            speechCurrentStation();
        }
    }

    public static void clearViews() {
        startStTextView.setText("");
        startStIdTextView.setText("");
        busTextView.setText("");
        busTextView.setBackgroundColor(Color.parseColor("#EFF7FF"));
        destStIdTextView.setText("");
        destStTextView.setText("");
    }

    @Override
    protected void onDestroy() {
        command.shutdownCommand();
        super.onDestroy();
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    private boolean checkPermissions() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private void speechCurrentStation() {
        command.tts.isInitialized.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isInitialized) {
                if (isInitialized) {
                    if (command.getStationInfo.checkWhereAmI(MainActivity.this)) {
                        command.tts.speech("안녕하세요. 버블입니다. 현재 위치하신 버스 정류장은 " + userData.startStation.name + "입니다.");
                    } else {
                        command.tts.speech("안녕하세요. 버블입니다.");
                    }

                    command.tts.isInitialized.removeObserver(this);
                }
            }
        });
    }
}
