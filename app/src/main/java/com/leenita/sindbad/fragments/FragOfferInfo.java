package com.leenita.sindbad.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leenita.sindbad.R;
import com.leenita.sindbad.model.SindOffer;
import com.leenita.sindbad.model.SindProduct;

import org.json.JSONObject;

public class FragOfferInfo extends Fragment {

    //Views
    private TextView tvTitle, tvDescreption;

    // Store instance variables
    private SindOffer offer;
    private SindProduct product;

    public static FragOfferInfo newInstance(SindOffer offer) {
        FragOfferInfo fragmentFirst = new FragOfferInfo();
        Bundle args = new Bundle();
        args.putString("offer", offer.getJsonString());
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    public static FragOfferInfo newInstance( SindProduct product) {
        FragOfferInfo fragmentFirst = new FragOfferInfo();
        Bundle args = new Bundle();
        args.putString("product", product.getJsonString());
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments().containsKey("offer")) {
                String str = getArguments().getString("offer");
                offer = SindOffer.fromJson(new JSONObject(str));
            } else if (getArguments().containsKey("product")) {
                String str = getArguments().getString("product");
                product = SindProduct.fromJson(new JSONObject(str));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_offer_info, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
        tvTitle = (TextView) getView().findViewById(R.id.tvTitle);
        tvDescreption = (TextView) getView().findViewById(R.id.tvDescreption);

        if (offer != null)
            updateFrag(offer);

        if(product != null)
            updateFrag(product);

    }

    public void updateFrag(SindOffer shopItem) {
        this.offer = shopItem;
        if (shopItem == null)
            return;
        try {
            if(offer.getTitle() != null && !offer.getTitle().isEmpty())
                tvTitle.setText(offer.getTitle());

            tvDescreption.setText(offer.getDescription());
        } catch (Exception e) {
        }
    }

    public void updateFrag(SindProduct shopItem) {
        this.product = shopItem;
        if (shopItem == null)
            return;
        try {
            if(shopItem.getName()!= null && !shopItem.getName().isEmpty())
                tvTitle.setText(shopItem.getName());
            tvDescreption.setText(shopItem.getDescription());
        } catch (Exception e) {
        }
    }

}
