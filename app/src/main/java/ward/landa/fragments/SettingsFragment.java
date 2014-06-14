package ward.landa.fragments;


import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import ward.landa.R;
import ward.landa.activities.Settings;

public class SettingsFragment extends Fragment implements OnClickListener {

    Switch updatesChkbox, workshopsChkbox;
    RadioGroup langChoice;
    boolean updateMe, workshopMe;
    String localLang;
    TextView version;

    private void initlizeUI(View root) {
        updatesChkbox = (Switch) root.findViewById(R.id.UpdatecheckBox);
        workshopsChkbox = (Switch) root.findViewById(R.id.workshopChkBox);
        langChoice = (RadioGroup) root.findViewById(R.id.languageChoice);
        version = (TextView) root.findViewById(R.id.version);
        try {
            String versionName = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0).versionName;
            version.setText(versionName);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            version.setText("1.0");
            return;

        }
    }

    private void loadSettings() {

        Settings.initlizeSettings(getActivity());
        updateMe = Settings.isToNotifyUpdates();
        workshopMe = Settings.isToNotifyCourse();
        localLang = Settings.getLocalLang();
    }

    private void setSettings(View root) {

        updatesChkbox.setChecked(updateMe);
        workshopsChkbox.setChecked(workshopMe);
        RadioButton btn = (RadioButton) root.findViewById(Settings.langId(localLang));
        btn.setChecked(true);
        langChoice.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                switch (arg1) {
                    case R.id.radioAr:
                        localLang = Settings.ARABIC;
                        break;
                    case R.id.radioHE:
                        localLang = Settings.HEBREW;
                        break;
                }

            }
        });
        updatesChkbox.setOnClickListener(this);
        workshopsChkbox.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.settings_fragment, container, false);
        initlizeUI(root);
        loadSettings();
        setSettings(root);
        return root;
    }

    @Override
    public void onPause() {
        Settings.saveSettings(getActivity(), localLang, workshopMe, updateMe);
        super.onPause();
    }

    @Override
    public void onStop() {

        super.onStop();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.UpdatecheckBox:
                updateMe = updatesChkbox.isChecked();
                break;
            case R.id.workshopChkBox:
                workshopMe = workshopsChkbox.isChecked();
                break;
        }
    }
}
