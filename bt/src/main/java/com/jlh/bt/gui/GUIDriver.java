package com.jlh.bt.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jlh.bt.constants.Constants;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This class is the entry point of the JavaFX application. 
 * It is responsible for launching the UI and handling scene switching. 
 */
public class GUIDriver extends Application {

    private Logger logger;
    private Constants CONSTANTS;
    private static volatile MusicSpotlightController spotlightController = null;
    private static volatile OnboardMediaSelectorController onboardController  = null;

    private static Stage stage;
    private static Scene spotlight;
    private static Scene onboardMenu;
    private static boolean spotlightShown = false;

    public GUIDriver(String[] args) {
        Application.launch(args);
    }

    public GUIDriver() {
        logger = LoggerFactory.getLogger(this.getClass());
        CONSTANTS = Constants.getInstance();
    }

    @Override
    public void start(Stage stage) throws Exception {
        logger.info("UI resource loading started.");
        GUIDriver.stage = stage;
        ClassLoader resourceLoader = ClassLoader.getSystemClassLoader();

        FXMLLoader spotlightLoader = new FXMLLoader(resourceLoader.getResource(CONSTANTS.PLAYER_FXML_FILENAME()));
        FXMLLoader onboardLoader = new FXMLLoader(resourceLoader.getResource(CONSTANTS.ONBOARD_FXML_FILENAME()));

        spotlight = new Scene(spotlightLoader.load());
        onboardMenu = new Scene(onboardLoader.load());

        spotlightController = spotlightLoader.getController();
        logger.debug("Music spotlight UI controller assigned");
        onboardController = onboardLoader.getController();
        logger.debug("Onboard menu UI controller assigned");

        stage.setScene(onboardMenu);
        stage.setFullScreen(CONSTANTS.IS_UI_FULLSCREEN());
        stage.setFullScreenExitHint("");
        stage.show();

        logger.info("UI resource loading complete, UI shown.");
    }

    public static void toggleScene() {
        if (spotlightShown) {
            Platform.runLater(() -> stage.setScene(onboardMenu));
        }else {
            Platform.runLater(() -> stage.setScene(spotlight));
        }
        spotlightShown = !spotlightShown;
    }

    public static synchronized MusicSpotlightController getBluetoothUIController() {
        return spotlightController;
    }

    public static synchronized OnboardMediaSelectorController getOnboardUIController() {
        return onboardController;
    }
    
}
