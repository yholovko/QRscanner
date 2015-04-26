package com.elance.qrscanner;

import java.util.HashMap;
import java.util.Map;

public class CodeRequestManager {

    public static Map<String, String> securityDataRequest(String date, String time, String phoneNumber, String latitude, String longitude) {
        Map<String, String> dataRequest = new HashMap<>();

        dataRequest.put("date", date);
        dataRequest.put("time", time);
        dataRequest.put("phonenumber", phoneNumber);
        dataRequest.put("latitude", latitude);
        dataRequest.put("longitude", longitude);

        return dataRequest;
    }

}