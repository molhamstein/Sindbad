package com.leenita.sindbad.model;

import com.google.android.gms.maps.model.LatLng;
import com.leenita.sindbad.SindbadApp;

import org.json.JSONObject;

import java.util.ArrayList;

public class SindBrand extends AppBaseModel {

    public enum mediaType {IMAGE, VIDEO}

    String id;
    String name;
    String cover;
    String logo;
    int upvotes;
    String description;
    boolean isUpVotedByUser;
    ArrayList<SindMedia> arrayMedia;
    String phoneNumber;
    String location;
    SindCategory category;
    ArrayList<SindContact> contacts;
    ArrayList<SindBranch> branches;
    ArrayList<SindTag> tags;

    // generated members;
//    float lat;
//    float lon;

    public SindBrand() {

    }

    public static SindBrand fromJson(JSONObject json) {
        try {
            SindBrand brand = SindbadApp.getSharedGsonParser().fromJson(json.toString(), SindBrand.class);
            return brand;
        }catch (Exception ignored){}
        return new SindBrand();
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

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public int getUpvotes() {
        return  upvotes;
    }

    public void setUpvotes(int upvotes) {this.upvotes = upvotes;}

    public boolean getIsUpVotedByUser() {return isUpVotedByUser;}

    public void setIsUpVotedByUser(boolean isUpVotedByUser) {this.isUpVotedByUser = isUpVotedByUser;}

    public String getCover() {return cover;}

    public ArrayList<SindMedia> getArrayMedia() {return arrayMedia;}

    public ArrayList<SindContact> getContacts() {return contacts;}

    public ArrayList<SindBranch> getBranches() {return branches;}

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLogo() {
        return logo;
    }

    public LatLng getCoordinates(){
        if(location != null && location.contains(",")) {
            String[] splits = location.split(",");
            float lat = Float.valueOf(splits[0]);
            float lon = Float.valueOf(splits[1]);
            return new LatLng(lat, lon);
        }
        return new LatLng(0, 0);
    }

    public SindCategory getCategory() {
        return category;
    }

    public ArrayList<SindTag> getTags() {
        return tags;
    }
}
