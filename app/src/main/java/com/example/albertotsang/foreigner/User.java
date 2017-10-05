package com.example.albertotsang.foreigner;


import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by AlbertoTsang on 12/20/15.
 */
public class User {

    private String idUser;
    private String name;
    private String about;
    private String gender;
    private Timestamp dob;
    private String profilePictureUrl;
    private String education;
    private String work;

    public User(String idUser, String name, String about, String gender, Timestamp dob, String education, String work) {
        this.idUser = idUser;
        this.name = name;
        this.about = about;
        this.gender = gender;
        this.dob = dob;
        this.education = education;
        this.work = work;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Timestamp getDob() {
        return dob;
    }

    public void setDob(Timestamp dob) {
        this.dob = dob;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }
}
