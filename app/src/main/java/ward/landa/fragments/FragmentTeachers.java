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
import java.util.regex.Pattern;

import utils.ConnectionDetector;
import utils.DBManager;
import utils.GCMUtils;
import utils.JSONParser;
import utils.Utilities;
import ward.landa.R;
import ward.landa.Teacher;
import ward.landa.activities.Settings;

public class FragmentTeachers extends Fragment {

    private static List<Teacher> tutors;
    private static DBManager db_mngr;
    private   List<Teacher> searched;
    private   callbackTeacher tCallback;
    private   gridAdabter gAdapter;
    private   JSONParser jParser;
    private  GridView gridView;
    private   boolean toFetchDataFromDB;
    private  ConnectionDetector connectionDetector;
    private  TeacherReciever tRsvr;
    private  SwingBottomInAnimationAdapter sb;
    private  View root;

    @Override
    public void onStop() {
        if (tRsvr != null) {

            getActivity().unregisterReceiver(tRsvr);
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        tRsvr = new TeacherReciever();
        IntentFilter intentFilter = new IntentFilter(
                "com.google.android.c2dm.intent.RECEIVE");
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        getActivity().registerReceiver(tRsvr, intentFilter);
        super.onResume();
    }

    /**
     * initlize the search editbox and handles the actions from the edit box
     *
     * @param v root view where the search is located
     */
    private void initlizeSearchEngine(View v) {

        EditText search = (EditText) v.findViewById(R.id.teacher_txt_search);
        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                if (s.length() != 0) {
                    gAdapter.setL(search(s.toString()), 1);
                    gAdapter.notifyDataSetChanged();

                } else {
                    gAdapter.setL(tutors, 0);
                    gAdapter.notifyDataSetChanged();
                }

            }

            private List<Teacher> search(String string) {
                searched.clear();
                for (Teacher t : tutors) {
                    if (t.getName().contains(string)) {
                        searched.add(t);
                    }
                }

                return searched;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.teacher_menu, menu);
        View v = menu.findItem(R.id.teacher_menu_search).getActionView();

        if (!getArguments().getBoolean("rtl"))
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        initlizeSearchEngine(v);
    }





    @Override
    public void onAttach(Activity activity) {
        try {
            tCallback = (callbackTeacher) activity;
            setHasOptionsMenu(true);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement callbackTeacher");
        }
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.teacher_custom_grid, container, false);
        connectionDetector = new ConnectionDetector(getActivity());
        db_mngr = new DBManager(getActivity());
        SharedPreferences sh = getActivity().getSharedPreferences(
                GCMUtils.DATA, Activity.MODE_PRIVATE);
        toFetchDataFromDB = sh.getBoolean(GCMUtils.LOAD_TEACHERS, false);
        jParser = new JSONParser();

        searched = new ArrayList<>();

        gridView = (GridView) root.findViewById(R.id.gridview);
        tutors = new ArrayList<>();

        boolean isConnected = connectionDetector.isConnectingToInternet();
        if (!toFetchDataFromDB && isConnected) {
            // fetch from internet
            new loadDataFromBackend().execute();
        } else {
            loadFromDataBase();

        }

        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                tCallback.OnTeacherItemClick(gAdapter.searched == 0 ? tutors
                        .get(arg2) : searched.get(arg2));

            }
        });

        return root;
    }

    /**
     * fetching teachers data from the data base and sets the adapter to
     * gridview
     */
    private void loadFromDataBase() {
        tutors = null;
        tutors = db_mngr.getCursorAllTeachers();
        gAdapter = new gridAdabter(root.getContext(), tutors, getResources());
        sb = new SwingBottomInAnimationAdapter(gAdapter);
        sb.setAbsListView(gridView);
        gridView.setAdapter(sb);
        sb.notifyDataSetChanged();

    }

    /**
     * showing dialog with message that there is no connection if its first time
     * the app will close otherwise will load data from database and woll be
     * offline mood
     *
     * @param isfirst true ,for first time lunching,false otherwise
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

                .setNeutralButton("Close",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (connectionDetector.isConnectingToInternet()) {
                                    new loadDataFromBackend().execute();

                                } else if (!toFetchDataFromDB) {
                                    getActivity().finish();
                                } else {
                                    loadFromDataBase();
                                }

                            }
                        }
                ).show();
    }

    public interface callbackTeacher {
        /**
         * handles the click action on teacher
         *
         * @param t Teacher Object that had been clicked
         */
        public void OnTeacherItemClick(Teacher t);
    }

    /**
     * custom adapter that handles the grid items
     *
     * @author wabbass
     */
    static class gridAdabter extends BaseAdapter {

      final   LayoutInflater inflater;
        List<Teacher> l;
     final   Resources res;
       final Context cxt;
        int searched = 0;

        public gridAdabter(Context context, List<Teacher> l, Resources res) {
            this.cxt = context;
            this.inflater = LayoutInflater.from(context);
            this.l = l;
            this.res = res;
            this.searched = 0;
        }

        public void setL(List<Teacher> l, int search) {
            this.l = l;
            this.searched = search;
        }

        @Override
        public int getCount() {

            return l.size();
        }

        @Override
        public Object getItem(int arg0) {

            return l.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {

            return l.get(arg0).getID();
        }

        @Override
        public View getView(int pos, View view, ViewGroup viewGroup) {
            View v = view;

            if (v == null) {
                TeacherViewHolder viewHolder = new TeacherViewHolder();
                v = inflater.inflate(R.layout.grid_textimg_item, viewGroup,
                        false);
                viewHolder.name = (TextView) v.findViewById(R.id.text);
                viewHolder.picture = (ImageView) v.findViewById(R.id.picture);
                v.setTag(viewHolder);
            }
            TeacherViewHolder viewHolder = (TeacherViewHolder) v.getTag();
            final Teacher teacher = (Teacher) getItem(pos);
            Target target = new Target() {
                @Override
                public void onBitmapFailed(Drawable arg0) {
                    // TODO Auto-generated method stub
                    teacher.setDownloadedImage(false);
                }

                @Override
                public void onBitmapLoaded(final Bitmap arg0, LoadedFrom arg1) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            Utilities.saveImageToSD(
                                    teacher.getImageLocalPath(), arg0);
                            teacher.setDownloadedImage(true);

                        }
                    }).start();

                }

                @Override
                public void onPrepareLoad(Drawable arg0) {
                    // TODO Auto-generated method stub

                }

            };
            db_mngr.UpdateTeacherImageDownloaded(teacher, true);
            if (!teacher.isDownloadedImage()) {
                Picasso.with(cxt).load(teacher.getImageUrl())
                        .error(R.drawable.ic_launcher).into(viewHolder.picture);
                Picasso.with(cxt).load(teacher.getImageUrl()).into(target);
            } else {
                Picasso.with(cxt).load(new File(teacher.getImageLocalPath()))
                        .error(R.drawable.ic_launcher).into(viewHolder.picture);
            }
            viewHolder.name.setText(teacher.toString());

            return v;
        }
    }

    static class TeacherViewHolder {
        ImageView picture;
        TextView name;
    }

    /**
     * boradcast reciver that handles messages from the backend
     * adding,updating,removing tutors and notifiying the user about that
     *
     * @author wabbass
     */
    private class TeacherReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction()
                    .compareTo("com.google.android.c2dm.intent.RECEIVE") == 0) {
                if (intent.getStringExtra("Type") != null) {
                    if (intent.getStringExtra("Type").contains("INSTRUCTOR")) {
                        abortBroadcast();
                        String type = intent.getStringExtra("Type");
                        tutors = null;
                        tutors = db_mngr.getCursorAllTeachers();
                        gAdapter = new gridAdabter(root.getContext(), tutors,
                                getResources());
                        sb = new SwingBottomInAnimationAdapter(gAdapter);
                        sb.setAbsListView(gridView);
                        gridView.setAdapter(sb);
                        sb.notifyDataSetChanged();
                    } else if (intent.getStringExtra("Type").contains(
                            "TEACHER_PIC")) {
                        abortBroadcast();
                        String id_number = intent.getStringExtra("Image");
                        final String[] file = id_number.split(Pattern
                                .quote("."));

                        Target target = new Target() {
                            @Override
                            public void onBitmapFailed(Drawable arg0) {
                                // TODO Auto-generated method stub
                            }

                            @Override
                            public void onBitmapLoaded(final Bitmap arg0,
                                                       LoadedFrom arg1) {
                                Utilities.saveImageToSD(
                                        Settings.picFromAbsoulotePath + file[0]
                                                + ".png", arg0
                                );
                                gAdapter.notifyDataSetChanged();
                            /*	int index=tutors.indexOf(t);
                                Teacher tmp=tutors.get(index);
								tmp.setImageLocalPath(Settings.picFromAbsoulotePath + file[0]
												+ "-new.png");
								tutors.set(index, tmp);*/

                            }

                            @Override
                            public void onPrepareLoad(Drawable arg0) {
                                // TODO Auto-generated method stub

                            }

                        };


                        Picasso.with(getActivity())
                                .load("http://nlanda.technion.ac.il/LandaSystem/pics/"
                                        + file[0] + ".png").into(target);

                    }

                }
            }

        }

    }

    /**
     * Asynctask to fetch the data for teachers from the backend and when
     * success saving the data in data base
     *
     * @author wabbass
     */
    class loadDataFromBackend extends AsyncTask<String, String, String> {
        final List<NameValuePair> params = new ArrayList<>();
        boolean allOk = false;
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
            JSONObject jsonUsers = jParser.makeHttpRequest(
                    Settings.URL_teachers, "GET", params);
            if (jsonUsers == null) {
                if (cancel(true)) {
                    Log.e(GCMUtils.TAG,
                            "loading teachers from internet canceled");
                }
            }
            Log.d("ward", jsonUsers != null ? jsonUsers.toString() : null);
            try {

                JSONArray teachers = jsonUsers.getJSONArray("users");
                for (int i = 0; i < teachers.length(); i++) {
                    JSONObject c = teachers.getJSONObject(i);
                    Teacher t = new Teacher(c.getString("fname"),
                            c.getString("lname"), c.getString("email"),
                            c.getString("id"), utils.Role.getRole(
                            Integer.valueOf(c.getString("position")))
                            .name(), c.getString("faculty")
                    );
                    t.setDownloadedImage(false);
                    tutors.add(t);

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
                Utilities.saveDownloadOnceStatus(true, GCMUtils.LOAD_TEACHERS,
                        getActivity());
                for (Teacher t : tutors) {
                    db_mngr.insertTeacher(t);
                }

                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        gAdapter = new gridAdabter(getActivity(), tutors,
                                getResources());
                        SwingBottomInAnimationAdapter sb = new SwingBottomInAnimationAdapter(
                                gAdapter);
                        sb.setAbsListView(gridView);
                        gridView.setAdapter(sb);
                        gAdapter.notifyDataSetChanged();
                        sb.notifyDataSetChanged(true);

                    }
                });

            } else {
                db_mngr.clearDb();
                showDialogNoconnection(!toFetchDataFromDB);
                Utilities.saveDownloadOnceStatus(false, GCMUtils.LOAD_TEACHERS,
                        getActivity());
                Utilities.saveDownloadOnceStatus(false, GCMUtils.LOAD_UPDATES,
                        getActivity());
                Utilities.saveDownloadOnceStatus(false, GCMUtils.LOAD_COURSES,
                        getActivity());

            }
            super.onPostExecute(result);
        }

    }

}
