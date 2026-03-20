package com.jlh.bt.onboard.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jlh.bt.constants.Constants;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * This class manages playback of onboard audio files. 
 */
public class MediaController {

    private MediaPlayer player = null;
    private Playlist playlist = null;
    
    private final Constants CONSTANTS;
    private final Runnable songChangeCallback;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MediaController(Runnable songChangeCallback) {
        this.songChangeCallback = songChangeCallback;
        CONSTANTS = Constants.getInstance();
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        //TODO buffer current playlist?
        createNewMediaPlayer(playlist.getCurrentTrack());
    }

    public Track getCurrentTrack() {
        if (playlist != null) {
            return playlist.getCurrentTrack();
        }
        return Track.NOTHING;
    }

    public String getPlaylistName() {
        if (playlist != null) {
            return playlist.getName();
        }else {
            return "-";
        }
    }

    /**
     * Return the percent complete the track is.
     * @return double between 0 and 1 inclusive
     */
    public double getPercentageComplete() {
        if (player != null) {
            return player.getCurrentTime().toSeconds() / player.getStopTime().toSeconds();
        }
        return 0;
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    public void play() {
        if (player != null) {
            player.play();
        }
    }

    public void skip() {
        if (playlist != null) {
            playlist.nextTrack();
            createNewMediaPlayer(playlist.getCurrentTrack());
        }
    }

    public void rewind() {
        if (player != null) {
            player.seek(Duration.ZERO);
        }
    }

    public void previous() {
        if (playlist != null) {
            playlist.previousTrack();
            createNewMediaPlayer(playlist.getCurrentTrack());
        }
    }

    public void toggleShuffle() {
        if (playlist != null) {
            playlist.toggleShuffle();
        }
    }

    public boolean playlistExists() {
        return playlist != null;
    }

    private void createNewMediaPlayer(Track media) {
        if (player != null) {
            player.stop();
            player.dispose();
        }
        player = new MediaPlayer(new Media(media.file().toURI().toString()));

        player.setOnEndOfMedia(this::endOfMediaHandler);

        player.setVolume(CONSTANTS.ONBOARD_MEDIA_MAX_VOLUME());
        player.setAutoPlay(true); // as soon as playback is ready we begin to play this track
        
        logger.trace(media.toStringLong() + "\n");

        songChangeCallback.run();
    }

    private void endOfMediaHandler() {
        playlist.nextTrack();
        createNewMediaPlayer(playlist.getCurrentTrack());
    }

}
