package ward.landa.ImageUtilities;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

class squareImage extends ImageView {

    public squareImage(Context context) {
        super(context);
    }

    public squareImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public squareImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
    }


}
