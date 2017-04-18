package com.leenita.sindbad.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.HashMap;

import com.leenita.sindbad.SindbadApp;
import com.leenita.sindbad.R;
import com.leenita.sindbad.SettingsActivity;
import com.leenita.sindbad.data.DataStore;
import com.leenita.sindbad.data.DataStore.DataRequestCallback;
import com.leenita.sindbad.data.ServerResult;
import com.leenita.sindbad.model.AppUser;


// TODO need full refacturing,this settings screen is taken from an different app
public class FragSettingsProfile extends Fragment implements OnCheckedChangeListener {

    LayoutInflater inflater;
    EditText etEmail;
    EditText etJob;
    CheckBox cbNotificationsMentions;
    CheckBox cbNotificationsNewApps;
    CheckBox cbNotificationsUpvotes;

    SettingsActivity settingsActivity;

    Dialog diagLoading;

    DataRequestCallback callbacUserUpdate = new DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult data, boolean success) {
            try {
                diagLoading.hide();
                if(!success)
                    SindbadApp.displaySnackBar(R.string.err_check_connection);
            }catch (Exception ignored){
                ignored.printStackTrace();
            }
        }
    };

    OnEditorActionListener listenerEmail = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            String text = textView.getText().toString();
            if(text != null && !text.isEmpty()) {
                //DataStore.getInstance().updateUserEmail(text, callbacUserUpdate);
                diagLoading.show();
            }
            return false;
        }
    };

    OnEditorActionListener listenerJob = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_GO)) {
                String text = textView.getText().toString();
                if(text != null && !text.isEmpty()) {
                    //DataStore.getInstance().updateUserDesc(text, callbacUserUpdate);
                    diagLoading.show();
                }
                return  true;
            }
            return  false;
            //
        }
    };

    public static FragSettingsProfile newInstance(){
        FragSettingsProfile frag = new FragSettingsProfile();
        Bundle extras = new Bundle();
        frag.setArguments(extras);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_settings_profile, container, false);
        this.inflater = inflater;
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        updateUi();
    }

    @Override
    public void onAttach(Activity activity) {
        try{
            super.onAttach(activity);
            settingsActivity = (SettingsActivity) activity;
        }catch (Exception ignored){}
    }

    private void init() {
        cbNotificationsMentions = (CheckBox) getView().findViewById(R.id.cbMentions);
        cbNotificationsNewApps = (CheckBox) getView().findViewById(R.id.cbNewApps);
        cbNotificationsUpvotes = (CheckBox) getView().findViewById(R.id.cbUpvotes);
        etEmail = (EditText) getView().findViewById(R.id.etEmail);
        etJob = (EditText) getView().findViewById(R.id.etJob);

        cbNotificationsMentions.setOnCheckedChangeListener(this);
        cbNotificationsNewApps.setOnCheckedChangeListener(this);
        cbNotificationsUpvotes.setOnCheckedChangeListener(this);
        etEmail.setOnEditorActionListener(listenerEmail);
        etJob.setOnEditorActionListener(listenerJob);

        diagLoading = SindbadApp.getNewLoadingDilaog(getActivity());

        //initial State
        AppUser me = DataStore.getInstance().getMe();
        if(me != null) {
            etJob.setText(me.getCompany());
            etEmail.setText(me.getEmail());
        }
    }

    private void updateUi(){

        cbNotificationsMentions.setOnCheckedChangeListener(null);
        cbNotificationsNewApps.setOnCheckedChangeListener(null);
        cbNotificationsUpvotes.setOnCheckedChangeListener(null);

        AppUser me = DataStore.getInstance().getMe();
        if(me != null){
            etEmail.setText(me.getEmail());
            etJob.setText(me.getCompany());

            cbNotificationsMentions.setChecked(me.isEnableMentionNotifications());
            cbNotificationsUpvotes.setChecked(me.isEnableFollowedUsersUpvotesNotifications());
            cbNotificationsNewApps.setChecked(me.isEnableAddedAppsNotifications());
        }

        cbNotificationsMentions.setOnCheckedChangeListener(this);
        cbNotificationsNewApps.setOnCheckedChangeListener(this);
        cbNotificationsUpvotes.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        boolean enableMnetions = cbNotificationsMentions.isChecked();
        boolean enableNewApps = cbNotificationsNewApps.isChecked();
        boolean enableUpvotes = cbNotificationsUpvotes.isChecked();
        //DataStore.getInstance().updateNotificationsSettings(enableMnetions, enableNewApps, enableUpvotes, callbacUserUpdate);
        diagLoading.show();
    }
}
