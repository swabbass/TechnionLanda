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
    private static double lat = 0, longt = 0;
    private  List<Teacher> teachers;
    private HashMap<String, List<String>> timesForEachTeacher;

    private ExpandableListView exList;

    private String courseName;

    private  DBManager db_mngr;
    private   AlarmCallBack alarmCheckListner;
    private Course c;
    private  LocationListener locationListener;
    private  LocationManager mLocationManager;

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
        HashMap<String, String> res = new HashMap<>(5);
        res.put("day", day);
        res.put("timeFrom", timeFrom);
        res.put("timeTo", tumeTo);
        res.put("place", place);
        res.put("notify", notify);
        res.put("id", id);
        return res;
    }

    private static String getlocation() {
        if (lat == 0 || longt == 0) {
            return null;
        }
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
        if (ab != null) {
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }

        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void fetchArguments() {
        Bundle ex = getArguments();
        if (ex != null) {
            courseName = ex.getString("name");
            this.c = (Course) ex.getSerializable("course");
        }
    }

    private void initlizeUI(View root) {
        TextView courseNameLable = (TextView) root.findViewById(R.id.courseLable);
        ImageView courseImg = (ImageView) root.findViewById(R.id.courseAvatar);
        locationListener = initlizeLocationListner();
        courseNameLable.setText(courseName);
        if (c != null) {
            if(c.isDownloadedImage()){
            Picasso.with(getActivity()).load(new File(c.getImagePath()))
                    .into(courseImg);
            }
         else{
                //if the downloading process not finished
            Picasso.with(getActivity()).load(c.getImageUrl())
                    .error(R.drawable.ic_launcher).into(courseImg);
        }
        }

        // l = (ListView) root.findViewById(R.id.courseTeachers);
        exList = (ExpandableListView) root.findViewById(R.id.courseTeachers);
        teachers = db_mngr.getTeachersForCourse(courseName);
        timesForEachTeacher = new HashMap<>();
        for (Teacher t : teachers) {
            timesForEachTeacher.put(t.getId_number(),
                    t.getTimePlaceForCourse(courseName));
        }
    }

    private LocationListener initlizeLocationListner() {
        return new LocationListener() {
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
               if (location != null) {
                   lat = location.getLatitude();
                   longt = location.getLongitude();
                   Log.d("GPS", location.toString());
               }


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
        HashMap<String, String> params;
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
        private final Context _context;
        private final List<Teacher> _teachers;
        private final HashMap<String, List<String>> _listTimes;
        private final LayoutInflater inflater;
        private final AlarmCallBack listner;

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
                ChildViewHolder childViewHolder = new ChildViewHolder();
                v = inflater.inflate(R.layout.teacher_course_times, null);
                childViewHolder.switchToggle = (Switch) v.findViewById(R.id.switch1);
                childViewHolder.time = (TextView) v.findViewById(R.id.dTLable);
                childViewHolder.place = (TextView) v.findViewById(R.id.placeLable);
                childViewHolder.location = (ImageView) v.findViewById(R.id.pinImageView);
                v.setTag(childViewHolder);
            }
            final ChildViewHolder childViewHolder = (ChildViewHolder) v.getTag();
            HashMap<String, String> params = getParamsForCourse(dateTime);
            childViewHolder.place.setText(params.get("place"));
            childViewHolder.time.setText(params.get("day") + " " + params.get("timeFrom") + "-"
                    + params.get("timeTo"));
            if (params.get("notify").equals("1")) {
                childViewHolder.switchToggle.setChecked(true);

            } else {
                childViewHolder.switchToggle.setChecked(false);
            }
            childViewHolder.location.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    String source = getlocation();
                    String dest = Utilities.getLocationByAddress(childViewHolder.place.getText().toString(), _context.getResources());
                    if (dest != null && source != null) {
                        Utilities.openMapToNavigate(source, dest, _context);
                    } else {
                        if (dest != null) {

                            String[] cords = dest.split(",");
                            double x, y;
                            try {
                                x = Double.valueOf(cords[0]);
                                y = Double.valueOf(cords[1]);
                                Utilities.openMapInLocation(x, y, _context);
                            } catch (NumberFormatException nfe) {

                            }


                        }
                    }
                }
            });
            childViewHolder.switchToggle
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
                GroupViewHolder groupViewHolder = new GroupViewHolder();
                groupViewHolder.teacherAvatar = (CircleImageView)
                        v.findViewById(R.id.tutorSmallAvatar);
                groupViewHolder.alarmMe = (ImageView) v.findViewById(R.id.alaramMe);
                groupViewHolder.faculty = (TextView)
                        v.findViewById(R.id.teacherFacultyLable);
                groupViewHolder.name = (TextView)
                        v.findViewById(R.id.teacherCourseName);
                v.setTag(groupViewHolder);

            }
            GroupViewHolder groupViewHolder = (GroupViewHolder) v.getTag();

            if (isExpanded) {
                groupViewHolder.alarmMe.setImageResource(R.drawable.alarm_close);
            } else {
                groupViewHolder.alarmMe.setImageResource(R.drawable.alarm_open);
            }
            Teacher t = _teachers.get(groupPosition);
            if(t.isDownloadedImage()) {
                Picasso.with(_context).load(new File(t.getImageLocalPath()))
                        .into(groupViewHolder.teacherAvatar);
            }
            else{
                Picasso.with(_context).load(t.getImageUrl())
                        .error(R.drawable.ic_launcher).into(groupViewHolder.teacherAvatar);
            }
            groupViewHolder.name.setText(t.getName() + " " + t.getLast_name());
            groupViewHolder.faculty.setText(t.getFaculty());

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

        static class ChildViewHolder {
            ImageView location;
            Switch switchToggle;
            TextView place;
            TextView time;
        }

        static class GroupViewHolder {
            CircleImageView teacherAvatar;
            ImageView alarmMe;
            TextView faculty;
            TextView name;

        }

    }
}
