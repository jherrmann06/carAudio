package com.jlh.bt.onboard.menu;

import com.jlh.bt.constants.Constants;
import com.jlh.bt.gui.ScrollingText;
import com.jlh.bt.onboard.media.Playlist;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;

/**
 * This class represents a menu item. Menu items can be an access point to a
 * deeper menu, such as a genre or artist menu. It can also represent a single
 * track. The menu element also contains a reference to a playlist which should
 * play if this menu is interacted with.
 */
public class MenuItem implements MenuElement {

    public final static MenuItem NO_ITEMS_ITEM = new MenuItem("No items", null, null, 0, 0); 

    private final Constants CONSTANTS = Constants.getInstance();

    private final Menu submenu;
    private final MenuHeader header;
    private final HBox component;
    private final Playlist playlist;
    private final int index;
    private final double leftSpacing;
    
    public MenuItem(String name, Menu submenu, MenuHeader header, Playlist playlist, int index, double leftSpacing) {
        this.header = header;
        this.submenu = submenu;
        this.playlist = playlist;
        this.leftSpacing = leftSpacing;
        component = setupComponent(name); 
        
        this.index = index;
    }

    public MenuItem(String name, Menu submenu, Playlist playlist, int index, double leftSpacing) {
        this(name, submenu, null, playlist, index, leftSpacing);
    }

    private HBox setupComponent(String name) {
        HBox box = new HBox();

        Region lSpacer = new Region();
        lSpacer.setMinWidth(leftSpacing);
        
        ScrollingText label = new ScrollingText(name, CONSTANTS.MENU_TEXT_WIDTH());
        label.setPadding(new Insets(0, 10, 0, 5));

        box.getChildren().add(lSpacer);
        box.getChildren().add(label);
        if (submenu != null) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            box.getChildren().addAll(spacer, new Label(">"));
        }

        return box;
    } 
    
    @Override
    public HBox getUIComponent() {
        return component;
    }

    public void setFocused(boolean focused) {
        ScrollingText text = (ScrollingText) component.getChildren().get(1);

        if (header != null) {
            header.setFocused(focused);
        }

        if (focused) {
            text.setScrollingEnabled(true);
            text.setTextFill(Paint.valueOf(CONSTANTS.FOCUSED_TEXT_COLOR()));
            if (submenu == null) { //individual song MenuItem
                text.setStyle(CONSTANTS.FOCUSED_CSS());
            }else {
                component.setStyle(CONSTANTS.FOCUSED_CSS());
            }
            
        }else {
            text.setScrollingEnabled(false);
            text.setTextFill(Paint.valueOf(CONSTANTS.UNFOCUSED_TEXT_COLOR()));
            if (submenu == null) {//individual song MenuItem
                text.setStyle(CONSTANTS.UNFOCUSED_CSS());
            }else {
                component.setStyle(CONSTANTS.UNFOCUSED_CSS());
            }
        }
    }

    /**
     * Get the submenu which this button leads to. May be null. 
     * 
     * @return Menu object or null
     */
    public Menu getSubmenu() {
        return submenu;
    }

    public boolean menuHeaderInvisible() {
        return header != null && !header.getIsVisible();
    }

    public MenuHeader getMenuHeader() {
        return header;
    }

    /**
     * Get the playlist associated with this menu item. May be null. 
     * The caller should start playback from the current track. 
     * 
     * @return Playlist object or null.
     */
    public Playlist getPlaylist() {
        playlist.setIndex(index);
        return playlist;
    }

}
