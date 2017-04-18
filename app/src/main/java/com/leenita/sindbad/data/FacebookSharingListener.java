package com.leenita.sindbad.data;

public interface FacebookSharingListener
{
	void onShareResult(boolean success);
	
	void onShareError(String error);
	
	void onShareCancelled();
}
