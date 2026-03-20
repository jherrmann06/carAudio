package com.jlh.bt.onboard.menu;

import com.jlh.bt.constants.Constants;
import com.jlh.bt.gui.ScrollingText;

import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;

/**
 * This class represents the header UI element. 
 * Headers contain the title of an album. 
 */
public class MenuHeader implements MenuElement {

    private final HBox box;
    private final ScrollingText label;
    private final Constants CONSTANTS = Constants.getInstance();
    private boolean isVisible = false;

    public MenuHeader(ImageView albumArt, String text) {

        Region lSpacer = new Region();
        lSpacer.setMinWidth(15);
        Region rSpacer = new Region();
        rSpacer.setMinWidth(15);

        label = new ScrollingText(text, CONSTANTS.MENU_HEADER_TEXT_WIDTH());

        box = new HBox(lSpacer, albumArt, rSpacer, label);
        box.setPadding(new Insets(0, 0, 5, 0));
        box.setStyle("-fx-border-style: hidden hidden solid hidden; -fx-border-color: "+ CONSTANTS.UNFOCUSED_TEXT_COLOR() +";");
    }

    @Override
    public HBox getUIComponent() {
        return box;
    }

    public void setFocused(boolean focused) {
        label.setScrollingEnabled(focused);
        if (focused) {
            label.setTextFill(Paint.valueOf(CONSTANTS.FOCUSED_TEXT_COLOR()));
            box.setStyle("-fx-border-style: hidden hidden solid hidden; -fx-border-color: "+ CONSTANTS.FOCUSED_TEXT_COLOR() +";");
        }else {
            label.setTextFill(Paint.valueOf(CONSTANTS.UNFOCUSED_TEXT_COLOR()));
            box.setStyle("-fx-border-style: hidden hidden solid hidden; -fx-border-color: "+ CONSTANTS.UNFOCUSED_TEXT_COLOR() +";");
        }
    }

    public void setIsVisible(boolean state) {
        isVisible = state;
    } 

    public boolean getIsVisible() {
        return isVisible;
    }
    
}