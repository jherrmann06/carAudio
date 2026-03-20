package com.jlh.bt.os;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jlh.bt.constants.Constants;

/**
 * Interact with the system via shell commands.
 */
public class ShellController {

    private static ShellController INSTANCE = null;

    public static ShellController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ShellController();
        }

        return INSTANCE;
    }

    private final Logger logger;
    private final Constants CONSTANTS;

    private ShellController() {
        logger = LoggerFactory.getLogger(this.getClass());
        CONSTANTS = Constants.getInstance();
    }

    /**
     * Reset the system audio volume to default value.
     */
    public void resetVolume() {
        if (!CONSTANTS.IS_PROD()) {
            logger.debug("Skipped volume reset command in dev env");
            return;
        }
        logger.debug("Volume reset command issued.");
        try {
            Runtime.getRuntime().exec("amixer -D pulse sset Master " + CONSTANTS.VOLUME_RESET_PERCENTAGE() + "%");
        } catch (IOException e) {
            logger.error("IO exception occurred while resetting system volume", e);
        }
    }

    /**
     * Increment the system audio volume.
     */
    public void incrementVolume() {
        logger.debug("Volume increment command issued.");
        try {
            Runtime.getRuntime().exec("amixer -D pulse sset Master " + CONSTANTS.VOLUME_CHANGE_PERCENTAGE() + "%+");
        } catch (IOException e) {
            logger.error("IO exception occurred while incrementing system volume", e);
        }
    }

    /**
     * Decrement the system audio volume.
     */
    public void decrementVolume() {
        logger.debug("Volume decrement command issued");
        try {
            Runtime.getRuntime().exec("amixer -D pulse sset Master " + CONSTANTS.VOLUME_CHANGE_PERCENTAGE() + "%-");
        } catch (IOException e) {
            logger.error("IO exception occurred while decrementing system volume", e);
        }
    }

    /**
     * Get the current system volume level.
     * See https://www.baeldung.com/linux/volume-level-command-line
     * @return int between 0 and 100 inclusive
     */
    public int getCurrentVolume() {
        if (!Constants.getInstance().IS_PROD()) {
            return getCurrentVolumeDev();
        }

        try {
            Process proc = Runtime.getRuntime().exec("amixer -D pulse sget Master");
            proc.waitFor();
            for (int i = 0; i < 5; i++) {
                proc.inputReader().readLine();
            }
            String str = proc.inputReader().readLine();
            int index = str.indexOf('%');
            str = str.substring(index - 3, index).replace('%', ' ').replace('[', ' ').trim();
            logger.trace("Current volume string is " + str);
            return Integer.parseInt(str);
            
        } catch (IOException e) {
            logger.error("IO exception occurred while getting system volume", e);
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while waiting for volume getter process to finish", e);
        } catch (NumberFormatException e) {
            logger.error("Number format exception occurred while parsing output of volume getter process.", e);
        }

        return 0;
    }

    private int getCurrentVolumeDev() {
        try {
            Process proc = Runtime.getRuntime().exec("wpctl get-volume @DEFAULT_AUDIO_SINK@");
            String out = proc.inputReader().readLine().replace("[MUTED]", "");
            return (int)(Double.parseDouble(out.substring(7)) * 100);
        } catch (IOException e) {
            logger.error("DEV: IO exception occurred while trying to get system volume", e);
        }

        return 0;
    }

}