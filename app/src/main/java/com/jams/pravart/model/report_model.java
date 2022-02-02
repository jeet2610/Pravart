package com.jams.pravart.model;

public class report_model {

    private String Image;
    private String location;

    private  report_model(){}


    public report_model(String Img, String loc){

        this.Image = Img;
        this.location  = loc;

    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }



}
