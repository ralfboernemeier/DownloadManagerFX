package de.ralfb_web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import de.ralfb_web.model.Model;
import de.ralfb_web.utils.ControllerFactory;
import de.ralfb_web.utils.Utils;
import de.ralfb_web.utils.ViewLoader;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;

/**
 * Main Class DownloadManager Application
 * 
 * @author Ralf Boernemeier
 * @version 1.0
 * @since 2019-07-05
 *
 */


public class Main extends Application {

	/**
	 * Constructor
	 */
	public Main() {
		super();
	}

	/**
	 * Fields
	 */
	private Model model = new Model();
	private final String WINDOWS = "Windows";
	private final String LINUX = "Linux";
	private final String MACOS = "Mac OS";
	private final String nameOS = "os.name";
	private String osType = null;
	private String logFileName = "DownloadManagerFX.0.log";

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
	 * The application initialization method. This method is called immediately
	 * after the Application class is loaded and constructed. Overrides init() in
	 * Application class, which does nothing.
	 */
	@Override
	public void init() {
		// Some initialization code can be placed here ...
		if (System.getProperty(nameOS).contains(WINDOWS)) {
			osType = "Windows";
		} else if (System.getProperty(nameOS).contains(LINUX)) {
			osType = "Linux";
		} else if (System.getProperty(nameOS).contains(MACOS)) {
			osType = "Mac OS";
		}
		
		// Set the osType in the model
		model.setOsTypeString(osType);

		if (System.getProperty("java.io.tmpdir") == null) {
			model.setLogFileLocationString("Environment variable TMPDIR not set! Logfile DownloadManagerFX.0.log saved to "
					+ osType + " specific tmpdir!");

		} else {
			if (osType == "Windows" || osType == "Mac OS") {
				model.setLogFileLocationString(System.getProperty("java.io.tmpdir") + logFileName);
			} else if (osType == "Linux") {
				model.setLogFileLocationString(System.getProperty("java.io.tmpdir") + "/" + logFileName);
			} else {
				System.out.println("Unsupported Operating System detected! Exit Application now!");
				LOGGER.severe("Unsupported Operating System detected! Exit Application now!");
				System.exit(1);
			}
		}
	}

	/**
	 * The main entry point for all JavaFX applications.The start method is called
	 * after the init() method has returned, and after the system is ready for the
	 * application to begin running.
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = ViewLoader.load("/de/ralfb_web/ui/Main.fxml",
					clazz -> ControllerFactory.controllerForClass(clazz, model));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("DownloadManagerFX");
			primaryStage.setOnCloseRequest(event -> {
				LOGGER.info("Exit DownloadManagerFX Application.");
			});
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called when the application should stop, and provides a
	 * convenient place to prepare for application exit and destroy resources.
	 */
	@Override
	public void stop() {
		// Do something at stop here ...
	}

	/**
	 * Main Method - Main task is to Launch the application
	 * 
	 * @param args All Parameters given during start of the program
	 */
	public static void main(String[] args) {

		/**
		 * Set default user language
		 */
		Locale.setDefault(new Locale("de"));
		System.setProperty("user.language", "de");

		/**
		 * <h3>Custom Font B612</h3>
		 * <p>
		 * Load the custom Font B612 (see http://b612-font.com) Font-Family B612:
		 * Regular Regular italic Bold Bold Italic Font-Family B612 Mono: Regular
		 * Regular italic Bold Bold Italic
		 * 
		 * How to use in application.css: -fx-font-size: 16.0px; -fx-font-family: "B612
		 * Regular"; -fx-font-family: "B612 Mono Bold";
		 *
		 */
		Font.loadFont(Main.class.getResource("font/B612MonoRegular.ttf").toExternalForm(), 10);
		Font.loadFont(Main.class.getResource("font/B612MonoRegularItalic.ttf").toExternalForm(), 10);
		Font.loadFont(Main.class.getResource("font/B612MonoBold.ttf").toExternalForm(), 10);
		Font.loadFont(Main.class.getResource("font/B612MonoBoldItalic.ttf").toExternalForm(), 10);
		Font.loadFont(Main.class.getResource("font/B612Regular.ttf").toExternalForm(), 10);
		Font.loadFont(Main.class.getResource("font/B612RegularItalic.ttf").toExternalForm(), 10);
		Font.loadFont(Main.class.getResource("font/B612Bold.ttf").toExternalForm(), 10);
		Font.loadFont(Main.class.getResource("font/B612BoldItalic.ttf").toExternalForm(), 10);

		/**
		 * Initialize Start of Application in Log File
		 */
		LOGGER.info("****** Start DownloadManagerFX application ******");

		/**
		 * Check of Java version - Version 8 is needed to run the application.
		 */
		if (Utils.getJavaVersion() != 8) {
			LOGGER.severe("Java Version 8 needed!");
			LOGGER.severe("Current Java Version: " + System.getProperty("java.version"));
			System.out.println("Java Version 8 needed!\n");
			System.out.println("Current Java Version: " + System.getProperty("java.version"));
			System.exit(1);
		} else {
			LOGGER.info("Current Java Version: " + System.getProperty("java.version"));
			LOGGER.info("Valid Java version to run the application.");
		}

		/**
		 * Launch a standalone application (GUI) . This method is typically called from
		 * the main method().
		 */
		launch(args);
	}
}

