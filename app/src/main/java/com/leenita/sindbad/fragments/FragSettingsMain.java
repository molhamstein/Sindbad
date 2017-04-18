package com.leenita.sindbad.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.leenita.sindbad.R;
import com.leenita.sindbad.SettingsActivity;
import com.leenita.sindbad.SettingsActivity.SETTINGS_FRAGMENT_TYPE;
import com.leenita.sindbad.data.DataStore;

public class FragSettingsMain extends Fragment implements  OnClickListener{

    LayoutInflater inflater;
    View btnProfileSettings, btnContactUs, btnFollowUs, btnPrivacyPolicy,btnTermsOfService;

    SettingsActivity settingsActivity;

    public static FragSettingsMain newInstance(){
        FragSettingsMain frag = new FragSettingsMain();
        Bundle extras = new Bundle();
        frag.setArguments(extras);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_settings_main, container, false);
        this.inflater = inflater;
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void onAttach(Activity activity) {
        try{
            super.onAttach(activity);
            settingsActivity = (SettingsActivity) activity;
        }catch (Exception ignored){}
    }

    private void init() {
        btnContactUs = getView().findViewById(R.id.btnContactUs);
        btnFollowUs = getView().findViewById(R.id.btnFollowUs);
        btnPrivacyPolicy = getView().findViewById(R.id.btnPrivacyPolicy);
        btnProfileSettings = getView().findViewById(R.id.btnProfileSettings);
        btnTermsOfService = getView().findViewById(R.id.btnTermsOfService);
        View btnlogout = getView().findViewById(R.id.btnLogout);

        btnTermsOfService.setOnClickListener(this);
        btnProfileSettings.setOnClickListener(this);
        btnPrivacyPolicy.setOnClickListener(this);
        btnFollowUs.setOnClickListener(this);
        btnContactUs.setOnClickListener(this);
        btnlogout.setOnClickListener(this);
    }

    private void followUs(){
        try{
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/follow?screen_name=saedgh"));
            startActivity(i);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnProfileSettings:
                if(settingsActivity != null)
                    settingsActivity.switchSection(SETTINGS_FRAGMENT_TYPE.SETTINGS_PROFILE);
                break;
            case R.id.btnContactUs:
                break;
            case R.id.btnFollowUs:
                followUs();
                break;
            case R.id.btnTermsOfService:
                if(settingsActivity != null)
                    settingsActivity.switchSection(SETTINGS_FRAGMENT_TYPE.SETTINGS_TERMS_OF_SERVICE);
                break;
            case R.id.btnPrivacyPolicy:
                if(settingsActivity != null)
                    settingsActivity.switchSection(SETTINGS_FRAGMENT_TYPE.SETTINGS_PRIVACY_POLICY);
                break;
            case R.id.btnLogout:
                DataStore.getInstance().logout();
                if(settingsActivity != null)
                    settingsActivity.finish();
                break;
        }
    }
}
