package ward.landa;

import java.io.Serializable;

import ward.landa.activities.Settings;

public class Course implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 2267973230480147607L;
    private String name;
    private String day;
    private String timeFrom;
    private String timeTo;
    private String place;
    private String Teacher;
    private int imgID;
    private String ImageUrl;
    private String ImagePath;
    private int courseID;
    private long course_db_id;
    private float rating;
    private String tutor_id;
    private int notify;
    private int subject_id;
    private boolean downloadedImage;
    //private List<Teacher> teachers;


    public Course(String sub_id) {
        this.subject_id = Integer.valueOf(sub_id);
    }

    public Course(String name, String day, String timeFrom, String timeTo, String place) {
        this.name = name;
        this.day = day;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.place = place;
        this.notify = 1;

    }



    public Course(int courseID, String name, String dateString, String timeFrom, String timeTo, String place, String tutor_id) {
        setCourseID(courseID);
        setName(name);
        setDateTime(dateString);
        setTimeFrom(timeFrom);
        setTimeTo(timeTo);
        setPlace(place);
        setImgID(0);
        setTutor_id(tutor_id);

    }

    @Override
    public boolean equals(Object o) {
        // TODO Auto-generated method stub
        if (o instanceof Course) {
            Course tmp = (Course) o;
            return tmp.getName().equals(this.getName());
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateTime() {
        return day;
    }

    public void setDateTime(String dateTime) {
        this.day = dateTime;
    }

    public String getTeacher() {
        return Teacher;
    }

    public void setTeacher(String teacher) {
        Teacher = teacher;
    }

    public int getImgID() {
        return imgID;
    }

    public void setImgID(int imgID) {
        this.imgID = imgID;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }





    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(String timeTo) {
        this.timeTo = timeTo;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(String timeFrom) {
        this.timeFrom = timeFrom;
    }

    public String getTutor_id() {
        return tutor_id;
    }

    public void setTutor_id(String tutor_id) {
        this.tutor_id = tutor_id;
    }

    public int getNotify() {
        return notify;
    }

    public void setNotify(int notify) {
        this.notify = notify;
    }

    public long getCourse_db_id() {
        return course_db_id;
    }

    public void setCourse_db_id(long course_db_id) {
        this.course_db_id = course_db_id;
    }

    public int getSubject_id() {
        return subject_id;
    }

    public void setSubject_id(int subject_id) {
        this.subject_id = subject_id;
    }

    public void setSubject_id(String subject_id) {
        this.subject_id = Integer.valueOf(subject_id);
    }

    public String getSubject_id_string() {
        return Integer.toString(this.subject_id);
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String subject_id) {
        ImageUrl = "http://nlanda.technion.ac.il/LandaSystem/pics/"
                + subject_id + ".png";
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String subject_id) {
        ImagePath = Settings.picFromAbsoulotePath + subject_id + ".png";
    }

    public boolean isDownloadedImage() {
        return downloadedImage;
    }

    public void setDownloadedImage(boolean downloadedImage) {
        this.downloadedImage = downloadedImage;
    }


}
