package com.leenita.sindbad;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import com.leenita.sindbad.data.DataStore;
import com.leenita.sindbad.data.DataStore.DataRequestCallback;
import com.leenita.sindbad.data.ServerResult;
import com.leenita.sindbad.fragments.FragUsersList;
import com.leenita.sindbad.fragments.FragUsersList.FRAG_USERS_TYPE;
import com.leenita.sindbad.model.AppUser;

public class FollowersListActivity extends BaseActivity{

    public enum FOLLOWERS_FRAGMENT_TYPE {FOLLOWERS, FOLLOWING}

    private static String TAG_Main = "fragFollowers";

    FragmentManager fragmentManager;
    Fragment fragment;
    View spinner;

    FOLLOWERS_FRAGMENT_TYPE type;
    String userId;

    DataRequestCallback callbackOnFollowersReceive = new DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult data, boolean success) {
            spinner.setVisibility(View.GONE);
            ArrayList<AppUser> list = (ArrayList)data.get("users");
            onDataReceived(list);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        init();

        try{
            type = (FOLLOWERS_FRAGMENT_TYPE) getIntent().getExtras().getSerializable("type");
            userId = getIntent().getExtras().getString("userId");
            if(userId == null || userId.isEmpty())
                userId = DataStore.getInstance().getMe().getId();
            if(type != null){
                requestFollowers(userId, type);
                spinner.setVisibility(View.VISIBLE);
            }
        }catch (Exception ignored){}

        initCustomActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.home:
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        fragmentManager = getSupportFragmentManager();
        spinner = findViewById(R.id.spinner);
    }

    private void initCustomActionBar() {
        try{
            if(type == FOLLOWERS_FRAGMENT_TYPE.FOLLOWING)
                getSupportActionBar().setTitle(R.string.profile_following);
            else
                getSupportActionBar().setTitle(R.string.profile_followers);
            if(android.os.Build.VERSION.SDK_INT > VERSION_CODES.JELLY_BEAN_MR1)
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.res_actionbar_back_with_padding);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }catch (Exception ignored){}
    }

    //TODO load followers
    private void requestFollowers(String userId, FOLLOWERS_FRAGMENT_TYPE type){
        //DataStore.getInstance().getFollowers(userId, type, callbackOnFollowersReceive);
    }

    private void onDataReceived(ArrayList<AppUser> followers){
        FRAG_USERS_TYPE listType ;
        if(type == FOLLOWERS_FRAGMENT_TYPE.FOLLOWING)
            listType = FRAG_USERS_TYPE.FOLLOWING;
        else
            listType = FRAG_USERS_TYPE.FOLLOWERS;
        //if its the list of the people I follow
        boolean shouldRemoveUnFollowedElements = (type == FOLLOWERS_FRAGMENT_TYPE.FOLLOWING && ( userId == null || userId.isEmpty() || DataStore.getInstance().getMe().getId().equals(userId)));
        fragment = FragUsersList.newInstance(followers, listType, shouldRemoveUnFollowedElements);
        fragmentManager.beginTransaction()
                .add(R.id.content_frame, fragment, TAG_Main).commit();
    }
}
