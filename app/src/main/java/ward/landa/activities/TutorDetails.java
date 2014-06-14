package ward.landa.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

import ward.landa.R;
import ward.landa.fragments.teacherFragment;

public class TutorDetails extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutor_details);
        overridePendingTransition(android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);
        if (savedInstanceState == null) {
            teacherFragment tf = new teacherFragment();
            setResult(Settings.TEACHERS);
            tf.setArguments(getIntent().getExtras());
            FragmentTransaction tr = getSupportFragmentManager()
                    .beginTransaction();
            tr.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.add(R.id.tutorDetailContainer, tf).commit();
        }
        setTitle(getIntent().getExtras().getCharSequence("name"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e1e1e")));
        return super.onCreateOptionsMenu(menu);
    }
}
