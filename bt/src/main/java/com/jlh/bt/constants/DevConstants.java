package com.jlh.bt.constants;

//visibility modifier omitted intentionally
class DevConstants extends Constants {

    public boolean IS_PROD() {return false;}
    public boolean IS_UI_FULLSCREEN() {return false;}

    public String COMMON_PATH_PREFIX() {return "/org/bluez/hci0/dev_";}

    public String FAVORITE_FILE_NAME() {return "/home/jake/CarDev/favoriteAddr";}
    public String FAVORITE_PLAYLIST_FILE() {return "/home/jake/CarDev/favoritePlaylist";}
    public int FAVORITE_CONNECTION_TIMEOUT() {return 1000;}

    public String LOG_FILE_PATH() {return "/home/jake/CarDev/java-bt.log";}
    public String LOG_LEVEL() {return "debug";}

    public String ONBOARD_MEDIA_DIRECTORY() {return "/home/jake/Music";}

    public int STEERING_WHEEL_DEVICE() {return -1;}
    public int SW_LEFT_BUTTON() {return -1;}
    public int SW_RIGHT_BUTTON() {return -1;}
    public int SW_UP_BUTTON() {return -1;}
    public int SW_DOWN_BUTTON() {return -1;}
    public int SW_OK_BUTTON() {return -1;}
    public int SW_SKIP_BUTTON() {return -1;}
    public int SW_PREV_BUTTON() {return -1;}
    public int SW_VOLUME_INCREMENT_BUTTON() {return 13;}
    public int SW_VOLUME_DECREMENT_BUTTON() {return 12;}
    public int SW_MUTE_BUTTON() {return -1;}
    public int SW_M_BUTTON() {return 41;} // tilde

    //center control panel
    public int CENTER_DEVICE(){ return -1;}
    public int SHUFFLE_BUTTON(){ return 19;}
    public int EQUALIZER_BUTTON(){ return -1;}
    public int RADIO_BUTTON(){ return -1;}
    public int MUTE_BUTTON(){ return 50;}
    public int MEDIA_BUTTON(){ return -1;}
    public int PHONE_BUTTON(){ return -1;}

    public int BUTTON_1(){return 2;}
    public int BUTTON_2(){return 3;}
    public int BUTTON_3(){return 4;}
    public int BUTTON_4(){return 5;}
    public int BUTTON_5(){return 6;}
    public int BUTTON_6(){return 7;}
    public int BUTTON_7(){return 8;}
    public int BUTTON_8(){return 9;}
    public int BUTTON_9(){return 10;}
    public int BUTTON_0(){return 11;}
    public int HASHTAG_BUTTON(){return 15;} //tab
    public int STAR_BUTTON(){return 42;} //shift

    public int LEFT_BUTTON() {return 30;}
    public int RIGHT_BUTTON() {return 32;}
    public int UP_BUTTON() {return 17;}
    public int DOWN_BUTTON() {return 31;}
    public int OK_BUTTON() {return 57;}
    public int SKIP_BUTTON() {return 18;}
    public int PREV_BUTTON() {return 16;}

    //unused
    public int PHONE_HANG_UP_BUTTON(){return -1;}
    public int PHONE_ANSWER_BUTTON(){return -1;}
    public int EJECT_BUTTON(){return -1;}
    public int PAUSE_PLAY_BUTTON(){return -1;}
    public int POWER_BUTTON(){return -1;}

}