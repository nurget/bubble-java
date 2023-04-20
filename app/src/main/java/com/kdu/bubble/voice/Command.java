package com.kdu.bubble.voice;

import static com.kdu.bubble.MainActivity.busTextView;
import static com.kdu.bubble.MainActivity.destStTextView;
import static com.kdu.bubble.MainActivity.rideBus;
import static com.kdu.bubble.MainActivity.setDestination;
import static com.kdu.bubble.MainActivity.startStIdTextView;
import static com.kdu.bubble.MainActivity.startStTextView;
import static com.kdu.bubble.MainActivity.userData;

import android.app.Activity;
import android.graphics.Color;

import com.kdu.bubble.bus.ride.GetStationInfo;

import java.util.ArrayList;
import java.util.List;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;

public class Command {
    private STT stt;
    public TTS tts;
    Komoran komoran;
    ArrayList<String> args = new ArrayList<String>();
    int commandFlag = 0;
    int cnt = 0;
    Activity mainActivity;
    public GetStationInfo getStationInfo;

    private String key;
    public static OnEndListeningListener onEndListeningListener = null;

    public void setOnEndListeningListener(OnEndListeningListener listener) {
        onEndListeningListener = listener;
    }

    public Command(Activity activity, String key) {
        this.key = key;

        stt = new STT(activity);

        this.komoran = new Komoran(DEFAULT_MODEL.LIGHT);
        tts = new TTS(activity);
        this.mainActivity = activity;
        this.getStationInfo = new GetStationInfo(key);
        setOnEndListeningListener(new OnEndListeningListener() {
            @Override
            public void onEndListening(ListeningEvent listeningEvent) throws InterruptedException {
                executeCommand(stt.result.get(0));
            }
        });
    }

    public void getCommand() {
        stt.startListening();
    }

    public KomoranResult analyzeCommand(String command) {
        return this.komoran.analyze(command);

    }

    public void executeCommand(String command) throws InterruptedException {
        KomoranResult analyzeResult = analyzeCommand(command);
        List<String> verb = analyzeResult.getMorphesByTags("VV");
        if (verb.contains("타")) {
            //탑승할 버스 지정
            String[] split = command.split("번");
            args.add(split[0]);
            tts.speech(split[0] + "번 버스가 맞습니까?");
            Thread.sleep(3000);
            commandFlag = 1;
            //맞는지 아닌지 확인
            this.cnt = 1;
            getCommand();
        } else if (verb.contains("내리")) {
            // 내릴 정류장 지정
            if (userData.ridingBus.routeId.equals("")) { //vehId
                tts.speech("죄송해요. 지금 탑승하시고 계신 버스를 모르겠어요.");
                return;
            }
            String[] split = command.split("에서");
            args.add(split[0].replace(" ", ""));
            tts.speech(split[0].replace(" ", "") + " 정류장이 맞습니까?");
            Thread.sleep(3000);
            commandFlag = 2;
            this.cnt = 1;
            //맞는지 아닌지 확인
            getCommand();
        } else if (analyzeResult.getMorphesByTags("NP").contains("여기")) {
            // 현재 정류장 확인
            if (getStationInfo.checkWhereAmI(mainActivity)) {
                tts.speech("이곳은" + userData.startStation.name + "정류장입니다. 정류장번호는 " + userData.startStation.arsId + "입니다.");
                startStTextView.setText(userData.startStation.name);
                startStIdTextView.setText(userData.startStation.arsId);
            } else
                tts.speech("이곳은 정류장이 아닙니다.");

        } else if (command.contains("그래") || command.contains("어") || command.contains("네") || command.contains("응") || command.contains("맞아")) {
            if (commandFlag == 1) {
                // 탑승지정 명령 실행
                if (rideBus.setBus(userData.startStation.arsId, this.args.get(0))) {
                    tts.speech(this.args.get(0) + "번 버스가 오면 알려드릴게요");
                    busTextView.setText(this.args.get(0));
                    // 버스 색 지정
                    // (11:직행좌석, 12:좌석, 13:일반, 14:광역급행, 15:따복형, 16:경기순환, 21:직행좌석농어촌, 22: 좌석농어촌)
//                  // (23:일반농어촌, 30:마을, 41:고속시외, 42:좌석시외, 43:일반시외, 51:리무진공항, 52:좌석공항, 53:일반공항)
                    int routeType = Integer.parseInt(userData.ridingBus.routeType);
                    if (routeType == 11 || routeType == 16) {
                        busTextView.setBackgroundColor(Color.parseColor("#FF0000"));
                    } else if (routeType == 12) {
                        busTextView.setBackgroundColor(Color.parseColor("#0075C8"));
                    } else if (routeType == 13) {
                        busTextView.setBackgroundColor(Color.parseColor("#33CC99"));
                    } else if (routeType == 15) {
                        busTextView.setBackgroundColor(Color.parseColor("#B62367"));
                    } else if (routeType == 30) {
                        busTextView.setBackgroundColor(Color.parseColor("#F99D1C"));
                    } else {
                        busTextView.setBackgroundColor(Color.parseColor("#1C1C1C"));
                    }
                } else
                    tts.speech("죄송해요 " + this.args.get(0) + "번 버스를 찾을수 없어요. 다시 확인해주세요.");
            } else if (commandFlag == 2) {
                String station = "";
                if (this.args.get(0).substring(this.args.get(0).length() - 3).equals("정류장"))
                    station = this.args.get(0).substring(0, this.args.get(0).length() - 3);
                else
                    station = this.args.get(0);
                System.out.println(station);
                // 하차지정 명령 실행
                if (setDestination.setBus(userData.ridingBus.routeId, userData.startStation.arsId, station)) {
                    tts.speech(station + " 정류장 에서 알려드릴게요");
                    destStTextView.setText(station);
                } else
                    tts.speech("죄송해요 " + station + " 정류장을 찾을수 없어요. 다시 확인해주세요.");
            }
            commandFlag = 0;
            args.clear();
        } else if (command.contains("아니")) {
            tts.speech("명령을 다시 내려주세요");
            commandFlag = 0;
            args.clear();
        } else {
            tts.speech("잘 모르겠어요. 다시 한번 말씀해주세요.");
        }
        cnt = 1;
    }

    public void shutdownCommand() {
        stt.shutdownSTT();
        tts.shutdownTTS();
    }
}
