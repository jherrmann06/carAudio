package com.jlh.bt.onboard.menu;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import org.github.jamm.MemoryMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jlh.bt.constants.Constants;
import com.jlh.bt.gui.NotificationSender;
import com.jlh.bt.onboard.media.Playlist;
import com.jlh.bt.onboard.media.Track;
import com.jlh.bt.util.Pair;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * This class loads music files from the disk and creates the root menu based on their metadata.
 */
public class MusicLoader {

    public record TrackStats(int trackCount, int artistCount, int albumCount, int coverCount, int genreCount) {}

    private MusicLoader() {
        meter = MemoryMeter.builder().build();
    }

    private static MusicLoader instance = null;

    public static MusicLoader getInstance() {
        if (instance == null) {
            instance = new MusicLoader();
        }

        return instance;
    }

    private TrackStats stats = null;

    private final Logger logger = LoggerFactory.getLogger(MusicLoader.class);
    private final Constants CONSTANTS = Constants.getInstance();

    private final MemoryMeter meter;

    private final FileFilter isAudioFile = (file) -> {
        if (file.isDirectory()) {
            return false;
        }
        int index = file.getName().lastIndexOf('.');
        return file.getName().substring(index+1).equals(CONSTANTS.ONBOARD_MEDIA_FILE_EXTENSION());
    };

    private final FileFilter isDirectory = (file) -> {
        return file.isDirectory();
    };

    private final HashMap<String, byte[]> albumArtCache = new HashMap<>();
    private final SortedMap<String, Playlist> artistPlaylistMap = new TreeMap<>();
    private final SortedMap<String, Playlist> genrePlaylistMap = new TreeMap<>();

    public TrackStats getStats() {
        return stats;
    }

    public Playlist getPlaylist(String name) {
        if (name == null) {
            return null;
        }
        if (artistPlaylistMap.containsKey(name)) {
            return artistPlaylistMap.get(name);
        }
        return genrePlaylistMap.get(name);
    }
    
    public Pair<Menu, Playlist> constructHighLevelMenu(File directory) {
        Playlist allSongs = new Playlist("All Songs", loadMusicFiles(directory));

        Menu artistMenu = getCategoryMenu(artistPlaylistMap, "Artists", (t) -> t.album());
        Menu genreMenu = getCategoryMenu(genrePlaylistMap, "Genres", (t) -> t.album() + " - " + t.artist());

        Menu rootMenu = new Menu(
            List.of(
                new MenuItem("Artist", artistMenu, allSongs, 0, 0),
                new MenuItem("Genre", genreMenu, allSongs, 0,0)
            ),
            "Onboard Music"
        );

        artistMenu.setParentMenu(rootMenu);
        genreMenu.setParentMenu(rootMenu);

        allSongs.sortSongs();

        Pair<Menu, Playlist> rtn = new Pair<Menu,Playlist>(rootMenu, allSongs);

        String totalSize = (meter.measureDeep(rtn) / (double)(Math.pow(10, 9))) + 0.005 + ""; //gigabytes

        logger.info("Menu and playlist total size: ~" + totalSize.substring(0, Math.min(4, totalSize.length())) + " gigabytes" ); 

        return rtn;
    }

    /**
     * Used to get either the artist menu or genre menu. 
     * @param map the hash map to traverse to create the menus
     * @param title the title of the parent menu
     * @return Menu object with the provided title
     */
    private Menu getCategoryMenu(SortedMap<String, Playlist> map, String title, Function<Track, String> headerName) {
        List<MenuElement> menuItems = new ArrayList<>(map.size());

        for (Entry<String, Playlist> entry : map.entrySet()) {
            Playlist playlist = entry.getValue();
            playlist.sortSongs();

            //create menu displaying an artists discography or all songs in a genre
            List<MenuElement> subMenuElements = new ArrayList<>(playlist.getTrackCount());

            String albumName = "";
            MenuHeader header = null;

            for (int i = 0; i < playlist.getTrackCount(); i++) {
                Track track = playlist.getCurrentTrack();

                if (!track.album().equals(albumName)) {
                    byte[] buffer = getAlbumArt(track);
                    if (buffer == null) {
                        buffer = HexFormat.ofDelimiter(" ").parseHex(CONSTANTS.BLANK_IMAGE_HEX_STRING());
                    }

                    header = new MenuHeader(
                        new ImageView(new Image(
                                    new ByteArrayInputStream(buffer),
                                    35,
                                    35,
                                    false,
                                    true)
                        ), 
                        headerName.apply(track)
                    );

                    subMenuElements.add(header);
                }
                MenuItem m = new MenuItem(track.name(), null, header, playlist, i, 80);
                albumName = track.album();

                subMenuElements.add(m);
                playlist.nextTrack();
            }

            Menu bottomMenu = new Menu(subMenuElements, entry.getKey()); //bottom because there are no submenus
            
            //create menu item for menu displaying each artist
            menuItems.add(new MenuItem(entry.getKey(), bottomMenu, playlist, 0, 0));
        }

        Menu menu = new Menu(menuItems, title);
        for (MenuElement item : menuItems) {
            ((MenuItem)item).getSubmenu().setParentMenu(menu);
        }

        return menu;
    }

    private void getMusicFilesRecursive(File directory, List<File[]> files) {
        File[] arr = directory.listFiles(isAudioFile);
        if (arr != null) {
            files.add(arr);
        }

        for (File dir : directory.listFiles(isDirectory)) {
            getMusicFilesRecursive(dir, files);
        }
    }

    private List<Track> loadMusicFiles(File directory) {
        long startTime = System.currentTimeMillis();
        //clear maps of any stale data
        albumArtCache.clear();
        artistPlaylistMap.clear();
        genrePlaylistMap.clear();

        List<File[]> files = new ArrayList<>();
        getMusicFilesRecursive(directory, files);
        
        int size = 0;
        for (File[] f : files) {
            size += f.length;
        }

        File[] audioFiles = new File[size];
        int i = 0;
        for (File[] arr : files) {
            for (File f : arr) {
                audioFiles[i] = f;
                i++;
            }
        }

        List<Track> trackList = new ArrayList<>(audioFiles.length);
        logger.info(audioFiles.length + " mp3 files found.");

        HashMap<String, Object> albumTable = new HashMap<>(); //take advantage of hashmap constant time reads to store album names

        int id = 0;
        for (File f : audioFiles) {
            Mp3File metadata;
            try {
                metadata = new Mp3File(f);
                if (metadata.hasId3v2Tag()) {
                    ID3v2 tags = metadata.getId3v2Tag();
                    
                    //for some reason this object is the only one which will do the release year properly
                    int year = 0;
                    try {
                        ID3v1 yearTag = metadata.getId3v1Tag();
                        year = parseNumericalString(yearTag.getYear());
                    }catch(Exception e) {
                        logger.info("Audio file " + f.getName() + " has issue with release year tagging. Using default year value.");
                    }
                    Track newTrack = new Track(
                        id, 
                        tags.getTitle(), 
                        tags.getArtist(), 
                        tags.getAlbum(), 
                        convertGenreIdToString(tags.getGenre()),
                        parseNumericalString(tags.getTrack()),
                        year,
                        f
                    );
                    id++;

                    trackList.add(newTrack);
                    updatePlaylistMap(artistPlaylistMap, newTrack.artist(), newTrack);
                    updatePlaylistMap(genrePlaylistMap, newTrack.genre(), newTrack);

                    if (albumTable.get(newTrack.artist() + "-" + newTrack.album()) == null) {
                        albumTable.put(newTrack.artist() + "-" + newTrack.album(), new Object());
                    }

                    if (tags.getAlbumImage() == null) {
                        logger.info("Album " + tags.getAlbum() + " has no art");
                    }

                    if (
                        tags.getAlbumImage() != null 
                        && !albumArtCache.containsKey(tags.getArtist() + "-" + tags.getAlbum())
                    ) {
                        logger.trace("Album " + tags.getArtist() + "-" + tags.getAlbum() + " has art, adding to map.");
                        albumArtCache.put(tags.getArtist() + "-" + tags.getAlbum(), tags.getAlbumImage());
                    }

                }else {
                    logger.info("Audio file " + f.getName() + " doesn't have Id3v2 tags, skipping file.");
                }
            } catch (Exception e) {
                logger.error("Encountered exception while parsing tags of audio file " + f.getName(), e);
            }
        }

        double loadTime = ((System.currentTimeMillis() - startTime) / 1000.0) + 0.005; //convert to seconds and prepare to round to 2 decimal places
        logger.info(String.format("%d audio files loaded successfully in %.2f seconds.", trackList.size(), loadTime));

        NotificationSender.sendInfoNotification(
            "File loading complete", 
            String.format(
                "%d files of %d successfully loaded.\nArtists: %d\nGenres: %d\nAlbums: %d (%d cover art images)\nin %.2f seconds", 
                trackList.size(), 
                audioFiles.length, 
                artistPlaylistMap.size(), 
                genrePlaylistMap.size(),
                albumTable.size(), 
                albumArtCache.size(),
                loadTime
            )
        );

        stats = new TrackStats(
            trackList.size(), 
            artistPlaylistMap.size(), 
            albumTable.size(), 
            albumArtCache.size(),
            genrePlaylistMap.size()
        );

        return trackList;
    }

    private int parseNumericalString(String str) {
        try {
            return Integer.parseInt(str);
        }catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updatePlaylistMap(SortedMap<String, Playlist> map, String key, Track track) {
        if (!map.containsKey(key)) {
            logger.trace("New track list created for " + key);
            map.put(key, new Playlist(key, List.of(track)));
        }else {
            map.get(key).appendTrack(track);
        }
    }

    /**
     * Get the album art associated with a particular track.
     * @param track Any track
     * @return byte array representing an image or null
     */
    public byte[] getAlbumArt(Track track) {

        return albumArtCache.get(track.artist() + "-" + track.album());
    }

    /**
     * Convert Id3v2 genre IDs into human-readable genres. 
     * Only a subset are converted because there's no chance im going to listen to most of the genres on the list. 
     * 
     * See https://exiftool.org/TagNames/ID3.html
     */
    private String convertGenreIdToString(int genre) {
        switch (genre) {
            case 2:
                return "Country";
            case 6:
                return "Grunge";
            case 9:
                return "Metal";
            case 15:
                return "Rap";
            case 17:
                return "Rock";
            case 43:
                return "Punk";
            case 66:
                return "New Wave";
            case 131:
                return "Indie / Alternative";
            default:
                return "Unknown";
        }
    }

}
