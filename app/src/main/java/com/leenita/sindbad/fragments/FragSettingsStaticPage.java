package com.leenita.sindbad.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leenita.sindbad.R;
import com.leenita.sindbad.SettingsActivity;

public class FragSettingsStaticPage extends Fragment{

    LayoutInflater inflater;
    TextView tvContent;

    SettingsActivity settingsActivity;

    public static FragSettingsStaticPage newInstance(int stringResId){
        FragSettingsStaticPage frag = new FragSettingsStaticPage();
        Bundle extras = new Bundle();
        extras.putInt("res", stringResId);
        frag.setArguments(extras);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_settings_static_page, container, false);
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
        tvContent = (TextView) getView().findViewById(R.id.tvContent);

        try {
            int resId = getArguments().getInt("res");
            tvContent.setText(resId);
        } catch (Exception ignored) {
        }
    }
}
