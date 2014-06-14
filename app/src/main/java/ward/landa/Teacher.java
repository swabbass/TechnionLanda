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

    // private HashMap<String, Course> courses;
    public Teacher(int ID, int imgId, String name, String email, String phone,
                   String pos, String faculty) {
        setID(ID);
        setImgId(imgId);
        setName(name);
        setEmail(email);
        setPosition(pos);
        setFaculty(faculty);
        timesForEachCourse = new HashMap<String, List<String>>();
    }

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
        timesForEachCourse = new HashMap<String, List<String>>();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Teacher) {
            Teacher t = (Teacher) o;
            return getId_number().equals(t.getId_number());
        }
        return false;
    }

    public void addCourse(String course_name, List<String> times) {

        timesForEachCourse.put(course_name, times);

    }

    public void addTimeToCourse(String name, String time) {
        List<String> times = timesForEachCourse.get(name);
        if (times == null) {
            times = new ArrayList<String>();
            timesForEachCourse.put(name, times);
        }
        times.add(time);
    }

    public void removeCourse(String course_name) {

        timesForEachCourse.remove(course_name);

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

    public void setID(int iD) {
        ID = iD;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public String getName() {
        return first_name;
    }

    public void setName(String name) {
        this.first_name = name.replaceAll("\\s", "");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.replaceAll("\\s", "");
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name.replaceAll("\\s", "");
    }

    public String getId_number() {
        return id_number;
    }

    public void setId_number(String id_number) {
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

    public HashMap<String, List<String>> getTimesForEachCourse() {
        return timesForEachCourse;
    }

    public void setTimesForEachCourse(
            HashMap<String, List<String>> timesForEachCourse) {
        this.timesForEachCourse = timesForEachCourse;
    }

    public Uri getUriFromLocal() {
        File f = new File(this.imageLocalPath);
        return Uri.fromFile(f);
    }
}
