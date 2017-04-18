package com.leenita.sindbad.view;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.leenita.sindbad.R;
import com.leenita.sindbad.data.PhotoProvider;
import com.leenita.sindbad.model.SindBrand;


public class AppParallaxBgScroller extends ScrollView {

    public interface ScrollViewListener {
        void onScrollChanged(AppParallaxBgScroller scrollView, int x, int y, int oldx, int oldy);
    }

    private ScrollViewListener scrollViewListener = null;

    public AppParallaxBgScroller(Context context) {
        super(context);
    }

    public AppParallaxBgScroller(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AppParallaxBgScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }
}