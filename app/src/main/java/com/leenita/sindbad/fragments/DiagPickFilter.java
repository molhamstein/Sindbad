package com.leenita.sindbad.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.leenita.sindbad.R;
import com.leenita.sindbad.SearchActivity;
import com.leenita.sindbad.SindbadApp;
import com.leenita.sindbad.data.DataStore;
import com.leenita.sindbad.model.AppBaseModel;
import com.leenita.sindbad.model.SindCategory;
import com.leenita.sindbad.view.SearchView;

import java.util.ArrayList;



/**
 * shows a list of strings to pick one item out of it
 */
public class DiagPickFilter extends Dialog implements OnClickListener , OnChildClickListener{

    SearchActivity activity;
	SearchView.SearchViewCallback callBack;
	Context context ;

	ExpandableListView lvSelecter=null;
	SelectorAdapter adapter = null;

	TextView selectButton , cancelButton , tvTitle;
	View vHeaderContainer ;

    //Filterstypes
    //Filter brands according the selected categories ids
	private ArrayList<SindCategory> providersTypes;
    private ArrayList<SindCategory> selectedProviderTypes;

	public DiagPickFilter(Context context, ArrayList<SindCategory> selectedCategories , SearchView.SearchViewCallback callback) {
		super(context, R.style.full_screen_dialog);
		this.context = context;
		this.callBack = callback;

        if(selectedCategories != null)
            selectedProviderTypes = selectedCategories;
        else
            selectedProviderTypes = new ArrayList<>();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.diag_selecter);
		init();
	}

	private void init() {
		tvTitle=(TextView)findViewById(R.id.tvTitle);
		vHeaderContainer = findViewById(R.id.vHeaderContainer);
		cancelButton=(TextView)findViewById(R.id.cancel);
		selectButton=(TextView)findViewById(R.id.select);

		cancelButton.setOnClickListener(this);
		selectButton.setOnClickListener(this);

		//Data
		providersTypes = DataStore.getInstance().getArrayCategories();

		/// Note ----------
		// dont swipe the following 2 blocks of code, we need to get the data before creating the adapter;

		//init List
		lvSelecter = (ExpandableListView) findViewById(R.id.countryList);
		adapter= new SelectorAdapter(context);
		lvSelecter.setAdapter(adapter);
		lvSelecter.setOnChildClickListener(this);
		lvSelecter.setOnGroupClickListener(adapter);

		// window params
		WindowManager.LayoutParams wmlp = getWindow().getAttributes();
		wmlp.gravity = Gravity.TOP |Gravity.CENTER_HORIZONTAL;
		wmlp.verticalMargin = 0f;
		wmlp.horizontalMargin = 0f;

		getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
		getWindow().setAttributes(wmlp);
		wmlp.x = 0;
		wmlp.y = 0;
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	private void setFilters(){
        if (callBack != null)
            callBack.onFilterSelect(selectedProviderTypes);
    }

	@Override
	public void onClick(View v) {
		int id=v.getId();
		switch (id) {
		case R.id.select:
			setFilters();
			break;
		case R.id.cancel:
			break;
		}
		dismiss();
	}

	@Override
	public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
		if(i1 <= 0) { // the first element in every group is "select all", so we do nothing here
            selectedProviderTypes.clear();
		}else {
				SindCategory type = providersTypes.get(i1-1);
				if(AppBaseModel.getById(selectedProviderTypes, type.getId()) != null){
					AppBaseModel.removeFromListById(selectedProviderTypes, type.getId());
				}else{
					selectedProviderTypes.add(type);
				}
		}
		adapter.notifyDataSetChanged();
		return false;
	}

	class SelectorAdapter extends BaseExpandableListAdapter implements OnGroupClickListener {

		private LayoutInflater inflater;

		public SelectorAdapter(Context context) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public boolean onGroupClick(ExpandableListView parent, View clickedView, int groupPosition, long rowId) {
			ImageView groupIndicator = (ImageView) clickedView.findViewById(R.id.ivIndicator);
			if (parent.isGroupExpanded(groupPosition)) {
				parent.collapseGroup(groupPosition);
				groupIndicator.setImageResource(R.drawable.ic_arrow_down);
			} else {
				parent.expandGroup(groupPosition);
				groupIndicator.setImageResource(R.drawable.ic_arrow_up);
			}
			return true;
		}

		@Override
		public int getGroupCount() {
			return 1;
		}

		@Override
		public int getChildrenCount(int i) {
				if(providersTypes != null)
					return providersTypes.size()+1;
			return  0;
		}

		@Override
		public Object getGroup(int i) {
			return null;
		}

		@Override
		public Object getChild(int i, int i1) {
			return null;
		}

		@Override
		public long getGroupId(int i) {
			return 0;
		}

		@Override
		public long getChildId(int i, int i1) {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int i, boolean b, View convertView, ViewGroup viewGroup) {
			GroupRowViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_diag_filter_group, viewGroup, false);
				holder = new GroupRowViewHolder();
				holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
				convertView.setTag(holder);
			} else {
				holder = (GroupRowViewHolder) convertView.getTag();
			}
			try {
					holder.tvTitle.setText(R.string.filters_group_types);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return convertView;
		}

		@Override
		public View getChildView(int i, int i1, boolean b, View convertView, ViewGroup viewGroup) {
			ChildRowViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_diag_filter_child, viewGroup, false);
				holder = new ChildRowViewHolder();
				holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
				holder.ivIndicator = (ImageView) convertView.findViewById(R.id.ivIndicator);
				holder.btnInfo = convertView.findViewById(R.id.ivInfo);
				holder.vRowContainer = convertView.findViewById(R.id.vRowContainer);
				convertView.setTag(holder);
			} else {
				holder = (ChildRowViewHolder) convertView.getTag();
			}try{
				String title ;
				 {
					/// first element in this group is "select All"
					if(i1 == 0){
						title = SindbadApp.getAppContext().getString(R.string.filters_select_all);
						if(selectedProviderTypes == null || selectedProviderTypes.isEmpty()) {
							holder.ivIndicator.setImageResource(R.drawable.ic_check_active);
							holder.vRowContainer.setBackgroundResource(R.color.filter_item_bg_active);
						}else {
							holder.ivIndicator.setImageResource(R.drawable.ic_check);
							holder.vRowContainer.setBackgroundResource(R.color.filter_item_bg);
						}
					}else {
						SindCategory type = providersTypes.get(i1 - 1);
						title = type.getName();
						if (AppBaseModel.getById(selectedProviderTypes, type.getId()) != null) {
							holder.ivIndicator.setImageResource(R.drawable.ic_check_active);
							holder.vRowContainer.setBackgroundResource(R.color.filter_item_bg_active);
						}else {
							holder.ivIndicator.setImageResource(R.drawable.ic_check);
							holder.vRowContainer.setBackgroundResource(R.color.filter_item_bg);
						}
					}
					holder.btnInfo.setVisibility(View.GONE);
				}
				holder.tvTitle.setText(title);
			}catch (Exception e) {
				e.printStackTrace();
			}
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int i, int i1) {
			return true;
		}
	}

	private static class GroupRowViewHolder{
		TextView tvTitle;
	}

	private static class ChildRowViewHolder{
		TextView tvTitle;
		ImageView ivIndicator;
		View btnInfo, vRowContainer;
	}

}
