package watcharaphans.bitcombine.co.th.bitcamera.touch2focus;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

/**
 * SurfaceView to show LenxCameraPreview2 feed
 */
public class PreviewSurfaceView extends SurfaceView {

    private CameraPreview camPreview;
    private boolean listenerSet = false;
    public Paint paint;
    private DrawingView drawingView;
    private boolean drawingViewSet = false;

    public PreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
		/*Paint paint = new Paint();
		paint.setColor(Color.GREEN);
		paint.setStrokeWidth(3);
		paint.setStyle(Paint.Style.STROKE);*/
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec)
        );
    }

    Rect touchRect = new Rect(-100, -100, 100, 100);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!listenerSet) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            float x = event.getX();
            float y = event.getY();

            touchRect = new Rect(
                    (int)(x - 100),
                    (int)(y - 100),
                    (int)(x + 100),
                    (int)(y + 100));

            final Rect targetFocusRect = new Rect(
                    touchRect.left * 2000/this.getWidth() - 1000,
                    touchRect.top * 2000/this.getHeight() - 1000,
                    touchRect.right * 2000/this.getWidth() - 1000,
                    touchRect.bottom * 2000/this.getHeight() - 1000);

            camPreview.doTouchFocus(targetFocusRect);
            if (drawingViewSet) {
                drawingView.setHaveTouch(true, touchRect, 0xeed7d7d7);
                drawingView.invalidate();

                // Remove the square after some time
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawingView.setHaveTouch(false, new Rect(0, 0, 0, 0), 0xeed7d7d7);
                        drawingView.invalidate();
                    }
                }, 1000);
            }
        }
        return false;
    }

    public void drawFocusRect() {
        if (drawingViewSet) {
            drawingView.setHaveTouch(true, touchRect, 0xff00ff00);
            drawingView.invalidate();

            // Remove the square after some time
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawingView.setHaveTouch(false, new Rect(0, 0, 0, 0), 0xff00ff00);
                    drawingView.invalidate();
                }
            }, 500);
        }
    }

    public void drawInitialFocusRect() {
        if (drawingViewSet) {
            drawingView.setHaveTouch(true, null, 0xff00ff00);
            drawingView.invalidate();

            // Remove the square after some time
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawingView.setHaveTouch(false, null, 0xff00ff00);
                    drawingView.invalidate();
                }
            }, 500);
        }
    }

    /**
     * set CameraPreview instance for touch focus.
     * @param camPreview - CameraPreview
     */
    public void setListener(CameraPreview camPreview) {
        this.camPreview = camPreview;
        listenerSet = true;
    }

    /**
     * set DrawingView instance for touch focus indication.
     * @param dView - DrawingView
     */
    public void setDrawingView(DrawingView dView) {
        drawingView = dView;
        drawingViewSet = true;
    }
}