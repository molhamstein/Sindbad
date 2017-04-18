package com.leenita.sindbad.model;

import com.leenita.sindbad.SindbadApp;

import org.json.JSONObject;

public class SindMedia extends AppBaseModel {
    public enum mediaType {IMAGE, VIDEO}

    private String id;
    private String path;
    private String type;

    public SindMedia() {
    }

    public static SindMedia fromJson(JSONObject json) {
        try {
            return SindbadApp.getSharedGsonParser().fromJson(json.toString(), SindMedia.class);
        } catch (Exception ignored) {
        }
        return new SindMedia();
    }

    public String getPath() {
        return path;
    }

    public mediaType getType() {
        switch (type){
            case "IMAGE":
               return mediaType.IMAGE;
            case "VIDEO":
                return mediaType.VIDEO;
        }
        return mediaType.IMAGE;
    }



    @Override
    public String getId() {
        return id;
    }
}
