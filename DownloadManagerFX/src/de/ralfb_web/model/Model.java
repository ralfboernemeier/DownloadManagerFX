package de.ralfb_web.model;

public class Model {

	/**
	 * Fields
	 */

//	private BooleanProperty exceptionOccured = new SimpleBooleanProperty(false);
	private String logFileLocationString;
	private String osTypeString;
	private String downloadMbps;

	/**
	 * Constructor
	 */
	public Model() {
		super();
	}

	/*
	 * Getter and Setter
	 */

	public String getLogFileLocationString() {
		return logFileLocationString;
	}

	public void setLogFileLocationString(String logFileLocationString) {
		this.logFileLocationString = logFileLocationString;
	}

	public String getOsTypeString() {
		return osTypeString;
	}

	public void setOsTypeString(String osTypeString) {
		this.osTypeString = osTypeString;
	}

	public String getDownloadMbps() {
		return downloadMbps;
	}

	public void setDownloadMbps(String downloadMbps) {
		this.downloadMbps = downloadMbps;
	}

}