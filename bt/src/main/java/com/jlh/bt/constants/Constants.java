package com.jlh.bt.constants;

import java.io.File;

public abstract class Constants {

    private static Constants instance = null;

    public static Constants getInstance() {
        if (instance == null) {
            if (new File("/home/jake").exists()) {
                instance = new DevConstants();
            }else {
                instance = new ProdConstants();
            }
        }
        return instance;
    }

    public abstract boolean IS_PROD();
    
    //dbus
    public final String BUSNAME() {return "org.bluez";};
    public abstract String COMMON_PATH_PREFIX();
    public final String PLAYER_SUFFIX() {return "/player0";}

    //favorite device
    public abstract String FAVORITE_FILE_NAME();
    public abstract int FAVORITE_CONNECTION_TIMEOUT();

    public abstract String FAVORITE_PLAYLIST_FILE();

    //volume
    public final int VOLUME_RESET_PERCENTAGE() {return 50;}
    public final int VOLUME_CHANGE_PERCENTAGE() {return 5;}

    //ui
    public final String PLAYER_FXML_FILENAME() {return "player.fxml";}
    public final String ONBOARD_FXML_FILENAME() {return "onboard.fxml";}
    public abstract boolean IS_UI_FULLSCREEN();

    public final int MENU_TEXT_WIDTH() {return 28;}
    public final int MENU_HEADER_TEXT_WIDTH() {return 31;}
    public final int MUSIC_DETAIL_TEXT_WIDTH() {return 11;}
    public final int MENU_MAX_VISIBLE_TRACK_COUNT() {return 12;}

    public final int MUSIC_SPOTLIGHT_TEXT_WIDTH() {return 16;}

    //logging
    public final String LOG_LEVEL_CLASSES_PROPERTY() {return "org.slf4j.simpleLogger.log.com.jlh.bt";}
    public abstract String LOG_FILE_PATH();
    public abstract String LOG_LEVEL();

    //menu css
    public final String FOCUSED_CSS() {return "-fx-border-color: white";}
    public final String UNFOCUSED_CSS() {return "-fx-border-color: black";}
    public final String FOCUSED_TEXT_COLOR() {return "white";}
    public final String UNFOCUSED_TEXT_COLOR() {return "#323232";}
    public final int ONBOARD_MENU_WIDTH() {return 600;}

    //onboard
    public abstract String ONBOARD_MEDIA_DIRECTORY();
    public final String ONBOARD_MEDIA_FILE_EXTENSION() {return "mp3";}    
    public final double ONBOARD_MEDIA_MAX_VOLUME() {return 1;}

    //CAN codes
    //steering wheel
    public abstract int STEERING_WHEEL_DEVICE();
    public abstract int SW_LEFT_BUTTON();
    public abstract int SW_RIGHT_BUTTON();
    public abstract int SW_UP_BUTTON();
    public abstract int SW_DOWN_BUTTON();
    public abstract int SW_OK_BUTTON(); 
    public abstract int SW_SKIP_BUTTON();
    public abstract int SW_PREV_BUTTON();
    public abstract int SW_VOLUME_INCREMENT_BUTTON();
    public abstract int SW_VOLUME_DECREMENT_BUTTON();
    public abstract int SW_MUTE_BUTTON();
    public abstract int SW_M_BUTTON();

    //center control panel
    public abstract int CENTER_DEVICE();
    public abstract int SHUFFLE_BUTTON();
    public abstract int EQUALIZER_BUTTON();
    public abstract int RADIO_BUTTON();
    public abstract int MUTE_BUTTON();
    public abstract int MEDIA_BUTTON();
    public abstract int PHONE_BUTTON();
    //clock button retains OEM functionality
    //menu button retains OEM functionality

    public abstract int BUTTON_1();
    public abstract int BUTTON_2();
    public abstract int BUTTON_3();
    public abstract int BUTTON_4();
    public abstract int BUTTON_5();
    public abstract int BUTTON_6();
    public abstract int BUTTON_7();
    public abstract int BUTTON_8();
    public abstract int BUTTON_9();
    public abstract int BUTTON_0();
    public abstract int HASHTAG_BUTTON();
    public abstract int STAR_BUTTON();

    public abstract int LEFT_BUTTON();
    public abstract int RIGHT_BUTTON();
    public abstract int UP_BUTTON();
    public abstract int DOWN_BUTTON();
    public abstract int OK_BUTTON(); 
    public abstract int SKIP_BUTTON();
    public abstract int PREV_BUTTON();

    //unused
    public abstract int PHONE_HANG_UP_BUTTON();
    public abstract int PHONE_ANSWER_BUTTON();
    public abstract int EJECT_BUTTON();
    public abstract int PAUSE_PLAY_BUTTON();
    public abstract int POWER_BUTTON();
    //volume and tune knob also unused

    public final String BLANK_IMAGE_HEX_STRING() {return "89 50 4E 47 0D 0A 1A 0A 00 00 00 0D 49 48 44 52 00 00 00 01 00 00 00 01 08 02 00 00 00 90 77 53 DE 00 00 00 0F 49 44 41 54 78 01 01 04 00 FB FF 00 00 00 00 00 04 00 01 65 49 C3 60 00 00 00 00 49 45 4E 44 AE 42 60 82";}
}
