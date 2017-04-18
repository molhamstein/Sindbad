package com.leenita.sindbad.data;


public interface FacebookProviderListener 
{
	void onFacebookSessionOpened(String accessToken, String userId);
	
	void onFacebookSessionClosed();
	
	void onFacebookException(Exception exception);
	
/*	void onFacebookFriendsReceived(ArrayList<AppFacebookFriend> arrayFriends, ArrayList<String> arrayActiveIds);*/
}
