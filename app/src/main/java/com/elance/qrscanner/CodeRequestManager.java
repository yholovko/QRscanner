package com.elance.qrscanner;

import java.util.HashMap;
import java.util.Map;

public class CodeRequestManager {

    public static Map<String, String> addData(String date, String time, String latitude, String longitude, String id_customer) {
        Map<String, String> dataRequest = new HashMap<>();

        dataRequest.put("date", date);
        dataRequest.put("time", time);
        dataRequest.put("latitude", latitude);
        dataRequest.put("longitude", longitude);
        dataRequest.put("id_customer", id_customer);

        return dataRequest;
    }

    public static Map<String, String> getCustomer(String phone) {
        Map<String, String> dataRequest = new HashMap<>();

        dataRequest.put("phone", phone);

        return dataRequest;
    }

    public static Map<String, String> checkLicense(String key) {
        Map<String, String> dataRequest = new HashMap<>();

        dataRequest.put("key", key);

        return dataRequest;
    }
}