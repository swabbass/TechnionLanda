package ward.landa.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import utils.Utilities;
import ward.landa.R;
import ward.landa.Update;

public class updateDetailsFragment extends Fragment {

    TextView subject, dateTime, content;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View root = inflater.inflate(R.layout.update_details_fragment,
                container, false);
        initlizeUI(root);

        return root;
    }

    private void initlizeUI(View root) {
        subject = (TextView) root.findViewById(R.id.updateDetailSubjectLable);
        dateTime = (TextView) root.findViewById(R.id.updateDetailDateTimeLable);
        content = (TextView) root.findViewById(R.id.updateDetailContentLable);
        Update u = (Update) getArguments().getSerializable("Update");
        String Cont = getArguments().getString("content");
        subject.setText(u.getSubject());
        dateTime.setText(u.getDateTime());

        String tmp = Utilities.removeTableFirstTrHtml(u.getText());
        String jsob = Utilities.html2Text(tmp == null ? u.getText() : tmp);
        content.setText(jsob);
    }
}
