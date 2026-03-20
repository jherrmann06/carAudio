package com.jlh.bt.hardware;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jlh.bt.constants.Constants;
import com.jlh.bt.gui.NotificationSender;

import tel.schich.javacan.CanChannels;
import tel.schich.javacan.CanFilter;
import tel.schich.javacan.CanFrame;
import tel.schich.javacan.CanSocketOptions;
import tel.schich.javacan.RawCanChannel;

public class CANListenerImpl extends CANListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RawCanChannel channel;

    private final ConcurrentHashMap<String, Runnable> onboardCallbacks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Runnable> btCallbacks = new ConcurrentHashMap<>();

    private final AtomicBoolean isListening = new AtomicBoolean(false);
    private final AtomicBoolean isBluetoothMode = new AtomicBoolean(false);

    public CANListenerImpl() {
        RawCanChannel channel = null;
        try {
            channel = CanChannels.newRawChannel(Constants.getInstance().CAN_DEVICE());
            channel.configureBlocking(true);
        } catch (IOException e) {
            logger.error("Failed to initialize CAN channel:", e);
            NotificationSender.sendWarningNotification("Failed to init. CAN!", e.getMessage());
        }
        this.channel = channel;
    }

    private void listener() {
        byte[] buffer = new byte[8];
        ConcurrentHashMap<String, Runnable> map;
        while(isListening()) {
            map = isBluetoothMode() ? btCallbacks : onboardCallbacks;

            try {
                CanFrame frame = channel.receive();
                frame.getData(buffer, 0, 8);

                if (buffer[7] != 0) { //TODO check this
                    String address = frame.getId() + ":" + Byte.toUnsignedInt(buffer[0]);
                    map.getOrDefault(address, () -> logger.trace("Unbound CAN button pressed: " + address)).run();
                }

            } catch (IOException e) {
                logger.error("Error occurred while reading CAN frame:", e);
                NotificationSender.sendWarningNotification("CAN read error", e.getMessage());
            }
        }
    }

    @Override
    public void registerDevices(List<Integer> devices) {
        if (!isListening()) {
            CanFilter[] filters = new CanFilter[devices.size()];

            for (int i = 0; i < devices.size(); i++) {
                filters[i] = new CanFilter(devices.get(i));
            }
            
            try {
                channel.setOption(CanSocketOptions.FILTER, filters);
            } catch (IOException e) {
                logger.error("Failed to set CAN filter option:", e);
                NotificationSender.sendWarningNotification("Failed to set CAN filter", e.getMessage());
            }
        }else {
            logger.warn("Attempted to set CAN filter while listening");
        }
    }

    @Override
    public void registerCallback(Runnable callback, int deviceID, int buttonID, boolean bluetooth) {
        if (bluetooth) {
            btCallbacks.put(deviceID + ":" + buttonID, callback);
        }else {
            onboardCallbacks.put(deviceID + ":" + buttonID, callback);
        }
    }

    @Override
    public void setCANListening(boolean can) {
        boolean prev = isListening();
        isListening.set(can);
        if (can && !prev) {
            new Thread(this::listener, "CAN listener");
        }
    }

    @Override
    public boolean isListening() {
        return isListening.get();
    }

    @Override
    public void setIsBluetoothMode(boolean mode) {
        isBluetoothMode.set(mode);
    }

    @Override
    public boolean isBluetoothMode() {
        return isBluetoothMode.get();
    }
    
}
