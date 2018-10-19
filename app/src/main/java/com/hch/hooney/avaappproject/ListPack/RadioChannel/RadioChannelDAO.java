package com.hch.hooney.avaappproject.ListPack.RadioChannel;

public class RadioChannelDAO {
    private String radioTitle;
    private String radioImage;
    private String radioURL;

    public RadioChannelDAO() {
        this.radioImage =null;
        this.radioTitle = null;
        this.radioURL = null;
    }

    public String getRadioTitle() {
        return radioTitle;
    }

    public void setRadioTitle(String radioTitle) {
        this.radioTitle = radioTitle;
    }

    public String getRadioImage() {
        return radioImage;
    }

    public void setRadioImage(String radioImage) {
        this.radioImage = radioImage;
    }

    public String getRadioURL() {
        return radioURL;
    }

    public void setRadioURL(String radioURL) {
        this.radioURL = radioURL;
    }

    @Override
    public String toString() {
        return this.radioTitle+" / " + this.radioImage + " / " + this.radioURL;
    }
}
