package ward.landa.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import utils.Utilities;
import ward.landa.R;
import ward.landa.Update;
import ward.landa.activities.Settings;

public class updateDetailsFragment extends Fragment {

    private TextView content;
    private  WebView webView;

    @Override
    public void onAttach(Activity activity) {
        setHasOptionsMenu(true);
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View root = inflater.inflate(R.layout.update_details_fragment,
                container, false);
        initlizeUI(root);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.update_details_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.htmlView:
                webView.setVisibility(WebView.VISIBLE);
                content.setVisibility(TextView.GONE);
                break;
            case R.id.normalView:
                webView.setVisibility(WebView.GONE);
                content.setVisibility(TextView.VISIBLE);
                break;
        }
        return true;
    }

    private void initlizeUI(View root) {
        Settings.initlizeSettings(getActivity());
        boolean isRich = Settings.isRichView();
        TextView subject = (TextView) root.findViewById(R.id.updateDetailSubjectLable);
        TextView dateTime = (TextView) root.findViewById(R.id.updateDetailDateTimeLable);
        content = (TextView) root.findViewById(R.id.updateDetailContentLable);
        Update u = (Update) getArguments().getSerializable("Update");
        subject.setText(u.getSubject());
        dateTime.setText(u.getDateTime());
        webView=(WebView) root.findViewById(R.id.webView);
        WebViewClient webViewClient=new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Otherwise, give the default behavior (open in browser)
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                String command="javascript:(function() { " +
                            "document.getElementsByTagName('body')[0].style.color = '#e0e0e0'; " +
                        "var ps= document.getElementsByTagName('p'); " +
                        "for(var i=0;i<ps.length;i++){" +
                        "ps[i].style.color = '#e0e0e0';" +
                        "}" +
                        "var spans=document.getElementsByTagName('span');" +
                        " for(var i=0;i<spans.length;i++){" +
                        "spans[i].style.color = '#e0e0e0';" +
                        "}" +
                        "document.getElementsByTagName('table')[0].style.color = '#e0e0e0';" +
                        "})()";
                view.loadUrl(command);
                super.onPageFinished(view, url);
            }
        };

        webView.setWebViewClient(webViewClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadDataWithBaseURL(null, u.getHtml_text(), "text/html", "UTF-8", null);
        webView.setBackgroundColor(0x00000000);
       if(!isRich)
       {
           webView.setVisibility(WebView.GONE);
           content.setVisibility(TextView.VISIBLE);
       }
        else {
           webView.setVisibility(WebView.VISIBLE);
           content.setVisibility(TextView.GONE);
       }

        String tmp = Utilities.removeTableFirstTrHtml(u.getText());
        String jsob = Utilities.html2Text(tmp == null ? u.getText() : tmp);
        content.setText(jsob);
    }
}
