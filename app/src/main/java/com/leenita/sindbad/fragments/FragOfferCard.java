package com.leenita.sindbad.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.leenita.sindbad.R;
import com.leenita.sindbad.SindbadApp;
import com.leenita.sindbad.data.PhotoProvider;
import com.leenita.sindbad.model.SindOffer;
import com.leenita.sindbad.model.SindOffer.OFFER_TYPE;
import com.leenita.sindbad.model.SindProduct;

import org.json.JSONObject;

public class FragOfferCard extends Fragment implements OnClickListener {

    public enum CARD_MODE {OFFER_CARD, PRODUCT_CARD}

    //Views
    private ImageView ivPhoto;
    private TextView tvName, tvPrice, tvBrand;

    // Store instance variables
    private int pageIndex;
    private SindOffer offer;
    private SindProduct product;

    CARD_MODE mode;

    /**
     */
    public static FragOfferCard newInstance(int pageIndex, SindOffer game) {
        FragOfferCard fragmentFirst = new FragOfferCard();
        Bundle args = new Bundle();
        args.putString("offer", game.getJsonString());
        args.putInt("pageNum", pageIndex);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    public static FragOfferCard newInstance(int pageIndex, SindProduct game) {
        FragOfferCard fragmentFirst = new FragOfferCard();
        Bundle args = new Bundle();
        args.putString("product", game.getJsonString());
        args.putInt("pageNum", pageIndex);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            pageIndex = getArguments().getInt("pageNum", 0);
            if (getArguments().containsKey("offer")) {
                String str = getArguments().getString("offer");
                offer = SindOffer.fromJson(new JSONObject(str));
                mode = CARD_MODE.OFFER_CARD;
            } else if (getArguments().containsKey("product")) {
                String str = getArguments().getString("product");
                product = SindProduct.fromJson(new JSONObject(str));
                mode = CARD_MODE.PRODUCT_CARD;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(outState.containsKey("offer"))
            outState.remove("offer");
        if(outState.containsKey("product"))
            outState.remove("product");

        if(mode == CARD_MODE.OFFER_CARD)
            outState.putString("offer", offer.getJsonString());
        else
            outState.putString("product", product.getJsonString());
        outState.putInt("pageNum", pageIndex);
        super.onSaveInstanceState(outState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_offer_card, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
        tvPrice = (TextView) getView().findViewById(R.id.tvPrice);
        tvName = (TextView) getView().findViewById(R.id.tvName);
        tvBrand = (TextView) getView().findViewById(R.id.tvBrand);
        ivPhoto = (ImageView) getView().findViewById(R.id.ivOffer);

        if (offer != null)
            updateFrag(offer);

        if(product != null)
            updateFrag(product);

        getView().setOnClickListener(this);
    }

    public void updateFrag(SindOffer shopItem) {
        this.offer = shopItem;
        if (shopItem == null)
            return;
        try {
			//PhotoProvider.getInstance().displayProfilePicture(offer.getCover(), ivPhoto);
            tvBrand.setText(offer.getBrand().getName());
            if(offer.getType() == OFFER_TYPE.FIXED_DEAL) {
                tvPrice.setText(offer.getDiscount() + "$");
            }else{
                tvPrice.setText(offer.getDiscount() + "%");
            }
            tvPrice.setBackgroundResource(R.drawable.shape_offer_price_badge);
            tvName.setText(offer.getTitle());
            PhotoProvider.getInstance().displayProfilePicture(offer.getPhoto(), ivPhoto);
            mode = CARD_MODE.OFFER_CARD;
        } catch (Exception e) {
        }
    }

    public void updateFrag(SindProduct shopItem) {
        this.product = shopItem;
        if (shopItem == null)
            return;
        try {
			PhotoProvider.getInstance().displayProfilePicture(shopItem.getCover(), ivPhoto);
            tvName.setText(shopItem.getName());
            tvBrand.setText(shopItem.getBrand().getName());
            tvPrice.setText(shopItem.getPrice()+"$");
            tvPrice.setBackgroundResource(R.drawable.shape_product_price_badge);
            mode = CARD_MODE.PRODUCT_CARD;
        } catch (Exception e) {
        }
    }

    public int getPageIndex() {
        return pageIndex;
    }

    @Override
    public void onClick(View arg0) {
        if(mode == CARD_MODE.OFFER_CARD)
            SindbadApp.showOfferDetails(getActivity(), offer, null);
        else if(mode == CARD_MODE.PRODUCT_CARD)
            SindbadApp.showProdDetails(getActivity(), product, null);
    }
}
