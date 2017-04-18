package com.leenita.sindbad.fragments;

import android.content.Context;
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

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

import com.leenita.sindbad.SindbadApp;
import com.leenita.sindbad.R;
import com.leenita.sindbad.data.DataStore;
import com.leenita.sindbad.data.DataStore.DataRequestCallback;
import com.leenita.sindbad.data.PhotoProvider;
import com.leenita.sindbad.data.ServerResult;
import com.leenita.sindbad.data.TrackingMgr;
import com.leenita.sindbad.model.AppUser;
import jp.wasabeef.recyclerview.animators.adapters.SlideInLeftAnimationAdapter;

public class FragUsersList extends Fragment{

    public enum FRAG_USERS_TYPE {UP_VOTERS, FOLLOWERS, FOLLOWING, SEARCH_RESULTS }

    ArrayList<AppUser> arrayUsers;
    FRAG_USERS_TYPE type;

    LayoutInflater inflater;
    RecyclerView listView;
    View vNoDataPlaceHolder;
    TextView tvPlaceHolderMsg;

    SlideInLeftAnimationAdapter animationsAdapter;
    UsersAdapter adapter;

    //indicates if we should animateOut an element when the user unFollows him,
    //this feature is used when displaying my following list
    boolean removeUnFollowedElements = false;

    /**
     * creates a properly configured instance of the fragment
     * @param removeUnFollowedElements : used in case we want to animate out the elements that the users unfollows
     */
    public static FragUsersList newInstance(ArrayList<AppUser> users, FRAG_USERS_TYPE type, boolean removeUnFollowedElements){
        FragUsersList frag = new FragUsersList();
        Bundle extras = new Bundle();
        if(users != null) {
            JSONArray jsonUsers = new JSONArray();
            for (int i = 0; i < users.size(); i++){
                jsonUsers.put(users.get(i).getJsonObject());
            }
            extras.putString("users", jsonUsers.toString());
            extras.putSerializable("type", type);
            extras.putBoolean("removeUnFollowedElements", removeUnFollowedElements);
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
            type = (FRAG_USERS_TYPE) getArguments().getSerializable("type");
            String strApps = getArguments().getString("users");
            JSONArray jsnArrayApps = new JSONArray(strApps);
            ArrayList<AppUser> arrayApps = new ArrayList<>();
            for (int i = 0; i < jsnArrayApps.length(); i++) {
                 arrayApps.add(AppUser.fromJson(jsnArrayApps.getJSONObject(i)));
            }

            //should we remove "animate out" the unliked elements
            removeUnFollowedElements = getArguments().getBoolean("removeUnFollowedElements", false);
            onDataReceived(arrayApps);
        }catch (Exception e){
            onDataReceived(null);
        }
    }

    private void init() {
        listView = (RecyclerView) getView().findViewById(R.id.list);
        vNoDataPlaceHolder = getView().findViewById(R.id.vNoDataPlaceHolder);
        tvPlaceHolderMsg = (TextView) getView().findViewById(R.id.tvPlaceHolderMsg);

        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new UsersAdapter(getActivity());
        animationsAdapter = new SlideInLeftAnimationAdapter(adapter);
        listView.setAdapter(animationsAdapter);

        //tvPlaceHolderMsg.setText(R.string.app_details_up_place_holder_no_results);
    }

    public void updateData(ArrayList<AppUser> collections){
        onDataReceived(collections);
    }

    private void onDataReceived(ArrayList<AppUser> users){
        try {
            this.arrayUsers = users;
            adapter.updateAdapter();
            if(arrayUsers != null && !arrayUsers.isEmpty()) {
                vNoDataPlaceHolder.setVisibility(View.GONE);
            }else{
                vNoDataPlaceHolder.setVisibility(View.VISIBLE);
            }
            //Store Data Back in bundle
            Bundle outState = getArguments();
            if(arrayUsers != null) {
                JSONArray jsonCollections = new JSONArray();
                for (int i = 0; i < arrayUsers.size(); i++) {
                    jsonCollections.put(arrayUsers.get(i).getJsonObject());
                }
                outState.putString("users", jsonCollections.toString());
            }else
                outState.putString("users", null);
            outState.putBoolean("removeUnFollowedElements", removeUnFollowedElements);
        }catch (Exception ignored){
        }
    }

    class UsersAdapter extends RecyclerView.Adapter<AppViewHolder> {

        private LayoutInflater inflater;

        OnClickListener onItemClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = listView.getChildPosition(v);
                AppUser user = arrayUsers.get(itemPosition);
                if (user != null) {
                    SindbadApp.showUserProfile(getActivity(), user, v.findViewById(R.id.ivUser), v.findViewById(R.id.tvName));
                }
            }
        };

        OnClickListener onBtnFollowClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int index  = (Integer) v.getTag();
                AppUser user = arrayUsers.get(index);
                if (user != null) {
                    //TODO send follow/unfollow request & update views accordingly
//                    boolean res = DataStore.getInstance().toggleFollow(getActivity(), user, new DataRequestCallback() {
//                        @Override
//                        public void onDataReady(ServerResult resutl, boolean success) {
//                            //required cuz we dont call notifyDataSetChanged directly in case of removing an element
//                            animationsAdapter.notifyDataSetChanged();
//                        }
//                    });
//                    if(res) {
//                        if(user.getIsFollowing())
//                            TrackingMgr.getInstance().sendTrackingEvent("Subscribe","Follow","UsersList");
//                        else {
//                            TrackingMgr.getInstance().sendTrackingEvent("Subscribe", "UnFollow", "UsersList");
//                            if(removeUnFollowedElements) {
//                                arrayUsers.remove(index);
//                                animationsAdapter.notifyItemRemoved(index); // animate out and remove
//                            }else
//                                animationsAdapter.notifyDataSetChanged();
//                        }
//                    }
                }
            }
        };

        public UsersAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void updateAdapter(){
            animationsAdapter.notifyDataSetChanged();
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View root = inflater.inflate(com.leenita.sindbad.R.layout.row_user, viewGroup, false);
            AppViewHolder holder = new AppViewHolder(root);
            root.setOnClickListener(onItemClickListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(AppViewHolder viewHolder, int i) {
            try {
                AppUser user = arrayUsers.get(i);
                viewHolder.tvName1.setText(user.getDisplayName());
                viewHolder.tvDesc.setText(user.getTwitterName());
                PhotoProvider.getInstance().displayPhotoNormal(user.getProfileImage(), viewHolder.ivPic);
                viewHolder.btnFollow.setOnClickListener(onBtnFollowClickListener);
                viewHolder.btnFollow.setTag(i);
                if(user.getIsFollowing()) {
                    viewHolder.btnFollow.setBackgroundResource(R.drawable.shape_rounded_unfollow);
                    viewHolder.btnFollow.setTextColor(getResources().getColor(R.color.twitter));
                    viewHolder.btnFollow.setText(R.string.unfollow);
                }else{
                    viewHolder.btnFollow.setBackgroundResource(R.drawable.shape_rounded_follow);
                    viewHolder.btnFollow.setTextColor(getResources().getColor(android.R.color.white));
                    viewHolder.btnFollow.setText(R.string.follow);
                }
                if(user.getId().equals(DataStore.getInstance().getMe().getId()))
                    viewHolder.btnFollow.setVisibility(View.INVISIBLE);
                else
                    viewHolder.btnFollow.setVisibility(View.VISIBLE);

            } catch (Exception ignored) {

            }
        }

        @Override
        public int getItemCount() {
            if(arrayUsers == null)
                return  0;
            return arrayUsers.size();
        }
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {

        TextView tvName1, tvDesc, btnFollow;
        ImageView ivPic;

        public AppViewHolder(View view) {
            super(view);
            tvName1 = (TextView) view.findViewById(R.id.tvName);
            tvDesc = (TextView) view.findViewById(R.id.tvDesc);
            ivPic = (ImageView) view.findViewById(R.id.ivUser);
            btnFollow = (TextView) view.findViewById(R.id.btnFollow);
        }
    }
}
