package de.ralfb_web.services;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import de.ralfb_web.utils.Download;
import de.ralfb_web.utils.DownloadListener;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class HttpDownloadTask extends Task<File> implements DownloadListener {

	private Download downloadImpl;

	public HttpDownloadTask(Download downloadImpl) {
		this.downloadImpl = downloadImpl;
	}

	@Override
	protected File call() throws Exception {
		downloadImpl.addDownloadListener(this);
		return downloadImpl.execute();
	}

	@Override
	protected void cancelled() {
		downloadImpl.cancel();
	}

	@Override
	public void onUpdateProgress(int progress, int total) {
		Platform.runLater(() -> updateProgress(progress, total));
	}

	@Override
	public void onUpdateMessage(String message) {
		Platform.runLater(() -> updateMessage(message));
	}

	@Override
	public void onSucceed() {
		succeeded();
	}

	@Override
	public void doCancel() {
		cancel();
	}

	@Override
	public void onFail(Exception ex) {
		// Make sure we will not overwrite messages that are useful in case of trouble.
		StringWriter writer = new StringWriter();
		ex.printStackTrace(new PrintWriter(writer));
		String cause = writer.toString().split("\n")[0];
		Platform.runLater(() -> updateMessage(getMessage() + "\n" + cause));
		throw new RuntimeException(ex);
	}
}