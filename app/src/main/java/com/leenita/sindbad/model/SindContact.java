package com.leenita.sindbad.model;

import com.leenita.sindbad.SindbadApp;

import org.json.JSONObject;

import java.util.ArrayList;


public class SindContact extends AppBaseModel {
    public enum contactType {MAIL, WEB, PHONE, MOBILE}

    private String id;
    private String value;
    private String type;
    private contactType contactType;
    
    public SindContact() {
    }

    public static SindContact fromJson(JSONObject json) {
        try {
            return SindbadApp.getSharedGsonParser().fromJson(json.toString(), SindContact.class);
        } catch (Exception ignored) {
        }
        return new SindContact();
    }

    public String getValue() {
        return value;
    }

    public contactType getType() {
        switch (type){
            case "PHONE":
               return contactType.PHONE;
            case "EMAIL":
                return contactType.MAIL;
            case "MOBILE":
                return contactType.MOBILE;
            case "WEBSITE":
                return contactType.WEB;
        }
        return contactType.PHONE;
    }



    @Override
    public String getId() {
        return id;
    }

    public static String getContactInfoByType(ArrayList<SindContact> contacts, contactType type)
    {
        if(contacts == null || contacts.isEmpty())
            return null;

        for(SindContact info : contacts)
            if(info.getType() == type)
                return info.getValue();

        return null;
    }
}
