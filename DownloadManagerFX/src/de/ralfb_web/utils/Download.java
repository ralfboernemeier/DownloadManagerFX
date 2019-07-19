package de.ralfb_web.utils;

import java.io.File;

public interface Download {
	void addDownloadListener(DownloadListener listener);

	void removeDownloadListener(DownloadListener listener);
	
	void cancel();

	File execute();
}
