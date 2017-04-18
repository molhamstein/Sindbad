package com.leenita.sindbad;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.leenita.sindbad.data.DataStore;
import com.leenita.sindbad.data.PhotoProvider;
import com.leenita.sindbad.data.ServerResult;
import com.leenita.sindbad.fragments.DiagBuyNoCredits;
import com.leenita.sindbad.fragments.DiagQrCode;
import com.leenita.sindbad.fragments.FragAppMediaList;
import com.leenita.sindbad.fragments.FragOfferInfo;
import com.leenita.sindbad.model.SindOffer;

import org.json.JSONObject;

public class OfferDetailsActivity extends BaseDetailsActivity implements OnClickListener {

    private TextView tvName, tvBrand, tvOfferPrice, tvOldPrice, tvDesc;
    private ImageView ivLogo, ivCover, ivCall, ivMap;
    private OfferPagerAdapter adapter;
    private TabLayout tabLayout;
    private Toolbar mToolbar;
    private View vOldPriceLine, spinner;
    private ViewPager viewPager;

    private int REQ_CODE_LOGIN = 555;

    //data
    SindOffer item;
    private DataStore.DataRequestCallback apiCallbackGetOffer = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            showProgress(false);
            if (success) {
                if (result.isValid()) {
                    item = (SindOffer) result.get("offer");
                    displayOfferInfo(item);
                }
            } else {
                SindbadApp.displayCustomToast(R.string.err_check_connection);
            }
        }
    };

    private DataStore.DataRequestCallback apiCallbackBuyOffer = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            showProgress(false);
            if (result.getRequestStatusCode() < 600) {
                try {
                    if (result.getApiError() == null) {
                        boolean buySuccess = (boolean) result.get("buy");
                        if (buySuccess) {
                            item.setIsOwnedOffer(true);
                            updateBuyButton(item);
                        }
                    } else {
                        DiagBuyNoCredits diag = DiagBuyNoCredits.newInstance(DiagBuyNoCredits.DIAG_TYPE.NO_CREDITS_OFFER, item.getTitle());
                        diag.show(getSupportFragmentManager(), null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                SindbadApp.displayCustomToast(R.string.err_check_connection);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_details);
        Intent intent = getIntent();
        init();
        try {
            JSONObject jsonCollection = new JSONObject(intent.getStringExtra("item"));
            item = SindOffer.fromJson(jsonCollection);
            displayOfferInfo(item);
        } catch (Exception ignored) {
            // TODO this case will cause issues as init will be called while item is null
        }
    }

    private void init() {
        ivLogo = (ImageView) findViewById(R.id.ivLogo);
        ivCover = (ImageView) findViewById(R.id.ivCover);
        ivCall = (ImageView) findViewById(R.id.ivCall);
        ivMap = (ImageView) findViewById(R.id.ivLocation);
        tvName = (TextView) findViewById(R.id.tvName);
        tvOfferPrice = (TextView) findViewById(R.id.tvOfferPrice);
        tvOldPrice = (TextView) findViewById(R.id.tvOldPrice);
        vOldPriceLine = findViewById(R.id.vLine);
        tvBrand = (TextView) findViewById(R.id.tvBrand);
        tvDesc = (TextView) findViewById(R.id.tvShortDesc);
        spinner = findViewById(R.id.spinner);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);

        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setNavigationOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                }
        );
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        ivCall.setOnClickListener(this);
        ivMap.setOnClickListener(this);

        showProgress(false);
    }


    private void displayOfferInfo(SindOffer offer) {
        if (offer == null)
            return;

        adapter = new OfferPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setTabsFromPagerAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        if (offer.getBrand() != null) {
            tvBrand.setText(offer.getBrand().getName());
            PhotoProvider.getInstance().displayPhotoNormal(offer.getBrand().getLogo(), ivLogo);
        }
        if (offer.getTitle() != null && !offer.getTitle().isEmpty())
            tvName.setText(offer.getTitle());

        tvDesc.setText(offer.getDescription());
        updateBuyButton(offer);

        tvOfferPrice.setOnClickListener(this);
        PhotoProvider.getInstance().displayCoverPhoto(offer.getCover(), ivCover);
    }

    private void updateBuyButton(SindOffer offer) {
        if (offer.isOwnedOffer()) {
            vOldPriceLine.setVisibility(View.GONE);
            tvOldPrice.setText(String.format(getString(R.string.offer_details_bought), offer.getDiscount()));
            tvOfferPrice.setText(getString(R.string.offer_details_get_qr));
            tvOfferPrice.setOnClickListener(this);
        } else {
            vOldPriceLine.setVisibility(View.VISIBLE);

            if (offer.getProduct() != null) {
                tvOldPrice.setText(offer.getProduct().getPrice() + "$");
            }
            tvOfferPrice.setText(offer.getDiscount() + "$");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_LOGIN && resultCode == RESULT_OK)
            buyOffer(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_offer_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.home:
            case android.R.id.home:
                finish();
                break;
            case R.id.action_share:
                DataStore.getInstance().shareOffer(this, item);
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivCall:
                if (item.getBrand() != null)
                    makeCall(item.getBrand().getPhoneNumber());
                break;
            case R.id.ivLocation:
                showMap(item.getBrand().getCoordinates());
                break;
            case R.id.tvOfferPrice:
                if (item.isOwnedOffer())
                    generateQRCode();
                else
                    buyOffer(item);
                break;
        }
    }

    private void generateQRCode() {
        DiagQrCode dialog = DiagQrCode.newInstance(item.getTitle(), item.getTitle());
        dialog.show(getSupportFragmentManager(), null);
    }

    public class OfferPagerAdapter extends FragmentPagerAdapter {

        FragOfferInfo fragInfo;
        FragAppMediaList fragMedia;

        boolean isDataReady = false;

        public OfferPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
            if (item == null)
                item = new SindOffer();

            fragInfo = FragOfferInfo.newInstance(item);
            fragMedia = FragAppMediaList.newInstance(item.getMedia());
        }

        public void updateAdapter(SindOffer offer) {
            try {
                if (fragMedia != null)
                    fragMedia.updateData(offer.getMedia());
                else
                    fragMedia = FragAppMediaList.newInstance(offer.getMedia());

                notifyDataSetChanged();
                tabLayout.setTabsFromPagerAdapter(adapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public Fragment getItem(int index) {
            switch (index) {
                case 0:
                    return fragMedia;
                case 1:
                    return fragInfo;
            }
            return null;
        }

        @Override
        public int getCount() {
            // get item count - equal to number of tabs
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            try {
                switch (position) {
                    case 0:
                        title = getString(R.string.offer_details_section_title_media);
                        break;
                    case 1:
                        title = getString(R.string.offer_details_section_title_info);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return title;
        }

    }

    public void showProgress(boolean show) {
        if (show)
            spinner.setVisibility(View.VISIBLE);
        else
            spinner.setVisibility(View.GONE);
    }

    public void buyOffer(SindOffer item) {
        if (DataStore.getInstance().isUserLoggedIn()) {
            DataStore.getInstance().buyOffer(item.getId(), apiCallbackBuyOffer);
        } else {
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, REQ_CODE_LOGIN);
        }
    }
}



