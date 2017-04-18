package com.leenita.sindbad.data;

import java.lang.reflect.Type;
import java.util.ArrayList;

import org.json.JSONArray;


import android.content.Context;
import android.content.SharedPreferences;

import com.leenita.sindbad.SindbadApp;
import com.leenita.sindbad.model.AppBaseModel;

public class DataCacheProvider {

    //public storage keys
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_APP_USER_ME = "UserMe";
    public static final String KEY_APP_ACCESS_MODE = "ACCESS_MODE";
    public static final String KEY_APP_ARRAY_NEARBY_OFFERS = "nearbyOffers";
    public static final String KEY_APP_ARRAY_PICKED_OFFERS = "pickedOffers";
    public static final String KEY_APP_ARRAY_FOLLOWED_OFFERS = "followedOffers";
    public static final String KEY_APP_ARRAY_NEARBY_PRODUCTS = "nearbyProducts";
    public static final String KEY_APP_ARRAY_PICKED_PRODUCTS = "pickedProducts";
    public static final String KEY_APP_ARRAY_FOLLOWED_PRODUCTS = "followedProducts";
    public static final String KEY_APP_ARRAY_CATEGORIES = "pickedCategories";
    public static final String KEY_APP_LAST_KNOWN_LAT = "lastKnownLat";
    public static final String KEY_APP_LAST_KNOWN_LON = "lastKnownLon";
    public static final String KEY_APP_ARRAY_NEARBY_Brands = "nearbyBrands";
    public static final String KEY_APP_ARRAY_MY_BRANDS = "mybrands";


    private static DataCacheProvider cacheProvider = null;
	// shared preferences
	SharedPreferences prefData;
	SharedPreferences.Editor prefDataEditor;

	private DataCacheProvider() {
		try {
			// initialize
			prefData = SindbadApp.appContext.getSharedPreferences("app_data", Context.MODE_PRIVATE);
			prefDataEditor = prefData.edit();
		} catch (Exception ignored) {
		}
	}

	public static DataCacheProvider getInstance() {
		if (cacheProvider == null) {
			cacheProvider = new DataCacheProvider();
		}
		return cacheProvider;
	}

	public void clearCache() {
		try {
			prefDataEditor.clear();
			prefDataEditor.commit();
		} catch (Exception ignored) {
		}
	}

    /**
     * Stores the timestamp of the last photo cache clear
     */
    public void storePhotoClearedCacheTimestamp(long timestamp){
        try {
            prefDataEditor.putLong("PhotoClearedCacheTimestamp", timestamp);
            prefDataEditor.commit();
        }
        catch (Exception ignored) {}
    }

    public long getStoredPhotoClearedCacheTimestamp(){
        long timestamp = 0;
        try {
            timestamp = prefData.getLong("PhotoClearedCacheTimestamp", 0);
        }
        catch (Exception ignored) {}
        return timestamp;
    }

    public void storeStringWithKey(String key, String value){
        try {
            prefDataEditor.putString(key, value);
            prefDataEditor.commit();
        }
        catch (Exception ignored) {}
    }

    public String getStoredStringWithKey(String key){
        String value = null;
        try {
            value = prefData.getString(key, "");
        }
        catch (Exception ignored) {}
        return value;
    }

    public void storeIntWithKey(String key, int value){
        try {
            prefDataEditor.putInt(key, value);
            prefDataEditor.commit();
        }
        catch (Exception ignored) {}
    }

    public int getStoredIntWithKey(String key){
        int value = 0;
        try {
            value = prefData.getInt(key, 0);
        }
        catch (Exception ignored) {}
        return value;
    }

    public void storeFloatWithKey(String key, float value){
        try {
            prefDataEditor.putFloat(key, value);
            prefDataEditor.commit();
        }
        catch (Exception ignored) {}
    }

    public float getStoredFloatWithKey(String key){
        float value = 0;
        try {
            value = prefData.getFloat(key, 0f);
        }
        catch (Exception ignored) {}
        return value;
    }

    public void storeObjectWithKey(String key, Object arrayGroups) {
        try {
            if (arrayGroups != null) {
                String strJson = SindbadApp.getSharedGsonParser().toJson(arrayGroups);
                prefDataEditor.putString(key, strJson);
                prefDataEditor.commit();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     *
     * @param key Key to fetch the value from preferences
     * @param objectType to determine the type of the object we want to retrieve,
     *                   we can get the type using the following: new TypeToken<Class>() {}.getType();
     * @param <T> Generic return type
     * @return object of the Type T
     */
    public <T> T getStoredObjectWithKey(String key, Type objectType) {
        try {
            String str = prefData.getString(key, null);
            T object = SindbadApp.getSharedGsonParser().fromJson(str, objectType);
            return object;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void storeArrayWithKey(String key, ArrayList<? extends AppBaseModel> array) {
        try {
            if (array != null) {
                JSONArray jsonArr = AppBaseModel.getJSONArray(array);
                String strJson = jsonArr.toString();
                prefDataEditor.putString(key, strJson);
                prefDataEditor.commit();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     *
     * @param key Key to fetch the value from preferences
     * @param objectType to determine the type of the objects contained in the array we want to retrieve,
     *                   we can get the type using the following: new TypeToken<Class>() {}.getType();
     * @param <T> Generic return type of the array Contents
     * @return array object of the Type T
     */
    public <T> ArrayList<T> getStoredArrayWithKey(String key, Type objectType) {
        try {
            String str = prefData.getString(key, null);
            ArrayList<T> object = SindbadApp.getSharedGsonParser().fromJson(str, objectType);
            return object;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
