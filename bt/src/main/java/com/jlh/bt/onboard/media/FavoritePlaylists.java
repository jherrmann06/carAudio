package com.jlh.bt.onboard.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jlh.bt.gui.NotificationSender;
import com.jlh.bt.onboard.menu.MusicLoader;

/**
 * This class stores the user-defined favorite playlists. It is also responsible for reading and writing favorite playlist information from the disk. 
 */
public class FavoritePlaylists {
    
    private final HashMap<Integer, String> favorites = new HashMap<>();
    private final MediaController media;
    private final File favoritesFile;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public FavoritePlaylists(File favoriteFile, MediaController media) {
        this.favoritesFile = favoriteFile;
        try {
            loadFavorites();
        }catch (FileNotFoundException e) {
            logger.error("File not found while loading favorite playlists", e);
        }
        this.media = media;
    }

    private void loadFavorites() throws FileNotFoundException {
        Scanner sc = new Scanner(favoritesFile);
        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] arr = line.split(":");
            try {
                favorites.put(Integer.parseInt(arr[0]), arr[1]);
            }catch (NumberFormatException e) {
                logger.error("Couldn't parse line " + line + " when loading favorites from file.", e);
            }
        }

        sc.close();
    }

    private void writeToFavFile() throws IOException {
        FileWriter w = new FileWriter(favoritesFile, false);
        
        for (Entry<Integer, String> e : favorites.entrySet()) {
            w.append(e.getKey() + ":" + e.getValue() + "\n");
        }

        w.close();
    }

    public void playFavorite(int key) {
        String fav = favorites.get(key);
        Playlist p = MusicLoader.getInstance().getPlaylist(fav);
        if (p != null) {
            p.setIndex(0);
            media.setPlaylist(p);
            NotificationSender.sendInfoNotification("Now playing " + fav, "Successfully switched to favorite playlist");
        }else {
            if (fav == null) {
                NotificationSender.sendWarningNotification("Button not bound", "No playlist associated with button " + key);
            }else {
                NotificationSender.sendWarningNotification("Playlist not found", "Playlist " + fav + " could not be found");
            }
        }
    }

    public void setFavorite(int key, String playlistName) {
        NotificationSender.sendInfoNotification("Playlist added to favorites", playlistName + " is now a favorite playlist on button " + key);
        favorites.put(key, playlistName);
        try {
            writeToFavFile();
        }catch (IOException e) {
            logger.error("Error occurred while writing favorite playlists to file.", e);
        }
    }

    public String getPlaylistName(Integer key) {
        if (key == null) {
            return "";
        }
        return favorites.get(key);
    }

}
