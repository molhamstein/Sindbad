package com.leenita.sindbad.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;

import com.google.gson.reflect.TypeToken;
import com.leenita.sindbad.R;
import com.leenita.sindbad.SindbadApp;
import com.leenita.sindbad.data.GCMHandler.AppGcmListener;
import com.leenita.sindbad.model.AppUser;
import com.leenita.sindbad.model.SindBrand;
import com.leenita.sindbad.model.SindCategory;
import com.leenita.sindbad.model.SindOffer;
import com.leenita.sindbad.model.SindProduct;
import com.leenita.sindbad.model.SindTag;

import java.util.ArrayList;

/**
 * This class is responsible for requesting new data from the data providers
 * and invoking the callback when ready and handling multithreading when required
 * @author MolhamStein
 *
 */
@SuppressWarnings({"unchecked", "UnusedAssignment"})
public class DataStore {

    public static String VERSIOIN_ID ="0.1";

    public enum GENERIC_ERROR_TYPE {NO_ERROR, UNDEFINED_ERROR, NO_CONNECTION, NOT_LOGGED_IN, NO_MORE_PAGES}
    public enum App_ACCESS_MODE {NOT_LOGGED_IN, NOT_VERIFIED, VERIFIED} ;
	private static DataStore instance = null ;

	private Handler handler ;
	private ArrayList<DataStoreUpdateListener> updateListeners ;
    private ServerAccess serverHandler;

	private AppUser me;
    private String apiAccessToken;
    private App_ACCESS_MODE accessMode ;

    // user location
    protected Location meLastLocation;

    // Home screen data
    private ArrayList<SindOffer> arrayNearbyOffers;
    private ArrayList<SindOffer> arrayPickedFroYouOffers;
    private ArrayList<SindOffer> arrayFollowedOffers;
    private ArrayList<SindProduct> arrayNearbyProducts;
    private ArrayList<SindProduct> arrayPickedFroYouProducts;
    private ArrayList<SindProduct> arrayFollowedProducts;


    private ArrayList<SindCategory> arrayCategories;
    private ArrayList<SindBrand> arrayNearbyBrands;
    private ArrayList<SindBrand> arrayFollowedBrands;

    // internal data
    private final int UPDATE_INTERVAL = 30000; // update Data each 30 sec

    // used to lock triggering data store full update while its updating
    private static boolean isUpdatingDataStore  = false;

    /**
     * called once on creating the application
     */
	private DataStore() {
		try {
			handler = new Handler() ;
			updateListeners = new ArrayList<DataStoreUpdateListener>() ;
            serverHandler = ServerAccess.getInstance();

			getLocalData() ;
		}
		catch (Exception ignored) {}
	}

	public static DataStore getInstance() {
		if(instance == null) {
			instance = new DataStore();
		}
		return instance;
	}


    /**
	 * used to invoke the DataRequestCallback on the main thread
	 */
	private void invokeCallback(final DataRequestCallback callback ,final boolean success, final ServerResult result){
		handler.post(new Runnable() {
			@Override
			public void run() {
				if(callback == null)
					return ;
				callback.onDataReady(result, success);
			}
		});
	}

    /**
     * make sure to clear all data in dataStore instance and cache,
     * ex: after logout
     */
	public void clearLocalData() {
		try {
            DataCacheProvider.getInstance().clearCache();
            me = null;
		}catch (Exception ignored) {}
	}

	public void logout (){
		//ParseMgr.getInstance().logout();
		clearLocalData();
        broadcastLoginStateChange();
	}

    /**
     * load the previously cached data into the datatStore instance
     */
	public void getLocalData(){

        DataCacheProvider cache =  DataCacheProvider.getInstance();

        arrayNearbyOffers = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_NEARBY_OFFERS, new TypeToken<ArrayList<SindOffer>>() {}.getType());
        arrayPickedFroYouOffers = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_PICKED_OFFERS, new TypeToken<ArrayList<SindOffer>>() {
        }.getType());
        arrayFollowedOffers = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_FOLLOWED_OFFERS, new TypeToken<ArrayList<SindOffer>>() {
        }.getType());

        arrayNearbyProducts = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_NEARBY_PRODUCTS, new TypeToken<ArrayList<SindProduct>>() {
        }.getType());
        arrayPickedFroYouProducts = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_PICKED_PRODUCTS, new TypeToken<ArrayList<SindProduct>>() {
        }.getType());
        arrayFollowedProducts = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_FOLLOWED_PRODUCTS, new TypeToken<ArrayList<SindProduct>>() {
        }.getType());

        arrayNearbyBrands = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_NEARBY_Brands, new TypeToken<ArrayList<SindBrand>>() {
        }.getType());
        arrayFollowedBrands = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_MY_BRANDS, new TypeToken<ArrayList<SindBrand>>() {
        }.getType());
        arrayCategories = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_CATEGORIES, new TypeToken<ArrayList<SindCategory>>() {
        }.getType());
        me = DataCacheProvider.getInstance().getStoredObjectWithKey(DataCacheProvider.KEY_APP_USER_ME, new TypeToken<AppUser>() {
        }.getType());

	}

    //--------------------
    // DataStore Update
    //-------------------------------------------

    public void startScheduledUpdates() {
        try {
            // start schedule timer
            handler.post(runnableUpdate);
        }catch (Exception e) {}
    }

    public void stopScheduledUpdates() {
        try {
            handler.removeCallbacks(runnableUpdate);
        }catch (Exception e) {}
    }

    /**
     * handle here all the data that should be updated periodically
     */
    Runnable runnableUpdate = new Runnable() {
        @Override
        public void run() {
            requestCategories(null);
            requestNearbyOffers(null);
            requestPickedForYourProducts(null);

            if(isUserLoggedIn()) {

            }
            handler.postDelayed(runnableUpdate, UPDATE_INTERVAL);
        }
    };

    /**
     * called to force a full updated to the datastore
     */
	public void triggerDataUpdate(){
        requestCategories(null);
        requestNearbyOffers(null);
        requestPickedForYourProducts(null);
        // get Following list and cache it for later use
        if(isUserLoggedIn()) {
            //updatetMe();
        }
	}

    /**
     * nofiy all registered listeners that a change has occured to the data store
     */
	private void broadcastDataStoreUpdate(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DataStoreUpdateListener listener : updateListeners) {
                    listener.onDataStoreUpdate();
                }
            }
        });
	}

	public void removeUpdateBroadcastListener(DataStoreUpdateListener listener){
		if(updateListeners != null && updateListeners.contains(listener))
			updateListeners.remove(listener);
	}

	public void addUpdateBroadcastListener(DataStoreUpdateListener listener){
		if(updateListeners == null )
			updateListeners = new ArrayList() ;
		if(!updateListeners.contains(listener))
			updateListeners.add(listener);
	}

    /**
     * triggered when the user is logged in or logged out
     */
    private void broadcastLoginStateChange(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DataStoreUpdateListener listener : updateListeners) {
                    listener.onLoginStateChange();
                }
            }
        });
    }

    //--------------------
    // Login
    //-------------------------------------------
    /**
     * @param FBID : pass null if signing-up without facebook
     * @param callback
     */
    public void attemptSignUp(final String phoneNum, final String firstName,final String lastName, final String countryCode, final String versionId,final String FBID, final DataRequestCallback callback) {

        new Thread( new Runnable() {
            @Override
            public void run() {
                boolean success = true ;
                ServerResult result = serverHandler.registerUser(firstName, lastName, phoneNum, countryCode, versionId, FBID);
                if(result.connectionFailed()){
                    success = false ;
                }else{
                    try {
                        AppUser me= (AppUser) result.getPairs().get("appUser") ;
                        apiAccessToken = me.getAccessToken();
                        setApiAccessToken(apiAccessToken);
                        setMe(me);
                        broadcastLoginStateChange();
                    } catch (Exception e) {
                        success = false ;
                    }
                }
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    /**
     * attempting login using phone number
     * @param phoneNumfinal if the phone number was found in the DB the user will be logged in otherwise error code will be returned
     */
    public void attemptLogin(final String phoneNumfinal, final DataRequestCallback callback) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                boolean success = true ;
                ServerResult result = serverHandler.login(phoneNumfinal);
                if(result.getRequestStatusCode() >= 600){
                    success = false ;
                }else{
                    if(result.isValid()){
                        me = (AppUser) result.getPairs().get("appUser") ;
                        apiAccessToken = me.getAccessToken();
                        setApiAccessToken(apiAccessToken);
                        setMe(me);
                        broadcastLoginStateChange();
                    }
                }
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    public void logoutUser() {
        try {
            stopScheduledUpdates();
            clearLocalData();
        }catch (Exception e) {}
    }

    /**
     * ued to request a SMS verification code to be sent to the user's phone number after login
     */
    public void requestVerificationMsg( final DataRequestCallback callback) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                boolean success = true ;
                ServerResult result = serverHandler.requestVerificationMsg(apiAccessToken);
                if(result.connectionFailed()){
                    success = false ;
                }
                if(callback != null)
                    invokeCallback(callback, success, result);
            }
        }).start();
    }

    /**
     * @param verifMsg : the verification code the user received over SMS
     * @param callback
     */
    public void verifyAccount(final String verifMsg,  final DataRequestCallback callback) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                boolean success = true ;
                ServerResult result = serverHandler.verifyAccount(apiAccessToken, verifMsg);
                if(result.connectionFailed()){
                    success = false ;
                }else{
                    //boolean loginSuccess = (Boolean) data.get("verified");
                    if(!result.isValid()){
                        setAccessMode(App_ACCESS_MODE.NOT_VERIFIED);
                    }else{
                        setAccessMode(App_ACCESS_MODE.VERIFIED);
                    }
                }
                if(callback != null)
                    invokeCallback(callback, success, result);
            }
        }).start();
    }

    //--------------------
    // GCM
    //-------------------------------------------
    /**
     * after login the app will recuest a GCM ID in order to be able to receive push notifications
     */
    public void requestGCMRegsitrationId() {

        GCMHandler.requestGCMRegistrationId(SindbadApp.getAppContext(), new AppGcmListener() {
            @Override
            public void onRegistratinId(final String regId) {
                sendGCMRegistrationId(regId);
            }

            @Override
            public void onPlayServicesError() {
                //Configuration.displayToast("onPlayServicesError", Toast.LENGTH_SHORT);
            }
        });
    }

    /**
     * Register the device with the API to receive Push notifications
     * @param regId The registration Id received from the GCM provider
     */
    public void sendGCMRegistrationId(final String regId) {
        try {
            new Thread( new Runnable() {
                @Override
                public void run() {
                    boolean success = true ;
                    ServerResult result = serverHandler.setGcmId(apiAccessToken, regId);
                    if(result.connectionFailed()){
                        success = false ;
                    }else{
                        success = (Boolean) result.getValue("success") ;
                    }
                    GCMHandler.storeIsGcmIdSetOnServer(SindbadApp.getAppContext(), success);
                }
            }).start();
        }catch (Exception igored) {}
    }

    //--------------------
    // Offer
    //-------------------------------------------
    public void requestNearbyOffers(final DataRequestCallback callback){
        new Thread( new Runnable() {
            @Override
            public void run() {
                boolean success = true ;
                float lat = meLastLocation!=null?(float)meLastLocation.getLatitude():0;
                float lon = meLastLocation!=null?(float)meLastLocation.getLongitude():0;
                ServerResult result = serverHandler.getNearbyOffers(lat, lon, 100000f);
                if(result.connectionFailed()){
                    success = false ;
                }else{
                    if(result.isValid()){
                        ArrayList<SindOffer> arrayRecieved = (ArrayList<SindOffer>) result.get("offers");
                        if(arrayRecieved!=null && !arrayRecieved.isEmpty()) {
                            arrayNearbyOffers = arrayRecieved;
                            arrayPickedFroYouOffers = arrayRecieved;
                            arrayFollowedOffers = arrayRecieved;
                            DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_NEARBY_OFFERS, arrayNearbyOffers);
                            DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_PICKED_OFFERS, arrayPickedFroYouOffers);
                            DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_FOLLOWED_OFFERS, arrayFollowedOffers);
                        }
                    }
                }
                broadcastDataStoreUpdate();
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    /**
     * get Offer by Id this is used incase of opening offer details activity through deeplinking
     */
    public void getOffer(final String offerId, final DataRequestCallback callback)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerResult result = serverHandler.getOfferById(offerId);
                broadcastDataStoreUpdate();
                invokeCallback(callback, !result.connectionFailed(), result);
            }
        }).start();
    }

    /**
     * buy Offer by Id
     */
    public void buyOffer(final String offerId, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String accessToken = getMe().getAccessToken();
                ServerResult result = serverHandler.buyOfferById(offerId, accessToken);
                invokeCallback(callback, !result.connectionFailed(), result);
            }
        }).start();
    }

    //--------------------
    // Categories
    //----------------------------------------------
    public void requestCategories(final DataRequestCallback callback){
        new Thread( new Runnable() {
            @Override
            public void run() {
                boolean success = true ;
                ServerResult result = serverHandler.getCategories();
                if(result.connectionFailed()){
                    success = false ;
                }else{
                    if(result.isValid()){
                        ArrayList<SindCategory> arrayRecieved = (ArrayList<SindCategory>) result.get("categories");
                        if(arrayRecieved!=null && !arrayRecieved.isEmpty()) {
                            arrayCategories = arrayRecieved;
                            DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_CATEGORIES, arrayRecieved);
                        }
                    }
                }
                broadcastDataStoreUpdate();
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    //--------------------
    // Products
    //----------------------------------------------
    public void requestPickedForYourProducts(final DataRequestCallback callback){
        new Thread( new Runnable() {
            @Override
            public void run() {
                boolean success = true ;
                ServerResult result = serverHandler.getPickedForYouProducts();
                if(result.connectionFailed()){
                    success = false ;
                }else{
                    if(result.isValid()){
                        ArrayList<SindProduct> arrayRecieved = (ArrayList<SindProduct>) result.get("products");
                        if(arrayRecieved!=null && !arrayRecieved.isEmpty()) {
                            arrayNearbyProducts = arrayRecieved;
                            arrayPickedFroYouProducts = arrayRecieved;
                            arrayFollowedProducts = arrayRecieved;
                            DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_NEARBY_PRODUCTS, arrayRecieved);
                            DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_PICKED_PRODUCTS, arrayRecieved);
                            DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_FOLLOWED_PRODUCTS, arrayRecieved);
                        }
                    }
                }
                broadcastDataStoreUpdate();
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    // TODO not implemented yet
    public void searchForKeyword(String keyWord, final DataRequestCallback callback){
        new Thread( new Runnable() {
            @Override
            public void run() {
                boolean success = true ;
                ServerResult result = serverHandler.getPickedForYouProducts();
                if(result.connectionFailed()){
                    success = false ;
                }
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    /**
     * get Product by Id this is used incase of opening product details activity through deeplinking
     */
    public void getProduct(final String prodId, final DataRequestCallback callback)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerResult result = serverHandler.getProductById(prodId);
                broadcastDataStoreUpdate();
                invokeCallback(callback, !result.connectionFailed(), result);
            }
        }).start();
    }

    //--------------------
    // Brands
    //----------------------------------------------

    /**
     * get all brands that fall into a certain geographical range
     * results can also be filtered by categories, tags, keyword
     * @param lat center lattitide
     * @param lon center longitude
     */
    public void requestBrands(final float lat, final float lon, final float radius, final int page , final String keyWord, final ArrayList<SindCategory> categories , final ArrayList<SindTag> tags , final DataRequestCallback callback){
        new Thread( new Runnable() {
            @Override
            public void run() {
                boolean success = true ;
                ServerResult result = serverHandler.getNearByBrands(lat, lon, radius, page, keyWord, categories, tags);
                if(result.connectionFailed()){
                    success = false ;
                }else{
                    if(result.isValid()){
                        ArrayList<SindBrand> arrayRecieved = (ArrayList<SindBrand>) result.get("brands");
                        if(arrayRecieved!=null && !arrayRecieved.isEmpty()) {
                            arrayNearbyBrands = arrayRecieved;
                            DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_NEARBY_Brands, arrayRecieved);
                        }
                    }
                }
                broadcastDataStoreUpdate();
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    public void unFollowBrand(final String brandId, final DataRequestCallback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerResult result = serverHandler.unFollowBrand(brandId, getMe().getAccessToken());
                getFollowedBrands(null);
                invokeCallback(callback, !result.connectionFailed(), result);
            }
        }).start();
    }

    public void followBrand(final String brandId, final DataRequestCallback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerResult result = serverHandler.followBrand(brandId, getMe().getAccessToken());
                getFollowedBrands(null);
                invokeCallback(callback, !result.connectionFailed(), result);
            }
        }).start();
    }

    public void getFollowedBrands(final DataRequestCallback callback){
        new Thread( new Runnable() {
            @Override
            public void run() {
                boolean success = true ;
                ServerResult result = serverHandler.getFollowedBrands(getMe().getAccessToken());
                if(result.connectionFailed()){
                    success = false ;
                }else{
                    if(result.isValid()){
                        ArrayList<SindBrand> arrayRecieved = (ArrayList<SindBrand>) result.get("brands");
                        if(arrayRecieved!=null && !arrayRecieved.isEmpty()) {
                            arrayFollowedBrands = arrayRecieved;
                            DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_MY_BRANDS, arrayRecieved);
                        }
                    }
                }
                broadcastDataStoreUpdate();
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    //--------------------
    // Getters
    //----------------------------------------------


    public boolean isUserLoggedIn() {
        return me != null;
    }

    public AppUser getMe(){
        if(me == null)
            me = DataCacheProvider.getInstance().getStoredObjectWithKey(DataCacheProvider.KEY_APP_USER_ME, new TypeToken<AppUser>(){}.getType());
        return me ;
    }

    public void setMe(AppUser newUser){
        if(isUserLoggedIn())
            this.me = newUser;
        DataCacheProvider.getInstance().storeObjectWithKey(DataCacheProvider.KEY_APP_USER_ME, newUser);
    }

    public void setApiAccessToken(String apiAccessToken) {
        this.apiAccessToken = apiAccessToken;
        DataCacheProvider.getInstance().storeStringWithKey(DataCacheProvider.KEY_ACCESS_TOKEN, apiAccessToken);

    }
    public void setAccessMode(App_ACCESS_MODE accessMode) {
        this.accessMode = accessMode;
        DataCacheProvider.getInstance().storeIntWithKey(DataCacheProvider.KEY_APP_ACCESS_MODE, accessMode.ordinal());
    }
    public App_ACCESS_MODE getAccessMode() {
        return accessMode;
    }

    public boolean isLoggedIn(){
        return apiAccessToken != null && !apiAccessToken.isEmpty() && accessMode == App_ACCESS_MODE.VERIFIED;
    }

    public ArrayList<SindCategory> getArrayCategories() {
        return arrayCategories;
    }

    public ArrayList<SindOffer> getArrayNearbyOffers() {
        return arrayNearbyOffers;
    }

    public ArrayList<SindOffer> getArrayPickedFroYouOffers() {
        return arrayPickedFroYouOffers;
    }

    public ArrayList<SindOffer> getArrayFollowedOffers() {
        return arrayFollowedOffers;
    }

    public ArrayList<SindProduct> getArrayPickedFroYouProducts() {
        return arrayPickedFroYouProducts;
    }

    public ArrayList<SindProduct> getArrayNearbyProducts() {
        return arrayNearbyProducts;
    }

    public ArrayList<SindProduct> getArrayFollowedProducts() {
        return arrayFollowedProducts;
    }

    public ArrayList<SindBrand> getArrayNearbyBrands() {
        return arrayNearbyBrands;
    }

    public Location getMeLastLocation() {
        return meLastLocation;
    }

    public ArrayList<SindBrand> getMyBrands() {
        return arrayFollowedBrands;
    }

    public void setMeLastLocation(Location meLastLocation) {
        this.meLastLocation = meLastLocation;
    }


    //TODO should not be in dataSore, move to a share manager
    /// Share
    private static String SHAREABLE_URL_BASE = "http://ec2-52-88-151-83.us-west-2.compute.amazonaws.com/Appsher/#";
    private static String DEEP_LINK_URL_BASE = "http://leenita.com/";

    @SuppressLint("StringFormatMatches")
    public void shareOffer(Activity activity, SindOffer offer) {
        String appUrl = DEEP_LINK_URL_BASE + "OfferDetails?offerId=" + offer.getId();
        String shareMsg = String.format(activity.getString(R.string.offer_details_share_msg, (Object) null), appUrl);
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.offer_details_share_title));
        share.putExtra(Intent.EXTRA_TEXT, shareMsg);
        activity.startActivity(Intent.createChooser(share, activity.getString(R.string.offer_details_share_diag_title)));
    }

    @SuppressLint("StringFormatMatches")
    public void shareProduct(Activity activity, SindProduct prod) {
        String appUrl = DEEP_LINK_URL_BASE + "ProdDetails?prodId=" + prod.getId();
        String shareMsg = String.format(activity.getString(R.string.prod_details_share_msg, (Object) null), appUrl);
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.prod_details_share_title));
        share.putExtra(Intent.EXTRA_TEXT, shareMsg);
        activity.startActivity(Intent.createChooser(share, activity.getString(R.string.prod_details_share_diag_title)));
    }

    //--------------------
    // Interfaces
    //----------------------------------------------
    public interface DataRequestCallback {
		void onDataReady(ServerResult result, boolean success);
	}

	public interface DataStoreUpdateListener {
		void onDataStoreUpdate();
        void onLoginStateChange();
	}

    public interface DataStoreErrorListener{
        void onError(GENERIC_ERROR_TYPE error);
    }
}
