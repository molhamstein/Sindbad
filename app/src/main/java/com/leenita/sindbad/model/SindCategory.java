package com.leenita.sindbad.model;

import com.leenita.sindbad.SindbadApp;
import com.parse.ParseObject;

import org.json.JSONObject;

public class SindCategory extends AppBaseModel {

    private String id;
    private int language_id;
    private String title;
    private String icon;
    private String cover;

    public static SindCategory fromJson(JSONObject json) {
        try {
            return SindbadApp.getSharedGsonParser().fromJson(json.toString(), SindCategory.class);
        } catch (Exception ignored) {
        }
        return new SindCategory();
    }

    public String getId() {
        return id;
    }

    public String getPhoto() {
        return icon;
    }

    public String getName() {
        return title;
    }

    public String getCover() {
        return cover;
    }
}
