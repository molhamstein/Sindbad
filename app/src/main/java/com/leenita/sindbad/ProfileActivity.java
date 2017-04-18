package com.leenita.sindbad;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
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

import org.json.JSONObject;

import com.leenita.sindbad.FollowersListActivity.FOLLOWERS_FRAGMENT_TYPE;
import com.leenita.sindbad.data.DataStore;
import com.leenita.sindbad.data.DataStore.DataRequestCallback;
import com.leenita.sindbad.data.DataStore.DataStoreUpdateListener;
import com.leenita.sindbad.data.PhotoProvider;
import com.leenita.sindbad.data.ServerResult;
import com.leenita.sindbad.data.TrackingMgr;
import com.leenita.sindbad.model.AppUser;
import com.leenita.sindbad.model.AppUserProfile;

public class ProfileActivity extends BaseActivity implements OnClickListener, DataStoreUpdateListener {

    // Need this to link with the Snackbar
    private CoordinatorLayout mCoordinator;
    //Need this to set the title of the app bar
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;
    private TextView tvDesc, tvCreator;
    private ImageView ivCreator, ivCover;
    private View vNoDataPlaceHolder;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private View spinner;

    private TextView tvFollowingCount, tvFollowersCount, btnFollow;

    private TabsPagerAdapter adapter;

    //data
    AppUser user;
    AppUserProfile userProfile;

    DataRequestCallback callbackGetApps = new DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult data, boolean success) {
            try {
                spinner.setVisibility(View.GONE);
                if (success) {
                    AppUserProfile apps = (AppUserProfile) data.get("profile");
                    onDataReceived(apps);
                    //viewPager.setCurrentItem(4);
                    vNoDataPlaceHolder.setVisibility(View.GONE);
                } else {
                    vNoDataPlaceHolder.setVisibility(View.VISIBLE);
                }
            } catch (Exception ignored) {
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        try {
            JSONObject jsonUser = new JSONObject(getIntent().getStringExtra("user"));
            user = AppUser.fromJson(jsonUser);
        } catch (Exception ignored) {}

        init();
        initCustomActionBar();
        displayBasicInfo(user);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO reload user data on resume
        // DataStore.getInstance().getUserProfile(user, callbackGetApps);
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
    public void onDataStoreUpdate() {}

    @Override
    public void onLoginStateChange() {
        if(!DataStore.getInstance().isUserLoggedIn())
            finish();
        else{
            displayBasicInfo(DataStore.getInstance().getMe());
        }
    }

    private void init() {
        mCoordinator = (CoordinatorLayout) findViewById(R.id.root_coordinator);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        ivCreator = (ImageView) findViewById(R.id.ivCreator);
        ivCover = (ImageView) findViewById(R.id.ivCover);
        btnFollow = (TextView) findViewById(R.id.btnFollow);
        tvDesc = (TextView) findViewById(R.id.tvDesc);
        tvCreator = (TextView) findViewById(R.id.tvCreator);
        vNoDataPlaceHolder = findViewById(R.id.vNoDataPlaceHolder);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        spinner = findViewById(R.id.spinner);
        tvFollowersCount = (TextView) findViewById(R.id.tvFollowers);
        tvFollowingCount = (TextView) findViewById(R.id.tvFollowing);

        View btnFollowing = findViewById(R.id.btnFollowing);
        View btnFollowers  =findViewById(R.id.btnFollowers);

        btnFollowers.setOnClickListener(this);
        btnFollowing.setOnClickListener(this);

        setSupportActionBar(mToolbar);

        adapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout.setTabsFromPagerAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        vNoDataPlaceHolder.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);
    }

    private void displayBasicInfo(AppUser user){

        tvCreator.setText(user.getDisplayName());
        tvDesc.setText(user.getCompany());
        PhotoProvider.getInstance().displayProfilePicture(user.getProfileImage(), ivCreator);
        PhotoProvider.getInstance().displayCoverPhoto(user.getCoverImage(), ivCover);
        if(DataStore.getInstance().isUserLoggedIn() && DataStore.getInstance().getMe().getId().equals(user.getId())){
            btnFollow.setBackgroundResource(R.drawable.shape_rounded_follow);
            btnFollow.setTextColor(getResources().getColor(android.R.color.white));
            btnFollow.setText(R.string.profile_my_page);
        }else {
            if (user.getIsFollowing()) {
                btnFollow.setBackgroundResource(R.drawable.shape_rounded_unfollow);
                btnFollow.setTextColor(getResources().getColor(R.color.twitter));
                btnFollow.setText(R.string.profile_unfollow);
            } else {
                btnFollow.setBackgroundResource(R.drawable.shape_rounded_follow);
                btnFollow.setTextColor(getResources().getColor(android.R.color.white));
                btnFollow.setText(R.string.profile_follow);
            }
            btnFollow.setOnClickListener(this);
        }
        //mCollapsingToolbarLayout.setTitle(user.getTitle());

        /// followers/following
        if(userProfile != null) {
            tvFollowersCount.setText(String.valueOf(userProfile.getFollowersCount()));
            tvFollowingCount.setText(String.valueOf(userProfile.getFollowingCount()));
        }
    }

    // TODO follow/unfollow brand
    private void toggleFollow(){
//        boolean result = DataStore.getInstance().toggleFollow(this, user, null);
//        if(result){// vote action success
//            displayBasicInfo(user);
//            if(user.getIsFollowing())
//                TrackingMgr.getInstance().sendTrackingEvent("Subscribe","Follow","UserProfile");
//            else
//                TrackingMgr.getInstance().sendTrackingEvent("Subscribe","UnFollow","UserProfile");
//        }
    }

    private void initCustomActionBar() {
        try {

            mToolbar.setNavigationIcon(R.drawable.ic_back);
            mToolbar.setNavigationOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }
            );
        } catch (Exception ignored) {}
    }

    private void onDataReceived(AppUserProfile profile) {
        try {
            this.userProfile = profile;
            this.user = profile.getUser();
            displayBasicInfo(user);
            adapter.updateAdapter(profile);
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //show menu options only if this is my profile
        if(DataStore.getInstance().isUserLoggedIn() && DataStore.getInstance().getMe().getId().equals(user.getId()))
            getMenuInflater().inflate(R.menu.menu_profile, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
            finish();
//        else if( id == R.id.action_settings)
//            openSettings();

        // Note action_notification wont be triggered here as it has a custom ActionView so
        // we setup a OnMenuItemClick for it in onCreateOptionsMenu

        return super.onOptionsItemSelected(item);
    }

    private void openSettings(){
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    private void openFriendsList(FOLLOWERS_FRAGMENT_TYPE type){
        Intent i = new Intent(this, FollowersListActivity.class);
        i.putExtra("userId", user.getId());
        i.putExtra("type", type);
        startActivity(i);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnFollow:
                toggleFollow();
                break;
            case R.id.btnFollowers:
                openFriendsList(FOLLOWERS_FRAGMENT_TYPE.FOLLOWERS);
                break;
            case R.id.btnFollowing:
                openFriendsList(FOLLOWERS_FRAGMENT_TYPE.FOLLOWING);
                break;
        }
    }

    public class TabsPagerAdapter extends FragmentPagerAdapter {

//        FragAppMediaList fragUpvotedApps;
//        FragAppMediaList fragCollections;
//        FragAppMediaList fragSuggestedApps;

        boolean isDataReady = false;

        public TabsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
//            fragApps = FragAppsList.newInstance(appDetails.getSimilarApps());
//            fragComments = new FragAppMediaList();
//            fragMedia = new FragAppMediaList();
//            fragUpvoters = FragUsersList.newInstance(appDetails.getUpvoters());
        }

        public void updateAdapter(AppUserProfile appDetails) {

        }

        @Override
        public Fragment getItem(int index) {
//            switch (index) {
//                case 0:
//                    return fragUpvotedApps;
//                case 1:
//                    return fragCollections;
//                case 2:
//                    return fragSuggestedApps;
//            }
            return null;
        }

        @Override
        public int getCount() {
            if (isDataReady)
                return 0; // change this to the number of tabs we have in profile
            else
                return 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            try {
                switch (position) {
                    case 0:
                        title = getString(R.string.profile_upvoted);
                        break;
                    case 1:
                        title = getString(R.string.profile_collections);
                        break;
                    case 2:
                        title = getString(R.string.profile_suggestions);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return title;
        }
    }
}



