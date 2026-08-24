package com.jlh.bt.os;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.bluez.MediaPlayer1;
import org.bluez.exceptions.BluezFailedException;
import org.bluez.exceptions.BluezNotSupportedException;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.UInt32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hypfvieh.bluetooth.DeviceManager;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice;
import com.jlh.bt.constants.Constants;

/**
 * Interact with the bluetooth adapter and bluetooth devices using DBus.
 */
public class BluetoothController {

    private static BluetoothController INSTANCE = null;

    public static BluetoothController getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new BluetoothController();
            } catch (DBusException e) {
                e.printStackTrace();
            }
        }
        
        return INSTANCE;
    }

    private final HashMap<String, Object> errorMap;

    private final DeviceManager manager;
    private final DBusConnection conn;
    private final Logger logger;
    private final Constants CONSTANTS;

    private Runnable connectionHandler;
    private Runnable disconnectionHandler;
    private int prevConnCount = 0;

    private BluetoothController() throws DBusException {
        CONSTANTS = Constants.getInstance();
        logger = LoggerFactory.getLogger(this.getClass());
        manager = DeviceManager.createInstance(false);
        conn = manager.getDbusConnection();
        
        prevConnCount = getConnectedDevices().size();

        connectionHandler = () -> logger.warn("Connection event fired with no handler set.");
        disconnectionHandler = () -> logger.warn("Disconnection event fired with no handler set.");

        // wtf is going on here???
        errorMap = new HashMap<String, Object>();
        errorMap.put("Artist", "");
        errorMap.put("Album", "");
        errorMap.put("Title", "");


        new Thread(
            () -> {while(true) {execute();}},
            "Bluetooth connection listener thread"
        ).start();        
    }

    public void setOnConnectionEvent(Runnable handler) {
        connectionHandler = handler;
    }

    public void setOnDisconnectionEvent(Runnable handler) {
        disconnectionHandler = handler;
    }

    public synchronized List<BluetoothDevice> getConnectedDevices() {
        List<BluetoothDevice> rtn = manager
                                    .getDevices(true)
                                    .stream()
                                    .filter(
                                        (dev1) -> dev1.isConnected() != null && dev1.isConnected()
                                    ).toList();

        logger.trace("Connected bluetooth devices: " + listToString(rtn));
        return rtn;
    }

    public String getConnectedDeviceName() {
        List<BluetoothDevice> list = getConnectedDevices();
        if (list.isEmpty()) {
            return "Disconnected";
        }
        return list.get(0).getName();
    }

    /**
     * Used for debug purposes only
     */
    private String listToString(List<BluetoothDevice> list) {
        if (list.isEmpty()) {
            return "<empty list>";
        }
        StringBuilder rtn = new StringBuilder();
        for (BluetoothDevice obj : list) {
            rtn.append(obj.getName() + "(" + obj.getAddress() + "), ");
        }

        return rtn.substring(0, rtn.length() - 1);
    }

    /**
     * Disconnect all bluetooth devices except the given device.
     * 
     * @param mac The MAC address of the device which should remain connected, or an empty string to disconnect all devices. 
     */
    public void disconnectAllExcept(String mac) {
        for (BluetoothDevice dev : getConnectedDevices()) {
            if (!dev.getAddress().equals(mac)) {
                dev.disconnect();
                logger.info("Device " + dev.getAddress() + " disconnected.");
            }
        }
    }

    /**
     * Interact with the MediaPlayer of the default bluetooth device.
     * 
     * @param operation
     */
    private void operateOnDefaultDevice(Consumer<MediaPlayer1> operation) {
        List<BluetoothDevice> devices = getConnectedDevices();
        if (!getConnectedDevices().isEmpty()) {
            String mac = devices.get(0).getAddress();
            try {
                operation.accept(
                    conn.getRemoteObject(
                        CONSTANTS.BUSNAME(), 
                        CONSTANTS.COMMON_PATH_PREFIX() + mac.replace(":", "_") + CONSTANTS.PLAYER_SUFFIX(),
                        MediaPlayer1.class
                    )
                );
            } catch (DBusException e) {
                logger.error("DBus exception occurred while trying to access player object (device MAC: " + mac + ")", e);
            }
            
        }else {
            logger.debug("No device present to perform MediaPlayer operation on.");
        }
        
    }

    /**
     * Attempt to connect to the device at the given MAC address. If the MAC address is not present in the device list, this method has no effect.
     * @param address Device MAC address
     */
    public boolean connect(String address) {
        logger.info("Searching for device " + address);
        for (BluetoothDevice dev : manager.getDevices()) {
            if (dev.getAddress().equals(address)) {
                logger.info("Device " + address + " found. Attempting connection...");
                return dev.connect();
            }
        }
        logger.info("Device " + address + " not found, so no connection attempted.");
        return false;
        
    }

    /**
     * Send the bluetooth command to skip to the next song on the default device.
     */
    public void playerNext() {
        logger.debug("Player skip called");
        operateOnDefaultDevice(player -> {
            try {
                player.Next();
            } catch (BluezNotSupportedException | BluezFailedException e) {
                logger.error("Exception occurred while trying to call player.next", e);
            }
        });
    }

    /**
     * Send the bluetooth command to skip to the previous position on the default device.
     */
    public void playerPrevious() {
        logger.debug("Player previous called");
        operateOnDefaultDevice(player -> {
            try {
                player.Previous();
            } catch (BluezNotSupportedException | BluezFailedException e) {
                logger.error("Exception occurred while trying to call player.previous", e);
            }
        });
    }

    /**
     * Send the bluetooth command to play on the default device.
     */
    public void play() {
        logger.debug("Player play called");
        operateOnDefaultDevice(player -> {
            try {
                player.Play();
            } catch (BluezNotSupportedException | BluezFailedException e) {
                logger.error("Exception occurred while trying to call player.play", e);
            }
        });
    }

    /**
     * Send the bluetooth command to pause playback on the default device.
     */
    public void pause() {
        logger.debug("Player pause called");
        operateOnDefaultDevice(player -> {
            try {
                player.Pause();
            } catch (BluezNotSupportedException | BluezFailedException e) {
                logger.error("Exception occurred while trying to call player.pause", e);
            }
        });
    }

    private Properties getProperties() throws DBusException {
        List<BluetoothDevice> devices = getConnectedDevices();
        if (devices.isEmpty()) {
            logger.trace("No connected device to get properties from");
            return null;
        }
        
        String mac = devices.get(0).getAddress();
        logger.trace("Getting properties from " + mac);

        return conn.getRemoteObject(
                CONSTANTS.BUSNAME(), 
                CONSTANTS.COMMON_PATH_PREFIX() + mac.replace(':', '_') + CONSTANTS.PLAYER_SUFFIX(),
                    Properties.class
        );
    }

    /**
     * Send the bluetooth command to pause if playing, or vice versa
     */
    public void togglePlay() {
        logger.debug("Toggle play called");
        Properties props;
        try {
            props = getProperties();

            if (props == null) {
                logger.debug("No connected device to get properties from.");
                return;
            }

        } catch (DBusException e) {
            logger.error("DBus exception occurred while trying to toggle playback. ", e);
            return;
        }

        logger.debug("MediaPlayer1 status: " + props.Get("org.bluez.MediaPlayer1", "Status"));

        if (props.Get("org.bluez.MediaPlayer1", "Status").equals("playing")) {
            pause();
        }else {
            play();
        }
    }

    private HashMap<String, Object> getBusMap() {
        Properties props;
        try {
            props = getProperties();

            if(props == null) {
                return errorMap;
            }

            HashMap<String, Object> map = props.Get("org.bluez.MediaPlayer1", "Track");   
            logger.trace("Track properties key set: ");
            for (String s : map.keySet()) {
                logger.trace("=====>" + s);
            }
            
            return map;

        }catch (Exception e) {
            logger.debug("Exception occurred while trying to get track information.", e);
            return errorMap;
        }
    }

    public String getSongName() {
        return (String)getBusMap().get("Title");
    }

    public String getArtistName() {
        return (String)getBusMap().get("Artist");
    }

    public String getAlbumName() {
        return (String)getBusMap().get("Album");
    }

    

    public int getCurrentPosition() {
        Properties props;
        try {
            props = getProperties();
            if (props == null) {
                return -1;
            }
            UInt32 rtn = props.Get("org.bluez.MediaPlayer1", "Position");
            logger.trace("Current position: " + rtn);
            return rtn.intValue();
        }catch (Exception e) {
            logger.debug("Exception occurred while trying to get track position.", e);
            return -1;
        }
    }

    /**
     * Set the discoverable state of the default bluetooth adapter.
     * @param state
     */
    public void setDiscoverableState(boolean state) {

        if (!state) {
            manager.getAdapter().stopDiscovery();
        }

        manager.getAdapter().setDiscoverable(state);
        logger.info(state ? "Adapter set to discoverable" : "Adapter set to undiscoverable");
    }

    public void discover() {
        logger.debug("Discovery started.");
        manager.getAdapter().startDiscovery();
    }

    public void stopDiscover() {
        logger.debug("Discovery stopped.");
        manager.getAdapter().stopDiscovery();
    }

    public boolean isDiscoverable() {
        return manager.getAdapter().isDiscoverable();
    }

    /**
     * Runs constantly in a separate thread. Used to trigger connection and disconnection events. 
     */
    private void execute() {
        int connCount = getConnectedDevices().size();
        if (prevConnCount > connCount) {
            logger.info("Bluetooth disconnection handler thread started.");
            new Thread(disconnectionHandler, "Bluetooth disconnection handler thread").run();
        }else if(connCount > prevConnCount) {
            logger.info("Bluetooth connection handler thread started.");
            new Thread(connectionHandler, "Bluetooth connection handler thread").run();
        }
        prevConnCount = connCount;
    }

}
