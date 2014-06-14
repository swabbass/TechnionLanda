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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nhaarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import utils.ConnectionDetector;
import utils.DBManager;
import utils.GCMUtils;
import utils.JSONParser;
import utils.Utilities;
import ward.landa.AboutActivity;
import ward.landa.ExpandableTextView;
import ward.landa.R;
import ward.landa.Update;
import ward.landa.activities.Settings;
import ward.landa.activities.SettingsActivity;

public class FragmentUpdates extends Fragment {

    GoogleCloudMessaging gcm;
    boolean isReg;
    String regKey;
    ListView l;
    List<Update> updates;
    boolean rtlSupport;
    boolean isExpanded = false;
    updateCallback callBack;
    boolean showAll;
    updatesAdapter uAdapter;
    updateReciever uR;
    JSONParser jParser;
    ConnectionDetector connectionDetector;
    DBManager db_mngr;
    View root;
    ViewGroup container;
    PullToRefreshLayout pullToRefreshLayout;
    private boolean toFetchDataFromDB;

    @Override
    public void onAttach(Activity activity) {
        try {
            callBack = (updateCallback) activity;
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
        root = inflater.inflate(R.layout.updates_frag_list, container, false);
        initlizeFragment(root, container);
        initlizeListners();
        initlizeDataForFragment();
        return root;
    }

    @Override
    public void onResume() {
        uR = new updateReciever();
        IntentFilter intentFilter = new IntentFilter(
                "com.google.android.c2dm.intent.RECEIVE");
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        getActivity().registerReceiver(uR, intentFilter);


        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        if (uR != null) {

            getActivity().unregisterReceiver(uR);
        }
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings:
                Intent i = new Intent(getActivity(), SettingsActivity.class);
                startActivity(i);

                break;
            case R.id.about:
                Intent s = new Intent(getActivity(), AboutActivity.class);
                startActivity(s);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        getActivity().getMenuInflater().inflate(R.menu.listmenu, menu);
        if (!getArguments().getBoolean("rtl"))
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * intilize fragment components
     *
     * @param root Root view that have ui components to find and set
     */
    private void initlizeFragment(View root, ViewGroup container) {
        db_mngr = new DBManager(getActivity());
        this.container = container;

       pullToRefreshLayout = (PullToRefreshLayout) root.findViewById(R.id.LinearLayout1);
        Options options = Options.create().scrollDistance(.25f).build();
        ActionBarPullToRefresh.from(getActivity()).
                options(options).
                theseChildrenArePullable(R.id.updates_listView)
                .listener(new OnPullToRefresh())
                .setup(pullToRefreshLayout);
        connectionDetector = new ConnectionDetector(getActivity());
        jParser = new JSONParser();
        l = (ListView) root.findViewById(R.id.updates_listView);

        showAll = false;
        updates = new ArrayList<Update>();

        SharedPreferences sh = getActivity().getSharedPreferences(
                GCMUtils.DATA, Activity.MODE_PRIVATE);
        toFetchDataFromDB = sh.getBoolean(GCMUtils.LOAD_UPDATES, false);

    }

    /**
     * initlize listview listners
     */
    private void initlizeListners() {
        l.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        l.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                callBack.onUpdateClick(updates.get(arg2));
            }

        });

        l.setMultiChoiceModeListener(new MultiChoiceModeListener() {
            int count = 0;

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.update_contextual_menu,
                        menu);
                mode.setTitle(count
                        + " "
                        + getActivity().getResources().getString(
                        R.string.selected));
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.deleteUpdate:
                        for (Update u : uAdapter.getSelected()) {
                            updates.remove(u);
                            db_mngr.deleteUpdate(u);
                        }
                        Collections.sort(updates);
                        uAdapter.notifyDataSetChanged();
                        mode.finish();
                        break;
                }

                return true;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {

                if (checked) {
                    if (count <= updates.size())
                        count++;
                    mode.setTitle(count + " "
                            + getResources().getString(R.string.selected));
                    uAdapter.toggleSelection(
                            (Update) uAdapter.getItem(position), true);

                } else {
                    if (count >= 0) {
                        count--;
                    }
                    uAdapter.toggleSelection(
                            (Update) uAdapter.getItem(position), false);
                    mode.setTitle(count + " "
                            + getResources().getString(R.string.selected));

                }

            }
        });

        l.setItemsCanFocus(true);

    }

    /**
     * initlizes the data for the fragment wether from back end or db
     */
    private void initlizeDataForFragment() {
        boolean isConnected = connectionDetector.isConnectingToInternet();
        if (!toFetchDataFromDB && isConnected) {
            new downloadRecentUpdates(false).execute();
        } else {
            loadFromDataBase();
        }
    }

    /**
     * loading the updates from data base and set it to updates list
     */
    private void loadFromDataBase() {
        updates = null;
        updates = db_mngr.getCursorAllUpdates();
        Collections.sort(updates);
        uAdapter = new updatesAdapter(updates, getActivity(), callBack, db_mngr);
        ScaleInAnimationAdapter sc = new ScaleInAnimationAdapter(uAdapter);
        sc.setAbsListView(l);
        l.setAdapter(sc);

    }

    /**
     * Adding update to list of updates and to db if updates is existed then
     * updating its info
     *
     * @param u Update Object to add/Update
     * @return true added ,false updated
     */
    private boolean addUpdate(Update u) {
        for (Update tmp : updates) {
            if (tmp.equals(u)) {
                tmp.setSubject(u.getSubject());
                tmp.setText(u.getText());
                tmp.setDateTime(u.getDateTime());
                tmp.setUrl(u.getUrl());

                db_mngr.updateUpdate(tmp);

                return false;
            }
        }
        updates.add(0, u);
        db_mngr.insertUpdate(u);

        return true;
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
                                    new downloadRecentUpdates(false).execute();

                                } else if (!toFetchDataFromDB) {
                                    getActivity().finish();
                                } else if (toFetchDataFromDB) {
                                    loadFromDataBase();
                                }

                            }
                        }
                ).show();
    }

    public interface updateCallback {
        /**
         * Handles the click action when update is clicked
         *
         * @param u Update object that has been clicked
         */
        public void onUpdateClick(Update u);
    }

    /**
     * Handles the updates listView items
     *
     * @author wabbass
     */
    static class updatesAdapter extends BaseAdapter {

        LayoutInflater inflater = null;
        updateCallback callback;
        List<Update> selected_items;
        WeakReference<Activity> weakActivity;
        WeakReference<DBManager> weakDb_mngr;
        private List<Update> updates;
        private boolean isActionMode;

        public updatesAdapter(List<Update> updates, Activity cxt,
                              updateCallback callback, DBManager db_mngr) {

            this.updates = updates;
            this.inflater = (LayoutInflater) cxt
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.callback = callback;
            this.weakActivity = new WeakReference<Activity>(cxt);
            this.selected_items = new ArrayList<Update>();
            this.weakDb_mngr = new WeakReference<DBManager>(db_mngr);
        }

        public List<Update> getUpdates() {
            return updates;
        }

        public void setUpdates(List<Update> updates) {
            this.updates = updates;
        }

        @Override
        public boolean isEnabled(int position) {
            if (this.isActionMode) {
                return true;
            }
            // no actionmode = everything enabled
            return true;
        }

        @Override
        public int getCount() {

            return updates.size();
        }

        @Override
        public Object getItem(int position) {

            return updates.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {

            View v = convertView;
            final Update u = updates.get(position);
            if (v == null) {
                v = inflater.inflate(R.layout.updates_item, parent, false);
                UpdateViewHolder viewHolder = new UpdateViewHolder();
                viewHolder.title = (TextView) v
                        .findViewById(R.id.title_updateLable);
                viewHolder.subject = (ExpandableTextView) v
                        .findViewById(R.id.contentTextBox);
                viewHolder.time = (TextView) v
                        .findViewById(R.id.update_timeLable);
                viewHolder.pin = (ImageView) v.findViewById(R.id.pinImageView);
                viewHolder.popUp = (ImageView) v.findViewById(R.id.popUpMenu);
                v.setTag(viewHolder);

            }
            final UpdateViewHolder viewHolder = (UpdateViewHolder) v.getTag();
            viewHolder.title.setText(u.getSubject());
            viewHolder.subject.setText(u.getText());
            viewHolder.time.setText(u.getDateTime());
            if (u.isPinned()) {
                viewHolder.pin.setVisibility(ImageView.VISIBLE);
            } else {
                viewHolder.pin.setVisibility(ImageView.INVISIBLE);
            }
            viewHolder.popUp.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    boolean popUpStatus = showPopUpWindow(viewHolder.popUp, u,
                            position, u.isPopUpOpend());
                    u.setPopUpOpend(popUpStatus);
                }
            });
            return v;
        }

        /**
         * @param parent   The view that has been clicked ,overflow icon
         * @param u        The Update data
         * @param position The index of the selected item in listView
         * @param isOpened true if popUp is open,otherwise false,(when click to open
         *                 and another click on overflow to close)
         * @return true, when popUp is Opened ,otherwise false
         */
        private boolean showPopUpWindow(View parent, final Update u,
                                        final int position, boolean isOpened) {
            View popupView = inflater.inflate(R.layout.pop_up_layout, null);
            final PopupWindow popUpWindow = new PopupWindow(weakActivity.get());
            popUpWindow.setContentView(popupView);
            popUpWindow.setWidth(LayoutParams.WRAP_CONTENT);
            popUpWindow.setHeight(LayoutParams.WRAP_CONTENT);
            final ImageView pin = (ImageView) popupView
                    .findViewById(R.id.pinPopUpAction);
            final ImageView unpin = (ImageView) popupView
                    .findViewById(R.id.unpinPopUpAction);
            final ImageView delete = (ImageView) popupView
                    .findViewById(R.id.deletePopUpAction);
            if (u.isPinned()) {
                unpin.setVisibility(ImageView.VISIBLE);

            } else {
                unpin.setVisibility(ImageView.GONE);
            }
            pin.setVisibility(unpin.getVisibility() == ImageView.VISIBLE ? ImageView.GONE
                    : ImageView.VISIBLE);
            OnClickListener actionListner = new OnClickListener() {

                boolean toDelete = false;

                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.pinPopUpAction) {
                        popUpWindow.dismiss();
                        Toast.makeText(weakActivity.get(),
                                R.string.UpdatePinned, Toast.LENGTH_SHORT)
                                .show();
                        u.setPinned(true);

                    } else if (v.getId() == R.id.deletePopUpAction) {
                        popUpWindow.dismiss();
                        Toast.makeText(weakActivity.get(),
                                R.string.UpdateDeleted, Toast.LENGTH_SHORT)
                                .show();
                        u.setPinned(false);
                        toDelete = true;
                    } else if (v.getId() == R.id.unpinPopUpAction) {
                        popUpWindow.dismiss();
                        Toast.makeText(weakActivity.get(),
                                R.string.UpdateUnPinned, Toast.LENGTH_SHORT)
                                .show();
                        u.setPinned(false);
                    }
                    updates.set(position, u);
                    if (toDelete) {
                        weakDb_mngr.get().deleteUpdate(u);
                        updates.remove(position);
                    } else {
                        weakDb_mngr.get().updateUpdate(u);

                    }
                    Collections.sort(updates);
                    notifyDataSetChanged();
                }
            };
            pin.setOnClickListener(actionListner);
            delete.setOnClickListener(actionListner);
            unpin.setOnClickListener(actionListner);
            if (!popUpWindow.isShowing() && !isOpened) {
                popUpWindow.setFocusable(false);
                popUpWindow.setOutsideTouchable(true);
                popUpWindow.showAsDropDown(parent);

                return true;

            } else {
                popUpWindow.dismiss();
            }
            return false;
        }

        /**
         * toggling that the given update is selected or not
         *
         * @param u          selected update
         * @param isSelected true if selected ,false otherwise
         */
        public void toggleSelection(Update u, boolean isSelected) {

            if (isSelected) {
                selected_items.add(u);

            } else {
                int index = selected_items.indexOf(u);
                if (index != -1) {
                    selected_items.remove(index);
                }
            }
        }

        /**
         * @return all selected items from the list view
         */
        public List<Update> getSelected() {
            return this.selected_items;
        }

    }

    /**
     * A Class that holds the Views for the list view adapter
     *
     * @author wabbass
     */
    static class UpdateViewHolder {
        TextView title;
        TextView time;
        ExpandableTextView subject;
        ImageView pin;
        ImageView popUp;
    }

    /**
     * Handle messages from the backend about new updates /adding /updating
     * notification will show when receiving new push
     *
     * @author wabbass
     */
    class updateReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().toString()
                    .compareTo("com.google.android.c2dm.intent.RECEIVE") == 0) {
                if (arg1.getStringExtra("Type") == null) {
                    abortBroadcast();
                    Update u = Utilities.generateUpdateFromExtras(
                            arg1.getExtras(), arg0);
                    if (u != null && u.getUrlToJason() == null) {
                        updates.add(0, u);
                        db_mngr.insertUpdate(u);
                        Collections.sort(updates);
                        uAdapter.notifyDataSetChanged();
                        Utilities.showNotification(getActivity(),
                                u.getSubject(),
                                Utilities.html2Text(u.getText()));
                    } else if (u.getUrlToJason() != null) {
                        Utilities.PostListener listner = new Utilities.PostListener() {

                            @Override
                            public void onPostUpdateDownloaded(Update u) {
                                addUpdate(u);
                                Utilities.showNotification(getActivity(),
                                        u.getSubject(),
                                        Utilities.html2Text(u.getText()));
                                Collections.sort(updates);
                                uAdapter.notifyDataSetChanged();

                            }
                        };
                        Utilities.fetchUpdateFromBackEndTask task = new Utilities.fetchUpdateFromBackEndTask(
                                getActivity(), listner);
                        task.execute(u.getUpdate_id());
                    }
                } else {

                }
            }

        }

    }

    /**
     * fetching data from the back end ,success saving data ,failuer clearing db
     * and close the app with alert with failuer
     *
     * @author wabbass
     */
    class downloadRecentUpdates extends AsyncTask<String, String, String> {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        boolean downloadOk = false;
        boolean toRefresh;
        private ProgressDialog pDialog;

        public downloadRecentUpdates(boolean pullToRefresh) {
            this.toRefresh = pullToRefresh;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (!toRefresh) {
                pDialog = new ProgressDialog(getActivity());
                pDialog.setMessage("Loading...");
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.show();
            }

        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject jObject = jParser.makeHttpRequest(Settings.URL_UPDATES,
                    "GET", this.params);
            if (jObject == null) {
                if (cancel(true)) {
                    Log.e(GCMUtils.TAG,
                            "loading Updates from internet canceled");
                    if(toRefresh)
                    {
                        Toast.makeText(getActivity(),getResources().getString(R.string.fialedToRefresh),Toast.LENGTH_LONG).show();
                        pullToRefreshLayout.setRefreshComplete();
                    }
                }
            } else {
                Log.d("ward", jObject.toString());

                try {
                    JSONArray updatesArray = jObject.getJSONArray("posts");
                    for (int i = 0; i < updatesArray.length(); ++i) {
                        JSONObject update = updatesArray.getJSONObject(i);
                        Update u = new Update(update.getString("id"),
                                update.getString("title"),
                                update.getString("date"),
                                Utilities.html2Text(update.getString("content")), false);
                        u.setUrl(update.getString("url"));
                        if (!toRefresh) {
                            updates.add(u);
                        } else {
                            addUpdate(u);
                        }
                    }
                    if (connectionDetector.isConnectingToInternet())
                        downloadOk = true;
                } catch (JSONException e) {
                    Log.e(GCMUtils.TAG, e.toString());
                    if (!connectionDetector.isConnectingToInternet()) {
                        Log.e(GCMUtils.TAG, "faild no internet ");
                            if(toRefresh)
                            {
                                Toast.makeText(getActivity(),getResources().getString(R.string.fialedToRefresh),Toast.LENGTH_LONG).show();
                                pullToRefreshLayout.setRefreshComplete();
                            }
                    }
                    if(!toRefresh)
                    db_mngr.clearDb();
                    return "";
                }
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (!toRefresh)
                pDialog.dismiss();
            if (downloadOk && !toRefresh) {
                Utilities.saveDownloadOnceStatus(true, GCMUtils.LOAD_UPDATES,
                        getActivity());
                for (Update u : updates) {
                    db_mngr.insertUpdate(u);
                }

                uAdapter = new updatesAdapter(updates, getActivity(), callBack,
                        db_mngr);
                ScaleInAnimationAdapter sc = new ScaleInAnimationAdapter(
                        uAdapter);
                sc.setAbsListView(l);
                l.setAdapter(sc);
            } else if (!toRefresh) {
                Utilities.saveDownloadOnceStatus(false, GCMUtils.LOAD_TEACHERS,
                        getActivity());
                Utilities.saveDownloadOnceStatus(false, GCMUtils.LOAD_UPDATES,
                        getActivity());
                Utilities.saveDownloadOnceStatus(false, GCMUtils.LOAD_COURSES,
                        getActivity());
                db_mngr.clearDb();
                showDialogNoconnection(!toFetchDataFromDB);
            } else if (toRefresh) {
                Collections.sort(updates);
                uAdapter.notifyDataSetChanged();
                pullToRefreshLayout.setRefreshComplete();
                Toast.makeText(getActivity(), getResources().getString(R.string.EndedRefreshing), Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }

    }

    private class OnPullToRefresh implements OnRefreshListener {


        @Override
        public void onRefreshStarted(View view) {
            Toast.makeText(getActivity(), getResources().getString(R.string.Refreshing), Toast.LENGTH_LONG).show();
            if(connectionDetector.isConnectingToInternet())
                new downloadRecentUpdates(true).execute();
            else{
                pullToRefreshLayout.setRefreshComplete();
                Toast.makeText(getActivity(),getResources().getString(R.string.fialedToRefresh),Toast.LENGTH_LONG).show();
            }

        }

    }
}
