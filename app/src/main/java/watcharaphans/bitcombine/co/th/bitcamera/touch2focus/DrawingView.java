package watcharaphans.bitcombine.co.th.bitcamera.touch2focus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import watcharaphans.bitcombine.co.th.bitcamera.utility.ScreenUtil;

/**
 * Extends View. Just used to draw Rect when the screen is touched
 * for auto focus.
 * 
 * Use setHaveTouch function to set the status and the Rect to be drawn.
 * Call invalidate to draw Rect. Call invalidate again after 
 * setHaveTouch(false, Rect(0, 0, 0, 0)) to hide the rectangle.
 */
public class DrawingView extends View {
	private boolean haveTouch = false;
	private Rect touchArea;
	private Paint paint;
	private int color;
	private int width, height;
	
	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		haveTouch = false;
	}
	
	public void setHaveTouch(boolean val, Rect rect, int color) {
		haveTouch = val;
		touchArea = rect;
		this.color = color;
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		paint.setColor(color);
		if (haveTouch){
		    //drawingPaint.setColor(Color.BLUE);
		    /*canvas.drawRect(
		    		touchArea.left,
					touchArea.top,
					touchArea.right,
					touchArea.bottom,
					paint
			);*/

		    int x, y;
            if (touchArea == null) {
                x = width / 2;
                y = height / 2;
            } else {
                x = Math.round((touchArea.left + touchArea.right) / 2);
                y = Math.round((touchArea.top + touchArea.bottom) / 2);
            }
			canvas.drawCircle(x, y, ScreenUtil.convertDpToPixel(50, getContext()), paint);
	    }
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }
}