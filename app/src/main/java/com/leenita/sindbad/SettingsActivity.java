package com.leenita.sindbad;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.view.Menu;
import android.view.MenuItem;

import com.leenita.sindbad.fragments.FragSettingsMain;
import com.leenita.sindbad.fragments.FragSettingsProfile;
import com.leenita.sindbad.fragments.FragSettingsStaticPage;

public class SettingsActivity extends BaseActivity implements OnBackStackChangedListener{

    public enum SETTINGS_FRAGMENT_TYPE {SETTINGS_MAIN, SETTINGS_PROFILE, SETTINGS_TERMS_OF_SERVICE, SETTINGS_PRIVACY_POLICY}
    private static String TAG_Main = "fragSettingsMain";

    FragmentManager fragmentManager;
    Fragment fragment;

    int homeId =-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
        initCustomActionBar();
        try{
            if(getIntent().hasExtra("target")) {
                SETTINGS_FRAGMENT_TYPE targetFrag = (SETTINGS_FRAGMENT_TYPE) getIntent().getSerializableExtra("target");
                if(targetFrag != null)
                    switchSection(targetFrag);
            }else{
                switchSection(SETTINGS_FRAGMENT_TYPE.SETTINGS_MAIN);
            }
        }catch (Exception ignored){}
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
                onBtnBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void onBtnBackPressed(){
        if(fragmentManager.getBackStackEntryCount()==0){
            finish();
        }else{
            popAllFragments();
        }
    }

    @Override
    public void onBackStackChanged() {
        try{
            Fragment fragMain = fragmentManager.findFragmentByTag(TAG_Main);
            if(fragMain == null && fragmentManager.getBackStackEntryCount() == 0){
                //no more fragments loaded in this activity
                //so we should close it
                finish();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void init() {
        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);
    }

    private void initCustomActionBar() {
        try{
            setTitle(R.string.settings_title);
            if(android.os.Build.VERSION.SDK_INT > VERSION_CODES.JELLY_BEAN_MR1)
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.res_actionbar_back_with_padding);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }catch (Exception ignored){}
    }

    public void switchSection(SETTINGS_FRAGMENT_TYPE section) {
        // reset fragments stack and display the current fragment
        popAllFragments();
        loadSection(section);
    }

    void loadSection(SETTINGS_FRAGMENT_TYPE section) {
        try {// getFragmentManager().beginTransaction()
            switch (section){
                case SETTINGS_MAIN:
                    fragmentManager.popBackStack();
                    int count = fragmentManager.getBackStackEntryCount();
                    if (count == 0) {
                        fragment = FragSettingsMain.newInstance();
                        fragmentManager.beginTransaction()
                                // .setCustomAnimations(R.anim.slide_in_from_left,
                                // R.anim.slide_out_to_right,R.anim.slide_in_from_right,R.anim.slide_out_to_left)
                                .add(R.id.content_frame, fragment, TAG_Main).commit();
                    } else {
                        fragment = fragmentManager.findFragmentByTag(TAG_Main);
                    }
                    setTitle(R.string.settings_title);
                    break;
                case SETTINGS_TERMS_OF_SERVICE:
                    fragment = FragSettingsStaticPage.newInstance(R.string.settings_terms_of_service);
                    fragmentManager.beginTransaction().addToBackStack(null).add(R.id.content_frame, fragment, null).commit();
                    setTitle(R.string.settings_main_about_terms_of_service);
                    break;
                case SETTINGS_PRIVACY_POLICY:
                    fragment = FragSettingsStaticPage.newInstance(R.string.settings_privacy_policy);
                    fragmentManager.beginTransaction().addToBackStack(null).add(R.id.content_frame, fragment, null).commit();
                    setTitle(R.string.settings_main_about_privacy_policy);
                    break;
                case SETTINGS_PROFILE:
                    FragSettingsProfile fragProfile = FragSettingsProfile.newInstance();
                    fragment = fragProfile;
                    fragmentManager.beginTransaction().addToBackStack(null).add(R.id.content_frame, fragment, null).commit();
                    setTitle(R.string.settings_main_account_title);
                    break;
            }
        } catch (Exception ignored) {}
    }

    void popAllFragments() {
        try {
            homeId = fragmentManager.getBackStackEntryAt(0).getId();
            fragmentManager.popBackStack(homeId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragment = fragmentManager.findFragmentByTag(TAG_Main);
            setTitle(R.string.settings_title);
			/*
			 * // update menu bar menuBar.updateMenu(getCurrentFragment());
			 */
        } catch (Exception ignored) {}
    }
}
