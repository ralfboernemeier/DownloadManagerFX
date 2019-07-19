package de.ralfb_web.utils;

public interface DownloadListener {
	void onUpdateProgress(int progress, int total);

	void onUpdateMessage(String message);

	void onSucceed();

	void doCancel();

	void onFail(Exception ex);
	
}
