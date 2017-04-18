package com.leenita.sindbad;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.leenita.sindbad.data.DataStore;
import com.leenita.sindbad.data.PhotoProvider;
import com.leenita.sindbad.model.SindCategory;

import java.util.ArrayList;

/**
 * Created by Molham on 10/20/15.
 *-------------------------------------------------
 */
public class ChooseFavCategoriesActivity extends BaseActivity{

    RecyclerView list;
    CategoriesAdapter adapter;
    ArrayList<SindCategory> arrayCategories;
    ArrayList<String> selectedCategoriesIds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_fav_categories);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            init();
        }
        selectedCategoriesIds = new ArrayList<>();
    }

    @Override public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        init();
    }

    private void init(){
        list = (RecyclerView) findViewById(R.id.rvGrid);

        arrayCategories = DataStore.getInstance().getArrayCategories();
        selectedCategoriesIds = new ArrayList<>();

        list.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new CategoriesAdapter(this);
        list.setAdapter(adapter);
        list.scheduleLayoutAnimation();
    }

    class CategoriesAdapter extends RecyclerView.Adapter<AppViewHolder> {

        private LayoutInflater inflater;

        OnClickListener onItemClickListner = new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int itemPosition = list.getChildLayoutPosition(view);
                    SindCategory app = arrayCategories.get(itemPosition);
                    if (selectedCategoriesIds.contains(app.getId())) {
                        selectedCategoriesIds.remove(app.getId());
                    }else{
                        selectedCategoriesIds.add(app.getId());
                    }
                    if(selectedCategoriesIds.size() >= 3){
                        setResult(RESULT_OK);
                        finish();
                    }
                    adapter.notifyDataSetChanged();
                }catch (Exception ignored){}
            }
        };

        public CategoriesAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View root = inflater.inflate(R.layout.row_fav_category, viewGroup, false);
            AppViewHolder holder = new AppViewHolder(root);
            root.setOnClickListener(onItemClickListner);
            return holder;
        }

        @Override
        public void onBindViewHolder(AppViewHolder viewHolder, int i) {
            try {
                final SindCategory application = arrayCategories.get(i);
                viewHolder.tvName.setText(application.getName());
                PhotoProvider.getInstance().displayPhotoNormal(application.getPhoto(), viewHolder.ivIcon);
                viewHolder.ivIcon.setTag(i);
                if(selectedCategoriesIds.contains(application.getId())){
                    viewHolder.tvName.setAlpha(1f);
                    viewHolder.ivIcon.setAlpha(1f);
                }else{
                    viewHolder.tvName.setAlpha(0.5f);
                    viewHolder.ivIcon.setAlpha(0.5f);
                }
            } catch (Exception ignored) {}
        }

        @Override
        public int getItemCount() {
            if(arrayCategories == null)
                return  0;
            return arrayCategories.size();
//            return 10;
        }
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivIcon;
        public AppViewHolder(View view) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.tvName);
            ivIcon = (ImageView) view.findViewById(R.id.ivIcon);
        }
    }
}
