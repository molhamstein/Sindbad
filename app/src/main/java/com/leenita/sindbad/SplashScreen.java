package com.leenita.sindbad;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.facebook.appevents.AppEventsLogger;


public class SplashScreen extends BaseActivity{

	private final static int LOGIN_ACTIVITY_REQ_CODE = 3454 ;
	
	Handler handler;
	Runnable proceedRunnable = new Runnable() {
		@Override
		public void run() {
            goToMain();
            finish() ;
		}
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.leenita.sindbad.R.layout.activity_splash);
		handler = new Handler() ;
		handler.postDelayed(proceedRunnable, 1000);
		initPushNotifications();
		AppEventsLogger.activateApp(this);
		SindbadApp.requestLastUserKnownLocation();
	}

	@Override
	protected void onStop() {
		super.onStop();
		//SindbadApp.disconnectGoogleApiClient();
	}

	private void goToMain(){
		Intent i = new Intent(this, HomeActivity.class);
		startActivity(i);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == LOGIN_ACTIVITY_REQ_CODE && resultCode == Activity.RESULT_OK){
			goToMain() ;
			finish();
		}else{
			finish() ;
		}
	}

	    @Override
	    public void onResume(){
	        super.onResume();
	        registerReceivers();
	    }
	     
	    @Override
	    public void onPause(){
	        super.onPause();
	        unregisterReceivers();
	    }

	    @Override
	    protected void onNewIntent(Intent intent){
	        super.onNewIntent(intent);
	        setIntent(intent);
	    }

	    private void showMessage(String message)
	    {
	        //Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	    }

}
