package com.example.albertotsang.foreigner;

import java.util.List;

/**
 * Created by AlbertoTsang on 12/18/15.
 */
public class SwipeCardData {

    private String iduser;

    private String name;

    private int age;

    private String occupation;

    private String imagePath;

    //private List<User> users = null;

    public SwipeCardData(String iduser, String name, String occupation, int age, String imagePath) {
        this.iduser = iduser;
        this.name = name;
        this.occupation = occupation;
        this.age = age;
        this.imagePath = imagePath;

    }

    public String getIdUser() {
        return iduser;
    }

    public String getName() {
        return name;
    }

    public String getOccupation() {
        return occupation;
    }

    public int getAge() { return age; }

    public String getImagePath() {
        return imagePath;
    }

}
