package com.leenita.sindbad.view;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.leenita.sindbad.LoginActivity;
import com.leenita.sindbad.MapActivity;
import com.leenita.sindbad.R;
import com.leenita.sindbad.SindbadApp;
import com.leenita.sindbad.data.DataStore;
import com.leenita.sindbad.data.PhotoProvider;
import com.leenita.sindbad.data.ServerResult;
import com.leenita.sindbad.fragments.FragMap;
import com.leenita.sindbad.model.AppBaseModel;
import com.leenita.sindbad.model.SindBrand;
import com.leenita.sindbad.model.SindContact;
import com.leenita.sindbad.model.SindTag;
import com.wefika.flowlayout.FlowLayout;


public class AppProviderCard extends FrameLayout implements OnClickListener {

    public enum CARD_TYPE {MAP, LIST}

    CARD_TYPE type;

    // views
    TextView tvProviderPreviewName;
    TextView tvProviderPreviewType;
    ImageView ivProviderPreviewLogo;
    View btnCall;
    View btnLocation;
    View btnFollow;
    View vSep;
    FlowLayout vTags;

    //Data
    SindBrand item;
    String phone;
    boolean isBrandInFollowingList;

    private DataStore.DataRequestCallback apiFollowCallback = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            enableFollowBtn(true);
            if (success) {
                try {
                    boolean following = (boolean) result.get("followed");
                    updateIsFollwingBtnText(following);
                    isBrandInFollowingList = true;
                } catch (Exception e) {
                }
            } else {
                SindbadApp.displayCustomToast(getResources().getString(R.string.err_check_connection));
            }
        }
    };

    private DataStore.DataRequestCallback apiUnFollowCallback = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            enableFollowBtn(true);
            if (success) {
                try {
                    boolean notFollowing = (boolean) result.get("unfollowed");
                    updateIsFollwingBtnText(!notFollowing);
                    isBrandInFollowingList = false;
                } catch (Exception e) {
                }
            } else {
                SindbadApp.displayCustomToast(getResources().getString(R.string.err_check_connection));
            }
        }
    };

    public AppProviderCard(Context context, CARD_TYPE type) {
        super(context);
        this.type = type;
        initialize();
    }

    public AppProviderCard(Context context) {
        super(context);
        initialize();
    }

    public AppProviderCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public AppProviderCard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    /**
     * Inflates and initializes the views and tool of this custom view
     */
    void initialize() {
        try {

            if (!isInEditMode()) {
                if (type != null && type == CARD_TYPE.LIST) {
                    inflate(getContext(), R.layout.row_provider_list, this);
                    btnLocation = findViewById(R.id.btnLocation);
                    btnLocation.setOnClickListener(this);
                } else {
                    inflate(getContext(), R.layout.row_provider_map, this);
                    btnFollow = findViewById(R.id.btnFollow);
                    btnFollow.setOnClickListener(this);
                }
                // views
                tvProviderPreviewName = (TextView) findViewById(R.id.tvName);
                tvProviderPreviewType = (TextView) findViewById(R.id.tvType);
                ivProviderPreviewLogo = (ImageView) findViewById(R.id.ivLogo);
                btnCall = findViewById(R.id.btnCall);
                vTags = (FlowLayout) findViewById(R.id.vTags);
                vSep = findViewById(R.id.vSep);
                btnCall.setOnClickListener(this);

            }
        } catch (Exception e) {
        }
    }

    /**
     * Updates the view data
     */
    public void updateUI(SindBrand item) {
        try {
            this.item = item;

            //Check if this brand is followed by me
            isBrandInFollowingList = false;
            if (AppBaseModel.getById(DataStore.getInstance().getMyBrands(), item.getId()) != null)
                isBrandInFollowingList = true;

            if (type != null && type == CARD_TYPE.MAP)
            {
                updateIsFollwingBtnText(isBrandInFollowingList);
                btnFollow.setEnabled(true);
                btnFollow.setAlpha(1f);
            }


            try {
                vTags.removeAllViews();
            } catch (Exception e) {
            }

            ///fill Data
            tvProviderPreviewName.setText(item.getName());
            if (item.getCategory() != null)
                tvProviderPreviewType.setText(item.getCategory().getName());
            PhotoProvider.getInstance().displayPhotoNormal(item.getLogo(), ivProviderPreviewLogo);

            phone = SindContact.getContactInfoByType(item.getContacts(), SindContact.contactType.PHONE);
            if (phone == null)
                phone = SindContact.getContactInfoByType(item.getContacts(), SindContact.contactType.MOBILE);

            if (phone == null || phone.isEmpty())
                btnCall.setVisibility(GONE);
            else
                btnCall.setVisibility(VISIBLE);



            if (item.getTags() != null && !item.getTags().isEmpty()) {
                vTags.setVisibility(VISIBLE);
                for (SindTag tag : item.getTags()) {
                    TextView tvTag = (TextView) inflate(getContext(), R.layout.layout_tag, null);
                    tvTag.setText(tag.getName());

                    FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT);
                    params.rightMargin = 10;
                    tvTag.setLayoutParams(params);

                    vTags.addView(tvTag);
                }
            } else {
                vTags.setVisibility(GONE);
            }

            if (type != null && type == CARD_TYPE.MAP) {
                if (vTags.getVisibility() == GONE && btnCall.getVisibility() == GONE)
                    vSep.setVisibility(INVISIBLE);
                else
                    vSep.setVisibility(VISIBLE);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callPhone() {
        try {
            if (phone != null) {
                String number = "tel:" + phone.trim();
                //ACTION_CALL needs permission
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(number));
                getContext().startActivity(callIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * opens Google Maps App to show the user how to get from the current location to the Provider location
     */
    private void showNavigateToProvider() {
//        SearchActivity activity = (SearchActivity) getContext();
        Intent i = new Intent(getContext(), MapActivity.class);
        i.putExtras(MapActivity.getLauncherBundle(FragMap.MAP_TYPE.BRAND, item));
        getContext().startActivity(i);
//        activity.openProvidersMap(item);
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btnCall:
                    callPhone();
                    break;
                case R.id.btnFollow:
                    updateFollowingState();
                    break;
                case R.id.btnLocation:
                    showNavigateToProvider();
                    break;
            }
        } catch (Exception e) {
        }
    }

    void updateFollowingState() {
        if (!DataStore.getInstance().isUserLoggedIn()) {
            getContext().startActivity(new Intent(getContext(), LoginActivity.class));
            return;
        }
        enableFollowBtn(false);
        if (isBrandInFollowingList)
            DataStore.getInstance().unFollowBrand(item.getId(), apiUnFollowCallback);
        else
            DataStore.getInstance().followBrand(item.getId(), apiFollowCallback);
    }

    private void enableFollowBtn(boolean enable) {
        btnFollow.setEnabled(enable);

        if (enable)
            btnFollow.setAlpha(1f);
        else
            btnFollow.setAlpha(0.5f);
    }

    void updateIsFollwingBtnText(boolean isFollowing) {
        if (isFollowing)
            ((TextView) btnFollow).setText(getResources().getString(R.string.search_unfollow));
        else
            ((TextView) btnFollow).setText(getResources().getString(R.string.search_follow));
    }
}
