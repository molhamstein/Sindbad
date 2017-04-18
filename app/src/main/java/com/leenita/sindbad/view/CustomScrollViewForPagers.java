package com.leenita.sindbad.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;

import com.leenita.sindbad.R;


/**
 * creates a tab bar layout with {@link TextViewCustomFont}
 */
public class CustomScrollViewForPagers extends NestedScrollView {

    GestureDetector mGestureDetector;

    public CustomScrollViewForPagers(Context context) {
        super(context);
        init(context);
    }

    public CustomScrollViewForPagers(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomScrollViewForPagers(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        mGestureDetector = new GestureDetector(context, new YScrollDetector());
        setFadingEdgeLength(0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean res = super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
        Log.d("scroll","SV :"+res);
        return res;//super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
    }

    // Return false if we're scrolling in the x direction
    class YScrollDetector extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return Math.abs(distanceY) > Math.abs(distanceX);
        }
    }
}