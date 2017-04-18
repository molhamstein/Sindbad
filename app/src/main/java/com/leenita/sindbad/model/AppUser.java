package com.leenita.sindbad.model;

import com.leenita.sindbad.SindbadApp;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class AppUser extends AppBaseModel{

	String id ;
    String token;
    int points;
    String name;
    String email;

    // to be cleared
    String profileImage;
    boolean isFollowing;
    String twitterName;
    String displayName;
    String coverImage;
    String company;
    boolean enableMentionNotifications;
    boolean enableAddedAppsNotifications;
    boolean enableFollowedUsersUpvotesNotifications;
    int followersCount;
    int followingCount;

    public static AppUser fromJson(JSONObject json) {
        try {
            return SindbadApp.getSharedGsonParser().fromJson(json.toString(), AppUser.class);
        }catch (Exception ignored){}
        return new AppUser();
    }

    public AppUser() {}

//    public AppUser(ParseObject parseOb) {
//        getDataFromParseObject(parseOb);
//    }
//
//    public AppUser(HashMap<String, Object> map) {
//        getDataFromParseObject((ParseObject)map.get("user"));
//        isFollowing = (Boolean) map.get("isFollowing");
//    }
//
//    public AppUser(JSONObject json) {
//        if (json == null)
//            return;
//        try {
//            id = json.getString("id");
//        }catch (Exception ignored){}
//        try {
//            email = json.getString("email");
//        }catch (Exception ignored){}
//        try {
//            displayName = json.getString("displayName");
//        }catch (Exception ignored){}
//        try {
//            profileImage = json.getString("profileImage");
//        }catch (Exception ignored){}
//        try {
//            isFollowing = json.getBoolean("isFollowing");
//        }catch (Exception ignored){}
//        try {
//            twitterName = json.getString("twitterName");
//        }catch (Exception ignored){}
//        try {
//            coverImage = json.getString("coverImage");
//        }catch (Exception ignored){}
//        try {
//            company = json.getString("company");
//        }catch (Exception ignored){}
//        try {
//            enableAddedAppsNotifications = json.getBoolean("enableAddedAppsNotifications");
//        }catch (Exception ignored){}
//        try {
//            enableFollowedUsersUpvotesNotifications = json.getBoolean("enableFollowedUsersUpvotesNotifications");
//        }catch (Exception ignored){}
//        try {
//            enableMentionNotifications = json.getBoolean("enableMentionNotifications");
//        }catch (Exception ignored){}
//        try {
//            followersCount = json.getInt("followedBy");
//        }catch (Exception ignored){}
//        try {
//            followingCount = json.getInt("following");
//        }catch (Exception ignored){}
//    }
//
//    private void getDataFromParseObject(ParseObject parseOb){
//        try {
//            id = parseOb.getObjectId();
//        }catch (Exception ignored){}
//        try {
//            profileImage = parseOb.getString("profileImage");
//        } catch (Exception ignored) {}
//        try {
//            email = ((ParseUser)parseOb).getEmail();
//        } catch (Exception ignored) {}
//        try {
//            displayName = parseOb.getString("displayName");
//        } catch (Exception ignored) {}
//        try {
//            twitterName = parseOb.getString("twitterName");
//        }catch (Exception ignored){}
//        try {
//            coverImage = parseOb.getString("coverImage");
//        }catch (Exception ignored){}
//        try {
//            company = parseOb.getString("company");
//        }catch (Exception ignored){}
//        try {
//            followersCount = parseOb.getInt("followedBy");
//        }catch (Exception ignored){}
//        try {
//            followingCount = parseOb.getInt("following");
//        }catch (Exception ignored){}
//        try {
//            enableAddedAppsNotifications = parseOb.getBoolean("enableAddedAppsNotifications");
//        }catch (Exception ignored){}
//        try {
//            enableFollowedUsersUpvotesNotifications = parseOb.getBoolean("enableFollowedUsersUpvotesNotifications");
//        }catch (Exception ignored){}
//        try {
//            enableMentionNotifications = parseOb.getBoolean("enableMentionNotifications");
//        }catch (Exception ignored){}
//    }

//    public JSONObject getJsonObject(){
//
//        JSONObject jsn = new JSONObject() ;
//        try{jsn.put("id",id);}catch (Exception ignored){}
//        try{jsn.put("displayName",displayName);}catch (Exception ignored){}
//        try{jsn.put("email",email);}catch (Exception ignored){}
//        try {
//            jsn.put("twitterName", twitterName);
//        } catch (JSONException ignored) {
//        }
//        try{jsn.put("company",company);}catch (Exception ignored){}
//        try{jsn.put("coverImage",coverImage);}catch (Exception ignored){}
//        try{jsn.put("enableAddedAppsNotifications",enableAddedAppsNotifications);}catch (Exception ignored){}
//        try{jsn.put("enableFollowedUsersUpvotesNotifications",enableFollowedUsersUpvotesNotifications);}catch (Exception ignored){}
//        try{jsn.put("enableMentionNotifications",enableMentionNotifications);}catch (Exception ignored){}
//        try{jsn.put("profileImage",profileImage);}catch (Exception ignored){}
//        try{jsn.put("isFollowing",isFollowing);}catch (Exception ignored){}
//        try{jsn.put("followedBy",followersCount);}catch (Exception ignored){}
//        try{jsn.put("following",followingCount);}catch (Exception ignored){}
//        return  jsn;
//    }

    public String getId() { return id; }

    public String getAccessToken() {
        return token;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public boolean getIsFollowing(){return  isFollowing;}

    public String getCompany() {
        return company;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public String getTwitterName() {
        return "@"+twitterName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return name;
    }

    public void setIsFollowing(boolean isFollowing){ this.isFollowing = isFollowing;}

    public String getEmail() {
        return email;
    }

    public boolean isEnableAddedAppsNotifications() {
        return enableAddedAppsNotifications;
    }

    public boolean isEnableFollowedUsersUpvotesNotifications() {
        return enableFollowedUsersUpvotesNotifications;
    }

    public boolean isEnableMentionNotifications() {
        return enableMentionNotifications;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }
}
