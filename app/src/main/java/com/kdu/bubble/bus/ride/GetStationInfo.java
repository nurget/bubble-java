package com.kdu.bubble.bus.ride;

import android.app.Activity;
import android.util.Log;

import com.kdu.bubble.bus.common.GpsTracker;
import com.kdu.bubble.bus.common.ParsingXML;

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import static com.kdu.bubble.MainActivity.userData;

public class GetStationInfo {

    String key;             // 인증키
    private final static int START_DISTANCE_FOR_GET_STATION = 30;

    // 인증키 값으로 초기화
    public GetStationInfo(String key) {
        this.key = key;
    }


    public Boolean checkWhereAmI(Activity mainActivity) {
        GpsTracker gpsTracker = new GpsTracker(mainActivity);

        double latitute = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        Log.d("GetStationInfo", "Latitude: " + latitute + ", longitude: " + longitude);

        ArrayList<String> stInfo = this.getApiData(Double.toString(longitude),Double.toString(latitute),  Integer.toString(START_DISTANCE_FOR_GET_STATION));

        if (stInfo == null)
            return false;
        Log.d("stInfo : ", stInfo.toString());
        userData.setStartStation(stInfo.get(0), stInfo.get(1), stInfo.get(2));
        return true;
    }

    // 현재 위치를 기반으로 제일 가까운 정류소의 정보 리턴
    // @param x : GPS X 좌표
    // @param y : GPS Y 좌표
    // @param radius : 반경(0~1500m)
    // @return List(0) : 정류소 아이디
    // @return List(1) : 정류소 이름
    // @return List(2) : 정류소 고유번호
    public ArrayList<String> getApiData(String tmX, String tmY, String raduis) {
        String url = "http://apis.data.go.kr/6410000/busstationservice/getBusStationAroundList" +
                "?serviceKey=" + key +
                "&x=" + tmX +
                "&y=" + tmY;
        ArrayList<String> stInfo = new ArrayList<>();
        String stId = "";
        String stName = "";
        String arsId = "";

        try {
            ParsingXML parsingXML = new ParsingXML(url);
            int itemsNum = parsingXML.getLength("busStationAroundList");
            Log.d("size : ", String.valueOf(itemsNum));
            if ( itemsNum > 0 ) {
                stId = parsingXML.parsing("busStationAroundList","stationId", 0);
                stName = parsingXML.parsing("busStationAroundList","stationName", 0);
                arsId = parsingXML.parsing("busStationAroundList","mobileNo", 0);

            } else {
                Log.d("null", "null");
                return null;
            }
        } catch (ParserConfigurationException | InterruptedException e) {
            e.printStackTrace();
        }

        stInfo.add(stId);
        stInfo.add(stName);
        stInfo.add(arsId);

        return stInfo;
    }

    // 현재 위치와 가장 가까운 정류장 인덱스 리턴
    private int findNearStation(ArrayList<ArrayList<Double>> list, double lon, double lat) {
        int ret = 0;
        double minDist = START_DISTANCE_FOR_GET_STATION;

        for(int i=0; i<list.size(); i++) {
            double dist = calDistance(list.get(i).get(1), list.get(i).get(0), lat, lon);
            if (minDist >= dist) {
                ret = i;
                minDist = dist;
            }
        }

        return ret;
    }

    // 좌표 사이에 거리 계산
    // @return : m 단위 거리
    private double calDistance(double lat1, double lon1, double lat2, double lon2){

        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // mile to km
        dist = dist * 1000.0;      // km to m

        return dist;
    }

    // degree to radian
    private double deg2rad(double deg){
        return (double)(deg * Math.PI / (double)180d);
    }

    // radian to degree
    private double rad2deg(double rad){
        return (double)(rad * (double)180d / Math.PI);
    }
}
