package com.jlh.bt.hardware;

import com.jlh.bt.constants.Constants;

public abstract class CANDriver {
    
    private static CANDriver instance = null;

    public static CANDriver getInstance() {
        if (instance == null) {
            if (Constants.getInstance().IS_PROD()) {

            }else {
                instance = new CANSimulator();
            }
        }
        return instance;
    }

    public abstract void registerCallback(Runnable callback, int deviceID, int buttonID, boolean bluetooth);

    /**
     * Used to disable button presses so that OEM car clock usage doesn't result in unwanted changes to our system
     */
    public abstract void setCANListening(boolean can);
    public abstract boolean isEnabled();

    public abstract void setIsBluetoothMode(boolean mode);
    public abstract boolean isBluetoothMode();
}
