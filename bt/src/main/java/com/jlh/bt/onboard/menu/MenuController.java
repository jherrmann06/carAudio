package com.jlh.bt.onboard.menu;

import java.io.File;

import com.jlh.bt.onboard.media.Playlist;

import javafx.scene.layout.VBox;

/**
 * This class provides access to the menu data structure. 
 */
public class MenuController {
    
    private Menu menu;

    public MenuController(File directory) {
        menu = MusicLoader.getInstance().constructHighLevelMenu(directory).getKey();
    }

    public Menu getCurrentMenu() {
        return menu;
    }

    public Playlist getPlaylist() {
        return menu.getPlaylist();
    }

    public void focusUp() {
        menu.focusUp();
    }

    public void focusDown() {
        menu.focusDown();
    }

    public void descend() {
        if (menu.canDescend()) {
            menu = menu.descend();
        }
    }

    public void ascend() {
        if (menu.hasParentMenu()) {
            menu = menu.getParentMenu();
        }
    }

    public VBox getUIComponent() {
        return menu.getUIComponent();
    }

}
