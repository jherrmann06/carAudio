package com.jlh.bt.gui;

import java.io.ByteArrayInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jlh.bt.constants.Constants;
import com.jlh.bt.onboard.media.MediaController;
import com.jlh.bt.onboard.menu.MusicLoader;
import com.jlh.bt.os.BluetoothController;
import com.jlh.bt.os.ShellController;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * This class represents the currently playing track UI. This UI is defined by FXML. 
 */
public class MusicSpotlightController {

    @FXML private Pane artist;
    @FXML private Pane album;
    @FXML private Pane name;
    @FXML private Pane playlistName;

    @FXML private ProgressBar volumeBar;
    @FXML private ProgressBar trackProgress;

    @FXML private ImageView shuffle;
    @FXML private ImageView albumArt;
    private Image unknownArtImage;

    private BluetoothController bt;
    private MediaController onboard;

    private boolean bluetoothActive = false;
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Constants CONSTANTS = Constants.getInstance();

    public void setBluetoothController(BluetoothController bt) {
        this.bt = bt;
    }

    public void setOnboardController(MediaController onboard) {
        this.onboard = onboard;

        unknownArtImage = albumArt.getImage();
        updateTrack();
        startTrackProgressUpdater();
    }

    public void updateTrack() {
        Platform.runLater(() -> {
            artist.getChildren().clear();
            album.getChildren().clear();
            name.getChildren().clear();
            playlistName.getChildren().clear();
        });
        if (bluetoothActive) {
            updateTrackBt();
        }else {
            updateTrackOnboard();
        }
    }

    private void updateTrackBt() {
        Platform.runLater(() -> {
            artist.getChildren().add(new ScrollingText(bt.getArtistName(), CONSTANTS.MUSIC_DETAIL_TEXT_WIDTH(), true));
            album.getChildren().add(new ScrollingText(bt.getAlbumName(), CONSTANTS.MUSIC_DETAIL_TEXT_WIDTH(), true));
            name.getChildren().add(new ScrollingText(bt.getSongName(), CONSTANTS.MUSIC_DETAIL_TEXT_WIDTH(), true));           
        });
    }

    private void updateTrackOnboard() {
        Platform.runLater(() -> {
            artist.getChildren().add(new ScrollingText(onboard.getCurrentTrack().artist(), CONSTANTS.MUSIC_SPOTLIGHT_TEXT_WIDTH(), true));
            album.getChildren().add(new ScrollingText(onboard.getCurrentTrack().album(), CONSTANTS.MUSIC_SPOTLIGHT_TEXT_WIDTH(), true));
            name.getChildren().add(new ScrollingText(onboard.getCurrentTrack().name(), CONSTANTS.MUSIC_SPOTLIGHT_TEXT_WIDTH(), true));
            playlistName.getChildren().add(new ScrollingText(onboard.getPlaylistName(), CONSTANTS.MUSIC_SPOTLIGHT_TEXT_WIDTH(), true));
            albumArt.setImage(getAlbumArt());
            trackProgress.setProgress(0);
        });
    }

    public void toggleShuffle() {
        Platform.runLater(() -> shuffle.setVisible(!shuffle.isVisible()));
    }

    private void startTrackProgressUpdater() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(0),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        if (!bluetoothActive) {
                            trackProgress.setProgress(onboard.getPercentageComplete());
                        }
                        volumeBar.setProgress(ShellController.getInstance().getCurrentVolume() / 100.0);
                    }
                }
            ),
            new KeyFrame(Duration.seconds(0.5))
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void setBluetoothActive(boolean active) {
        bluetoothActive = active;

        if (bluetoothActive) {
            Platform.runLater(() -> albumArt.setImage(unknownArtImage));
        }
    }

    private Image getAlbumArt() {
        byte[] buffer = MusicLoader.getInstance().getAlbumArt(onboard.getCurrentTrack());
        if (buffer == null) {
            logger.trace("Unknown album art found for track " + onboard.getCurrentTrack() + ", reverting to default image");
            return unknownArtImage;
        }
        return new Image(new ByteArrayInputStream(buffer));
    }
    
}