package com.leenita.sindbad.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.gson.reflect.TypeToken;
import com.leenita.sindbad.R;
import com.leenita.sindbad.SearchActivity;
import com.leenita.sindbad.data.DataStore;
import com.leenita.sindbad.data.DataStore.DataStoreUpdateListener;
import com.leenita.sindbad.data.ServerResult;
import com.leenita.sindbad.model.AppBaseModel;
import com.leenita.sindbad.model.SindBrand;
import com.leenita.sindbad.model.SindCategory;
import com.leenita.sindbad.model.SindProduct;
import com.leenita.sindbad.view.AppProviderCard;
import com.leenita.sindbad.view.SearchView;

import java.util.ArrayList;


public class FragSearchResultsList extends Fragment implements DataStoreUpdateListener, OnItemClickListener, SearchView.SearchViewCallback {

    RecyclerView list;
    ArrayList<SindBrand> providers = null;

    ProvidersAdapter adapter;
    String keyword;
    SearchActivity activity;

    public DataStore.DataRequestCallback searchResultCallback = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            activity.closeKeyBoard();
            if (success) {
                ArrayList<SindBrand> brands;
                try {
                    brands = new ArrayList<>();
                    if (result.getPairs().containsKey("products")) {
                        ArrayList<SindProduct> products = (ArrayList<SindProduct>) result.get("products");
                        if (products != null && !products.isEmpty())
                            for (SindProduct p : products) {
                                if (p.getBrand() != null)
                                    brands.add(p.getBrand());
                            }
                    }
                    else {
                        brands = (ArrayList<SindBrand>) result.get("brands");
                    }
                    providers = brands;
                    updateView();

                } catch (Exception e) {
                }
            }
        }
    };

    public static FragSearchResultsList newInstance(String searchKeyword, ArrayList<SindBrand> brands, ArrayList<SindCategory> selectedCategories) {
        FragSearchResultsList frag = new FragSearchResultsList();
        Bundle extras = new Bundle();
        if (searchKeyword != null)
            extras.putString("searchKeyword", searchKeyword);
        if (brands != null)
            extras.putString("brands", AppBaseModel.getJSONArray(brands).toString());

        frag.setArguments(extras);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_main_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments().containsKey("searchKeyword"))
            keyword = getArguments().getString("searchKeyword");
        // read passed data
        try {
            String strApps = getArguments().getString("brands");
            ArrayList<SindBrand> brands = AppBaseModel.getArrayFromJsonSting(strApps, new TypeToken<ArrayList<SindBrand>>() {
            }.getType());
            this.providers = brands;
        } catch (Exception e) {
        }

        init();
        DataStore.getInstance().addUpdateBroadcastListener(this);
        updateView();
    }

    @Override
    public void onPause() {
        super.onPause();
        DataStore.getInstance().removeUpdateBroadcastListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (SearchActivity) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        DataStore.getInstance().addUpdateBroadcastListener(this);
    }

    private void init() {
        list = (RecyclerView) getView().findViewById(R.id.list);

        // recycler View
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ProvidersAdapter();
        list.setAdapter(adapter);
        //list.addItemDecoration(new VerticalSpaceItemDecoration(DamanApp.getPXSize(3))); // adding a 3dp divider between items
    }

    @SuppressLint("StringFormatMatches")
    private void updateView() {
        adapter.updateAdapter();
    }

    @Override
    public void onDataStoreUpdate() {
        updateView();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onLoginStateChange() {

    }

    @Override
    public void onFilterSelect(ArrayList<SindCategory> categories) {
        DataStore.getInstance().searchForKeyword(  activity.getKeyWord(), searchResultCallback);
        activity.setSelectedCategories(categories);
    }

    @Override
    public void onSearchForKeyword(String keyWord) {
        DataStore.getInstance().searchForKeyword(  activity.getKeyWord(), searchResultCallback);
    }

    class ProvidersAdapter extends RecyclerView.Adapter<AppViewHolder> {

        public void updateAdapter() {
            notifyDataSetChanged();
        }

        OnClickListener onItemClickListner = new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int itemPosition = list.getChildPosition(view);
                    SindBrand provider = providers.get(itemPosition);
                    if (provider != null) {
                        //TODO
                        //((NavigatableActivity) getActivity()).showProviderOnMap(provider);
                    }
                } catch (Exception ignored) {
                }
            }
        };


        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            AppProviderCard root = new AppProviderCard(getActivity(), AppProviderCard.CARD_TYPE.LIST);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            root.setLayoutParams(lp);
            AppViewHolder holder = new AppViewHolder(root);
            root.setOnClickListener(onItemClickListner);
            return holder;
        }

        @Override
        public void onBindViewHolder(AppViewHolder viewHolder, int i) {
            try {
                final SindBrand provider = providers.get(i);
                viewHolder.root.updateUI(provider);
            } catch (Exception ignored) {

            }
        }

        @Override
        public int getItemCount() {
            if (providers == null)
                return 0;
            return providers.size();
        }
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        AppProviderCard root;

        public AppViewHolder(AppProviderCard view) {
            super(view);
            root = view;
        }
    }

}
