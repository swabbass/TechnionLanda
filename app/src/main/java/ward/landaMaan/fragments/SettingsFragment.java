package ward.landaMaan.fragments;


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

import ward.landaMaan.R;
import ward.landaMaan.activities.Settings;

public class SettingsFragment extends Fragment implements OnClickListener {

    private  Switch updatesChkbox, workshopsChkbox;
    private   RadioGroup langChoice,viewChoice;
    private boolean updateMe, workshopMe;
    private  String localLang;
    private  boolean richView;

    private void initlizeUI(View root) {
        updatesChkbox = (Switch) root.findViewById(R.id.UpdatecheckBox);
        workshopsChkbox = (Switch) root.findViewById(R.id.workshopChkBox);
        langChoice = (RadioGroup) root.findViewById(R.id.languageChoice);
        viewChoice=(RadioGroup)root.findViewById(R.id.updateViewCoice);
        TextView version = (TextView) root.findViewById(R.id.version);
        try {
            String versionName = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0).versionName;
            version.setText(versionName);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            version.setText("1.0");


        }
    }

    private void loadSettings() {

        Settings.initlizeSettings(getActivity());
        updateMe = Settings.isToNotifyUpdates();
        workshopMe = Settings.isToNotifyCourse();
        localLang = Settings.getLocalLang();
        richView=Settings.isRichView();

    }

    private void setSettings(View root) {

        updatesChkbox.setChecked(updateMe);
        workshopsChkbox.setChecked(workshopMe);
        RadioButton btn = (RadioButton) root.findViewById(Settings.langId(localLang));
        RadioButton viewRadioChoice=(RadioButton)root.findViewById(Settings.isRichView()?R.id.richViewRadio:R.id.normalViewRadio);
        viewRadioChoice.setChecked(true);
        btn.setChecked(true);
        viewChoice.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i)
                {
                    case R.id.richViewRadio:richView=true;break;
                    case R.id.normalViewRadio:richView=false;break;
                }
            }
        });
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
        Settings.saveSettings(getActivity(), localLang, workshopMe, updateMe,richView);
        super.onPause();
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
