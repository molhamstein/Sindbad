package com.leenita.sindbad;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.leenita.sindbad.fragments.DiagBuyNoCredits;
import com.leenita.sindbad.model.SindProduct;

public class BaseDetailsActivity extends BaseActivity {

    private final static int PERMISSIONS_REQUEST_CALL = 300;
    private String phoneToCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCustomActionBar();
    }

    private void initCustomActionBar() {
        try {
//            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1)
//                getSupportActionBar().setHomeAsUpIndicator(R.drawable.res_actionbar_back_with_padding);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        } catch (Exception ignored) {
        }
    }

    public void makeCall(String phone) {
        //TODO need handling persmissions on android 6
        try {
            if (phone == null || phone.trim().isEmpty())
                return;

            phoneToCall = phone;

            String uri = "tel:" + phoneToCall.trim();
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse(uri));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(BaseDetailsActivity.this,
                        "android.permission.CALL_PHONE")
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BaseDetailsActivity.this,
                            new String[]{"android.permission.CALL_PHONE"},
                            PERMISSIONS_REQUEST_CALL);
                    return;
                }
            }
            startActivity(intent);
        }catch (Exception e){

        }
    }

    public void showMap(LatLng location) {
        //TODO
        // open Map Activity with the location param
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CALL: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall(phoneToCall);
                }
                return;
            }
        }
    }


    public void buyProduct(SindProduct item)
    {
        DiagBuyNoCredits diag = DiagBuyNoCredits.newInstance(DiagBuyNoCredits.DIAG_TYPE.NO_CREDITS_PRODUCT, item.getName());
        diag.show(getSupportFragmentManager(), null);
    }

}
