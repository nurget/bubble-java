package com.kdu.bubble.bus.common;

import static com.kdu.bubble.MainActivity.anim;
import static com.kdu.bubble.MainActivity.clearViews;
import static com.kdu.bubble.MainActivity.destStIdTextView;
import static com.kdu.bubble.MainActivity.destStTextView;
import static com.kdu.bubble.MainActivity.getAppContext;
import static com.kdu.bubble.MainActivity.startStIdTextView;
import static com.kdu.bubble.MainActivity.startStTextView;
import static com.kdu.bubble.MainActivity.userData;
import static com.kdu.bubble.MainActivity.vibrator;

import android.content.Context;
import android.media.MediaPlayer;

import com.kdu.bubble.R;
import com.kdu.bubble.voice.TTS;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;

public class TraceBus {

    private Context context;

    String key;         // 서비스 키
    String url;
    String vehId;

    ParsingXML parsingXML;

    // TraceBus 생성자
    // @param 서비스 키 값
    public TraceBus(Context context, String key) {
        this.context = context.getApplicationContext();
        this.key = key;
    }

    // API Url 생성후 스케쥴러 메소드로 넘겨주는 함수
    // @param 정류소 ID, 차량 ID, 탑승예정 or 탑승 중 플레그
    // @flag = 1 탑승 예정 버스인 경우
    // @flag = 2 탑승 중인 버스인 경우
    public void tracing(String prevStId, String vehId, int flag) {

        url = "http://openapi.gbis.go.kr/ws/rest/buslocationservice" +
                "?ServiceKey=" + key +
                "&vehId=" + vehId;
        this.vehId = vehId;
        checkBusLoc(url, prevStId, flag);


    }

    // 스케쥴러 메소드, 10초마다 버스 위치를 확인한다.
    // 이전 정류장에 도착하면 플레그에 따라 다음 메소드를 호출
    // @param API URL, 정류소 ID, 탑승예정 or 탑승 중 플레그
    public void checkBusLoc(final String url, final String stId, final int flag) {

        final TTS tts = new TTS(context);
        final Timer timer = new Timer();
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    ParsingXML parsingXML = new ParsingXML(url);
                    String s = "";
                    s = parsingXML.parsing("stId", 0);
                    if (s.equals(stId)) {
                        Thread.sleep(20000);
                        URL url = new URL("https://cobitsa.herokuapp.com/bus/" + vehId);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        // 음성, 진동 알림
                        MediaPlayer mediaPlayer = MediaPlayer.create(getAppContext(), R.raw.sound_dingdong);
                        mediaPlayer.start();
                        vibrator.vibrate(500);
                        //탑승 예정 버스가 이전 정류장 도착한 경우
                        if (flag == 1) {
                            tts.speech("버스가 이전 정류장을 출발했습니다. 탑승준비를 해주세요.");
                            conn.setRequestMethod("POST");
                            startStTextView.startAnimation(anim);
                            startStIdTextView.startAnimation(anim);
                        }
                        // 탑승 중인 버스가 이전 정류장 도착한 경우
                        else if (flag == 2) {
                            tts.speech("목적지가 다음 정류장 입니다. 하차준비를 해주세요.");
                            conn.setRequestMethod("PUT");
                            destStTextView.startAnimation(anim);
                            destStIdTextView.startAnimation(anim);
                            Thread.sleep(10000);
                            userData.cleanUserData();
                            clearViews();
                        }
                        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                        out.write("");
                        out.close();
                        conn.getInputStream();
                        timer.cancel();
                    }
                } catch (ParserConfigurationException | InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        };

        // 10초 주기로 스케줄러 실행
        timer.schedule(task, 0, 10000);
    }

    private String getPreStId(String url, String stId) {
        String preStId = "";
        int preIndex = -1;
        try {
            parsingXML = new ParsingXML(url);
            preIndex = parsingXML.index("station", stId) - 1;
            preStId = parsingXML.parsing("station", preIndex);
        } catch (ParserConfigurationException | InterruptedException e) {
            e.printStackTrace();
        }

        return preStId;
    }
}
