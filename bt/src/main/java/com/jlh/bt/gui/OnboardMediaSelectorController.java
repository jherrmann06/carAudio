package com.jlh.bt.gui;

import java.io.ByteArrayInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jlh.bt.constants.Constants;
import com.jlh.bt.onboard.media.MediaController;
import com.jlh.bt.onboard.menu.MenuController;
import com.jlh.bt.onboard.menu.MusicLoader;
import com.jlh.bt.onboard.menu.MusicLoader.TrackStats;
import com.jlh.bt.os.ShellController;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * This class represents the library viewing UI. This UI is defined by FXML. 
 */
public class OnboardMediaSelectorController {
    
    @FXML private AnchorPane menuPane;

    @FXML private ImageView albumArt;
    @FXML private Pane name;
    @FXML private Pane artist;

    @FXML private ImageView shuffle;
    @FXML private ProgressBar volume;
    @FXML private ProgressBar trackProgress;

    @FXML private Label trackCount;
    @FXML private Label artistCount;
    @FXML private Label albumCount;
    @FXML private Label coverCount;
    @FXML private Label genreCount;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Constants CONSTANTS = Constants.getInstance();
    private MediaController onboard;
    private MenuController menu;
    private Image unknownArtImage;

    public void setOnboardController(MediaController onboard, MenuController menu) {
        this.onboard = onboard;
        this.menu = menu;

        this.unknownArtImage = albumArt.getImage();
        startTrackProgressUpdater();
        updateMenu();
        updateTrack();
        setTrackStats(MusicLoader.getInstance().getStats());
    }

    public void updateMenu() {
        Platform.runLater(() -> {
            menuPane.getChildren().clear();
            menuPane.getChildren().add(menu.getUIComponent());
        });
    }

    public void updateTrack() {
        Platform.runLater(() -> {
            artist.getChildren().clear();
            name.getChildren().clear();

            artist.getChildren().add(new ScrollingText(onboard.getCurrentTrack().artist(), CONSTANTS.MUSIC_DETAIL_TEXT_WIDTH(), true));
            name.getChildren().add(new ScrollingText(onboard.getCurrentTrack().name(), CONSTANTS.MUSIC_DETAIL_TEXT_WIDTH(), true));
            albumArt.setImage(getAlbumArt());
            trackProgress.setProgress(0);
        });
    }

    public void toggleShuffle() {
        Platform.runLater(() -> shuffle.setVisible(!shuffle.isVisible()));
    }

    private void setTrackStats(TrackStats stats) {
        Platform.runLater(() -> {
            trackCount.setText(trackCount.getText() + stats.trackCount());
            artistCount.setText(artistCount.getText() + stats.artistCount());
            albumCount.setText(albumCount.getText() + stats.albumCount());
            coverCount.setText(coverCount.getText() + stats.coverCount());
            genreCount.setText(genreCount.getText() + stats.genreCount());
        });
    }

    private void startTrackProgressUpdater() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(0),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        trackProgress.setProgress(onboard.getPercentageComplete());
                        volume.setProgress(ShellController.getInstance().getCurrentVolume() / 100.0);
                    }
                }
            ),
            new KeyFrame(Duration.seconds(0.5))
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public Image getAlbumArt() {
        byte[] buffer = MusicLoader.getInstance().getAlbumArt(onboard.getCurrentTrack());
        if (buffer == null) {
            logger.trace("Unknown album art found for track " + onboard.getCurrentTrack() + ", reverting to default image");
            return unknownArtImage;
        }
        return new Image(new ByteArrayInputStream(buffer));
    }
    
}