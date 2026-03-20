package com.jlh.bt.hardware;

import java.util.List;

import com.jlh.bt.constants.Constants;

public abstract class CANListener {
    
    private static CANListener instance = null;

    public static CANListener getInstance() {
        if (instance == null) {
            if (Constants.getInstance().IS_PROD()) {

            }else {
                instance = new CANSimulator();
            }
        }
        return instance;
    }

    /**
     * Register CAN devices which this listener should listen to.
     * If currently listening, this method will do nothing. 
     * @param devices
     */
    public abstract void registerDevices(List<Integer> devices);

    public abstract void registerCallback(Runnable callback, int deviceID, int buttonID, boolean bluetooth);

    /**
     * Used to disable button presses so that OEM car clock usage doesn't result in unwanted changes to our system
     */
    public abstract void setCANListening(boolean can);
    public abstract boolean isListening();

    public abstract void setIsBluetoothMode(boolean mode);
    public abstract boolean isBluetoothMode();
}
