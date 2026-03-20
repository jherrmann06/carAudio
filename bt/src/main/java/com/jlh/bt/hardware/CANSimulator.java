package com.jlh.bt.hardware;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

//visibility modifier omitted intentionally
class CANSimulator extends CANDriver implements NativeKeyListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HashMap<Integer, Runnable> onboardCallbacks = new HashMap<>();
    private final HashMap<Integer, Runnable> btCallbacks = new HashMap<>();

    private boolean isEnabled = true;
    private boolean isBt = false;

    public CANSimulator() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            logger.error("Exception occurred while trying to register native hook for dev environment global keypress detection.", e);
        }
        GlobalScreen.addNativeKeyListener(this);
    }

    @Override
    public void registerCallback(Runnable callback, int canID, int buttonID, boolean bt) {
        if (bt) {
            btCallbacks.put(canID, callback);
        }else {
            onboardCallbacks.put(canID, callback);
        }
    }

    @Override
    public void setCANListening(boolean can) {
        isEnabled = can;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void setIsBluetoothMode(boolean mode) {
        isBt = mode;
    }

    @Override
    public boolean isBluetoothMode() {
        return isBt;
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (isEnabled) {
            if (isBt) {
                btCallbacks.getOrDefault(e.getKeyCode(), () -> {}).run();
            }else {
                onboardCallbacks.getOrDefault(e.getKeyCode(), () -> {}).run();
            }
        }
    }
    
}
