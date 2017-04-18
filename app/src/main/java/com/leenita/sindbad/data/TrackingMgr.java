package com.leenita.sindbad.data;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.leenita.sindbad.SindbadApp;
import com.leenita.sindbad.R;


public class TrackingMgr {



	private static TrackingMgr instance;
	private Tracker tracker;

	public static TrackingMgr getInstance() {

		if (instance == null)
			instance = new TrackingMgr();
		return instance;
	}

	public TrackingMgr() {
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(SindbadApp.getAppContext());
       	tracker = analytics.newTracker(R.xml.appsher_tracker);
	}

	public void onActivityStart(Activity activity) {
		try {
//            Class activiyuClass = activity.getClass();
//            if(activiyuClass = HomeActivity.class){
//
//            }

			tracker.setScreenName(activity.getLocalClassName());
			tracker.send(new HitBuilders.ScreenViewBuilder().build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendTrackingEvent(String event, String action, String label) {
		try {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(event)
                    .setAction(action)
                    .setLabel(label)
                    .build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendFragmentTracking(Fragment fragment) {
		try {

//			tracker.setScreenName(fragment.getLocalClassName());
//			tracker.send(new HitBuilders.ScreenViewBuilder().build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
