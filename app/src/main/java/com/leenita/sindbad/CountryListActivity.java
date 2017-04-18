package com.leenita.sindbad;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.leenita.sindbad.view.TextViewCustomFont;
import com.sithagi.countrycodepicker.CountryPicker;
import com.sithagi.countrycodepicker.CountryPickerListener;

import java.util.Locale;



public class CountryListActivity extends BaseActivity implements View.OnClickListener {

    int onStartCount = 0;
    ImageView ivBack;
    TextView tvFragTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_list);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        CountryPicker picker = new CountryPicker();
        transaction.replace(R.id.vCountryListContainer, picker);
        transaction.commit();

        picker.setListener(new CountryPickerListener() {
            @Override
            public void onSelectCountry(String name, String code, String dialCode) {
                Locale l = new Locale("", code);
                name = l.getDisplayCountry();
                Intent dataBackIntent = new Intent();
                dataBackIntent.putExtra("name", code);
                dataBackIntent.putExtra("code", dialCode);
                setResult(RESULT_OK, dataBackIntent);
                finish();
            }
        });

        //SindbadApp.hideKeyboardWhenTouchOut(this, findViewById(R.id.vCountrylistActivity));
        initCustomActionBar();

    }

    private void initCustomActionBar() {
        View mCustomView = findViewById(R.id.customeActionBar);
        tvFragTitle = (TextViewCustomFont) mCustomView
                .findViewById(R.id.tvTitle);
        ivBack = (ImageView) mCustomView.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.ivBack:
                this.finish();
                break;
        }
    }

}
