package com.jlh.bt.gui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.util.Duration;

/**
 * This class represents a custom label which has a scrolling animation for 
 * long text. 
 */
public class ScrollingText extends Label {

    private int index = 0;

    private final boolean alwaysEnabled;
    private final Timeline timeline;

    private final String text;
    private final int length;

    public ScrollingText(String text, int length, boolean alwaysEnabled) {
        this.alwaysEnabled = alwaysEnabled;
        this.text = text;
        this.length = length;

        this.setTextOverrun(OverrunStyle.CLIP);

        if (text.length() <= length) {
            this.setText(text);
            timeline = null;
        }else {
            setText(text.substring(index, index+length-3) + "...");
            index = 0;

            timeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                    new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            if (index + length > text.length()) {
                                index = 0;
                            }
                            setText(text.substring(index, index + length));
                            index++;
                        }
                    }
                ),
                new KeyFrame(Duration.seconds(0.75))
            );
        }

        if (alwaysEnabled && timeline != null) {
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
    }

    public ScrollingText(String text, int length) {
        this(text, length, false);
    }
    
    public void setScrollingEnabled(boolean enabled) {
        if (alwaysEnabled || timeline == null) {
            return;
        }

        if (enabled) {
            Platform.runLater(() -> {
                timeline.setCycleCount(Animation.INDEFINITE);
                timeline.play();
            });
        }else {
            index = 0;
            Platform.runLater(() -> {
                timeline.stop();
                setText(text.substring(index, index+length-3) + "...");
            });
        }
    }
}
