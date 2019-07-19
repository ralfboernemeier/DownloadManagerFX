package de.ralfb_web.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import de.ralfb_web.Main;
import de.ralfb_web.model.Model;
import de.ralfb_web.services.HttpDownloadTask;
import de.ralfb_web.services.HttpDownloadWorker;
import de.ralfb_web.utils.FadeToggleButton;
import de.ralfb_web.utils.ModelInjectable;
import de.ralfb_web.utils.Utils;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;

public class MainController implements ModelInjectable {

	/**
	 * Constructor
	 */
	public MainController() {
		super();

		// Load default.properties
		try {
			InputStream in = Main.class.getResourceAsStream("default.properties");
			this.defaultProps = new Properties();
			defaultProps.load(in);
			in.close();
		} catch (Exception ex) {
			String msg = String.valueOf(ex);
			LOGGER.severe(msg);
		}
	}

	/**
	 * Fields
	 */
	private Properties defaultProps;
	private Model model;
	private FadeToggleButton toggleStartStopDownloadButton;
	private final DirectoryChooser downloadFolderDirectoryChooser = new DirectoryChooser();
	private Service<File> downloadService;

	@Override
	public void setModel(Model model) {
		this.model = model;
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

	/**
	 * Initialize Method will be executed after all FXML Nodes are ready. This is
	 * the place for Initialization of Nodes.
	 */
	public void initialize() {

		proxyServerTextField.setText(defaultProps.getProperty("app.http_proxy_server"));
		proxyPortTextField.setText(defaultProps.getProperty("app.http_proxy_server_port"));

		// Add an InvalidationListener to the TextField to make sure the SourceURL,
		// DownloadFolder,
		// and Proxy Server / Proxy Port are not empty.
		// For the SourceURL check if the String starts with either http or https.
		downloadFolderTextField.textProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable arg0) {
				if (downloadFolderTextField.getText().equals("") || sourceUrlTextField.getText().equals("")) {
					startDownloadButton.setDisable(true);
					startDownloadButton.setOpacity(0.25);
				} else {
					startDownloadButton.setDisable(false);
					startDownloadButton.setOpacity(1.0);
				}
			}
		});

		sourceUrlTextField.textProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable arg0) {
				if (sourceUrlTextField.getText().equals("") || downloadFolderTextField.getText().equals("")) {
					startDownloadButton.setDisable(true);
					startDownloadButton.setOpacity(0.25);
				} else {
					if (Utils.checkforValidUrl(sourceUrlTextField.getText())) {
						startDownloadButton.setDisable(false);
						startDownloadButton.setOpacity(1.0);
					} else {
						startDownloadButton.setDisable(true);
						startDownloadButton.setOpacity(0.25);
					}
				}

			}
		});

		httpProxyCheckBox.selectedProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable arg0) {
				if (httpProxyCheckBox.isSelected()) {
					if (proxyServerTextField.getText().equals("") || proxyPortTextField.getText().equals("")) {
						startDownloadButton.setDisable(true);
						startDownloadButton.setOpacity(0.25);
					} else {
						if (!downloadFolderTextField.getText().equals("") && !sourceUrlTextField.getText().equals("")) {
							startDownloadButton.setDisable(false);
							startDownloadButton.setOpacity(1.0);
						}
					}
				} else {
					if (!downloadFolderTextField.getText().equals("") && !sourceUrlTextField.getText().equals("")) {
						startDownloadButton.setDisable(false);
						startDownloadButton.setOpacity(1.0);
					}
				}

			}
		});

		// Set the graphic for the buttons
		startDownloadButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PLAY_CIRCLE, "3.0em"));
		stopDownloadButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.STOP_CIRCLE, "3.0em"));
		speedButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TACHOMETER, "2.0em"));

		// Initialize FadeTransition Show Start/Stop Check Button
		toggleStartStopDownloadButton = new FadeToggleButton(startDownloadButton, stopDownloadButton);

		// Open DirectoryChooser if Mouse clicked on TextFiled downloadFolderTextField.
		// Set chosen download directory value string to the TextField.
		downloadFolderTextField.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				// Set the DirectoryChooser Appearance
				configuringDirectoryChooser(downloadFolderDirectoryChooser);
				File dirPathDownloadFolder = downloadFolderDirectoryChooser
						.showDialog(downloadFolderTextField.getScene().getWindow());
				if (dirPathDownloadFolder != null) {
					downloadFolderTextField.setText(dirPathDownloadFolder.getAbsolutePath());
				}
			}
		});

		downloadService = new Service<File>() {
			@Override
			protected Task<File> createTask() {

				// Implementation von @robat
				File downloadFolderFile = new File(downloadFolderTextField.getText());
				HttpDownloadTask downloadTask = new HttpDownloadTask(new HttpDownloadWorker(model,
						sourceUrlTextField.getText(), downloadFolderFile, httpProxyCheckBox.isSelected(),
						proxyServerTextField.getText(), Integer.valueOf(proxyPortTextField.getText())));

				// Bind messageProperty/progressProperty updated by the downloadTask to the
				// messsages TextField
				messages.textProperty().bind(downloadTask.messageProperty());
				pBar.progressProperty().bind(downloadTask.progressProperty());

				// Implement the Task EventHandler for the different transition states
				// (SUCCEEDED, CANCELED, FAILED)
				downloadTask.setOnSucceeded(event -> {
					LOGGER.info("Download Manager Task Succeeded!");
					toggleStartStopDownloadButton.setFadeTransitionShowButtonOne(true);
					downloadFolderTextField.setDisable(false);
					sourceUrlTextField.setDisable(false);
					if (httpProxyCheckBox.isSelected()) {
						proxyServerTextField.setDisable(false);
						proxyServerTextField.setOpacity(1.0);
						proxyPortTextField.setDisable(false);
						proxyPortTextField.setOpacity(1.0);
					}
					httpProxyCheckBox.setDisable(false);
					speedButton.setOpacity(1);
					mbpsLabel.setText(model.getDownloadMbps() + " Mbps");
				});
				downloadTask.setOnCancelled(event -> {
					LOGGER.info("Download Manager Task Cancelled!");
					toggleStartStopDownloadButton.setFadeTransitionShowButtonOne(true);
					downloadFolderTextField.setDisable(false);
					sourceUrlTextField.setDisable(false);
					if (httpProxyCheckBox.isSelected()) {
						proxyServerTextField.setDisable(false);
						proxyServerTextField.setOpacity(1.0);
						proxyPortTextField.setDisable(false);
						proxyPortTextField.setOpacity(1.0);
					}
					httpProxyCheckBox.setDisable(false);
					speedButton.setOpacity(0);
					mbpsLabel.setText("");
				});
				downloadTask.setOnFailed(event -> {
					LOGGER.info("Download Manager Task Failed!");
					toggleStartStopDownloadButton.setFadeTransitionShowButtonOne(true);
					downloadFolderTextField.setDisable(false);
					sourceUrlTextField.setDisable(false);
					if (httpProxyCheckBox.isSelected()) {
						proxyServerTextField.setDisable(false);
						proxyServerTextField.setOpacity(1.0);
						proxyPortTextField.setDisable(false);
						proxyPortTextField.setOpacity(1.0);
					}
					httpProxyCheckBox.setDisable(false);
					speedButton.setOpacity(0);
					mbpsLabel.setText("");
				});

				return downloadTask;
			}
		};

	}

	/**
	 * Initialize FXML Nodes (fx:id)
	 */

	@FXML
	TextField sourceUrlTextField;

	@FXML
	TextField downloadFolderTextField;

	@FXML
	Button startDownloadButton;

	@FXML
	Button stopDownloadButton;

	@FXML
	Button speedButton;

	@FXML
	Label mbpsLabel;

	@FXML
	ProgressBar pBar = new ProgressBar();

	@FXML
	TextArea messages;

	@FXML
	CheckBox httpProxyCheckBox;

	@FXML
	Label proxyServerLabel;

	@FXML
	TextField proxyServerTextField;

	@FXML
	Label proxyPortLabel;

	@FXML
	TextField proxyPortTextField;

	/**
	 * Method to be executed if startDownloadButton is pressed. The method will
	 * start a Service to start downloading the URL.
	 */

	public void startDownloadButtonTapped() {
		downloadFolderTextField.setDisable(true);
		sourceUrlTextField.setDisable(true);
		mbpsLabel.setText("");
		speedButton.setOpacity(0);
		if (httpProxyCheckBox.isSelected()) {
			proxyServerTextField.setDisable(true);
			proxyServerTextField.setOpacity(0.25);
			proxyPortTextField.setDisable(true);
			proxyPortTextField.setOpacity(0.25);
		}
		httpProxyCheckBox.setDisable(true);
		toggleStartStopDownloadButton.setFadeTransitionShowButtonOne(false);
		if (!downloadService.isRunning()) {
			LOGGER.info("Start downloading service task ...");
			downloadService.reset();
			downloadService.start();
		}

	}

	/**
	 * Method to be executed if stopDownloadButton is pressed. The method will stop
	 * the service that was started by the startDownloadButtonTapped method.
	 */

	public void stopDownloadButtonTapped() {
		downloadService.cancel();
		LOGGER.info("Stop button pressed. Stop downloading service task now!");
	}

	/**
	 * Method to configure the DirectoryChooser appearance.
	 * 
	 * @param directoryChooser DirectoryChooser Object
	 */
	private void configuringDirectoryChooser(DirectoryChooser directoryChooser) {
		// Set title for DirectoryChooser
		directoryChooser.setTitle("Please select directory where to download the file");

		// Set Initial Directory
		directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
	}

	public void checkHttpProxyServerCheckBox(ActionEvent event) {
		if (httpProxyCheckBox.isSelected()) {
			setProxyServerInput(true);
		} else {
			setProxyServerInput(false);
		}
	}

	public void setProxyServerInput(boolean enable) {
		if (enable) {
			proxyServerLabel.setOpacity(1.0);
			proxyServerTextField.setDisable(false);
			proxyServerTextField.setOpacity(1.0);
			proxyPortLabel.setOpacity(1.0);
			proxyPortTextField.setOpacity(1.0);
			proxyPortTextField.setDisable(false);
		} else {
			proxyServerLabel.setOpacity(0.0);
			proxyServerTextField.setDisable(true);
			proxyServerTextField.setOpacity(0.0);
			proxyPortLabel.setOpacity(0.0);
			proxyPortTextField.setOpacity(0.0);
			proxyPortTextField.setDisable(true);
		}
	}

}
