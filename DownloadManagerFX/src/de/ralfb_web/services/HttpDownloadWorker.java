package de.ralfb_web.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import de.ralfb_web.Main;
import de.ralfb_web.model.Model;
import de.ralfb_web.utils.*;

public class HttpDownloadWorker implements Download {

	/**
	 * Fields
	 */
	private List<DownloadListener> listeners;
	private String link;
	private Boolean useProxy;
	@SuppressWarnings("unused")
	private String proxyServer;
	@SuppressWarnings("unused")
	private int proxyPort;
	private File downloadFolderFile;
	private Proxy proxy;
	private HttpURLConnection hConnection;
	private Boolean isCanceled;
	private Model model;

	/**
	 * Constructor
	 * 
	 * @param link
	 * @param downloadFolderFile
	 * @param useProxy
	 * @param proxyServer
	 * @param proxyPort
	 */
	public HttpDownloadWorker(Model model, String link, File downloadFolderFile, Boolean useProxy, String proxyServer,
			int proxyPort) {
		this.listeners = new ArrayList<>();
		this.link = link;
		this.downloadFolderFile = downloadFolderFile;
		this.useProxy = useProxy;
		this.proxyServer = proxyServer;
		this.proxyPort = proxyPort;

		if (!downloadFolderFile.exists()) {
			downloadFolderFile.mkdirs();
		}

		this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyServer, proxyPort));
		this.isCanceled = false;
		this.model = model;
	}

	/**
	 * Constructor
	 * 
	 * @param link
	 * @param downloadFolderFile
	 * @param useProxy
	 */
	public HttpDownloadWorker(String link, File downloadFolderFile, Boolean useProxy) {
		this.listeners = new ArrayList<>();
		this.link = link;
		this.downloadFolderFile = downloadFolderFile;
		this.useProxy = useProxy;

		if (!downloadFolderFile.exists()) {
			downloadFolderFile.mkdirs();
		}
		this.isCanceled = false;
	}

	/**
	 * <h3>Logger configuration.</h3> The properties file cannot be directly loaded
	 * from classpath by using java.util.logging.config.file system property.
	 * <p>
	 * <u>Reference:</u>
	 * </p>
	 * <a>https://www.logicbig.com/tutorials/core-java-tutorial/logging/loading-properties.html</a>
	 */
	private static Logger LOGGER;
	static {
		InputStream stream = Main.class.getClassLoader().getResourceAsStream("de/ralfb_web/config/logging.properties");
		try {
			LogManager.getLogManager().readConfiguration(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER = Logger.getLogger(Main.class.getName());
	}

	public File execute() {
		File outputFile = null;
		String saveFileName = null;
		try {
			URL url = new URL(link);
			if (useProxy) {
				hConnection = (HttpURLConnection) url.openConnection(proxy);
			} else {
				hConnection = (HttpURLConnection) url.openConnection();
			}
			listeners.forEach(l -> l.onUpdateMessage(""));

			// Try to determine the filesize
			listeners.forEach(l -> l.onUpdateMessage("Determine Filesize ..."));

			int fileSize = getFileSize();
			if (fileSize == -1) {
				return null;
			}

			listeners.forEach(l -> {
				l.onUpdateProgress(0, fileSize);
				l.onUpdateMessage(String.format("Download: %d / %d Byte ", 0, fileSize));
			});
			BufferedInputStream bufferedInputStream = new BufferedInputStream(hConnection.getInputStream());

			saveFileName = link.substring(link.lastIndexOf('/') + 1);
			outputFile = new File(downloadFolderFile, saveFileName);
			OutputStream outputStream = new FileOutputStream(outputFile);
			BufferedOutputStream bOutputStream = new BufferedOutputStream(outputStream, 1024);

			byte[] buffer = new byte[1024];
			int downloaded = 0;
			int readByte = 0;

			// Variables needed by calculating throughput
			long start = System.nanoTime();
			final double NANOS_PER_SECOND = 1000000000.0;
			final double BYTES_PER_MB = 1024 * 1024;
			double speedInMBps = 0;
			double speedInMbps = 0;

			while ((readByte = bufferedInputStream.read(buffer, 0, 1024)) >= 0 && !isCanceled) {
				bOutputStream.write(buffer, 0, readByte);
				downloaded += readByte;

				final int progress = downloaded;
				listeners.forEach(l -> {
					l.onUpdateProgress(progress, fileSize);
					l.onUpdateMessage(String.format("Download: %d / %d Byte ", progress, fileSize));
				});
			}
			LOGGER.info(String.format("Download: %d / %d Byte ", downloaded, fileSize));
			speedInMBps = NANOS_PER_SECOND / BYTES_PER_MB * downloaded / (System.nanoTime() - start);
			speedInMbps = speedInMBps * 8;
			NumberFormat nfFormat = NumberFormat.getInstance();
			nfFormat.setMaximumFractionDigits(2);
			model.setDownloadMbps(String.valueOf(nfFormat.format(speedInMbps)));
			LOGGER.info("Throughput Mbps: " + String.valueOf(nfFormat.format(speedInMbps)));

			// Close BufferedStreams
			bOutputStream.close();
			bufferedInputStream.close();

			// Check for isCanceled - if yes due to Stop button pressed, set message and
			// inform DownloadListener about the cancel
			if (isCanceled) {
				listeners.forEach(l -> l.onUpdateMessage("Download cancelled by User request."));
				LOGGER.info("Download cancelled by User request.");
				listeners.forEach(DownloadListener::doCancel);
			}

		} catch (Exception ex) {
			listeners.forEach(l -> {
				l.onUpdateMessage("Error occured during download!");
				LOGGER.severe("Error occured during download!");
				l.onUpdateProgress(0, 0);
				l.onFail(ex);
			});
		}
		return outputFile;
	}

	private int getFileSize() {
		URLConnection conn = null;
		try {
			URL url = new URL(link);
			if (useProxy) {
				conn = url.openConnection(proxy);
			} else {
				conn = url.openConnection();
			}

			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).setRequestMethod("HEAD");
			}

			conn.getInputStream();

			return conn.getContentLength();
		} catch (Exception ex) {
			listeners.forEach(l -> {
				l.onUpdateMessage("Error during determing filesize!");
				LOGGER.severe("Error during determing filesize!");
				l.onUpdateProgress(0, 0);
				l.onFail(ex);
			});
			return -1;
		} finally {
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).disconnect();
			}
		}
	}

	@Override
	public void addDownloadListener(DownloadListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeDownloadListener(DownloadListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public void cancel() {
		this.isCanceled = true;
	}

}
