package com.leenita.sindbad;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.florent37.viewanimator.AnimationListener.Stop;
import com.github.florent37.viewanimator.ViewAnimator;
import com.leenita.sindbad.adapters.SmartFragmentStatePagerAdapter;
import com.leenita.sindbad.callbacks.HomeCallbacks;
import com.leenita.sindbad.data.DataStore;
import com.leenita.sindbad.data.DataStore.DataStoreUpdateListener;
import com.leenita.sindbad.fragments.FragOfferCard;
import com.leenita.sindbad.model.SindCategory;
import com.leenita.sindbad.model.SindOffer;
import com.leenita.sindbad.model.AppUser;
import com.leenita.sindbad.model.SindProduct;
import com.leenita.sindbad.view.ViewCategoryRow;

import java.util.ArrayList;


public class HomeActivity extends BaseActivity implements HomeCallbacks, OnClickListener, DataStoreUpdateListener {

    private enum VIEW_STATUS {HEADER_OPEN, HEADER_COLLAPSED}
    private enum DISPLAYED_DATA_TYPE {OFFERS, PRODUCTS}

    private static final int VIEW_STATUS_LIMIT = 20;
    // dont edit those values unless you edit the corresponding values in the XML file
    private static int HEADER_SHIFT = 20;
    private static int HEADER_HEIGHT_DP = 190;
    private static int actionBarSize = 0;

    private Toolbar mToolbar;
    private View vContainer;
    private View vHeader;
    private View vBody;
    private ScrollView svMain;

    OffersAdapter adapterNearbyOffers;
    OffersAdapter adapterFollowedOffers;
    OffersAdapter adapterPickedForYouOffers;
//    ProductsAdapter adapterNearbyProducts;
//    ProductsAdapter adapterFollowedProducts;
//    ProductsAdapter adapterPickedForYouProducts;


    private TextView tvUsername;
    private TextView tvBalance;
    private ImageView ivUser;
    private TextView tvSlogan;
    private View vBasicInfoContainer;
    private ViewGroup vCategoriesContainer;
    private ViewPager vpOffers;
    private ViewPager vpProducts;
    private ViewPager vpFollowed;
    private MenuItem menuItemExchangeCredits;
    private TextView btnProducts;
    private TextView btnOffers;

    View spinner;

    /// vertical swipe Detection
    private static final int SWIPE_MIN_DISTANCE = 30;
    private static final int SWIPE_MAX_OFF_PATH = 100;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    VIEW_STATUS currentViewStatus;
    DISPLAYED_DATA_TYPE currentDisplayedDataType;
    boolean isInTransitionalState;
    Stop onTransitionStopListener = new Stop() {
        @Override
        public void onStop() {
            isInTransitionalState = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.leenita.sindbad.R.layout.activity_home);
        init();
        initCustomActionBar();
        DataStore.getInstance().triggerDataUpdate();
        updateHeader();
        updateBody();
        switchDisplayedDataType(DISPLAYED_DATA_TYPE.OFFERS);
        playIntroAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.leenita.sindbad.R.menu.menu_main, menu);
        menuItemExchangeCredits = menu.findItem(R.id.action_exchange);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case com.leenita.sindbad.R.id.action_search:
                openSearchActivity();
                break;
            case com.leenita.sindbad.R.id.action_profile:
                openProfile();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DataStore.getInstance().addUpdateBroadcastListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataStore.getInstance().removeUpdateBroadcastListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void init() {
        //llLoading = (LinearLayout) findViewById(com.leenita.sindbad.R.id.llLoading);
        //tvLoadingMsg = (TextViewCustomFont) findViewById(R.id.tvLoadingMsg);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        vHeader = findViewById(R.id.vHeaderContainer);
        vBody = findViewById(R.id.vBodyContainer);
        vContainer = findViewById(R.id.vContainer);
        svMain = (ScrollView) findViewById(R.id.svMain);
        spinner = findViewById(R.id.spinner);
        vpOffers = (ViewPager) findViewById(R.id.vpOffers);
        vpProducts = (ViewPager) findViewById(R.id.vpProducts);
        vpFollowed = (ViewPager) findViewById(R.id.vpFollowed);
        tvUsername = (TextView) findViewById(R.id.tvDesc);
        tvBalance = (TextView) findViewById(R.id.tvCreator);
        ivUser = (ImageView) findViewById(R.id.ivCreator);
        tvSlogan = (TextView) findViewById(R.id.tvSlogan);
        vBasicInfoContainer = findViewById(R.id.vBasicInfoContainer);
        vCategoriesContainer = (ViewGroup) findViewById(R.id.vCategoriesContainer);
        btnOffers = (TextView) findViewById(R.id.btnOffers);
        btnProducts = (TextView) findViewById(R.id.btnProducts);

        adapterNearbyOffers = new OffersAdapter();
        adapterFollowedOffers = new OffersAdapter();
        adapterPickedForYouOffers = new OffersAdapter();

//        adapterPickedForYouProducts = new ProductsAdapter();
//        adapterFollowedProducts = new ProductsAdapter();
//        adapterFollowedProducts = new ProductsAdapter();

        currentDisplayedDataType = DISPLAYED_DATA_TYPE.OFFERS;
        adapterPickedForYouOffers.updateOffersAdapter(DataStore.getInstance().getArrayPickedFroYouOffers());
        adapterNearbyOffers.updateOffersAdapter(DataStore.getInstance().getArrayNearbyOffers());
        adapterFollowedOffers.updateOffersAdapter(DataStore.getInstance().getArrayNearbyOffers());

        vpOffers.setAdapter(adapterNearbyOffers);
        vpProducts.setAdapter(adapterPickedForYouOffers);
        vpFollowed.setAdapter(adapterFollowedOffers);

        vpOffers.setPageMargin(SindbadApp.getPXSize(15));
        vpProducts.setPageMargin(SindbadApp.getPXSize(15));
        vpFollowed.setPageMargin(SindbadApp.getPXSize(15));

        setSupportActionBar(mToolbar);
        vpOffers.requestDisallowInterceptTouchEvent(true);
        vpProducts.requestDisallowInterceptTouchEvent(false);
        vpFollowed.requestDisallowInterceptTouchEvent(false);

        // scroll view
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        svMain.setOnTouchListener(gestureListener);

        isInTransitionalState = false;
        // get actionBar size
        final TypedArray styledAttributes = this.getTheme().obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
        actionBarSize = SindbadApp.getDPSize((int) styledAttributes.getDimension(0, 0));
        styledAttributes.recycle();

        HEADER_HEIGHT_DP = SindbadApp.getDPSize((int)getResources().getDimension(R.dimen.cover_height)) ;

        //initial state
        spinner.setVisibility(View.GONE);
        if(menuItemExchangeCredits != null)
            menuItemExchangeCredits.setVisible(false);

        btnOffers.setTextColor(ContextCompat.getColor(this,R.color.app_theme2));
        btnProducts.setTextColor(ContextCompat.getColor(this, R.color.txt_gray_light));

        tvUsername.setOnClickListener(this);
        btnOffers.setOnClickListener(this);
        btnProducts.setOnClickListener(this);
    }

    private void initCustomActionBar() {
        try {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.res_actionbar_logo);
            //getSupportActionBar().setElevation(0);
        } catch (Exception ignored) {
        }
    }

    private void playIntroAnimation() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        View txtNearby = findViewById(R.id.txtNearby);
        View txtPicked = findViewById(R.id.txtPickedForYou);

        vHeader.setTranslationY(SindbadApp.getPXSize(-300));
        vpOffers.setTranslationX(screenWidth);
        vpProducts.setTranslationX(screenWidth);
        txtNearby.setTranslationX(screenWidth);
        txtPicked.setTranslationX(screenWidth);
        svMain.setTranslationY(SindbadApp.getPXSize(HEADER_HEIGHT_DP - actionBarSize));
        // Animate in
        ViewAnimator.animate(vHeader).dp().startDelay(800).alpha(0, 1).translationY(-300, 0).duration(400)
                //.interpolator(new OvershootInterpolator())
                .start();

        ViewAnimator.animate(vpOffers).startDelay(200).translationX(screenWidth, 0).duration(400)
                .interpolator(new OvershootInterpolator())
                .start();

        ViewAnimator.animate(txtNearby).startDelay(200).translationX(screenWidth, 0).duration(500)
                .interpolator(new OvershootInterpolator())
                .start();

        ViewAnimator.animate(vpProducts).startDelay(400).translationX(screenWidth, 0).duration(400)
                .interpolator(new OvershootInterpolator())
                .start();

        ViewAnimator.animate(txtPicked).startDelay(400).translationX(screenWidth, 0).duration(500)
                .interpolator(new OvershootInterpolator())
                .start();

        currentViewStatus = VIEW_STATUS.HEADER_OPEN;
    }

    private void switchDisplayedDataType(DISPLAYED_DATA_TYPE newDisplayDataType){
        if(newDisplayDataType == currentDisplayedDataType)
            return;
        switch (newDisplayDataType){
            case OFFERS:
                btnOffers.setTextColor(ContextCompat.getColor(this,R.color.app_theme2));
                btnProducts.setTextColor(ContextCompat.getColor(this,R.color.txt_gray_light));
                break;
            case PRODUCTS:
                btnOffers.setTextColor(ContextCompat.getColor(this,R.color.txt_gray_light));
                btnProducts.setTextColor(ContextCompat.getColor(this,R.color.app_theme2));
                break;
        }
        currentDisplayedDataType = newDisplayDataType;
        updateBody();
    }

    private void switchViewStatus(VIEW_STATUS newState){

        if(currentViewStatus == newState)
            return;

        isInTransitionalState = true;
        switch (newState){
            case HEADER_OPEN:
                Log.d("vState", " opening");
                // Animate in
                ViewAnimator.animate(vHeader).dp().translationY(0).duration(500)
                        //.interpolator(new OvershootInterpolator())
                        .start();
                ViewAnimator.animate(svMain).dp().translationY(0, (HEADER_HEIGHT_DP - actionBarSize)).duration(400)
                        //.interpolator(new OvershootInterpolator())
                        .onStop(onTransitionStopListener)
                        .start();
                tvSlogan.setVisibility(View.VISIBLE);
                menuItemExchangeCredits.setVisible(false);
                //invalidateOptionsMenu();
                break;
            case HEADER_COLLAPSED:
                Log.d("vState", " collapsing");
                ViewAnimator.animate(vHeader).dp().translationY(-(HEADER_HEIGHT_DP - actionBarSize) + HEADER_SHIFT).duration(500)
                        //.interpolator(new OvershootInterpolator())
                        .start();
                ViewAnimator.animate(svMain).dp().translationY((HEADER_HEIGHT_DP - actionBarSize), 0).duration(400)
                        //.interpolator(new OvershootInterpolator())
                        .onStop(onTransitionStopListener)
                        .start();
                tvSlogan.setVisibility(View.INVISIBLE);
                menuItemExchangeCredits.setVisible(true);
                //invalidateOptionsMenu();
                break;
        }
        currentViewStatus = newState;
    }

    private void updateHeader() {
        if (DataStore.getInstance().isUserLoggedIn()) {
            vBasicInfoContainer.setVisibility(View.VISIBLE);
            tvSlogan.setVisibility(View.GONE);
            AppUser me = DataStore.getInstance().getMe();
            tvBalance.setText("Balance: 123$");
            tvUsername.setText("---------");
        } else {
            vBasicInfoContainer.setVisibility(View.GONE);
            tvSlogan.setVisibility(View.VISIBLE);
        }
    }

    private void updateBody() {
        try {

            if(currentDisplayedDataType == DISPLAYED_DATA_TYPE.OFFERS) {
                adapterPickedForYouOffers.updateOffersAdapter(DataStore.getInstance().getArrayPickedFroYouOffers());
                adapterNearbyOffers.updateOffersAdapter(DataStore.getInstance().getArrayNearbyOffers());
                adapterFollowedOffers.updateOffersAdapter(DataStore.getInstance().getArrayNearbyOffers());
            }else{
                adapterPickedForYouOffers.updateProductAdapter(DataStore.getInstance().getArrayPickedFroYouProducts());
                adapterNearbyOffers.updateProductAdapter(DataStore.getInstance().getArrayNearbyProducts());
                adapterFollowedOffers.updateProductAdapter(DataStore.getInstance().getArrayNearbyProducts());
            }

            // display categories list
            ArrayList<SindCategory> categories = DataStore.getInstance().getArrayCategories();
            if (categories != null) {
                vCategoriesContainer.removeAllViews();
                for (int i = 0; i < categories.size(); i++) {
                    ViewCategoryRow categoryRow = new ViewCategoryRow(this);
                    categoryRow.update(categories.get(i));
                    vCategoriesContainer.addView(categoryRow);
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onDataStoreUpdate() {
        // update only if it was initialized before
        if (adapterNearbyOffers != null) {
            updateBody();
        }
    }

    @Override
    public void onTabListScroll(int firstVisibleItemIndex, int previousFirstVisibleItemIndex) {

    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public void onLoginStateChange() {
        updateHeader();
    }


    private void openProfile() {
        if (DataStore.getInstance().isUserLoggedIn()) {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("user", DataStore.getInstance().getMe().getJsonObject().toString());
            startActivity(i);
        } else {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }
    }

    private void openSearchActivity() {
        Intent i = new Intent(this, SearchActivity.class);
        startActivity(i);
    }

    @Override
    public void showProgress(boolean show, int msg) {
//        if (show) {
//            llLoading.setVisibility(View.VISIBLE);
//            if (msg <= 0) {
//                tvLoadingMsg.setVisibility(View.GONE);
//            } else {
//                tvLoadingMsg.setText(msg);
//                tvLoadingMsg.setVisibility(View.VISIBLE);
//            }
//        } else {
//            llLoading.setVisibility(View.GONE);
//        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tvDesc:
                break;
            case R.id.btnProducts:
                switchDisplayedDataType(DISPLAYED_DATA_TYPE.PRODUCTS);
                break;
            case R.id.btnOffers:
                switchDisplayedDataType(DISPLAYED_DATA_TYPE.OFFERS);
                break;
        }
    }

    class OffersAdapter extends SmartFragmentStatePagerAdapter {
        ArrayList<SindOffer> arrayOffersData = new ArrayList<>();
        ArrayList<SindProduct> arrayProductsData = new ArrayList<>();

        public OffersAdapter() {
            super(getSupportFragmentManager());
        }

        public void updateOffersAdapter(ArrayList<SindOffer> offers) {
            try {
                if (offers == null || offers.isEmpty()) {
                    arrayOffersData.clear();
                } else {
                    arrayOffersData.clear();
                    arrayOffersData.addAll(offers);
                }
                notifyDataSetChanged();
            } catch (Exception e) {
            }
        }

        public void updateProductAdapter(ArrayList<SindProduct> offers) {
            try {
                if (offers == null || offers.isEmpty()) {
                    arrayProductsData.clear();
                } else {
                    arrayProductsData.clear();
                    arrayProductsData.addAll(offers);
                }
                notifyDataSetChanged();
            } catch (Exception e) {
            }
        }

        @Override
        public int getCount() {
            if(currentDisplayedDataType == DISPLAYED_DATA_TYPE.OFFERS) {
                if (arrayOffersData == null || arrayOffersData.isEmpty())
                    return 0;
                else
                    return arrayOffersData.size();
            }else{
                if (arrayProductsData == null || arrayProductsData.isEmpty())
                    return 0;
                else
                    return arrayProductsData.size();
            }
        }

        @Override
        public int getItemPosition(Object object) {
            try {
                if (object instanceof FragOfferCard) {
                    if(currentDisplayedDataType == DISPLAYED_DATA_TYPE.OFFERS) {
                        FragOfferCard frag = (FragOfferCard) object;
                        int pos = frag.getPageIndex();
                        SindOffer offer = arrayOffersData.get(pos);
                        frag.updateFrag(offer);
                        return pos;
                    }else{
                        FragOfferCard frag = (FragOfferCard) object;
                        int pos = frag.getPageIndex();
                        SindProduct offer = arrayProductsData.get(pos);
                        frag.updateFrag(offer);
                        return pos;
                    }
                } else
                    return super.getItemPosition(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return POSITION_NONE;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            if(currentDisplayedDataType == DISPLAYED_DATA_TYPE.OFFERS)
                return FragOfferCard.newInstance(position, arrayOffersData.get(position));
            return FragOfferCard.newInstance(position, arrayProductsData.get(position));
        }

//		@Override
//		public void destroyItem(ViewGroup container, int position, Object object) {
//			((ViewPager) container).removeView((View) object);
//		}
//
//		@Override
//		public boolean isViewFromObject(View view, Object object) {
//			return (view == (View) object);
//		}

        @Override
        public float getPageWidth(int position) {
            return 0.43F;
        }
    }

    class ProductsAdapter extends SmartFragmentStatePagerAdapter {
        ArrayList<SindProduct> arrayData = new ArrayList<>();

        public ProductsAdapter() {
            super(getSupportFragmentManager());
        }

        public void updateAdapter(ArrayList<SindProduct> offers) {
            try {
                if (offers == null || offers.isEmpty()) {
                    arrayData.clear();
                } else {
                    arrayData.clear();
                    arrayData.addAll(offers);
                }
                notifyDataSetChanged();
            } catch (Exception e) {
            }
        }


        @Override
        public int getCount() {
            if (arrayData == null || arrayData.isEmpty())
                return 0;
            else
                return arrayData.size();
        }

        @Override
        public int getItemPosition(Object object) {
            try {
                if (object instanceof FragOfferCard) {
                    FragOfferCard frag = (FragOfferCard) object;
                    int pos = frag.getPageIndex();
                    return pos;
                } else
                    return super.getItemPosition(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return POSITION_NONE;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return FragOfferCard.newInstance(position, arrayData.get(position));
        }

        @Override
        public float getPageWidth(int position) {
            return 0.43F;
        }
    }

    class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            try {
                if (!isInTransitionalState) {
                    // bottom to top swipe
                    if ((e1.getY() - e2.getY()) > SWIPE_MIN_DISTANCE && currentViewStatus == VIEW_STATUS.HEADER_OPEN) {
                        switchViewStatus(VIEW_STATUS.HEADER_COLLAPSED);
                        Log.d("vState", " swipe up");
                    }
                    //top to bottom
                    else if ((e2.getY() - e1.getY()) > SWIPE_MIN_DISTANCE && currentViewStatus == VIEW_STATUS.HEADER_COLLAPSED && svMain.getScrollY() < VIEW_STATUS_LIMIT) {
                        switchViewStatus(VIEW_STATUS.HEADER_OPEN);
                        Log.d("vState", " swipe down");
                    }
                }
            }catch (Exception e){
                Log.d("vState", " swipe exception" + e.getMessage());
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

    }

}
