package com.jlh.bt.onboard.media;

import java.io.File;
import java.util.Comparator;

/**
 * This class represents a single track. 
 */
public record Track(int id, String name, String artist, String album, String genre, int trackNo, int albumReleaseYear, File file) implements Comparable<Track> {

    public static final Track NOTHING = new Track(-1, "No track", "-", "-", "", -1, -1, null);

    @Override
    public String toString() {
        return id+": " + name + "-" + artist + "-" + album;
    }

    public String toStringLong() {
        return 
            "ID:       " + id + "\n" + 
            "name:     " + name + "\n" +
            "artist:   " + artist + "\n" + 
            "album:    " + album + "\n" + 
            "genre:    " + genre + "\n" + 
            "track no. : " + trackNo + "\n" + 
            "release yr: " + albumReleaseYear ;
    }

    @Override
    public int compareTo(Track other) {
        if (other.album.equals(this.album)) {
            return this.trackNo - other.trackNo; //tracks in numerical order
        }
        //albums are different
        if(other.artist.equals(this.artist)) {
            //albums in order of most recent to least recent (reverse numerical)
            return other.albumReleaseYear - this.albumReleaseYear; 
        }

        //artist is different
        return this.artist.compareTo(other.artist);
    }

    public static class TrackComparator implements Comparator<Track> {

        @Override
        public int compare(Track arg0, Track arg1) {
            return arg0.compareTo(arg1);
        }
        
    }
}
