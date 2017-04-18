package com.leenita.sindbad.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.leenita.sindbad.R;
import com.leenita.sindbad.SindbadApp;
import com.leenita.sindbad.data.PhotoProvider;
import com.leenita.sindbad.model.SindCategory;

public class ViewCategoryRow extends FrameLayout{

	TextView tvTitle;
	ImageView ivCardBg;

	//data
	SindCategory category;

	public ViewCategoryRow(Context context){
		super(context);
		init();
	}

	public ViewCategoryRow(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}

	public ViewCategoryRow(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init();
	}
	
	private void init(){
		try {
			// inflate layout
			inflate(getContext(), R.layout.row_category_card, this);
			int padding = SindbadApp.getPXSize(16);
			setPadding(padding, padding/2, padding, padding/2);
			if(!isInEditMode()) {
				tvTitle = (TextView) findViewById(R.id.tvTitle);
				ivCardBg = (ImageView) findViewById(R.id.ivCategory);
			}
		}catch (Exception ignored) {}
	}


	public void update(SindCategory category){
		this.category = category;
		if(category !=null){
			tvTitle.setText(category.getName());
			PhotoProvider.getInstance().displayPhotoNormal(category.getCover(), ivCardBg);
		}
	}

	public SindCategory getCategory() {
		return category;
	}
}
