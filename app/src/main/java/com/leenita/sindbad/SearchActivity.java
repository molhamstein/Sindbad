package com.leenita.sindbad;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.view.View;
import android.view.View.OnClickListener;

import com.leenita.sindbad.fragments.FragMap;
import com.leenita.sindbad.fragments.FragSearchResultsList;
import com.leenita.sindbad.model.SindBrand;
import com.leenita.sindbad.model.SindCategory;
import com.leenita.sindbad.view.SearchView;

import java.util.ArrayList;



public class SearchActivity extends BaseActivity implements OnClickListener, OnBackStackChangedListener{

    public enum FRAG_TYPE {MAP, LIST}
    private static String TAG_MAIN_MAP_FRAG = "mainMapFrag";

    Fragment fragment;
    FragmentManager fragmentManager;
    FRAG_TYPE currentFrag;
    Dialog diagLoading;

    // temp Data Holder
    String keyword;
    ArrayList<SindBrand> brands;
    SindBrand selectedProvider;
    ArrayList<SindCategory> categoriesFilter;

    SearchView vSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();

    }

    private void init(){
        vSearchBar = (SearchView) findViewById(R.id.vSearchBar);
        fragmentManager = getSupportFragmentManager();
        switchSection(FRAG_TYPE.MAP);
        fragmentManager.addOnBackStackChangedListener(this);
    }

    void switchSection(FRAG_TYPE section){
        if(currentFrag == section)
            return ;
        fragmentManager.popBackStack();
        loadSection(section);
    }

    void loadSection(FRAG_TYPE section){
        try {
            switch(section) {
                case MAP:
                    FragMap fragMap = FragMap.newInstance(FragMap.MAP_TYPE.SEARCH, null);
                    fragment = fragMap;
                    fragmentManager.beginTransaction()
                            .add(R.id.flMainFragmentContainer, fragment, section.name())
                            .commit();
                    currentFrag = section ;

                    if(selectedProvider != null)
                        fragMap.selectProvider(selectedProvider);

                    vSearchBar.setSearchListener((FragMap)fragment);
                    break;
                case LIST:
                    fragment = FragSearchResultsList.newInstance(getKeyWord(), brands, categoriesFilter);
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_from_top_dialog, R.anim.slide_out_to_top, R.anim.slide_in_from_top_dialog, R.anim.slide_out_to_top)
                            .addToBackStack(section.name())
                            .add(R.id.flMainFragmentContainer, fragment)
                            .commit();
                    currentFrag = section ;
                    vSearchBar.setSearchListener((FragSearchResultsList)fragment);
                    break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackStackChanged() {
        int  entrys = getSupportFragmentManager().getBackStackEntryCount() ;

        try {
            String entryName ;
            if(entrys == 0)
                entryName = FRAG_TYPE.MAP.name();
            else
                entryName = getSupportFragmentManager().getBackStackEntryAt(entrys-1).getName() ;
            FRAG_TYPE fragType = FRAG_TYPE.valueOf(entryName) ;
            if( fragType != null ){
                currentFrag = fragType;
                fragment = getSupportFragmentManager().findFragmentByTag(entryName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  void backToMap(){
        try {
            final FragmentManager fm = getSupportFragmentManager();
            while (fm.getBackStackEntryCount() > 0) {
                fm.popBackStackImmediate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openProvidersList(){
        this.brands = ((FragMap)fragment).getBrandsOnMap();
        switchSection(FRAG_TYPE.LIST);
    }

    //TODO
    public void switchResultMode(){
       if(currentFrag == FRAG_TYPE.MAP)
           openProvidersList();
       else
           openProvidersMap(null);
    }

    public void openProvidersMap(SindBrand selectedBrand){
        if(selectedBrand != null)
            this.selectedProvider = selectedBrand;

        getSupportFragmentManager().popBackStack();
        int count = this.getSupportFragmentManager().getBackStackEntryCount();
         fragment = getSupportFragmentManager().getFragments().get(count>0?count-1:count);

        if(selectedBrand != null)
            ((FragMap)fragment).selectProvider(selectedBrand);

        vSearchBar.setSearchListener((FragMap)fragment);
    }

    @Override
    public void onClick(View view) {

    }

    public String getKeyWord()
    {
        return vSearchBar.getKeyWord();
    }

    public ArrayList<SindCategory> getCategoriesFilter ()
    {
        return vSearchBar.getSelectedCategories();
    }

    public void setSelectedCategories(ArrayList<SindCategory> categories)
    {
        categoriesFilter = categories;
        vSearchBar.setSelectedCategories(categories);
    }

    public void closeKeyBoard()
    {
        vSearchBar.closeKeyBorad();
    }

}
