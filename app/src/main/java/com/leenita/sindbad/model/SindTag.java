package com.leenita.sindbad.model;

import com.leenita.sindbad.SindbadApp;

import org.json.JSONObject;


public class SindTag extends AppBaseModel {


    String id;
    String name;
    public SindTag() {}

    public static SindTag fromJson(JSONObject json) {
        try {
            return  SindbadApp.getSharedGsonParser().fromJson(json.toString(), SindTag.class);
        }catch (Exception ignored){}
        return new SindTag();
    }

    public JSONObject getJsonObject(){
        try {
            SindbadApp.getSharedGsonParser().toJson(this);
            return new JSONObject(SindbadApp.getSharedGsonParser().toJson(this));
        }catch (Exception e){}
        return  new JSONObject();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
