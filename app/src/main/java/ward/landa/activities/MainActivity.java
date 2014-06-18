package ward.landa.activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import utils.ConnectionDetector;
import utils.DBManager;
import utils.GCMUtils;
import utils.JSONParser;
import utils.Utilities;
import ward.landa.AboutActivity;
import ward.landa.Course;
import ward.landa.CourseNotification;
import ward.landa.R;
import ward.landa.Teacher;
import ward.landa.Update;
import ward.landa.fragments.FragmentCourses;
import ward.landa.fragments.FragmentTeachers;
import ward.landa.fragments.FragmentUpdates;

public class MainActivity extends FragmentActivity implements
        FragmentCourses.OnCourseSelected, FragmentTeachers.callbackTeacher,
        FragmentUpdates.updateCallback, OnBackStackChangedListener,
        OnNavigationListener {

     private static boolean rtlSupported;
    private static ArrayList<DrawerItem> items;
    private GoogleCloudMessaging gcm;
    private  String localLang;
    private   ViewPager mViewPager;
    private DrawerLayout drawerLayout;
    private   ActionBarDrawerToggle drawertoggle;
    private  ListView draweList;

    private Fragment[] pages;
    private  boolean isReg;
    private   boolean isTutuors;
    private  boolean isCourses;
    private   String regKey;
    private utils.DBManager db_mngr;
    private   ConnectionDetector connection_detector;

    // ------------------------------Activity LifeCycle--------------------\\
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        loadSettings();
        forceRTLIfSupported();

    }

    @Override
    protected void onStart() {
        Log.e("Fragment", "Main Activity started");
        connection_detector = new ConnectionDetector(getApplicationContext());
        initlizeDataBase();
        //checkPicsFromServer();
        loadRegsetrationData();
        setLocalLang();
        setTitle(R.string.app_name);

        initlizeFragments();
        if (rtlSupported)
            initlizeDrawerNavigation();
        else {
            initlizeSpinner();
            ListView l = (ListView) findViewById(R.id.left_drawer);
            l.setVisibility(DrawerLayout.GONE);
        }
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
        initlizePager();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (connection_detector.isConnectingToInternet())
            initlizeGCM();
        else {
            showDialogNoconnection((!isReg || !isCourses || !isTutuors));
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        int savedPos = getSharedPreferences("Position", MODE_PRIVATE).getInt(
                "viewPager", -1);
        if (savedPos != -1) {
            mViewPager.setCurrentItem(savedPos);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.e("Fragment", "Main Activity paused");
        SharedPreferences shP = getSharedPreferences("Position", MODE_PRIVATE);
        SharedPreferences.Editor ed = shP.edit();
        ed.putInt("viewPager", mViewPager.getCurrentItem());
        ed.commit();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.e("Fragment", "Main Activity stopped");
        super.onStop();
    }



    // ------------------------------Activity Extended-------------------\\

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 122) {
            CourseNotification c = (CourseNotification) data
                    .getSerializableExtra("notfiy");
            if (c != null) {
                Log.d("wordpress", c.getName());
                setAlarms(c);
            }
        }
        // super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);
        if (rtlSupported)
            drawertoggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        if (rtlSupported)
            drawertoggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO find the items and in menu and set it !drawerOpen
        boolean draweOpen;
        if (rtlSupported)
            draweOpen = drawerLayout.isDrawerOpen(draweList);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e1e1e")));
        // getActionBar().setDisplayShowHomeEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (rtlSupported)
            if (drawertoggle.onOptionsItemSelected(item)) {
                return true;
            }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackStackChanged() {
        // TODO Auto-generated method stub
        boolean canback = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getActionBar().setDisplayHomeAsUpEnabled(canback);
    }

    // ------------------------------Initlizations --------------------\\


    private void initlizeGCM() {
        gcm = GoogleCloudMessaging.getInstance(this);
        if (!isReg) {
            if (connection_detector.isConnectingToInternet()) {
                registerGcm task = new registerGcm();
                task.execute();

            }
        }

    }

    private void loadSettings() {
        Settings.initlizeSettings(getApplicationContext());
        this.localLang = Settings.getLocalLang();

    }

    private void setLocalLang() {
        Locale locale = new Locale(localLang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    private void initlizeDataBase() {
        db_mngr = new DBManager(getApplicationContext());
        db_mngr.checkForDownloadedImages();
    }

    private void initlizeFragments() {

        pages = new Fragment[3];
        pages[0] = new FragmentCourses();
        pages[1] = new FragmentTeachers();
        pages[2] = new FragmentUpdates();
    }

    private void initlizeDrawerNavigation() {
        items = new ArrayList<>();
        items.add(new DrawerSection(getResources().getString(
                R.string.Navigation)));
        items.add(new DrawerSubSectionItem(getResources().getString(
                R.string.updates), R.drawable.updates_icon,
                FragmentTypes.UPDATES
        ));
        items.add(new DrawerSubSectionItem(getResources().getString(
                R.string.teachers), R.drawable.tutor_icon, FragmentTypes.TUTORS));
        items.add(new DrawerSubSectionItem(getResources().getString(
                R.string.courses), R.drawable.courses_icon,
                FragmentTypes.COURSES
        ));
        items.add(new DrawerSection(getResources().getString(R.string.General)));
        items.add(new DrawerSubSectionItem(getResources().getString(
                R.string.settings), R.drawable.ic_action_settings,
                FragmentTypes.SETTINGS
        ));
        items.add(new DrawerSubSectionItem(getResources().getString(
                R.string.about), R.drawable.ic_action_about,
                FragmentTypes.ABOUT
        ));
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        draweList = (ListView) findViewById(R.id.left_drawer);
        draweAdapter dAdapter = new draweAdapter(getApplicationContext());
        draweList.setAdapter(dAdapter);
        draweList.setOnItemClickListener(new drawerOnItemClick(this));

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        drawertoggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {

                super.onDrawerClosed(drawerView);

                getActionBar().setTitle(
                        getResources().getString(R.string.app_name));
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {

                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(
                        getResources().getString(R.string.navigateTo));
                invalidateOptionsMenu();
            }

        };

        drawerLayout.setDrawerListener(drawertoggle);
    }

    private void initlizePager() {
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager(), getResources(), pages);

        // Setup the ViewPager with the sections adapter.
        PagerTabStrip strip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        mViewPager.setAdapter(mSectionsPagerAdapter);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mViewPager.setCurrentItem(2);

    }

    private void initlizeSpinner() {
        ActionBar actionBar = getActionBar();
        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.drawer_array, R.layout.spinner_layout);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(mSpinnerAdapter, this);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        switch (itemPosition) {
            case 0:
                mViewPager.setCurrentItem(2 - itemPosition);
                break;
            case 1:
                mViewPager.setCurrentItem(2 - itemPosition);
                break;
            case 2:
                mViewPager.setCurrentItem(2 - itemPosition);
                break;
            case 3:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            case 4:
                Intent s = new Intent(this, AboutActivity.class);
                startActivity(s);
                break;
        }
        return true;

    }

    // ------------------------------Fragments CallBacks --------------------\\

    @Override
    public void onUpdateClick(Update u) {

        Bundle extras = new Bundle();
        extras.putString("subject", u.getSubject());
        extras.putString("dateTime", u.getDateTime());
        extras.putString("content", u.getText());
        Intent i = new Intent(this, UpdateDetailActivity.class);
        // i.putExtras(extras);
        i.putExtra("Update", u);
        startActivityFromFragment(pages[2], i, 144);

    }


    @Override
    public void onCourseClick(Course c) {
        Bundle extras = new Bundle();
        extras.putString("name", c.getName());
        extras.putInt("ImageID", c.getImgID());
        extras.putInt("courseID", c.getCourseID());
        extras.putInt("position", 0);
        extras.putSerializable("course", c);
        Intent i = new Intent(this, CourseDeatilsActivity.class);
        i.putExtras(extras);
        startActivityForResult(i, 122);

    }

    @Override
    public void OnTeacherItemClick(Teacher t) {

        Bundle extras = new Bundle();
        extras.putSerializable("teacher", t);
        Intent i = new Intent(this, TutorDetails.class);
        i.putExtras(extras);
        startActivityFromFragment(pages[1], i, 133);

    }

    // ------------------------------Helping Methods --------------------\\

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (!Settings.getLocalLang().equals(Settings.ENGLISH))
                getWindow().getDecorView().setLayoutDirection(
                        View.LAYOUT_DIRECTION_RTL);
            rtlSupported = true;
        } else {

            rtlSupported = false;
        }
    }

    private void loadRegsetrationData() {
        SharedPreferences sh = getSharedPreferences(GCMUtils.DATA,
                Activity.MODE_PRIVATE);
        isReg = sh.getBoolean(GCMUtils.REGSITER, false);
        isTutuors = sh.getBoolean(GCMUtils.LOAD_COURSES, false);
        isCourses = sh.getBoolean(GCMUtils.LOAD_TEACHERS, false);
        regKey = sh.getString(GCMUtils.REG_KEY, null);

    }

    private void saveRegstrationData(boolean isReg, String regKey) {
        SharedPreferences sh = getSharedPreferences(GCMUtils.DATA,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor ed = sh.edit();
        ed.putBoolean(GCMUtils.REGSITER, isReg);
        ed.putString(GCMUtils.REG_KEY, regKey);
        ed.commit();
    }

    private void setAlarms(CourseNotification notification) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        for (Course c : notification.getCourse()) {
            if(c.getNotify()!=0) {
                int dayNum = Utilities.dayWeekNumber(c.getDateTime());
                if (dayNum == -1)
                    return;
                int hourFrom = Integer.parseInt(c.getTimeFrom().split(":")[0]
                        .replaceAll("\\s", ""));
                int minFrom = Integer.parseInt(c.getTimeFrom().split(":")[1]
                        .replaceAll("\\s", ""));
                if (minFrom - 20 < 0) {
                    hourFrom--;
                    minFrom = 40;
                }
                minFrom -= 20;
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, dayNum);
                calendar.set(Calendar.HOUR_OF_DAY, hourFrom);
                calendar.set(Calendar.MINUTE, minFrom);
                calendar.set(Calendar.SECOND, 0);
                Intent myIntent = new Intent(this, Reciever.class);
                myIntent.setAction(Settings.WARD_LANDA_ALARM);
                myIntent.putExtra("course", c);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                        c.getCourseID(), myIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

			/*
             * alarmManager.set(AlarmManager.RTC_WAKEUP,
			 * calendar.getTimeInMillis(), pendingIntent);
			 */
                if(c.getNotify()==1) {
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7,
                            pendingIntent);
                    calendar.set(Calendar.MINUTE, minFrom + 10);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7,
                            pendingIntent);
                }
                else{
                    alarmManager.cancel(pendingIntent);
                }
            }
            //TODO delete the Alarm (cancel)
            db_mngr.UpdateCourseNotification(c, c.getNotify());
        }
    }

    private void showDialogNoconnection(boolean isfirst) {
        new AlertDialog.Builder(this)
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
                                if (connection_detector
                                        .isConnectingToInternet()) {
                                    initlizeGCM();
                                    initlizePager();

                                } else if (!isReg || !isTutuors || !isCourses) {
                                    finish();
                                }

                            }
                        }
                ).show();
    }

    /**
     * @author wabbass enum for drawer item types
     */
    enum DrawerItemType {
        SECTION, ITEM
    }

    static enum FragmentTypes {
        UPDATES, TUTORS, COURSES, SETTINGS, ABOUT
    }

    /**
     * @author wabbass interface for draweItem Type
     */
    interface DrawerItem {

        public String getText();

        public DrawerItemType getType();

    }

    // ------------------------------Inner Static Classes --------------------\\
    /**
     * @author wabbass
     */
    static class SectionsPagerAdapter extends FragmentPagerAdapter {

     final   Resources res;
    final    Fragment[] f;

        public SectionsPagerAdapter(FragmentManager fm, Resources res,
                                    Fragment[] f) {
            super(fm);
            this.f = f;
            this.res = res;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Log.i("FragmentPosition", "Fposition = " + position);
            Bundle ex = new Bundle();
            ex.putBoolean("rtl", rtlSupported);
            f[position].setArguments(ex);
            switch (position) {
                case 0:

                    return f[0];
                case 1:

                    return f[1];
                case 2:
                    return f[2];

            }

            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return res.getString(R.string.courses);
                case 1:
                    return res.getString(R.string.teachers);
                case 2:
                    return res.getString(R.string.updates);

            }
            return null;
        }

        @Override
        public int getItemPosition(Object object) {

            return POSITION_NONE;
        }

    }

    /**
     * @author wabbass
     */
    static class draweAdapter extends BaseAdapter {

        LayoutInflater inflater = null;

        public draweAdapter(Context cxt) {

            inflater = (LayoutInflater) cxt
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {

            return items.size();
        }

        @Override
        public Object getItem(int arg0) {

            return items.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {

            return arg0;
        }

        @Override
        public View getView(int position, View converView, ViewGroup parent) {
            View v;
            DrawerItem item = (DrawerItem) getItem(position);
            if (item.getType() == DrawerItemType.SECTION) {
                v = getSectionView(converView, parent, item);
            } else {
                v = getSubSectionItemView(converView, parent, item);
            }
            return v;

        }

        private View getSectionView(View convertView, ViewGroup parentView,
                                    DrawerItem item) {
            DrawerSection section = (DrawerSection) item;
            SectionViewHolder vHolder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.drawe_section,
                        parentView, false);
                TextView tv = (TextView) convertView
                        .findViewById(R.id.draweSectionLable);
                vHolder = new SectionViewHolder();
                vHolder.sectionLable = tv;
                convertView.setTag(vHolder);
            }
            if (vHolder == null) {
                vHolder = (SectionViewHolder) convertView.getTag();
            }
            vHolder.sectionLable.setText(section.getText());
            return convertView;
        }

        private View getSubSectionItemView(View convertView,
                                           ViewGroup parentView, DrawerItem item) {
            DrawerSubSectionItem sectionItem = (DrawerSubSectionItem) item;
            SubSectionViewHolder vHolder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.draweritem, parentView,
                        false);
                TextView tv = (TextView) convertView
                        .findViewById(R.id.drawerTextView);
                ImageView ic = (ImageView) convertView
                        .findViewById(R.id.drawerIcon);
                vHolder = new SubSectionViewHolder();
                vHolder.itemLable = tv;
                vHolder.icon = ic;
                convertView.setTag(vHolder);
            }
            if (vHolder == null) {
                vHolder = (SubSectionViewHolder) convertView.getTag();
            }
            vHolder.itemLable.setText(sectionItem.getText());
            vHolder.icon.setImageResource(sectionItem.getIconId());
            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            DrawerItemType type = ((DrawerItem) getItem(position)).getType();
            return type == DrawerItemType.SECTION ? 0 : 1;

        }

        @Override
        public boolean isEnabled(int position) {
            DrawerItemType type = ((DrawerItem) getItem(position)).getType();
            return type == DrawerItemType.ITEM;
        }

        class SectionViewHolder {
            TextView sectionLable;
        }

        class SubSectionViewHolder {
            TextView itemLable;
            ImageView icon;
        }

    }

    /**
     * @author wabbass
     */
    static class drawerOnItemClick implements ListView.OnItemClickListener {

        final MainActivity activityRef;

        public drawerOnItemClick(MainActivity activity) {
            // TODO Auto-generated constructor stub
            activityRef = activity;
        }

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            DrawerItem item = items.get(arg2);

            if (item.getType() == DrawerItemType.ITEM) {
                DrawerSubSectionItem subItem = (DrawerSubSectionItem) item;
                switch (subItem.getvType()) {
                    case UPDATES:
                        activityRef.mViewPager.setCurrentItem(2);
                        break;
                    case COURSES:
                        activityRef.mViewPager.setCurrentItem(0);
                        break;
                    case TUTORS:
                        activityRef.mViewPager.setCurrentItem(1);
                        break;
                    case SETTINGS:
                        Intent i = new Intent(activityRef, SettingsActivity.class);
                        activityRef.startActivity(i);
                        break;
                    case ABOUT:
                        Intent s = new Intent(activityRef, AboutActivity.class);
                        activityRef.startActivity(s);
                        break;
                    default:
                        break;
                }
            }

            activityRef.drawerLayout.closeDrawer(activityRef.draweList);
        }

    }

    /**
     * @author wabbass class of drawer item type that represents the section
     *         lable
     */
    static class DrawerSection implements DrawerItem {

        private final String text;
        private final DrawerItemType type;

        public DrawerSection(String txt) {
            this.text = txt;
            this.type = DrawerItemType.SECTION;
        }

        @Override
        public String getText() {
            // TODO Auto-generated method stub
            return text;
        }

        @Override
        public DrawerItemType getType() {
            // TODO Auto-generated method stub
            return type;
        }

    }

    /**
     * @author wabbass class of drawer item type that represents the sub section
     *         item
     */
    static class DrawerSubSectionItem implements DrawerItem {

        private final String text;
        private final DrawerItemType type;
        private final int iconId;

        private final FragmentTypes vType;

        public DrawerSubSectionItem(String text, int icon, FragmentTypes type) {
            this.text = text;
            this.iconId = icon;
            this.type = DrawerItemType.ITEM;
            this.vType = type;
        }

        public FragmentTypes getvType() {
            return vType;
        }

        public int getIconId() {
            return iconId;
        }

        @Override
        public String getText() {
            // TODO Auto-generated method stub
            return text;
        }

        @Override
        public DrawerItemType getType() {
            // TODO Auto-generated method stub
            return type;
        }

    }

    /**
     * @author wabbass
     */
    public class registerGcm extends AsyncTask<String, String, String> {
        String st = null;

        @Override
        protected String doInBackground(String... arg0) {
            try {
                st = gcm.register(GCMUtils.SENDER_ID);
                if (st != null && !st.isEmpty()) {
                    isReg = true;
                    regKey = st;
                    Log.d(GCMUtils.TAG, "regKey is : " + regKey);
                    GCMUtils.sendRegistrationIdToBackend(regKey);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(GCMUtils.TAG, e.toString());
                if (!connection_detector.isConnectingToInternet()) {
                    Log.e(GCMUtils.TAG, "faild no internet ");
                    cancel(true);
                }
            }

            return "";

        }

        @Override
        protected void onPostExecute(String result) {
            if (isReg) {
                saveRegstrationData(true, st);
                Settings.saveSettings(getApplicationContext(), localLang, true,
                        true,true);
            }
            super.onPostExecute(result);
        }

    }

}
