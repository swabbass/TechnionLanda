package utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ward.landa.Course;
import ward.landa.Teacher;
import ward.landa.Update;
import ward.landa.activities.Settings;

public class DBManager {
    private static final String DB_NAME = "db_LANDA";

    private static final int DB_VER = 13;

  private final    DB_HELPER dbHelper;

    public DBManager(Context context) {
        this.dbHelper = new DB_HELPER(context);
    }

    /**
     * replace escaped qoutes
     *
     * @param s string to replace
     * @return string with legal escaped qoutes
     */
    public static String removeQoutes(String s) {
        return s.replace("\"", "");
    }

    /**
     * Adding teacher to data base
     *
     * @param teacher Given teacher to add
     * @return UID in the data base ,-1 if not added
     */
    public long insertTeacher(Teacher teacher) {
        SQLiteDatabase teacher_db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dbTeacher.ID_NUMBER, teacher.getId_number());
        values.put(dbTeacher.FIRST_NAME, teacher.getName());
        values.put(dbTeacher.LAST_NAME, teacher.getLast_name());
        values.put(dbTeacher.EMAIL, teacher.getEmail());
        values.put(dbTeacher.FACULTY, teacher.getFaculty());
        values.put(dbTeacher.ROLE, teacher.getPosition());
        values.put(dbTeacher.DOWNLOADED_IMAGE,
                Boolean.toString(teacher.isDownloadedImage()));
        values.put(dbTeacher.IMAGE_URL, teacher.getImageUrl());
        values.put(dbTeacher.LOCAL_IMAGE_PATH, teacher.getImageLocalPath());
        long id = teacher_db.insert(dbTeacher.TEACHERS_TABLE, null, values);
        teacher_db.close();
        return id;
    }

    /**
     * Adding update to data base
     *
     * @param update Given update to add
     * @return UID in the data base ,-1 if not added
     */
    public long insertUpdate(Update update) {
        SQLiteDatabase updated_db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dbUpdate.UPDATE_ID, update.getUpdate_id());
        values.put(dbUpdate.UPDATE_SUBJECT, update.getSubject());
        values.put(dbUpdate.UPDATE_CONTENT, update.getText());
        values.put(dbUpdate.UPDATE_DATE, update.getDateTime());
        values.put(dbUpdate.UPDATE_URL, update.getUrl());
        values.put(dbUpdate.HTML_CONTENT,update.getHtml_text());
        values.put(dbUpdate.UPDATE_PINNED, Boolean.toString(update.isPinned()));
        long id = updated_db.insert(dbUpdate.UPDATES_TABLE, null, values);
        updated_db.close();
        return id;
    }

    /**
     * Adding course to data base
     *
     * @param course Given course to add
     * @return UID in the data base ,-1 if not added
     */
    public long insertCourse(Course course) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dbCourse.COURSE_ID, course.getCourseID());
        values.put(dbCourse.SUBJECT_ID, course.getSubject_id_string());
        values.put(dbCourse.TEACHER_ID, course.getTutor_id());
        values.put(dbCourse.COURSE_NAME, removeQoutes(course.getName()));
        values.put(dbCourse.COURSE_PLACE, course.getPlace());
        values.put(dbCourse.COURSE_DAY, course.getDateTime());
        values.put(dbCourse.COURSE_TIME_FROM, course.getTimeFrom());
        values.put(dbCourse.COURSE_TIME_TO, course.getTimeTo());
        values.put(dbCourse.DOWNLOADED_IMAGE, Boolean.toString(false));
        values.put(dbCourse.NOTIFIED, 0);
        long id = db.insert(dbCourse.COURSE_TABLE, null, values);
        db.close();
        return id;
    }

    /**
     * Update course in db with given course
     *
     * @param course Updated course to update in the data base
     *
     * @return true all ok ,false otherwise
     */
    public boolean UpdateCourse(Course course) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dbCourse.COURSE_ID, course.getCourseID());
        values.put(dbCourse.SUBJECT_ID, course.getSubject_id_string());
        values.put(dbCourse.TEACHER_ID, course.getTutor_id());
        values.put(dbCourse.COURSE_NAME, removeQoutes(course.getName()));
        values.put(dbCourse.COURSE_PLACE, course.getPlace());
        values.put(dbCourse.COURSE_DAY, course.getDateTime());
        values.put(dbCourse.COURSE_TIME_FROM, course.getTimeFrom());
        values.put(dbCourse.COURSE_TIME_TO, course.getTimeTo());

        values.put(dbCourse.DOWNLOADED_IMAGE,
                Boolean.toString(course.isDownloadedImage()));
        values.put(dbCourse.NOTIFIED, course.getNotify());

        boolean res = db.update(
                dbCourse.COURSE_TABLE,
                values,
                dbCourse.SUBJECT_ID + " = "
                        + getSQLText(Integer.toString(course.getSubject_id())),
                null
        ) > 0;
        // TODO add isdownload image and save the image and when update course
        // update for all subjects

        db.close();
        return res;
    }

    /**
     * update thet the given course notification status by notifiy
     *
     * @param course course to update
     * @param notify 1 enable notificatins for this course else 0
     * @return success true ,false otherwise
     */
    public boolean UpdateCourseNotification(Course course, int notify) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dbCourse.NOTIFIED, notify);
        boolean res = db.update(
                dbCourse.COURSE_TABLE,
                values,
                dbCourse.COURSE_ID + " = "
                        + getSQLText(Integer.toString(course.getCourseID())),
                null
        ) > 0;
        // TODO add isdownload image and save the image and when update course
        // update for all subjects

        db.close();
        return res;
    }

    /**
     * update image status for Course in db
     *
     * @param course     Course to update
     * @param downloaded true downlaoded ,false otherwise
     * @return success true ,false otherwise
     */
    public boolean UpdateCourseImageDownloaded(Course course, boolean downloaded) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dbCourse.DOWNLOADED_IMAGE, Boolean.toString(downloaded));
        boolean res = db.update(
                dbCourse.COURSE_TABLE,
                values,
                dbCourse.SUBJECT_ID + " = "
                        + getSQLText(course.getSubject_id_string()), null
        ) > 0;
        // TODO add isdownload image and save the image and when update course
        // update for all subjects

        db.close();
        return res;
    }

    /**
     * update image status for teacher in db
     *
     * @param teacher    teacher to update
     * @param downloaded true downlaoded ,false otherwise
     * @return success true ,false otherwise
     */
    public boolean UpdateTeacherImageDownloaded(Teacher teacher,
                                                boolean downloaded) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dbTeacher.DOWNLOADED_IMAGE, Boolean.toString(downloaded));
        boolean res = db.update(dbTeacher.TEACHERS_TABLE, values,
                dbTeacher.ID_NUMBER + " = "
                        + getSQLText(teacher.getId_number()), null
        ) > 0;
        // TODO add isdownload image and save the image and when update course
        // update for all subjects

        db.close();
        return res;
    }

    /**
     * update teacher info in db
     *
     * @param teacher updated teacher
     * @return success true ,false otherwise
     */
    public boolean updateTeacher(Teacher teacher) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dbTeacher.ID_NUMBER, teacher.getId_number());
        values.put(dbTeacher.FIRST_NAME, teacher.getName());
        values.put(dbTeacher.LAST_NAME, teacher.getLast_name());
        values.put(dbTeacher.EMAIL, teacher.getEmail());
        values.put(dbTeacher.ROLE, teacher.getPosition());
        values.put(dbTeacher.IMAGE_URL, teacher.getImageUrl());
        values.put(dbTeacher.DOWNLOADED_IMAGE,
                Boolean.toString((teacher.isDownloadedImage())));
        values.put(dbTeacher.LOCAL_IMAGE_PATH, teacher.getImageLocalPath());
        boolean res = database.update(dbTeacher.TEACHERS_TABLE, values,
                dbTeacher.ID_NUMBER + " = "
                        + getSQLText(teacher.getId_number()), null
        ) > 0;
        database.close();
        return res;
    }

    /**
     * Update update in the db
     *
     * @param update Updated update
     * @return success true ,false otherwise
     */
    public boolean updateUpdate(Update update) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dbUpdate.UPDATE_SUBJECT, update.getSubject());
        values.put(dbUpdate.UPDATE_CONTENT, update.getText());
        values.put(dbUpdate.UPDATE_DATE, update.getDateTime());
        values.put(dbUpdate.UPDATE_URL, update.getUrl());
        values.put(dbUpdate.HTML_CONTENT,update.getHtml_text());
        values.put(dbUpdate.UPDATE_PINNED, Boolean.toString(update.isPinned()));
        boolean res = database.update(dbUpdate.UPDATES_TABLE, values,
                dbUpdate.UPDATE_ID + " = " + getSQLText(update.getUpdate_id()),
                null) > 0;
        database.close();
        return res;
    }

    /**
     * deletes teacher in the db
     *
     * @param teacher to delete
     * @return success true ,false otherwise
     */
    public boolean deleteTeacher(Teacher teacher) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        boolean b = database.delete(dbTeacher.TEACHERS_TABLE,
                dbTeacher.ID_NUMBER + " = "
                        + getSQLText(teacher.getId_number()), null
        ) > 0;
        database.close();
        return b;
    }

    /**
     * deletes course from db
     *
     * @param course course to delte
     * @return success true ,false otherwise
     */
    public boolean deleteCourse(Course course) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        boolean b = database.delete(dbCourse.COURSE_TABLE, dbCourse.COURSE_ID
                        + " = " + getSQLText(Integer.toString(course.getCourseID())),
                null
        ) > 0;
        database.close();
        return b;
    }

    /**
     * deletes update from db
     *
     * @param update update to delete
     * @return success true ,false otherwise
     */
    public boolean deleteUpdate(Update update) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        boolean b = database.delete(dbUpdate.UPDATES_TABLE, dbUpdate.UPDATE_ID
                + "=" + getSQLText(update.getUpdate_id()), null) > 0;
        database.close();
        return b;
    }

    /**
     * resetting the data base deleting all the data
     *
     * @return true resetting success ,otherwise false
     */
    public boolean clearDb() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        boolean b = database.delete(dbUpdate.UPDATES_TABLE, "1", null) > 0;
        boolean b1 = database.delete(dbCourse.COURSE_TABLE, "1", null) > 0;
        boolean b2 = database.delete(dbTeacher.TEACHERS_TABLE, "1", null) > 0;
        database.close();
        return b && b1 && b2;
    }

    /**
     * adding ' char inorder to handle sql text with comparison example : hane
     * => 'hane'
     *
     * @param text text to add
     * @return string well formatted for sql query
     */
    String getSQLText(String text) {
        Character ch = (char) 34;
        return ch.toString() + text + ch.toString();
    }

    /**
     * gets all notified courses that user wanted to be notified
     *
     * @return list of notified courses from db
     */
    public List<Course> getAllNotifiedCourses() {
        Cursor cursor;

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        cursor = database.query(dbCourse.COURSE_TABLE, new String[]{
                dbCourse.COURSE_ID, dbCourse.COURSE_NAME, dbCourse.COURSE_DAY,
                dbCourse.COURSE_TIME_FROM, dbCourse.COURSE_TIME_TO,
                dbCourse.COURSE_PLACE, dbCourse.SUBJECT_ID

        }, dbCourse.NOTIFIED + " = " + "1", null, null, null, null);
        List<Course> result = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            Course c = new Course(cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4),
                    cursor.getString(5));
            c.setCourseID(Integer.parseInt(cursor.getString(0)));
            c.setNotify(1);
            c.setSubject_id(cursor.getString(6));
            result.add(c);
        }
        database.close();
        return result;
    }

    /**
     * Gets list of teachers from db
     *
     * @return list of teachers from db
     */
    public List<Teacher> getCursorAllTeachers() {
        Cursor cursor;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        cursor = database.query(dbTeacher.TEACHERS_TABLE, new String[]{
                dbTeacher.ID_NUMBER, dbTeacher.FIRST_NAME, dbTeacher.LAST_NAME,
                dbTeacher.ROLE, dbTeacher.EMAIL, dbTeacher.FACULTY,
                dbTeacher.IMAGE_URL, dbTeacher.LOCAL_IMAGE_PATH,
                dbTeacher.DOWNLOADED_IMAGE}, null, null, null, null, null);

        List<Teacher> res = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            String id_number = cursor.getString(0);
            String first_name = cursor.getString(1);
            String last_name = cursor.getString(2);
            String role = cursor.getString(3);
            String email = cursor.getString(4);
            String faculty = cursor.getString(5);
            String imageUrl = cursor.getString(6);
            String localImgPath = cursor.getString(7);
            String isDownloaded = cursor.getString(8);
            Teacher t = new Teacher(first_name, last_name, email, id_number,
                    role, faculty);
            t.setImageUrl(imageUrl);
            t.setImageLocalPath(localImgPath);
            t.setDownloadedImage(Boolean.valueOf(isDownloaded));
            res.add(t);
        }
        database.close();

        return res;
    }

    /**
     * Get all updates from db
     *
     * @return list f updates
     */
    public List<Update> getCursorAllUpdates() {
        Cursor cursor;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        cursor = database.query(dbUpdate.UPDATES_TABLE, new String[]{
                        dbUpdate.UPDATE_ID, dbUpdate.UPDATE_SUBJECT,
                        dbUpdate.UPDATE_CONTENT, dbUpdate.UPDATE_DATE,
                        dbUpdate.UPDATE_URL, dbUpdate.UPDATE_PINNED,dbUpdate.HTML_CONTENT}, null, null,
                null, null, dbUpdate.UPDATE_DATE + " DESC"
        );
        List<Update> updates = new ArrayList<>();
        while (cursor.moveToNext()) {
            Update u = new Update(cursor.getString(0), cursor.getString(1),
                    cursor.getString(3), cursor.getString(2),
                    cursor.getString(6));
            u.setUrl(cursor.getString(4));
            u.setPinned(Boolean.valueOf(cursor.getString(5)));
            updates.add(u);
        }
        database.close();

        return updates;
    }

    /**
     * Gets all Courses from the db no duplicates
     *
     * @return list of all the courses from the db
     */
    public List<Course> getCursorAllWithCourses() {
        List<Course> notified = getAllNotifiedCourses();
        Cursor cursor;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        cursor = database.query(false, dbCourse.COURSE_TABLE, new String[]{
                        dbCourse.COURSE_ID,
                        dbCourse.COURSE_NAME,
                        dbCourse.TEACHER_ID,
                        dbCourse.COURSE_PLACE,
                        dbCourse.COURSE_DAY,
                        dbCourse.COURSE_TIME_FROM,
                        dbCourse.COURSE_TIME_TO,
                        dbCourse.SUBJECT_ID, dbCourse.DOWNLOADED_IMAGE}, null, null,
                dbCourse.SUBJECT_ID, null, null, null
        );
        List<Course> courses = new ArrayList<>();
        while (cursor.moveToNext()) {
            Course c=new Course(cursor.getString(7));
            c.setCourseID(Integer.parseInt(cursor.getString(0)));
            c.setName(cursor.getString(1));
            c.setTimeFrom(cursor.getString(5));
            c.setTimeTo(cursor.getString(6));
            c.setDateTime(cursor.getString(4));
            c.setTutor_id(cursor.getString(2));
            c.setPlace(cursor.getString(3));
            c.setSubject_id(cursor.getString(7));
            c.setDownloadedImage(Boolean.valueOf(cursor.getString(8)));
            c.setImageUrl(c.getSubject_id_string());
            c.setImagePath(c.getSubject_id_string());
            if (notified.contains(c)) {
                c.setNotify(1);
            } else {
                c.setNotify(0);
            }
            courses.add(c);
        }
        database.close();
        return courses;
    }

    /**
     * Gets the teachers for given Course
     *
     * @param name course name
     * @return list og teachers that teaching the given course
     */
    public List<Teacher> getTeachersForCourse(String name) {
        Cursor cursor;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        List<Teacher> teachers = new ArrayList<>();
        cursor = database.query(dbCourse.COURSE_TABLE,
                new String[]{dbCourse.TEACHER_ID, dbCourse.COURSE_PLACE,
                        dbCourse.COURSE_DAY, dbCourse.COURSE_TIME_FROM,
                        dbCourse.COURSE_TIME_TO, dbCourse.NOTIFIED,
                        dbCourse.COURSE_ID}, dbCourse.COURSE_NAME + " = "
                        + getSQLText(name), null, null, null, null
        );
        cursor.moveToNext();
        do {
            String tID = cursor.getString(0);
            Teacher t = getTeacherByIdNumber(tID);
            if (!teachers.contains(t)) {
                teachers.add(t);
            }
            t = teachers.get(teachers.indexOf(t));
            String timeInfo = cursor.getString(1) + " - " + cursor.getString(2)
                    + " - " + cursor.getString(3) + " - " + cursor.getString(4)
                    + " - " + cursor.getString(5) + " - " + cursor.getString(6);
            t.addTimeToCourse(name, timeInfo);
            teachers.remove(t);
            teachers.add(t);
        } while (cursor.moveToNext());
        database.close();

        return teachers;
    }

    /**
     * Get Teacher from data base by teacher id number
     *
     * @param idNum String id number
     * @return the matching teacher ,null otherwise
     */
    public Teacher getTeacherByIdNumber(String idNum) {
        Cursor cursor;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        cursor = database.query(dbTeacher.TEACHERS_TABLE, new String[]{
                dbTeacher.ID_NUMBER, dbTeacher.FIRST_NAME, dbTeacher.LAST_NAME,
                dbTeacher.FACULTY,dbTeacher.DOWNLOADED_IMAGE}, dbTeacher.ID_NUMBER + " = "
                + getSQLText(idNum), null, null, null, null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToNext();
            Teacher t = new Teacher(cursor.getString(1), cursor.getString(2),
                    "", idNum, "T", cursor.getString(3));
            t.setDownloadedImage(Boolean.valueOf(cursor.getString(4)));
            database.close();

            return t;
        } else {
            database.close();

            return null;
        }

    }

    /**
     * Gets the course from db by course id
     *
     * @param id Database UID for the courser
     * @return the matching course ,null otherwise
     */
    public Course getCourseById(String id) {
        Cursor cursor;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        cursor = database.query(dbCourse.COURSE_TABLE, new String[]{
                dbCourse.COURSE_ID, dbCourse.COURSE_NAME,
                dbCourse.COURSE_PLACE, dbCourse.COURSE_DAY,
                dbCourse.COURSE_TIME_FROM, dbCourse.COURSE_TIME_TO,
                dbCourse.TEACHER_ID, dbCourse.SUBJECT_ID,
                dbCourse.DOWNLOADED_IMAGE}, dbCourse.COURSE_ID + " = "
                + getSQLText(id), null, null, null, null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToNext();
            Course c = new Course(cursor.getString(1), cursor.getString(3),
                    cursor.getString(4), cursor.getString(5),
                    cursor.getString(2));
            c.setCourseID(Integer.parseInt(cursor.getString(0)));
            c.setTutor_id(cursor.getString(6));
            c.setSubject_id(cursor.getString(7));
            c.setDownloadedImage(Boolean.valueOf(cursor.getString(8)));
            database.close();

            return c;
        } else {
            database.close();

            return null;
        }

    }


    /**
     * loops over the database and checks the images path if they existed
     * ,otherwise set the value downloaded to false
     * <p/>
     * This in case of user deleting the images manually
     */
    public void checkForDownloadedImages() {
        Cursor courses, tutors;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        courses = database.query(dbCourse.COURSE_TABLE,
                new String[]{dbCourse.SUBJECT_ID}, null, null, null, null,
                null);


        while (courses.moveToNext()) {
            String path = Settings.picFromAbsoulotePath + courses.getString(0)
                    + ".png";
            if (!Utilities.checkIfImageExistsInSd(path)) {
                UpdateCourseImageDownloaded(new Course(courses.getString(0)),
                        false);
            }

        }
        database.close();
        database = dbHelper.getWritableDatabase();
        tutors = database.query(dbTeacher.TEACHERS_TABLE,
                new String[]{dbTeacher.ID_NUMBER}, null, null, null, null,
                null);
        while (tutors.moveToNext()) {
            String path = Settings.picFromAbsoulotePath + tutors.getString(0)
                    + ".png";
            if (!Utilities.checkIfImageExistsInSd(path)) {
                UpdateTeacherImageDownloaded(new Teacher(tutors.getString(0)),
                        false);
            }

        }
        database.close();
    }


    public List<File> getImagesFiles() {
        Cursor courses, tutors;
        List<File> files = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        courses = database.query(dbCourse.COURSE_TABLE,
                new String[]{dbCourse.SUBJECT_ID}, null, null, null, null,
                null);


        while (courses.moveToNext()) {
            String path = Settings.picFromAbsoulotePath + courses.getString(0)
                    + ".png";
            if (Utilities.checkIfImageExistsInSd(path)) {
                File f = new File(path);
                files.add(f);
            }

        }
        database.close();
        database = dbHelper.getWritableDatabase();
        tutors = database.query(dbTeacher.TEACHERS_TABLE,
                new String[]{dbTeacher.ID_NUMBER}, null, null, null, null,
                null);
        while (tutors.moveToNext()) {
            String path = Settings.picFromAbsoulotePath + tutors.getString(0)
                    + ".png";
            if (Utilities.checkIfImageExistsInSd(path)) {
                File f = new File(path);
                files.add(f);
            }

        }
        database.close();
        return files;
    }

    /**
     * empty tutors and workshops table for new semester data
     * @return true no error ,false otherwise
     */
    public boolean resetSemester() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        boolean b1 = database.delete(dbCourse.COURSE_TABLE, "1", null) > 0;
        boolean b2 = database.delete(dbTeacher.TEACHERS_TABLE, "1", null) > 0;
        database.close();
        return (b1 || b2);
    }

    class DB_HELPER extends SQLiteOpenHelper {

        public DB_HELPER(Context context
                        ) {
            super(context, DB_NAME, null, DB_VER);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            String sql, sql2, sql3;
            sql = String.format("drop table if exists %s;",
                    dbTeacher.TEACHERS_TABLE);
            sql2 = String.format("drop table if exists %s;",
                    dbUpdate.UPDATES_TABLE);
            sql3 = String.format("drop table if exists %s;",
                    dbCourse.COURSE_TABLE);
            db.execSQL(sql);
            db.execSQL(dbTeacher.CREATE);
            db.execSQL(sql2);
            db.execSQL(dbUpdate.CREATE);
            db.execSQL(sql3);
            db.execSQL(dbCourse.CREATE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
            onCreate(db);

        }

    }

}

/**
 * Class that Demonstrates the Tutors Table in data base contans String Column
 * names and create sql script
 *
 * @author wabbass
 */
class dbTeacher {
    public static final String TEACHERS_TABLE = "Teachers";// ׳�?׳�?׳‘׳�׳�? ׳©׳�
    private static final String UID = "id";
    public static final String ID_NUMBER = "id_number";
    public static final String FIRST_NAME = "first_name";//
    public static final String LAST_NAME = "last_name";// ׳�?׳�?׳�׳₪׳•׳�
    // ׳�׳¡׳₪׳¨
    public static final String EMAIL = "email";
    public static final String FACULTY = "faculty";
    public static final String IMAGE_URL = "image_url";
    public static final String ROLE = "role";
    public static final String DOWNLOADED_IMAGE = "cached_img";
    public static final String LOCAL_IMAGE_PATH = "local_img_path";

    public static final String CREATE = "create table " + TEACHERS_TABLE + " ("
            + UID + " INTEGER PRIMARY KEY AUTOINCREMENT," + ID_NUMBER
            + " text not null, " + FIRST_NAME + " text not null, " + LAST_NAME
            + " text not null, " + EMAIL + " text not null, " + FACULTY
            + " text not null, " + ROLE + " text not null, " + DOWNLOADED_IMAGE
            + " text not null, " + IMAGE_URL + " text not null, "
            + LOCAL_IMAGE_PATH + " text not null " + ");";
    public static String ROLE_TEACHER = "T";
    public static String ROLE_INSTRUCTOR = "I";

}

/**
 * Class that Demonstrates the Courses Table in data base contans String Column
 * names and create sql script
 *
 * @author wabbass
 */
class dbCourse {
    public static final String COURSE_TABLE = "Courses";
    private static final String UID = "id";
    public static final String COURSE_ID = "course_id";
    public static final String SUBJECT_ID = "subject_id";
    public static final String TEACHER_ID = "id_number";
    public static final String COURSE_NAME = "course_name";
    public static final String COURSE_DAY = "course_day";
    public static final String NOTIFIED = "notified";
    public static final String COURSE_PLACE = "course_place";
    public static final String COURSE_DESCRIPTION = "course_description";
    public static final String COURSE_TIME_FROM = "course_time_from";
    public static final String COURSE_TIME_TO = "course_time_to";
    public static final String DOWNLOADED_IMAGE = "cached_img";
    public static final String CREATE = "create table " + COURSE_TABLE + " ("
            + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TEACHER_ID
            + " text not null, " + COURSE_ID + " text not null, " + SUBJECT_ID
            + " text not null, " + COURSE_NAME + " text not null, "
            + COURSE_DAY + " text not null, " + COURSE_PLACE
            + " text not null, " + COURSE_TIME_FROM + " text not null, "
            + COURSE_TIME_TO + " text not null, " + DOWNLOADED_IMAGE
            + " text not null, " + NOTIFIED + " NUMERIC not null " + " );";
}

/**
 * Class that Demonstrates the Updates Table in data base contans String Column
 * names and create sql script
 *
 * @author wabbass
 */
class dbUpdate {
    public static final String UPDATES_TABLE = "updates";// ׳�?׳�?׳‘׳�׳�? ׳©׳�
    private static final String UID = "id";
    public static final String UPDATE_ID = "subject_id";
    public static final String UPDATE_SUBJECT = "subject";
    public static final String UPDATE_CONTENT = "content";
    public static final String HTML_CONTENT = "html_content";
    public static final String UPDATE_PINNED = "pinned"; // ׳�?׳�?׳§׳¡׳�?
    public static final String UPDATE_DATE = "date";// ׳�?׳�?׳�׳₪׳•׳� ׳�׳¡׳₪׳¨
    public static final String UPDATE_URL = "url";
    public final static String CREATE = "create table " + UPDATES_TABLE + " ("
            + UID + " INTEGER PRIMARY KEY AUTOINCREMENT," + UPDATE_ID
            + " text not null, " + UPDATE_SUBJECT + " text not null, "
            + UPDATE_CONTENT + " text not null, " + UPDATE_PINNED
            + " text not null, " + UPDATE_DATE + " text not null, "+ HTML_CONTENT + " text not null, "
            + UPDATE_URL + " text not null" + ");";
}
