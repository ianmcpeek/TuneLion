package com.huscii.ian.tunelion;

/**
 * Created by Ian on 8/15/2015.
 */
public class SongData {
    private String songName;
    private String songArtist;
    private String songAlbum;
    private String songPath;

    public SongData(String songName, String songArtist, String songAlbum, String songPath) {
        this.songName = songName;
        this.songArtist = songArtist;
        this.songAlbum = songAlbum;
        this.songPath = songPath;
    }

    public String getSongName() {
        return songName;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getSongAlbum() {
        return songAlbum;
    }

    public String getSongPath() {
        return songPath;
    }

    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && object instanceof SongData)
        {
            sameSame = this.songName.equals(((SongData) object).songName) && this.songArtist.equals( ((SongData) object).songArtist);
        }

        return sameSame;
    }
}
