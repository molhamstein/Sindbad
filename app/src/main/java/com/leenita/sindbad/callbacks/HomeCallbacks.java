package com.leenita.sindbad.callbacks;


public interface HomeCallbacks {

	void showProgress(boolean show, int msg);
	void setTitle(String title);
    void onTabListScroll(int firstVisibleItemIndex,int previousFirstVisibleItemIndex);
}
