package com.leenita.sindbad.model;

import com.google.android.gms.maps.model.LatLng;
import com.leenita.sindbad.SindbadApp;

import org.json.JSONObject;


public class SindBranch extends AppBaseModel {

    private String name;
    private String location;
    private String phone;
    private String fax;
    private String email;
    private String website;
//    private float lat;
//    private float lon;

    public SindBranch() {
    }

    public static SindBranch fromJson(JSONObject json) {
        try {
            SindBranch branch = SindbadApp.getSharedGsonParser().fromJson(json.toString(), SindBranch.class);
            return branch;
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return new SindBranch();
    }

    @Override
    public String getId() {
        return this.location;
    }

    public JSONObject getJsonObject() {
        try {
            return new JSONObject(SindbadApp.getSharedGsonParser().toJson(this));
        } catch (Exception e) {
        }
        return new JSONObject();
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getFax() {
        return fax;
    }

    public String getEmail() {
        return email;
    }

    public String getWebsite() {
        return website;
    }

    public LatLng getCoordinates() {
        LatLng loc = new LatLng(0, 0);
        if (this.location != null && this.location.contains(",")) {
            String[] splits = location.split(",");
            float lat = Float.valueOf(splits[0]);
            float lon = Float.valueOf(splits[1]);
            loc = new LatLng(lat, lon);
        }
        return loc;
    }
}
