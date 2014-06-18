package ward.landa;

import android.net.Uri;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ward.landa.activities.Settings;

public class Teacher implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4848852330456784447L;
    private int ID;
    private int imgId;
    private String imageUrl;
    private String imageLocalPath;
    private String last_name;
    private String id_number;
    private String first_name;
    private String email;
    private String position;
    private String faculty;
    private boolean downloadedImage;
    private HashMap<String, List<String>> timesForEachCourse;



    public Teacher(String id_number) {
        this.id_number = id_number;
    }

    public Teacher(String fname, String lname, String email, String id_number,
                   String pos, String faculty) {

        this.id_number = id_number;
        setId_number(id_number);
        setName(fname);
        setLast_name(lname);
        setEmail(email);
        setPosition(pos);
        setFaculty(faculty);
        setImageUrl("http://nlanda.technion.ac.il/LandaSystem/pics/"
                + id_number + ".png");
        String t = Settings.picFromAbsoulotePath + id_number + ".png";
        setImageLocalPath(t);
        setDownloadedImage(false);
        timesForEachCourse = new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Teacher) {
            Teacher t = (Teacher) o;
            return getId_number().equals(t.getId_number());
        }
        return false;
    }

    public void addTimeToCourse(String name, String time) {
        List<String> times = timesForEachCourse.get(name);
        if (times == null) {
            times = new ArrayList<>();
            timesForEachCourse.put(name, times);
        }
        times.add(time);
    }

    public List<String> getTimePlaceForCourse(String course_name) {
        return timesForEachCourse.get(course_name);
    }

    @Override
    public String toString() {
        return getName() + " " + getLast_name();
    }

    /*
     *
     *
     *
     * setters and getters
     */
    public int getID() {
        return ID;
    }

    void setID(int iD) {
        ID = iD;
    }

    int getImgId() {
        return imgId;
    }

    void setImgId(int imgId) {
        this.imgId = imgId;
    }


    public String getName() {
        return first_name;
    }

    void setName(String name) {
        this.first_name = name.replaceAll("\\s", "");
    }

    public String getEmail() {
        return email;
    }


    void setEmail(String email) {
        this.email = email.replaceAll("\\s", "");
    }

    public String getPosition() {
        return position;
    }

    void setPosition(String position) {
        this.position = position;
    }

    public String getFaculty() {
        return faculty;
    }


    void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getLast_name() {
        return last_name;
    }

    void setLast_name(String last_name) {
        this.last_name = last_name.replaceAll("\\s", "");
    }

    public String getId_number() {
        return id_number;
    }

    void setId_number(String id_number) {
        this.id_number = id_number.replaceAll("\\s", "");
    }

    public String getImageUrl() {
        return imageUrl;
    }


    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageLocalPath() {
        return imageLocalPath;
    }

    public void setImageLocalPath(String imageLocalPath) {
        this.imageLocalPath = imageLocalPath;
    }

    public boolean isDownloadedImage() {
        return downloadedImage;
    }

    public void setDownloadedImage(boolean downloadedImage) {
        this.downloadedImage = downloadedImage;
    }

}
