package com.leenita.sindbad.fragments;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.leenita.sindbad.R;
import com.leenita.sindbad.SearchActivity;
import com.leenita.sindbad.SindbadApp;
import com.leenita.sindbad.data.DataStore;
import com.leenita.sindbad.data.DataStore.DataRequestCallback;
import com.leenita.sindbad.data.ServerResult;
import com.leenita.sindbad.model.SindBranch;
import com.leenita.sindbad.model.SindBrand;
import com.leenita.sindbad.model.SindCategory;
import com.leenita.sindbad.model.SindProduct;
import com.leenita.sindbad.view.AppProviderCard;
import com.leenita.sindbad.view.SearchView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class FragMap extends Fragment implements OnMapReadyCallback, OnMarkerClickListener, GoogleMap.InfoWindowAdapter, SearchView.SearchViewCallback {


    SupportMapFragment fragment;
    GoogleMap googleMap;

    public enum MAP_TYPE {SEARCH, BRAND}

    MAP_TYPE type;
    ArrayList<LocatableProvider> providers = null;
    ArrayList<SindBrand> brands = null;
    HashMap<String, LocatableProvider> mapMarkerIdToLocatableProvider;
    LocatableProvider selectedLocatableProvider;

    AppProviderCard vItemDetailsPreview;
    boolean focusMap;

    SearchActivity activity;
    float radius;
    double centerLat;
    double centerLon;

    Handler handler;

    //TempDataHolders
    SindBrand brandToShow;

    public DataRequestCallback searchResultCallback = new DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            activity.closeKeyBoard();
            if (success) {
                ArrayList<SindBrand> brands;
                try {
                    if (result.getPairs().containsKey("products")) {
                        brands = new ArrayList<>();
                        ArrayList<SindProduct> products = (ArrayList<SindProduct>) result.get("products");
                        if (products != null && !products.isEmpty())
                            for (SindProduct p : products) {
                                if (p.getBrand() != null)
                                    brands.add(p.getBrand());
                            }
                    } else {
                        brands = (ArrayList<SindBrand>) result.get("brands");
                        Log.i("MAP", "received brands: " + brands.size());
                    }
                    updateView(brands, focusMap);
                } catch (Exception e) {
                }
            }
        }
    };

    public static FragMap newInstance(MAP_TYPE type, SindBrand brandToShow) {
        FragMap frag = new FragMap();
        Bundle extras = new Bundle();
        extras.putInt("type", type.ordinal());

        if(brandToShow != null)
            extras.putString("brand", brandToShow.getJsonString());

        frag.setArguments(extras);
        return frag;
    }

    public void resolveExtra(Bundle extra) {
        try {
            type = MAP_TYPE.values()[extra.getInt("type")];
            switch (type) {
                case BRAND:
                    String jsonStr = extra.getString("brand");
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    brandToShow = SindBrand.fromJson(jsonObject);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_main_map, container, false);
        resolveExtra(getArguments());
        if (type == MAP_TYPE.SEARCH)
            this.activity = (SearchActivity) getContext();
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SindbadApp.checkPlayServices(getActivity());
        init();
    }

    private void init() {

        focusMap = false;

        vItemDetailsPreview = (AppProviderCard) getView().findViewById(R.id.vProviderPreview);
        mapMarkerIdToLocatableProvider = new HashMap<>();

        handler = new Handler();

        // load Map Frag
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.flContentFrame);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.flContentFrame, fragment).commit();
            fragment.getMapAsync(this);
        }

        //inital state
        hidePreview();

    }

    private void onDataRecieved() {
        switch (type) {
            case SEARCH:
                // this means the fragments was loaded before, so we just update it and make sure nothing is selected
                selectedLocatableProvider = null;
                focusMap = true;
                if (activity != null)
                    DataStore.getInstance().searchForKeyword(activity.getKeyWord(), searchResultCallback);
                break;

            case BRAND:
                if (brandToShow != null)
                    showBrand(brandToShow);
                break;

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // this block is needed on some devices that throw a
        // "java.lang.NullPointerException: IBitmapDescriptorFactory is not initialized" exception
        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.googleMap = googleMap;
        this.googleMap.setOnCameraChangeListener(onCameraChangeListener);
        this.googleMap.setOnMarkerClickListener(this);
        // used to force Google maps bring
        // the marker to top onClick by showing an empty info window
        this.googleMap.setInfoWindowAdapter(this);
        this.googleMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                selectedLocatableProvider = null;
                hidePreview();
            }
        });

        onDataRecieved();
    }


    @SuppressLint("StringFormatMatches")
    private void updateView(ArrayList<SindBrand> brands, boolean reFocusMap) {
        this.brands = brands;
        try {
            if (brands != null && googleMap != null) {

                this.providers = new ArrayList<>();

                for (SindBrand brand : brands) {
                    LocatableProvider locatableProvider = new LocatableProvider();
                    locatableProvider.provider = brand;
                    locatableProvider.type = LocatableProvider.MarkType.BRAND;
                    boolean isSelected = selectedLocatableProvider != null
                            && locatableProvider.provider != null
                            && locatableProvider.provider.getId().equals(selectedLocatableProvider.provider.getId());

                    locatableProvider.markerOptions =
                            new MarkerOptions()
                                    .position(brand.getCoordinates())
                                    // if its the highlighted provider then draw it with a different marker icon
                                    .icon(BitmapDescriptorFactory.fromResource(isSelected ? R.drawable.ic_marker_active : R.drawable.ic_marker));
                    this.providers.add(locatableProvider);

                    //Add brand's branches to the markers list
                    if (getLocatableBranches(brand) != null)
                        this.providers.addAll(getLocatableBranches(brand));
                }

                // Map
                drawProvidersOnMap(this.providers, reFocusMap);

                //tv Message
//            AppPlanType selectedPlan = DataStore.getInstance().getSelectedPlanType();
//            String msg ;
//            if(providers.size() == 1)
//                msg = getString(R.string.main_providers_found_one_prefix);
//            else
//                msg = String.format(getString(R.string.main_providers_found_prefix, (Object) null), String.valueOf(providers.size()));
//            if(selectedPlan == null)
//                msg += " - " + getString(R.string.main_all_plans);
//            else
//                msg += " - " + selectedPlan.getTitle();
//            tvMsg.setText(msg);
            }
        } catch (Exception e) {
        }
    }

    private ArrayList<LocatableProvider> getLocatableBranches(SindBrand brand) {
        if (brand.getBranches() == null && brand.getBranches().isEmpty())
            return null;

        ArrayList<LocatableProvider> loactableBranches = new ArrayList<>();
        for (SindBranch branch : brand.getBranches()) {
            LocatableProvider locatableBranch = new LocatableProvider();
            locatableBranch.provider = brand;
            locatableBranch.providerBranch = branch;
            locatableBranch.type = LocatableProvider.MarkType.BRANCH;
            boolean isSelected =
                    selectedLocatableProvider != null &&
                            locatableBranch.providerBranch != null &&
                            selectedLocatableProvider.providerBranch != null &&
                            locatableBranch.providerBranch.getId().equals(
                                    selectedLocatableProvider.providerBranch.getId());
            locatableBranch.markerOptions =
                    new MarkerOptions()
                            .position(branch.getCoordinates())
                            // if its the highlighted provider then draw it with a different marker icon
                            .icon(BitmapDescriptorFactory.fromResource(isSelected ? R.drawable.ic_marker_active : R.drawable.ic_marker));
            loactableBranches.add(locatableBranch);
        }
        return loactableBranches;
    }


    /**
     * used to re draw all providers and update Camera position if required
     *
     * @param providers array of providers that wil be represented on map with markers
     * @param focusMap: if true, we animated the map camera to fit all the markers in view
     */
    private void drawProvidersOnMap(ArrayList<LocatableProvider> providers, boolean focusMap) {
        try {
            if (providers != null && googleMap != null) {
                googleMap.clear();

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LocatableProvider provider : providers) {
                    try {

                        Marker marker = googleMap.addMarker(provider.markerOptions);
                        mapMarkerIdToLocatableProvider.put(marker.getId(), provider);
                        LatLng position = provider.markerOptions.getPosition();
                        builder.include(position);
                        if (selectedLocatableProvider != null && provider.provider.getId().equals(selectedLocatableProvider.provider.getId()))
                            marker.showInfoWindow();
                    } catch (Exception e) {
                    }
                }

                if (focusMap) {
                    LatLngBounds bounds = builder.build();
                    focusMap(bounds);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void focusMap(LatLngBounds bounds) {
        int padding = 50;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);
    }

    public void displayProviderDetailsPreview(LocatableProvider locatableProvider) {
        if (locatableProvider != null && locatableProvider.provider != null) {
            vItemDetailsPreview.updateUI(locatableProvider.provider);
            vItemDetailsPreview.setVisibility(View.VISIBLE);
        }
    }

    public void displayBrandPreview(SindBrand brand) {
        if (brand != null) {
            vItemDetailsPreview.updateUI(brand);
            vItemDetailsPreview.setVisibility(View.VISIBLE);
        }
    }

    /**
     * hide details Preview
     */
    public void hidePreview() {
        vItemDetailsPreview.setVisibility(View.GONE);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        try {
            LocatableProvider locatableProvider = mapMarkerIdToLocatableProvider.get(marker.getId());
            if (locatableProvider != null) {
                displayProviderDetailsPreview(locatableProvider);
                selectedLocatableProvider = locatableProvider;
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_active));
                focusMapOnMarker(marker.getPosition());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     *  show brand and its branches
     */
    public void showBrand(SindBrand provider) {
        if (provider == null)
            return;

        try {
            ArrayList<LocatableProvider> brandBranches = new ArrayList<>();
            if(getLocatableBranches(provider) != null)
            brandBranches.addAll(getLocatableBranches(provider));
            displayBrandPreview(provider);
            drawProvidersOnMap(brandBranches, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void selectProvider(SindBrand provider) {
        try {
            if (providers == null || provider == null)
                return;
            // scan providers array for the LocatableProvider that matches the supplied provider
            for (int i = 0; i < providers.size(); i++) {
                if (provider.getId().equals(providers.get(i).provider.getId())) {
                    selectedLocatableProvider = providers.get(i);
                    break;
                }
            }

            ArrayList<LocatableProvider> brandBranches = new ArrayList<>();
            if (selectedLocatableProvider != null) {

                brandBranches.add(selectedLocatableProvider);
                if (getLocatableBranches(provider) != null)
                    brandBranches.addAll(getLocatableBranches(provider));

                displayProviderDetailsPreview(selectedLocatableProvider);
                drawProvidersOnMap(brandBranches, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // used to force Google maps bring
    // the marker to top onClick by showing an empty info window
    @Override
    public View getInfoWindow(Marker marker) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.layout_marker_info_window, null);
        return v;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    /**
     * update the camer to make the marker at the bottom half of the screen and zoom in to focus on marker
     * by first zoomin in
     */
    private void focusMapOnMarker(LatLng latLng) {
        try {
            // zooming above the marker a little to make sure the marker stays at
            // the lower half of screen so it wont be covered by details card
            float paddingAboveMarker = 0.006f;
            final LatLng markerLatLng = new LatLng(latLng.latitude + paddingAboveMarker, latLng.longitude);
            CameraUpdate center = CameraUpdateFactory.newLatLng(markerLatLng);
            googleMap.animateCamera(center);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getNearByBrands() {
        Log.i("MAP", "getNearByBrands");
        //get coordinates of center screen point
        VisibleRegion vr = googleMap.getProjection().getVisibleRegion();
        centerLat = vr.latLngBounds.getCenter().latitude;
        centerLon = vr.latLngBounds.getCenter().longitude;

        //calculate distane between middleSouthEdge and screen center map location

        //center location
        Location centerLoc = new Location("center");
        centerLoc.setLatitude(centerLat);
        centerLoc.setLongitude(centerLon);
        //middle South location
        Location middlleSouthLocation = new Location("left");
        middlleSouthLocation.setLongitude(centerLoc.getLongitude());
        middlleSouthLocation.setLatitude(vr.latLngBounds.southwest.latitude);

        radius = centerLoc.distanceTo(middlleSouthLocation);

        focusMap = false;
        Log.i("Params", centerLat + "," + centerLon + "," + radius);
        DataStore.getInstance().requestBrands((float) centerLat, (float) centerLon, radius, 0, activity.getKeyWord(), activity.getCategoriesFilter(), null, searchResultCallback);
    }

    GoogleMap.OnCameraChangeListener onCameraChangeListener = new GoogleMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            if (type == MAP_TYPE.SEARCH)
                getNearByBrands();
        }
    };


    //Getters
    public float getRadius() {
        return radius;
    }

    public float getCenterLat() {
        return (float) centerLat;
    }

    public float getCenterLon() {
        return (float) centerLon;
    }

    public void setFocusMap(boolean focusMap) {
        this.focusMap = focusMap;
    }

    public ArrayList<SindBrand> getBrandsOnMap() {
        return this.brands;
    }

    @Override
    public void onFilterSelect(ArrayList<SindCategory> categories) {
        hidePreview();
        setFocusMap(true);
        activity.setSelectedCategories(categories);
        DataStore.getInstance().requestBrands(getCenterLat(), getCenterLon(), getRadius(), 0, activity.getKeyWord(), categories, null, searchResultCallback);
    }

    @Override
    public void onSearchForKeyword(String keyWord) {
        focusMap = true;

        DataStore.getInstance().searchForKeyword(activity.getKeyWord(), searchResultCallback);
        hidePreview();// hide details panel
    }

    public static class LocatableProvider {
        SindBrand provider;
        SindBranch providerBranch;
        MarkerOptions markerOptions;

        enum MarkType {BRAND, BRANCH}

        ;
        MarkType type;
    }
}
