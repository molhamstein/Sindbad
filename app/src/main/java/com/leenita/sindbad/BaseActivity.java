package com.leenita.sindbad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.leenita.sindbad.data.DataStore;
import com.pushwoosh.BasePushMessageReceiver;
import com.pushwoosh.BaseRegistrationReceiver;
import com.pushwoosh.PushManager;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Molham on 11/23/15.
 * -------------------------------------------------
 */
public class BaseActivity extends AppCompatActivity{

//    @Override
//    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//    }

    //Registration receiver
    BroadcastReceiver mBroadcastReceiver = new BaseRegistrationReceiver(){
        @Override
        public void onRegisterActionReceive(Context context, Intent intent){
            //checkMessage(intent);
        }
    };

    //Push message receiver
    private BroadcastReceiver mReceiver = new BasePushMessageReceiver(){
        @Override
        protected void onMessageReceive(Intent intent){
            showMessage("push message is " + intent.getExtras().getString(JSON_DATA_KEY));
        }
    };

    protected void initPushNotifications(){
        registerReceivers();

        //Create and start push manager
        PushManager pushManager = PushManager.getInstance(this);

        //Start push manager, this will count app open for Pushwoosh stats as well
        try {
            pushManager.onStartup(this);
            pushManager.setBadgeNumber(0);
        }
        catch(Exception e){
            //push notifications are not available or AndroidManifest.xml is not configured properly
        }

        //Register for push!
        pushManager.registerForPushNotifications();
    }

    private void showMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    //Registration of the receivers
    public void registerReceivers(){
        IntentFilter intentFilter = new IntentFilter(getPackageName() + ".action.PUSH_MESSAGE_RECEIVE");

        registerReceiver(mReceiver, intentFilter, getPackageName() +".permission.C2D_MESSAGE", null);

        registerReceiver(mBroadcastReceiver, new IntentFilter(getPackageName() + "." + PushManager.REGISTER_BROAD_CAST_ACTION));
    }

    public void unregisterReceivers(){
        //Unregister receivers on pause
        try{
            unregisterReceiver(mReceiver);
        }catch (Exception e){}

        try{
            unregisterReceiver(mBroadcastReceiver);
        }catch (Exception e){}
    }
}
