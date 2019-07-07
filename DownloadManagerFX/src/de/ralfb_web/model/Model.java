package de.ralfb_web.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Model {

	/**
	 * Fields
	 */

	private BooleanProperty exceptionOccured = new SimpleBooleanProperty(false);
	private String logFileLocationString;
	private String osTypeString;

	/**
	 * Constructor
	 */
	public Model() {
		super();
	}

	/*
	 * Getter and Setter
	 */

	public BooleanProperty getExceptionOccured() {
		return exceptionOccured;
	}

	public void setExceptionOccured(Boolean exceptionOccured) {
		this.exceptionOccured.setValue(exceptionOccured);
	}

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

}