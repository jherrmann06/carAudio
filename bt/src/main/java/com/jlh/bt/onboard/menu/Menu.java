package com.jlh.bt.onboard.menu;

import java.util.ArrayList;
import java.util.List;

import com.jlh.bt.constants.Constants;
import com.jlh.bt.onboard.media.Playlist;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

/**
 * This class represents a single menu. A menu contains any number of elements
 * and may have a single parent menu. This class also tracks which element of 
 * the menu is selected.
 */
public class Menu {
    
    private Menu parentMenu;
    private final List<MenuElement> elements; 
    private final Label title;
    private final Constants CONSTANTS = Constants.getInstance();

    private int focus = 0;

    private int minVisible = 0;
    private int maxVisible = CONSTANTS.MENU_MAX_VISIBLE_TRACK_COUNT();

    public Menu(List<MenuElement> items, String title) {    
        this.elements = new ArrayList<>(items.size());
        this.elements.addAll(items);

        if (this.elements.size() == 0) {
            this.elements.add(MenuItem.NO_ITEMS_ITEM);
        }

        getMenuItem(false).setFocused(true);

        this.title = new Label(title);
        this.title.setTextFill(Paint.valueOf(CONSTANTS.FOCUSED_TEXT_COLOR()));
    }

    private MenuItem getMenuItem(boolean up) {
        alignToMenuItem(up);
        return (MenuItem) elements.get(focus);
    }

    private void alignToMenuItem(boolean up) {
        if (focusInBounds() && elements.get(focus) instanceof MenuItem) {
            return;
        }else {
            if (up) {
                focus--;
            }else {
                focus++;
            }
            if (!focusInBounds()) {
                alignToMenuItem(!up);
            }
            alignToMenuItem(up);
        }
    }

    private boolean focusInBounds() {
        return focus >= 0 && focus < elements.size();
    }

    public void focusDown() {
        getMenuItem(false).setFocused(false);

        if (focus != elements.size() - 1) {
            focus++;
        }
        getMenuItem(false).setFocused(true);
        while (maxVisible < elements.size() && focus + 1 >= maxVisible) {
            minVisible++;
            maxVisible++;
        }
    }

    public void focusUp() {
        getMenuItem(true).setFocused(false);
        if (focus != 0) {
            focus--;
        }
        getMenuItem(true).setFocused(true);

        while (minVisible > 0 && focus - 1 < minVisible) {
            minVisible--;
            maxVisible--;
        }
    }

    /**
     * Get the submenu which is associated with the currently focused item. 
     * @return Menu object or null
     */
    public Menu descend() {
        return getMenuItem(false).getSubmenu();
    }

    public boolean canDescend() {
        return descend() != null;
    }

    /**
     * Get the playlist associated with the current menu item
     * @return Playlist object or null
     */
    public Playlist getPlaylist() {
        return getMenuItem(false).getPlaylist();
    }

    public VBox getUIComponent() {
        VBox rtn = new VBox();
        ObservableList<Node> children = rtn.getChildren();

        for (MenuElement e : elements) {
            if (e instanceof MenuHeader) {
                ((MenuHeader)e).setIsVisible(false);
            }
        }

        if (needsToScroll()) {
            HBox titleBox = new HBox();
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label scrollIndicator = new Label(getScrollIndicator());
            scrollIndicator.setTextFill(Paint.valueOf("ffffff"));

            titleBox.getChildren().addAll(title, spacer, scrollIndicator);
            children.add(titleBox);
        }else {
            children.add(title);
        }
        children.add(new Separator());

        if (needsToScroll()) {
            for (int i = minVisible; i < maxVisible; i++) {
                children.add(elements.get(i).getUIComponent());
                if (elements.get(i) instanceof MenuHeader) {
                    ((MenuHeader)elements.get(i)).setIsVisible(true);
                }
            }
            MenuItem currentlyFocused = getMenuItem(false);
            if (currentlyFocused.menuHeaderInvisible()) {
                children.remove(1);
                children.add(1, currentlyFocused.getMenuHeader().getUIComponent());
            }
        }else {
            //all of the items can be visible at once
            for (MenuElement element : elements) {
                children.add(element.getUIComponent());
            }
        }

        rtn.setPrefWidth(Constants.getInstance().ONBOARD_MENU_WIDTH());
        return rtn;
    }

    /**
     * Determine if the additional logic required for scrolling items is required. 
     */
    private boolean needsToScroll() {
        return elements.size() > CONSTANTS.MENU_MAX_VISIBLE_TRACK_COUNT();
    }

    private String getScrollIndicator() {
        if (minVisible > 0 && maxVisible < elements.size()) {
            return "\u21F3"; // up down arrow
        }else if(minVisible > 0) {
            return "\u21E7"; //up arrow
        }else {
            return "\u21E9"; //down arrow
        }
    }

    public String getTitle() {
        return title.getText();
    }

    /**
     * Get the menu which owns this one. May be null
     * @return The parent menu
     */
    public Menu getParentMenu() {
        return parentMenu;
    }

    /**
     * Set the parent menu. 
     * 
     * If the parent menu already has a non-null value, 
     * this method will do nothing.
     * 
     * @param parentMenu
     */
    public void setParentMenu(Menu parentMenu) {
        if (this.parentMenu == null) {
            this.parentMenu = parentMenu;
        }
    }

    public boolean hasParentMenu() {
        return parentMenu != null;
    }
    
}
