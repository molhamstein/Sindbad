package com.leenita.sindbad;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;

import com.leenita.sindbad.BaseActivity;
import com.leenita.sindbad.R;
import com.leenita.sindbad.fragments.FragMap;
import com.leenita.sindbad.model.SindBrand;

import org.json.JSONObject;

/**
 * displays the nearby BPs the maps loads only the BPs that are inside the visible bounds of the map
 */

public class MapActivity extends BaseActivity implements View.OnClickListener{


    Fragment fragment;
    FragmentManager fragmentManager;
    private static String TAG_MAIN_MAP_FRAG = "mainMapFrag";

    TextView tvTitle;

    // temp Data Holder
    SindBrand brand;

    public static Bundle getLauncherBundle(FragMap.MAP_TYPE type, SindBrand brand){

        Bundle extras = new Bundle();
        if(brand == null)
            brand = new SindBrand();

        extras.putString("brand", brand.getJsonString());
        extras.putInt("type", type.ordinal());
        return extras;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        init();
        resolveIntentExtra(getIntent().getExtras());

    }

    private void resolveIntentExtra(Bundle extras) {
        try{
            int type = extras.getInt("type");
            FragMap.MAP_TYPE mapType = FragMap.MAP_TYPE.values()[type];
            switch (mapType)
            {
                case BRAND:
                    String jsonStr = extras.getString("brand");
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    brand = SindBrand.fromJson(jsonObject);
                    showBrandOnMap();
                    break;
            }
        }catch (Exception e){}
    }

    private void init(){
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setOnClickListener(this);
    }

    private void showBrandOnMap()
    {
        fragmentManager = getSupportFragmentManager();
        FragMap fragMap = FragMap.newInstance(FragMap.MAP_TYPE.BRAND , brand);
        fragment = fragMap;
        fragmentManager.beginTransaction()
                .add(R.id.flMainFragmentContainer, fragment, TAG_MAIN_MAP_FRAG)
                .commit();

        tvTitle.setText(brand.getName());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id)
        {
            case R.id.tvTitle:
                finish();
                break;
        }

    }


}
