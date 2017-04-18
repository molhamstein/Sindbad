package com.leenita.sindbad.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.leenita.sindbad.R;


/**
 * creates a tab bar layout with {@link TextViewCustomFont}
 */
public class CustomDesignSupportTabBar extends TabLayout {

    public CustomDesignSupportTabBar(Context context) {
        super(context);
    }

    public CustomDesignSupportTabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDesignSupportTabBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setTabsFromPagerAdapter(@NonNull PagerAdapter adapter) {
        this.removeAllTabs();
        for (int i = 0, count = adapter.getCount(); i < count; i++) {
            // create custom font textView
            TextViewCustomFont textView = (TextViewCustomFont) LayoutInflater.from(getContext()).inflate(R.layout.row_tab_view_title_blue, null);
            textView.setText(adapter.getPageTitle(i));
            // create new tab
            Tab tab = this.newTab();
            this.addTab(tab);
            tab.setCustomView(textView);
        }
    }

}