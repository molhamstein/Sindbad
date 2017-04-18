package com.leenita.sindbad.model;

import com.leenita.sindbad.SindbadApp;

import org.json.JSONObject;

import java.util.ArrayList;


public class SindOffer extends AppBaseModel {

    public static enum OFFER_TYPE {FIXED_DEAL, VOUCHER};

    String id;
    String title;
    String description;
    String value;
    SindBrand brand;
    ArrayList<SindProduct> products;
    String type;
    String expiry_date;
    int tolerance;
    long validity_duration;
    String photo;
    String cover;

    int upvotes;

    boolean isUpVotedByUser;
    boolean isOwnedOffer;


    public SindOffer() {

    }

    public static SindOffer fromJson(JSONObject json) {
        try {
            return SindbadApp.getSharedGsonParser().fromJson(json.toString(), SindOffer.class);
        }catch (Exception ignored){
            ignored.printStackTrace();
        }
        return new SindOffer();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public SindProduct getProduct() {
        if(products == null || products.isEmpty())
            return null;
        return products.get(0);
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getDescription() {
        return description;
    }

    public SindBrand getBrand() {
        return brand;
    }

    public String getDiscount() {
        return value;
    }

    public int getUpvotes() {
        return  upvotes;
    }

    public void setUpvotes(int upvotes) {this.upvotes = upvotes;}

    public boolean getIsUpVotedByUser() {return isUpVotedByUser;}

    public void setIsUpVotedByUser(boolean isUpVotedByUser) {this.isUpVotedByUser = isUpVotedByUser;}

    public String getCover() {
        if(cover != null && !cover.isEmpty())
            return cover;
        return null;
    }

    public OFFER_TYPE getType(){
        switch (type){
            case "FIXED_DEAL":
                return OFFER_TYPE.FIXED_DEAL;
            case "VOUCHER":
                return OFFER_TYPE.VOUCHER;
            default:
                return OFFER_TYPE.FIXED_DEAL;
        }
    }

    public String getPhoto() {
        if(photo != null && !photo.isEmpty())
            return photo;
        return null;
    }

    public boolean isOwnedOffer() {
        return isOwnedOffer;
    }

    public void setIsOwnedOffer(boolean isOwnedOffer) {
        this.isOwnedOffer = isOwnedOffer;
    }

    public ArrayList<SindMedia> getMedia() {

        if(products != null && !products.isEmpty()){
            ArrayList<SindMedia> allMedia = new ArrayList<>();
            for (SindProduct prod : products) {
                allMedia.addAll(prod.getMedia());
            }
            return allMedia;
        }
        return null;
    }


}
