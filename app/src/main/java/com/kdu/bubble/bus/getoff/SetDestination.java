package com.kdu.bubble.bus.getoff;

import java.util.ArrayList;

import com.kdu.bubble.bus.common.GetPrevStId;
import com.kdu.bubble.bus.common.ParsingXML;
import com.kdu.bubble.bus.common.TraceBus;

import javax.xml.parsers.ParserConfigurationException;

import static com.kdu.bubble.MainActivity.destStIdTextView;
import static com.kdu.bubble.MainActivity.userData;

import android.content.Context;

public class SetDestination {

    private Context context;

    String key;                                                 // 서비스 키
    GetPrevStId getPrevStId;
    TraceBus traceBus;

    // 해당 노선의 모든 정류소들의 정보를 담는 List
    ArrayList<String> nameList;                 // 정류소 이름 List
    ArrayList<String> idList;                       // 정류소 아이디 List
    ArrayList<String> arsIdList;                 // 정류소 고유번호 List

    // SetDestination 생성자
    // @param key = 서비스 키
    public SetDestination(Context context, String key) {
        this.context = context.getApplicationContext();

        this.key = key;
        nameList = new ArrayList<>();
        idList = new ArrayList<>();
        arsIdList = new ArrayList<>();
        this.getPrevStId = new GetPrevStId(key);
        this.traceBus = new TraceBus(context, key);
    }

    // 메인 메소드
    // @param busRouteId : 버스 노선 ID
    // @param startArsId : 버스를 탑승한 정류소의 고유번호
    // @param sttDestination : STT로 받은 정류소 명
    // @return List(0) : 정류소 이름
    // @return List(1) : 정류소 아이디
    // @return List(2) : 정류소 고유번호
    public Boolean setBus(String busRouteId, String startArsId, String sttDestination) {
        String desId = "";
        String desName = "";
        String desArs = "";
        String tmpName = "";
        String tmpId = "";
        String tmpArs = "";

        // API URL 생성
        final String url = "http://apis.data.go.kr/6410000/busrouteservice/getBusRouteStationList" +
                "?serviceKey=" + key +
                "&routeId=" + busRouteId;

        try {
            ParsingXML parsingXML = new ParsingXML(url);
            for (int i = 0; i < parsingXML.getLength(); i++) {
                tmpArs = parsingXML.parsing("mobileNo", i);
                tmpId = parsingXML.parsing("stationId", i);
                tmpName = parsingXML.parsing("stationName", i);
                idList.add(tmpId);
                nameList.add(tmpName);
                arsIdList.add(tmpArs);
            }
        } catch (ParserConfigurationException | InterruptedException e) {
            e.printStackTrace();
        }

        // 유효한 정류소인지 확인
        // 만약 유효하다면 목적지가 될수있는 모든 정류소의 인덱스를 리스트에 저장
        if (isExist(sttDestination, nameList)) {
            ArrayList<Integer> indexList = new ArrayList<>();

            indexList.add(getIndex(startArsId, arsIdList));

            for (int index = 0; index < nameList.size(); index++) {
                if (nameList.get(index).equals(sttDestination)) {
                    indexList.add(index);
                }
            }

            // 현재 또는 탑승한 정류장의 다음 정류장 위치 고려하여
            // 도착지 인덱스 리스트에서 맞는 값 선택하는 과정
            if (indexList.size() == 2) {
                desId = idList.get(indexList.get(1));
                desName = nameList.get(indexList.get(1));
                desArs = arsIdList.get(indexList.get(1));
            } else if (indexList.size() == 3) {
                desId = idList.get(indexList.get(1));
                desName = nameList.get(indexList.get(1));
                desArs = arsIdList.get(indexList.get(1));
                if (indexList.get(0) > indexList.get(1) && indexList.get(0) < indexList.get(2)) {
                    desId = idList.get(indexList.get(2));
                    desName = nameList.get(indexList.get(2));
                    desArs = arsIdList.get(indexList.get(2));
                }
            }
        }
        else
            return false;

        ArrayList<String> refList = new ArrayList<>();
        refList.add(desName);
        refList.add(desId);
        refList.add(desArs);
        userData.setDestStation(desId, desName, desArs);
        destStIdTextView.setText(desArs);
        // Process 6 : 버스 추적을 위한 이전 정류소 정보 검색
        userData.desStation.prevId = getPrevStId.get(userData.ridingBus.routeId, userData.desStation.id);
        // Process 7 : 탑승중인 버스 추적
        traceBus.tracing(userData.desStation.prevId, userData.ridingBus.vehId, 2);
        return true;
    }

    // STT로 입력받은 정류소 이름이 유효한 정유장인지 확인 후 인덱스 값 반환
    // @param STT로 입력받은 정류소, 정류소 이름 리스트
    public boolean isExist(String sttDestination, ArrayList<String> nameList) {
        boolean ret = false;

        for (String name : nameList) {
            if (name.equals(sttDestination)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    // @param 정류소 ID, 정류소 ID 리스트
    // @return 유효하다면 인덱스 값, 유효하지 않다면 -1
    public int getIndex(String id, ArrayList<String> list) {
        int index = -1;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(id)) {
                index = i;
                break;
            }
        }
        return index;
    }
}
