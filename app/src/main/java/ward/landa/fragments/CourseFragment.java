package ward.landa.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import utils.DBManager;
import utils.Utilities;
import ward.landa.Course;
import ward.landa.ImageUtilities.CircleImageView;
import ward.landa.R;
import ward.landa.Teacher;

public class CourseFragment extends Fragment {

    private static final long LOCATION_REFRESH_TIME = 30000;
    private static final float LOCATION_REFRESH_DISTANCE = 2.5f;
    private static double lat, longt;
    List<Teacher> teachers;
    HashMap<String, List<String>> timesForEachTeacher;
    ListView l;
    ExpandableListView exList;
    ExpandableListAdapter exAdapter;
    int courseID;
    int imgId;
    String courseName;
    String courseDesription;
    TextView courseNameLable;
    ImageView courseImg;
    DBManager db_mngr;
    AlarmCallBack alarmCheckListner;
    Course c;
    LocationListener locationListener;
    LocationManager mLocationManager;

    /**
     * @param dateTime a day -timefrom-timeto-place-toNotify pattern to
     * @return hashmap with the values seperated
     */
    private static HashMap<String, String> getParamsForCourse(String dateTime) {
        String[] info = dateTime.split(Pattern.quote(" - "));
        int lastIndex = info.length - 3;
        String day = info[lastIndex - 2];
        String timeFrom = info[lastIndex - 1];
        String tumeTo = info[lastIndex];
        String place = info[lastIndex - 3];
        String notify = info[lastIndex + 1];
        String id = info[lastIndex + 2];
        for (int i = lastIndex - 4; i >= 0; --i) {
            place += " " + info[i];
        }
        HashMap<String, String> res = new HashMap<String, String>(5);
        res.put("day", day);
        res.put("timeFrom", timeFrom);
        res.put("timeTo", tumeTo);
        res.put("place", place);
        res.put("notify", notify);
        res.put("id", id);
        return res;
    }

    public static String getlocation() {
        return Double.toString(lat) + "," + Double.toString(longt);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            alarmCheckListner = (AlarmCallBack) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement callbackTeacher");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ActionBar ab = getActivity().getActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void fetchArguments() {
        Bundle ex = getArguments();
        if (ex != null) {
            courseName = ex.getString("name");
            this.imgId = ex.getInt("ImageID");
            this.courseID = ex.getInt("courseID");
            this.c = (Course) ex.getSerializable("course");
        }
    }

    private void initlizeUI(View root) {
        courseNameLable = (TextView) root.findViewById(R.id.courseLable);
        courseImg = (ImageView) root.findViewById(R.id.courseAvatar);
        locationListener = initlizeLocationListner();
        courseNameLable.setText(courseName);
        if (c != null) {
            Picasso.with(getActivity()).load(new File(c.getImagePath()))
                    .into(courseImg);
        } else
            courseImg.setImageResource(imgId);

        // l = (ListView) root.findViewById(R.id.courseTeachers);
        exList = (ExpandableListView) root.findViewById(R.id.courseTeachers);
        teachers = db_mngr.getTeachersForCourse(courseName);
        timesForEachTeacher = new HashMap<String, List<String>>();
        for (Teacher t : teachers) {
            timesForEachTeacher.put(t.getId_number(),
                    t.getTimePlaceForCourse(courseName));
        }
    }

    private LocationListener initlizeLocationListner() {
        final LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                lat = location.getLatitude();
                longt = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d("GPS", s);
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d("GPS", s);
                Location location = getLastBestLocation();
                lat = location.getLatitude();
                longt = location.getLongitude();
                Log.d("GPS", location.toString());

            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("GPS", s);
                Location location = getLastBestLocation();
                if (location != null) {
                    lat = location.getLatitude();
                    longt = location.getLongitude();
                }
            }
        };
        return mLocationListener;
    }

    private void initlizeLocationManager() {

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, locationListener);
    }

    private Location getLastBestLocation() {
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_course, null);
        db_mngr = new DBManager(getActivity());
        locationListener = initlizeLocationListner();
        initlizeLocationManager();
        fetchArguments();
        initlizeUI(root);
        ExpandableListAdapter ad = new ExpandableListAdapter(getActivity(),
                teachers, timesForEachTeacher, alarmCheckListner);
        exList.setAdapter(ad);
        expandNotifiedDateTimes();

        return root;
    }

    private void expandNotifiedDateTimes() {
        HashMap<String, String> params = null;
        for (Teacher teacher : teachers) {
            List<String> dates = timesForEachTeacher
                    .get(teacher.getId_number());
            for (String dateTime : dates) {
                params = getParamsForCourse(dateTime);
                if (params.get("notify").equals("1")) {
                    exList.expandGroup(teachers.indexOf(teacher), true);
                }
            }
        }

    }

    public interface AlarmCallBack {
        public void onTimeChecked(String time, boolean isChecked);
    }

    static class ExpandableListAdapter extends BaseExpandableListAdapter {
        private Context _context;
        private List<Teacher> _teachers;
        private HashMap<String, List<String>> _listTimes;
        private LayoutInflater inflater;
        private AlarmCallBack listner;

        public ExpandableListAdapter(Context context, List<Teacher> teachers,
                                     HashMap<String, List<String>> listTimes, AlarmCallBack lister) {
            this._context = context;
            this._teachers = teachers;
            this._listTimes = listTimes;
            this.listner = lister;
            this.inflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public Object getChild(int groupPos, int ChildPos) {
            // return the list of theacher time for this course (String List) by
            // id number given and get the spiceifec child (String )from strings
            // list
            return this._listTimes.get(
                    this._teachers.get(groupPos).getId_number()).get(ChildPos);
        }

        @Override
        public long getChildId(int groupPos, int ChildPos) {
            // TODO Auto-generated method stub
            return ChildPos;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final String dateTime = (String) getChild(groupPosition,
                    childPosition);
            View v = convertView;
            if (v == null) {
                v = inflater.inflate(R.layout.teacher_course_times, null);
                v.setTag(R.id.switch1, v.findViewById(R.id.switch1));
                v.setTag(R.id.dTLable, v.findViewById(R.id.dTLable));
                v.setTag(R.id.placeLable, v.findViewById(R.id.placeLable));
                v.setTag(R.id.pinImageView, v.findViewById(R.id.pinImageView));
            }
            ImageView location = (ImageView) v.getTag(R.id.pinImageView);
            Switch switchToggle = (Switch) v.getTag(R.id.switch1);
            HashMap<String, String> params = getParamsForCourse(dateTime);
            final TextView place = (TextView) v.getTag(R.id.placeLable);
            TextView time = (TextView) v.getTag(R.id.dTLable);
            place.setText(params.get("place"));
            time.setText(params.get("day") + " " + params.get("timeFrom") + "-"
                    + params.get("timeTo"));
            if (params.get("notify").equals("1")) {
                switchToggle.setChecked(true);

            } else {
                switchToggle.setChecked(false);
            }
            location.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    String source = getlocation();
                    String dest = Utilities.getLocationByAddress(place.getText().toString(), _context.getResources());
                    if (dest != null) {
                        Utilities.openMapToNavigate(source, dest, _context);
                    }
                }
            });
            switchToggle
                    .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {

                            listner.onTimeChecked(dateTime, isChecked);
                        }
                    });
            return v;
        }

        @Override
        public int getChildrenCount(int arg0) {

            return this._listTimes.get(this._teachers.get(arg0).getId_number())
                    .size();
        }

        @Override
        public Object getGroup(int arg0) {
            return this._teachers.get(arg0);
        }

        @Override
        public int getGroupCount() {
            return this._teachers.size();
        }

        @Override
        public long getGroupId(int arg0) {
            return arg0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                v = inflater.inflate(R.layout.course_teacher_details, parent,
                        false);
                v.setTag(R.id.tutorSmallAvatar,
                        v.findViewById(R.id.tutorSmallAvatar));
                v.setTag(R.id.alaramMe, v.findViewById(R.id.alaramMe));
                v.setTag(R.id.teacherFacultyLable,
                        v.findViewById(R.id.teacherFacultyLable));
                v.setTag(R.id.teacherCourseName,
                        v.findViewById(R.id.teacherCourseName));

            }
            CircleImageView teacherAvatar = (CircleImageView) v
                    .getTag(R.id.tutorSmallAvatar);
            ImageView alarmMe = (ImageView) v.getTag(R.id.alaramMe);
            if (isExpanded) {
                alarmMe.setImageResource(R.drawable.alarm_close);
            } else {
                alarmMe.setImageResource(R.drawable.alarm_open);
            }
            TextView faculty = (TextView) v.getTag(R.id.teacherFacultyLable);
            TextView name = (TextView) v.getTag(R.id.teacherCourseName);
            Teacher t = (Teacher) _teachers.get(groupPosition);
            Picasso.with(_context).load(new File(t.getImageLocalPath()))
                    .into(teacherAvatar);
            name.setText(t.getName() + " " + t.getLast_name());
            faculty.setText(t.getFaculty());

            return v;
        }

        @Override
        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isChildSelectable(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return true;
        }

    }
}
