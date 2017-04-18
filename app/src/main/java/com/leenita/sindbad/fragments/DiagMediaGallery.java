package com.leenita.sindbad.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.leenita.sindbad.R;
import com.leenita.sindbad.SindbadApp;
import com.leenita.sindbad.data.PhotoProvider;
import com.leenita.sindbad.model.SindMedia;
import com.leenita.sindbad.model.SindMedia.mediaType;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * this dialog contains aNavigation bar to overLAy the original ActionBar in the main screen 
 */
public class DiagMediaGallery extends DialogFragment{

    View view;
    ViewPager viewPager;
    TextView tvPageIndicator;
    View btnClose;

    Handler handler;
    GalleryAdapter adapter;
    ArrayList<SindMedia> arrayMedia;

    OnPageChangeListener onPAgeChangeLIstener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
        @Override
        public void onPageScrollStateChanged(int state) {}
        @Override
        public void onPageSelected(int position) {
            tvPageIndicator.setText((position+1)+"/"+adapter.getCount());
        }
    };

    public static DiagMediaGallery newInstance(ArrayList<SindMedia> collections, int currentElementIndex){
        DiagMediaGallery frag = new DiagMediaGallery();
        Bundle extras = new Bundle();
        if(collections != null) {
            JSONArray jsonCollections = new JSONArray();
            for (int i = 0; i < collections.size(); i++) {
                jsonCollections.put(collections.get(i).getJsonObject());
            }
            extras.putString("media", jsonCollections.toString());
            extras.putInt("index", currentElementIndex);
        }
        frag.setArguments(extras);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.diag_media_gallery, null);
        Dialog diag = new Dialog(getActivity());
        diag.requestWindowFeature(Window.FEATURE_NO_TITLE);
        diag.setContentView(view);

        diag.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        diag.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.app_theme_gallery_bg)));
        init();
        return diag ;
    }

    private void init (){
        try {
            viewPager = (ViewPager) view.findViewById(R.id.viewPager);
            tvPageIndicator = (TextView) view.findViewById(R.id.tvPageIndicator);
            btnClose = view.findViewById(R.id.btnClose);

            adapter = new GalleryAdapter((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE));

            int initialPosition = getArguments().getInt("index");
            String strApps = getArguments().getString("media");
            JSONArray jsnArrayApps = new JSONArray(strApps);
            arrayMedia = new ArrayList<>();
            for (int i = 0; i < jsnArrayApps.length(); i++) {
                arrayMedia.add(SindMedia.fromJson(jsnArrayApps.getJSONObject(i)));
            }

            viewPager.setAdapter(adapter);
            btnClose.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            handler = new Handler();

            //initial state
            viewPager.addOnPageChangeListener(onPAgeChangeLIstener);
            viewPager.setCurrentItem(initialPosition);
            tvPageIndicator.setText((initialPosition+1)+"/"+adapter.getCount());

        }catch (Exception ignored){}
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnim;
    }

    public void playYouTubeVideo(String url){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private class GalleryAdapter extends PagerAdapter{

        LayoutInflater inflater;

        OnClickListener onVideoClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPosition = (Integer) view.getTag();
                SindMedia appMedia = arrayMedia.get(itemPosition);
                if(appMedia.getType() == mediaType.VIDEO){
                    playYouTubeVideo(appMedia.getPath());
                }
            }
        };

        public GalleryAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public int getCount() {
            if (arrayMedia != null)
                return arrayMedia.size();
            return 0;
        }
        @Override
        public float getPageWidth(int position) {
            return 1f;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            viewPager.removeView((View) view);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View rowView = inflater.inflate(R.layout.row_media_gallery, container, false);
            ImageView ivImg = (ImageView) rowView.findViewById(R.id.ivImg);
            View ivPLayIcon = rowView.findViewById(R.id.ivPLayIcon);
            try {
                //PhotoProvider.getInstance().displayPhotoNormal(arrayMedia.get(position).getPath(),ivImg);
                SindMedia media = arrayMedia.get((position));
                if(media.getType() == mediaType.IMAGE){
                    PhotoProvider.getInstance().displayPhotoNormal(media.getPath(), ivImg);
                    ivPLayIcon.setVisibility(View.GONE);
                }else{
                    String videoThmbUrl = SindbadApp.getThumbnailURLFromVideoURL(media.getPath());
                    PhotoProvider.getInstance().displayProfilePicture(videoThmbUrl, ivImg);
                    ivPLayIcon.setVisibility(View.VISIBLE);
                    ivImg.setTag(position);
                    ivImg.setOnClickListener(onVideoClickListener);
                }
                container.addView(rowView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return rowView;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

}
