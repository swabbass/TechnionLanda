package ward.landa.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import ward.landa.R;
import ward.landa.fragments.updateDetailsFragment;

public class UpdateDetailActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_details_activity);
        overridePendingTransition(android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);

        if (savedInstanceState == null) {


            updateDetailsFragment uf = new updateDetailsFragment();
            uf.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.updateDetailsContainer, uf).commit();

        }
        setResult(2);
        setTitle(getIntent().getExtras().getCharSequence("subject"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e1e1e")));
        return super.onCreateOptionsMenu(menu);
    }
}
