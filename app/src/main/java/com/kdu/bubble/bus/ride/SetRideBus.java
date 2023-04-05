package com.kdu.bubble.bus.ride;

import com.kdu.bubble.bus.common.GetPrevStId;
import com.kdu.bubble.bus.common.ParsingXML;
import com.kdu.bubble.bus.common.TraceBus;
import com.kdu.bubble.voice.TTS;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import static com.kdu.bubble.MainActivity.userData;

import android.content.Context;

public class SetRideBus {

    private Context context;
    String key;                 // 서비스 키
    GetPrevStId getPrevStId;
    TraceBus traceBus;

    // 키값으로 초기화
    public SetRideBus(Context context, String key) {
        this.context = context.getApplicationContext();
        this.key = key;
        this.getPrevStId = new GetPrevStId(key);
        this.traceBus = new TraceBus(context, key);
    }

    // 탑승할 버스 정보를 List로 반환하는 메소드
    // @param arsId : 현재 정류소의 고유번호
    // @param sttBus : STT로 받은 탑승하려는 버스 번호
    // @return List(0) : 버스 번호
    // @return List(1) : 버스 노선 아이디
    public Boolean setBus(String arsId, String sttBus) {

        String url = "http://ws.bus.go.kr/api/rest/stationinfo/getRouteByStation" +
                "?ServiceKey=" + key +
                "&arsId=" + arsId;

        ArrayList<String> infoList = new ArrayList<>();
        String tmpNum = "";
        String tmpRouteId = "";
        String tmpRouteType = "";

        try {
            ParsingXML parsingXML = new ParsingXML(url);
            for (int i = 0; i < parsingXML.getLength(); i++) {
                if (parsingXML.parsing("busRouteNm", i).equals(sttBus)) {
                    tmpNum = parsingXML.parsing("busRouteNm", i);
                    tmpRouteId = parsingXML.parsing("busRouteId", i);
                    tmpRouteType = parsingXML.parsing("busRouteType", i);
                }
            }

        } catch (ParserConfigurationException | InterruptedException e) {
            e.printStackTrace();
        }

        if(tmpNum.equals(""))
            return false;
        infoList.add(tmpNum);
        infoList.add(tmpRouteId);
        infoList.add(tmpRouteType);
        userData.ridingBus.number = infoList.get(0);
        userData.ridingBus.routeId = infoList.get(1);
        userData.ridingBus.routeType = infoList.get(2);

        // Process 2-2 : 탑승예정 버스 지정
        // 탑승 예정 버스의 차량 아이디 저장
        userData.ridingBus.vehId = this.setVehId(userData.startStation.id, userData.ridingBus.routeId);

        // Process 3 : 버스 추적을 위한 이전 정류소 정보 검색
        userData.startStation.prevId = getPrevStId.get(userData.ridingBus.routeId, userData.startStation.id);

        // Process 4 : 탑승예정 버스 추적
        traceBus.tracing(userData.startStation.prevId, userData.ridingBus.vehId, 1);
        return true;
    }

    // 탑승하려는 노선의 버스 중 첫번째로 도착예정인 버스의 차량 아이디 반환
    // @param stId : 현재 정류소의 아이디
    // @param busRouteId : 탑승하려는 버스 노선 아이디
    // @return 첫번째 도착예정인 차량 아이디
    public String setVehId(String stId, String busRouteId) {

        String url = "http://ws.bus.go.kr/api/rest/arrive/getArrInfoByRouteAll" +
                "?ServiceKey=" + key +
                "&busRouteId=" + busRouteId;
        String vehId = "";

        try {
            ParsingXML parsingXML = new ParsingXML(url);
            for (int i = 0; i < parsingXML.getLength(); i++) {
                if (parsingXML.parsing("stId", i).equals(stId)) {
                    vehId = parsingXML.parsing("vehId1", i);
                    break;
                }
            }

        } catch (ParserConfigurationException | InterruptedException e) {
            e.printStackTrace();
        }

        return vehId;
    }
}
