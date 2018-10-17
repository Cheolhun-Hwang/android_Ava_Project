package com.hch.hooney.avaappproject.ListPack.MelonChart;

public class MelonChartDAO {
    private String rank;
    private String artTitle;
    private String artist;
    private String pickture;

    public MelonChartDAO(){
        this.rank = null;
        this.artTitle = null;
        this.artist = null;
        this.pickture = null;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getArtTitle() {
        return artTitle;
    }

    public void setArtTitle(String artTitle) {
        this.artTitle = artTitle;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPickture() {
        return pickture;
    }

    public void setPickture(String pickture) {
        this.pickture = pickture;
    }
}
