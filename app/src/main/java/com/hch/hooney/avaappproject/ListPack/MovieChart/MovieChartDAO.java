package com.hch.hooney.avaappproject.ListPack.MovieChart;

public class MovieChartDAO {
    private String name;
    private String rank;
    private String showDate;
    private String ticketSales;
    private String imageURL;

    public MovieChartDAO(){
        this.name = null;
        this.rank = null;
        this.showDate=null;
        this.ticketSales = null;
        this.imageURL = null;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public String getTicketSales() {
        return ticketSales;
    }

    public void setTicketSales(String ticketSales) {
        this.ticketSales = ticketSales;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
