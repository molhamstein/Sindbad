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
import com.leenita.sindbad.fragments.FragAppMediaList;
import com.leenita.sindbad.fragments.FragOfferInfo;
import com.leenita.sindbad.model.SindProduct;

import org.json.JSONObject;

public class ProdDetailsActivity extends BaseDetailsActivity implements OnClickListener {

    private TextView tvName, tvBrand, tvProdPrice, tvDesc;
    private ImageView ivLogo, ivCover, ivCall, ivMap;
    private ProdPagerAdapter adapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar mToolbar;
    private View spinner;

    //data
    SindProduct product;

    DataStore.DataRequestCallback apiCallbackGetProduct = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {

            showProgress(false);
            if (success) {
                if (result.isValid()) {
                    SindProduct prod = (SindProduct) result.get("product");
                    displayProdInfo(prod);
                }
            } else {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        init();
        Intent intent = getIntent();
        try {
            JSONObject jsonCollection = new JSONObject(intent.getStringExtra("item"));
            product = SindProduct.fromJson(jsonCollection);
            displayProdInfo(product);

        } catch (Exception ignored) {}
    }

    private void init() {
        ivLogo = (ImageView) findViewById(R.id.ivLogo);
        ivCover = (ImageView) findViewById(R.id.ivCover);
        ivCall = (ImageView) findViewById(R.id.ivCall);
        ivMap = (ImageView) findViewById(R.id.ivLocation);
        tvName = (TextView) findViewById(R.id.tvName);
        tvProdPrice = (TextView) findViewById(R.id.tvProdPrice);
        tvBrand = (TextView) findViewById(R.id.tvBrand);
        tvDesc = (TextView) findViewById(R.id.tvShortDesc);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        spinner = findViewById(R.id.spinner);
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
        tvProdPrice.setOnClickListener(this);
        showProgress(false);
    }


    private void displayProdInfo(SindProduct prod) {
        if (prod == null)
            return;

        adapter = new ProdPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setTabsFromPagerAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        if (prod.getBrand() != null) {
            tvBrand.setText(prod.getBrand().getName());
            PhotoProvider.getInstance().displayPhotoNormal(prod.getBrand().getLogo(), ivLogo);
        }
        if (prod.getName() != null && !prod.getName().isEmpty())
            tvName.setText(prod.getName());

        tvDesc.setText(prod.getDescription());
        if (prod.getPrice() != null) {
            tvProdPrice.setText(prod.getPrice() + "$");
        }
        PhotoProvider.getInstance().displayCoverPhoto(prod.getCover(), ivCover);
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
                DataStore.getInstance().shareProduct(this, product);
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivCall:
                if (product.getBrand() != null)
                    makeCall(product.getBrand().getPhoneNumber());
                break;
            case R.id.ivLocation:
                showMap(product.getBrand().getCoordinates());
                break;
            case R.id.tvProdPrice:
//                buyProduct(product);
                break;
        }
    }

    public class ProdPagerAdapter extends FragmentPagerAdapter {

        FragOfferInfo fragInfo;
        FragAppMediaList fragMedia;

        boolean isDataReady = false;

        public ProdPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);

            fragInfo = FragOfferInfo.newInstance(product);
            fragMedia = FragAppMediaList.newInstance(product.getMedia());
        }

        public void updateAdapter(SindProduct prod) {
            try {

                if (fragMedia != null)
                    fragMedia.updateData(product.getMedia());
                else
                    fragMedia = FragAppMediaList.newInstance(product.getMedia());

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
                        title = getString(R.string.prod_details_section_title_media);
                        break;
                    case 1:
                        title = getString(R.string.prod_details_section_title_info);
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

}



