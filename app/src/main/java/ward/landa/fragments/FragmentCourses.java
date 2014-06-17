package ward.landa.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import utils.ConnectionDetector;
import utils.DBManager;
import utils.GCMUtils;
import utils.JSONParser;
import utils.Utilities;
import ward.landa.Course;
import ward.landa.R;
import ward.landa.activities.Settings;

public class FragmentCourses extends Fragment {

   private static DBManager db_mngr;
    private JSONParser jParser;
    private  OnCourseSelected callback;

    private GridView g;
    private  List<Course> courses;
    private    coursesAdapter uAdapter;
    private  List<Course> searced;
    private   boolean loadFromDb;
    private  reciever corseRsvr;
    private    ConnectionDetector connectionDetector;


    @Override
    public void onPause() {
        if (corseRsvr != null) {

            getActivity().unregisterReceiver(corseRsvr);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        corseRsvr = new reciever();
        IntentFilter intentFilter = new IntentFilter(
                "com.google.android.c2dm.intent.RECEIVE");
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 2);
        getActivity().registerReceiver(corseRsvr, intentFilter);
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.course_menu, menu);
        View v = menu.findItem(R.id.course_menu_search).getActionView();
        if (!getArguments().getBoolean("rtl"))
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);

        EditText search = (EditText) v.findViewById(R.id.course_txt_search);
        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub
                Log.d("text", "text now changed");
                if (s.length() != 0) {
                    uAdapter.setCourses(search(s.toString()), 1);
                    uAdapter.notifyDataSetChanged();

                } else {
                    uAdapter.setCourses(courses, 0);
                    uAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                Log.d("text", "before text now changed");

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("text", "after text now changed");

            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Activity activity) {

        try {
            callback = (OnCourseSelected) activity;
            setHasOptionsMenu(true);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnCourseSelected");
        }
        super.onAttach(activity);
    }

    /**
     * showing dialog of no connection and handles the states if its first time
     * then close else offline mood working the data base information
     *
     * @param isfirst true first time ,false otherwise
     */
    private void showDialogNoconnection(boolean isfirst) {
        new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle("Info")
                .setMessage(
                        isfirst ? getResources().getString(
                                R.string.noConntectionMsgFirsttime)
                                : getResources().getString(
                                R.string.noConntection)
                )

                .setNeutralButton("Retry",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (connectionDetector.isConnectingToInternet()) {
                                    new loadDataFromBackend().execute();

                                } else if (!loadFromDb) {
                                    getActivity().finish();
                                } else {
                                    loadFromDataBase();
                                }

                            }
                        }
                ).show();
    }

    /**
     * loading the Courses from data base and attach the adapter to the gridview
     */
    private void loadFromDataBase() {
        courses = null;
        courses = db_mngr.getCursorAllWithCourses();
        uAdapter = new coursesAdapter(courses, getActivity(), getResources());

        SwingBottomInAnimationAdapter sb = new SwingBottomInAnimationAdapter(
                uAdapter);
        sb.setAbsListView(g);
        g.setAdapter(sb);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.courses_frag_grid, container, false);
        g = (GridView) root.findViewById(R.id.gridviewcourses);

        connectionDetector = new ConnectionDetector(getActivity());
        jParser = new JSONParser();
        db_mngr = new DBManager(getActivity());
        searced = new ArrayList<>();
        courses = new ArrayList<>();
        SharedPreferences sh = getActivity().getSharedPreferences(
                GCMUtils.DATA, Activity.MODE_PRIVATE);
        loadFromDb = sh.getBoolean(GCMUtils.LOAD_COURSES, false);
        boolean isConnected = connectionDetector.isConnectingToInternet();
        if (!loadFromDb && isConnected) {
            new loadDataFromBackend().execute();
        } else if (loadFromDb) {
            loadFromDataBase();

        }

        g.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                callback.onCourseClick(uAdapter.searched == 0 ? courses
                        .get(arg2) : searced.get(arg2));
                g.setItemChecked(arg2, true);
            }

        });

        return root;
    }

    /**
     * Searching for course name withen the courses
     *
     * @param st Course name (string to search or sub string )
     * @return List<Course> that matches the search
     */
    List<Course> search(String st) {
        searced.clear();
        for (Course c : courses) {
            if (c.getName().contains(st)) {
                searced.add(c);
            }
        }
        return searced;
    }

    public interface OnCourseSelected {

        public void onCourseClick(Course c);
    }

    /**
     * Handles the gridview items and inflate them
     *
     * @author wabbass
     */
    static class coursesAdapter extends BaseAdapter {

        List<Course> courses;
        LayoutInflater inflater = null;
        Context cxt = null;
       final Resources res;
        int searched;

        public coursesAdapter(List<Course> courses, Context cxt, Resources res) {
            this.courses = courses;
            this.cxt = cxt;
            this.inflater = (LayoutInflater) cxt
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.res = res;
            this.searched = 0;

        }

        public void setCourses(List<Course> courses, int search) {
            this.courses = courses;
            this.searched = search;
        }

        @Override
        public int getCount() {

            return courses.size();
        }

        @Override
        public Object getItem(int position) {

            return courses.get(position);
        }

        @Override
        public long getItemId(int position) {

            return courses.get(position).getCourseID();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            View v = view;

            final Course c = (Course) getItem(position);
            if (v == null) {
                CourseViewHolder viewHolder = new CourseViewHolder();
                v = inflater.inflate(R.layout.course_item_grid, parent, false);
                viewHolder.picture = (ImageView) v
                        .findViewById(R.id.list_image);
                viewHolder.name = (TextView) v.findViewById(R.id.title);
                viewHolder.alarm = (ImageView) v
                        .findViewById(R.id.alarm_course_image_view);
                v.setTag(viewHolder);
            }
            CourseViewHolder viewHolder = (CourseViewHolder) v.getTag();
            if (c.getNotify() == 0) {
                viewHolder.alarm.setVisibility(ImageView.INVISIBLE);
            } else if (c.getNotify() == 1) {
                viewHolder.alarm.setVisibility(ImageView.VISIBLE);
            }
            Target target = new Target() {
                @Override
                public void onBitmapFailed(Drawable arg0) {
                    c.setDownloadedImage(false);
                }

                @Override
                public void onBitmapLoaded(final Bitmap arg0, LoadedFrom arg1) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            Utilities.saveImageToSD(c.getImagePath(), arg0);

                        }
                    }).start();
                    c.setDownloadedImage(true);
                    db_mngr.UpdateCourseImageDownloaded(c, true);
                }

                @Override
                public void onPrepareLoad(Drawable arg0) {

                }

            };
            if (!c.isDownloadedImage()) {
                Picasso.with(cxt).load(c.getImageUrl())
                        .error(R.drawable.ic_launcher).into(viewHolder.picture);
                Picasso.with(cxt).load(c.getImageUrl()).into(target);
            } else {
                Picasso.with(cxt).load(new File(c.getImagePath()))
                        .error(R.drawable.ic_launcher).into(viewHolder.picture);
            }

            viewHolder.name.setText(c.getName());
            return v;
        }

    }

    static class CourseViewHolder {
        ImageView picture;
        TextView name;
        ImageView alarm;
    }

    /**
     * fetching data from back end for the first time when fetching fails due to
     * connectivity failuer then resting the data base and the settings so the
     * next time launch the app as first time
     *
     * @author wabbass
     */
    class loadDataFromBackend extends AsyncTask<String, String, String> {
        final    List<NameValuePair> params = new ArrayList<>();
        boolean allOk = false;
        List<Course> toSave = new ArrayList<>();

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading...");
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {

            JSONObject jsonCourses = jParser.makeHttpRequest(
                    Settings.URL_COURSES, "GET", params);
            if (jsonCourses == null) {
                if (cancel(true)) {
                    Log.e(GCMUtils.TAG,
                            "loading courses from internet canceled");
                }
            }
            Log.d("ward", jsonCourses != null ? jsonCourses.toString() : null);

            try {
                JSONArray jsonCoursesArray = jsonCourses
                        .getJSONArray("courses");

                for (int i = 0; i < jsonCoursesArray.length(); i++) {
                    JSONObject c = jsonCoursesArray.getJSONObject(i);
                    int id = Integer.valueOf(c.getString("id"));
                    Course tmp = new Course(id, DBManager.removeQoutes(c
                            .getString("subject_name")), c.getString("day"),
                            c.getString("time_from"), c.getString("time_to"),
                            c.getString("place"), c.getString("tutor_id")
                    );
                    tmp.setImgID(R.drawable.ic_launcher);
                    tmp.setSubject_id(c.getString("subject_id"));
                    tmp.setImageUrl(tmp.getSubject_id_string());
                    tmp.setImagePath(tmp.getSubject_id_string());
                    tmp.setDownloadedImage(false);
                    if (!courses.contains(tmp))
                        courses.add(tmp);
                    toSave.add(tmp);
                }
                if (connectionDetector.isConnectingToInternet())
                    allOk = true;
            } catch (JSONException e) {
                Log.e(GCMUtils.TAG, e.toString());
                if (!connectionDetector.isConnectingToInternet()) {
                    Log.e(GCMUtils.TAG, "faild no internet ");

                }

                return "";
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            if (allOk) {
                Utilities.saveDownloadOnceStatus(true, GCMUtils.LOAD_COURSES,
                        getActivity());

                for (Course c : toSave) {
                    db_mngr.insertCourse(c);
                }
                toSave = null;
                courses = db_mngr.getCursorAllWithCourses();
                uAdapter = new coursesAdapter(courses, getActivity(),
                        getResources());

                SwingBottomInAnimationAdapter sb = new SwingBottomInAnimationAdapter(
                        uAdapter);
                sb.setAbsListView(g);
                g.setAdapter(sb);

            } else {
                /*
                 * failing to fetch data from back end then reset the data base
				 * and the settings to launch as first time for the next launch
				 */
                db_mngr.clearDb();
                Utilities.saveDownloadOnceStatus(false, GCMUtils.LOAD_TEACHERS,
                        getActivity());
                Utilities.saveDownloadOnceStatus(false, GCMUtils.LOAD_UPDATES,
                        getActivity());
                Utilities.saveDownloadOnceStatus(false, GCMUtils.LOAD_COURSES,
                        getActivity());
                showDialogNoconnection(!loadFromDb);
            }
            super.onPostExecute(result);
        }

    }

    /**
     * Handles Messages that sent from back end adding,Updating,deleting
     * Workshop and notifiying the user about that
     *
     * @author wabbass
     */
    private class reciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction()
                    .compareTo("com.google.android.c2dm.intent.RECEIVE") == 0) {
                if (intent.getStringExtra("Type") != null) {
                    if (intent.getStringExtra("Type").contains("WORKSHOP")) {
                        abortBroadcast();
                        GCMUtils.HandleWorkshop(intent.getStringExtra("Type"),
                                context, db_mngr, intent);
                        courses = null;
                        courses = db_mngr.getCursorAllWithCourses();
                        uAdapter = new coursesAdapter(courses, getActivity(),
                                getResources());

                        SwingBottomInAnimationAdapter sb = new SwingBottomInAnimationAdapter(
                                uAdapter);
                        sb.setAbsListView(g);
                        g.setAdapter(sb);

                    }
                }
            }

        }

    }

}
