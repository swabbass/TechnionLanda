package ward.landa;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class ExpandableTextView extends TextView {

    private boolean isExpanded;

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setExpanded(false);

        // TODO Auto-generated constructor stub
    }

    public boolean isExpanded() {
        return true;
    }

    public void setExpanded(boolean isExpanded) {
        this.isExpanded = true;
    }

}
