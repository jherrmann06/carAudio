package com.jlh.bt.onboard.media;

import java.io.File;

import org.controlsfx.tools.Utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.util.Duration;

/**
 * This class manages the favorite playlists. It handles user input processing
 * and starts media playback.
 */
public class FavoritePlaylistManager {

    private final FavoritePlaylists favoritePlaylists;
    private final MediaController media;
    private Integer selectedPlaylist = null;

    private Popup popup = null;
    private Timeline timeline = null;

    public FavoritePlaylistManager(File file, MediaController media) {
        this.favoritePlaylists = new FavoritePlaylists(file, media);
        this.media = media;
    }

    public void setSelectedPlaylist(int selected) {
        selectedPlaylist = selected;
        String name = favoritePlaylists.getPlaylistName(selectedPlaylist);

        if (name == null) {
            showSelectedPlaylist("No associated playlist");
        }else {
            showSelectedPlaylist(name);
        }
    }

    private void showSelectedPlaylist(String name) {
        HBox box = new HBox();
        box.setStyle("-fx-background-color: white");
        
        Label label = new Label(name);
        label.setPadding(new Insets(0, 5, 0, 5));

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        box.getChildren().addAll(leftSpacer, label, rightSpacer);

        Platform.runLater(() -> {
                //this popup should replace any existing popup
                if (popup != null) {
                    popup.hide();
                }

                //disable any previously existing timeline
                if (timeline != null) {
                    timeline.stop();
                }

                popup = new Popup();
                popup.getContent().add(box);

                //place the popup at the top center of the screen
                Rectangle2D bounds = Screen.getPrimary().getBounds();
                int PADDING = 15;

                //show the popup so that the child box is rendered and we get an accurate width
                popup.show(Utils.getWindow(null), 0, 0);

                //now that we can get the actual width, calculate final coordinates and set them
                double anchorX = bounds.getMinX() + ((bounds.getMaxX() - bounds.getMinX()) / 2.0) - (box.getWidth() / 2.0) - (PADDING / 2.0);
                double anchorY = bounds.getMinY() + PADDING;
                popup.setAnchorX(anchorX);
                popup.setAnchorY(anchorY);

                //after the selection time is up, fade out the box 
                KeyValue fadeOutBegin = new KeyValue(box.opacityProperty(), 1.0);
                KeyValue fadeOutEnd = new KeyValue(box.opacityProperty(), 0.0);

                KeyFrame kfBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
                KeyFrame kfEnd = new KeyFrame(Duration.millis(500), fadeOutEnd);

                timeline = new Timeline(kfBegin, kfEnd);
                timeline.setDelay(Duration.seconds(3));

                //make sure no playlist is selected when no popup is showing
                timeline.setOnFinished(e -> {popup.hide(); selectedPlaylist = null;});

                timeline.setCycleCount(1);
                timeline.play();
            }
        );
    }

    public void setFavorite() {
        if (selectedPlaylist != null && media.playlistExists()) {
            favoritePlaylists.setFavorite(selectedPlaylist, media.getPlaylistName());
        }
    }

    public void playCurrentlySelected() {
        if (selectedPlaylist != null) {
            favoritePlaylists.playFavorite(selectedPlaylist);
        }
    }
}
