package com.jlh.bt;

import java.io.File;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.simple.SimpleLogger;

import com.jlh.bt.constants.Constants;
import com.jlh.bt.gui.MusicSpotlightController;
import com.jlh.bt.gui.GUIDriver;
import com.jlh.bt.gui.OnboardMediaSelectorController;
import com.jlh.bt.hardware.CANDriver;
import com.jlh.bt.onboard.media.FavoritePlaylistManager;
import com.jlh.bt.onboard.media.MediaController;
import com.jlh.bt.onboard.menu.MenuController;
import com.jlh.bt.os.BluetoothController;
import com.jlh.bt.os.FavoriteFileController;
import com.jlh.bt.os.ShellController;

public class Main {

    private final Logger logger;
    private final MusicSpotlightController bluetoothUI;
    private final OnboardMediaSelectorController onboardUI;

    private final MediaController media;
    private final MenuController menu;

    private final FavoritePlaylistManager favoritePlaylistManager;

    private final Constants CONSTANTS;

    public static void main(String[] args) {
        new Main(args);
    }

    private Main(String[] args) {
        CONSTANTS = Constants.getInstance();

        System.setProperty(CONSTANTS.LOG_LEVEL_CLASSES_PROPERTY(), CONSTANTS.LOG_LEVEL());
        System.setProperty(SimpleLogger.LOG_FILE_KEY, CONSTANTS.LOG_FILE_PATH());

        logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Execution started.");
        debugPrintConstants();

        new Thread( () -> {
                new GUIDriver(args);
                System.exit(0); //kill other threads if UI closes. 
            }, 
            "GUI driver thread"
        ).start();

        while (GUIDriver.getBluetoothUIController() == null || GUIDriver.getOnboardUIController() == null) {
            //do nothing
        }

        bluetoothUI = GUIDriver.getBluetoothUIController();
        onboardUI = GUIDriver.getOnboardUIController();
        logger.debug("GUIDriver successfully provided UI controller");

        menu = new MenuController(new File(CONSTANTS.ONBOARD_MEDIA_DIRECTORY()));
        media = new MediaController(() -> {onboardUI.updateTrack(); bluetoothUI.updateTrack();});

        onboardUI.setOnboardController(media, menu);
        bluetoothUI.setOnboardController(media);

        favoritePlaylistManager = new FavoritePlaylistManager(new File(CONSTANTS.FAVORITE_PLAYLIST_FILE()), media);

        ShellController.getInstance().resetVolume();
        registerCanCallbacks();

        if (CONSTANTS.IS_PROD()) {
            bluetoothInit();
        }else {
            logger.warn("Skip bluetooth init because we are in dev environment");
        }
    }

    private void registerCanCallbacks() {
        CANDriver can = CANDriver.getInstance();
        can.registerCallback(() -> {menu.focusUp(); onboardUI.updateMenu();},   CONSTANTS.STEERING_WHEEL_DEVICE(), CONSTANTS.SW_UP_BUTTON(), false);
        can.registerCallback(() -> {menu.focusDown(); onboardUI.updateMenu();}, CONSTANTS.STEERING_WHEEL_DEVICE(), CONSTANTS.SW_DOWN_BUTTON(), false);
        can.registerCallback(() -> {menu.ascend(); onboardUI.updateMenu();},    CONSTANTS.STEERING_WHEEL_DEVICE(), CONSTANTS.SW_LEFT_BUTTON(), false);
        can.registerCallback(() -> {menu.descend(); onboardUI.updateMenu();},   CONSTANTS.STEERING_WHEEL_DEVICE(), CONSTANTS.SW_RIGHT_BUTTON(), false);
        can.registerCallback(() -> media.setPlaylist(menu.getPlaylist()),       CONSTANTS.STEERING_WHEEL_DEVICE(), CONSTANTS.SW_OK_BUTTON(), false);

        can.registerCallback(() -> GUIDriver.toggleScene(), CONSTANTS.STEERING_WHEEL_DEVICE(), CONSTANTS.SW_M_BUTTON(), false);

        can.registerCallback(() -> media.skip(),     CONSTANTS.STEERING_WHEEL_DEVICE(), CONSTANTS.SW_SKIP_BUTTON(), false);
        can.registerCallback(() -> media.previous(), CONSTANTS.STEERING_WHEEL_DEVICE(), CONSTANTS.PREV_BUTTON(), false);
        
        can.registerCallback(() -> {
            if (media.playlistExists()) {
                media.toggleShuffle();
                onboardUI.toggleShuffle();
                bluetoothUI.toggleShuffle();
            }
        }, CONSTANTS.CENTER_DEVICE(), CONSTANTS.SHUFFLE_BUTTON(), false);

        can.registerCallback(() -> favoritePlaylistManager.setSelectedPlaylist(0), CONSTANTS.CENTER_DEVICE(), CONSTANTS.BUTTON_0(), false);
        can.registerCallback(() -> favoritePlaylistManager.setSelectedPlaylist(1), CONSTANTS.CENTER_DEVICE(), CONSTANTS.BUTTON_1(), false);
        can.registerCallback(() -> favoritePlaylistManager.setSelectedPlaylist(2), CONSTANTS.CENTER_DEVICE(), CONSTANTS.BUTTON_2(), false);
        can.registerCallback(() -> favoritePlaylistManager.setSelectedPlaylist(3), CONSTANTS.CENTER_DEVICE(), CONSTANTS.BUTTON_3(), false);
        can.registerCallback(() -> favoritePlaylistManager.setSelectedPlaylist(4), CONSTANTS.CENTER_DEVICE(), CONSTANTS.BUTTON_4(), false);
        can.registerCallback(() -> favoritePlaylistManager.setSelectedPlaylist(5), CONSTANTS.CENTER_DEVICE(), CONSTANTS.BUTTON_5(), false);
        can.registerCallback(() -> favoritePlaylistManager.setSelectedPlaylist(6), CONSTANTS.CENTER_DEVICE(), CONSTANTS.BUTTON_6(), false);
        can.registerCallback(() -> favoritePlaylistManager.setSelectedPlaylist(7), CONSTANTS.CENTER_DEVICE(), CONSTANTS.BUTTON_7(), false);
        can.registerCallback(() -> favoritePlaylistManager.setSelectedPlaylist(8), CONSTANTS.CENTER_DEVICE(), CONSTANTS.BUTTON_8(), false);
        can.registerCallback(() -> favoritePlaylistManager.setSelectedPlaylist(9), CONSTANTS.CENTER_DEVICE(), CONSTANTS.BUTTON_9(), false);
        
        can.registerCallback(() -> favoritePlaylistManager.playCurrentlySelected(), CONSTANTS.CENTER_DEVICE(), CONSTANTS.HASHTAG_BUTTON(), false);
        can.registerCallback(() -> favoritePlaylistManager.setFavorite(), CONSTANTS.CENTER_DEVICE(), CONSTANTS.STAR_BUTTON(), false);

    }

    private void bluetoothInit() {
        BluetoothController.getInstance().setDiscoverableState(false);

        //assume that there is only one device connected at once
        BluetoothController.getInstance().setOnDisconnectionEvent(() -> {
            BluetoothController.getInstance().setDiscoverableState(true);
        });
        BluetoothController.getInstance().setOnConnectionEvent(() -> {
            BluetoothController.getInstance().setDiscoverableState(false);
            FavoriteFileController.getInstance().setAddress(BluetoothController.getInstance().getConnectedDevices().get(0).getAddress());
        });

        bluetoothUI.setBluetoothController(BluetoothController.getInstance());
        attemptFavoriteConnection();
    }

    private void attemptFavoriteConnection() {
        //load mac from file
        String address = FavoriteFileController.getInstance().getAddress();

        logger.info("Favorite address retrieved: " + address);

        if (address != null) {
            BluetoothController.getInstance().disconnectAllExcept(address);
            BluetoothController.getInstance().discover();
            if (BluetoothController.getInstance().connect(address)) {
                logger.info("Favorite connection successful.");
                BluetoothController.getInstance().play();
            }else {
                logger.info("Favorite device connection unsuccessful. Enabling discovery.");
                BluetoothController.getInstance().setDiscoverableState(true);
            }
        }else {
            //start over. No devices should be connected before this program starts running anyways.
            BluetoothController.getInstance().disconnectAllExcept("");
            BluetoothController.getInstance().setDiscoverableState(true);
        }
    }

    /**
     * Used for debug purposes only.
     */
    private void debugPrintConstants() {
        logger.debug("All Constants: ");
        for (Method m : Constants.class.getMethods()) {
            try {
                if (m.getName().equals("toString") || m.getName().equals("hashCode") || m.getName().equals("getClass")) {
                    continue;
                }
                logger.debug(m.getName() + ": " + m.invoke(Constants.getInstance()) + "(" + m.getReturnType().getName() +")");
            } catch (Exception ignored) {}
            
        }
    }
}
