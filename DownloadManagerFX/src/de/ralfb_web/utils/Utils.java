package de.ralfb_web.utils;

import java.awt.Desktop;

import java.net.URI;

public class Utils {

	/**
	 * Method to get the running Java version and return the major number as an int
	 * value
	 * 
	 * @return int JavaVersion Major number
	 */
	public static int getJavaVersion() {

		/**
		 * Returns the Java version as an int value.
		 * 
		 * @return the Java version as an int value (8, 9, etc.)
		 * @since 12130
		 */

		String version = System.getProperty("java.version");
		if (version.startsWith("1.")) {
			version = version.substring(2);
		}
		// Allow these formats:
		// 1.8.0_72-ea
		// 9-ea
		// 9
		// 9.0.1
		int dotPos = version.indexOf('.');
		int dashPos = version.indexOf('-');
		return Integer.parseInt(version.substring(0, dotPos > -1 ? dotPos : dashPos > -1 ? dashPos : 1));
	}
	
	/**
	 * Method to open a given URL in a new Desktop Browser Window
	 */
	public static void onOpenUrl(String url) throws Exception {
			Desktop.getDesktop().browse(new URI(url));
	}
	
	/**
	 * Method to check if 1st word of a given string equals the string "http://" or "https://" and
	 * return true or false.
	 * 
	 * @param url Input URL String to check if 1st word of string equals "http://" or "https://"
	 * @return boolean true | false
	 */
	public static boolean checkforValidUrl(String url) {
		// url.trim() will remove leading whitespace(s) of the string
		String[] parts = url.trim().split(":");
		if (parts[0].equalsIgnoreCase("http") || parts[0].equalsIgnoreCase("https")) {
			return true;
		} else
			return false;
	}

}