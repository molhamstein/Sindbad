package com.leenita.sindbad.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.leenita.sindbad.R;
import com.leenita.sindbad.SindbadApp;
import com.leenita.sindbad.data.PhotoProvider;
import com.leenita.sindbad.model.SindMedia.mediaType;
import com.leenita.sindbad.model.SindMedia;

import org.json.JSONArray;

import java.util.ArrayList;

public class FragAppMediaList extends Fragment{

    LayoutInflater inflater;
    ArrayList<SindMedia> arrayMedia;
    RecyclerView listView;
    View vNoDataPlaceHolder;
    TextView tvPlaceHolderMsg;

    MediaAdapter adapter;

    public static FragAppMediaList newInstance(ArrayList<SindMedia> collections){
        FragAppMediaList frag = new FragAppMediaList();
        Bundle extras = new Bundle();
        if(collections != null) {
            JSONArray jsonCollections = new JSONArray();
            for (int i = 0; i < collections.size(); i++) {
                jsonCollections.put(collections.get(i).getJsonObject());
            }
            extras.putString("media", jsonCollections.toString());
        }
        frag.setArguments(extras);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_list_view, container, false);
        this.inflater = inflater;
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();

        // read passed data
        try {
            String strApps = getArguments().getString("media");
            JSONArray jsnArrayApps = new JSONArray(strApps);
            ArrayList<SindMedia> arrayApps = new ArrayList<>();
            for (int i = 0; i < jsnArrayApps.length(); i++) {
                arrayApps.add(SindMedia.fromJson(jsnArrayApps.getJSONObject(i)));
            }
            onDataReceived(arrayApps);
        }catch (Exception e){
            onDataReceived(null);
        }
    }

    private void init() {
        listView = (RecyclerView) getView().findViewById(R.id.list);
        vNoDataPlaceHolder = getView().findViewById(R.id.vNoDataPlaceHolder);
        tvPlaceHolderMsg = (TextView) getView().findViewById(R.id.tvPlaceHolderMsg);

        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MediaAdapter(getActivity());
        listView.setAdapter(adapter);

        tvPlaceHolderMsg.setText(R.string.app_details_media_place_holder_no_results);
    }

    public void playYouTubeVideo(String url){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public void updateData(ArrayList<SindMedia> medias){
        onDataReceived(medias);
    }

    private void onDataReceived(ArrayList<SindMedia> media){
        try {
            this.arrayMedia = media;
            adapter.updateAdapter(media);
            if(arrayMedia != null && !arrayMedia.isEmpty()) {
                vNoDataPlaceHolder.setVisibility(View.GONE);
            }else{
                vNoDataPlaceHolder.setVisibility(View.VISIBLE);
            }

            //Store Data Back in bundle
            Bundle outState = getArguments();
            if(arrayMedia != null) {
                JSONArray jsonCollections = new JSONArray();
                for (int i = 0; i < arrayMedia.size(); i++) {
                    jsonCollections.put(arrayMedia.get(i).getJsonObject());
                }
                outState.putString("media", jsonCollections.toString());
            }else
                outState.putString("media", null);
        }catch (Exception ignored){}
    }

    class MediaAdapter extends RecyclerView.Adapter<AppViewHolder> {

        private LayoutInflater inflater;
        ArrayList<SindMedia> arrayMedia;

        OnClickListener onItemClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = listView.getChildPosition(v);
                SindMedia appMedia = arrayMedia.get(itemPosition);
                if(appMedia.getType() == mediaType.IMAGE){
                    DiagMediaGallery diag  = DiagMediaGallery.newInstance(arrayMedia, itemPosition);
                    diag.show(getFragmentManager(),null);
                }else{ // Video
                    playYouTubeVideo(appMedia.getPath());
                }
            }
        };

        public MediaAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void updateAdapter(ArrayList<SindMedia> apps){
            this.arrayMedia = apps;
            notifyDataSetChanged();
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View root = inflater.inflate(R.layout.row_media, viewGroup, false);
            AppViewHolder holder = new AppViewHolder(root);
            root.setOnClickListener(onItemClickListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(AppViewHolder viewHolder, int i) {
            try {
                SindMedia media = arrayMedia.get(i);
                if(media.getType() == mediaType.IMAGE){
                    PhotoProvider.getInstance().displayPhotoNormal(media.getPath(), viewHolder.ivMedia);
                    viewHolder.ivPLayIcon.setVisibility(View.GONE);
                }else{
                    String videoThmbUrl = SindbadApp.getThumbnailURLFromVideoURL(media.getPath());
                    PhotoProvider.getInstance().displayPhotoNormal(videoThmbUrl, viewHolder.ivMedia);
                    viewHolder.ivPLayIcon.setVisibility(View.VISIBLE);
                }
            } catch (Exception ignored) {

            }
        }

        @Override
        public int getItemCount() {
            if(arrayMedia == null)
                return  0;
            return arrayMedia.size();
        }
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {

        ImageView ivMedia, ivPLayIcon;

        public AppViewHolder(View view) {
            super(view);
            ivMedia = (ImageView) view.findViewById(R.id.ivMedia);
            ivPLayIcon = (ImageView) view.findViewById(R.id.ivPLayIcon);
        }
    }
}
