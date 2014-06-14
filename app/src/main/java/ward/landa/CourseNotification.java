package ward.landa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CourseNotification implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7747987935693346513L;
    private String name;
    private List<Course> course;

    public CourseNotification(String name) {
        this.setName(name);
        this.course = new ArrayList<Course>();
    }

    public void addTimeForCourse(Course c) {
        this.course.add(c);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Course> getCourse() {
        return course;
    }

    public void setCourse(List<Course> course) {
        this.course = course;
    }


}
