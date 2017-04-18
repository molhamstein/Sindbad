package com.leenita.sindbad.model;

import com.leenita.sindbad.SindbadApp;

import org.json.JSONObject;

import java.util.ArrayList;

public class SindProduct extends AppBaseModel {

    private String id;
    private String name;
    private String description;
    private String price;
    private String cover;
    private SindBrand brand;
    private ArrayList<SindMedia> media;


    public static SindProduct fromJson(JSONObject json) {
        try {
            return SindbadApp.getSharedGsonParser().fromJson(json.toString(), SindProduct.class);
        }catch (Exception ignored){}
        return new SindProduct();
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

    public SindBrand getBrand() {
        return brand;
    }

    public String getPrice() {
        return price;
    }

    public String getCover() {
        if(cover != null && !cover.isEmpty())
            return cover;
        if(media !=null && !media.isEmpty())
            return media.get(0).getPath();
        return null;
    }

    public ArrayList<SindMedia> getMedia() {return media;}



}
