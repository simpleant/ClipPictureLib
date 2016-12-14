package com.antwei.clippicture;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


/**
 * @author Administrator
 *         整体思想是：截取屏幕的截图，然后截取矩形框里面的图片
 */
public class ClipPictureActivity extends Activity implements OnTouchListener,
        OnClickListener {

    ImageView srcPic;
    Button sure;
    ClipView clipview;
    String imgFullName;

    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    private static final String TAG = "11";
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clip_activity);

        init();

    }

    private void init() {

        srcPic = (ImageView) this.findViewById(R.id.src_pic);
        srcPic.setOnTouchListener(this);

        sure = (Button) this.findViewById(R.id.sure);
        sure.setOnClickListener(this);

        Intent intent = getIntent();
//        ClipParam.setImgUri(intent.getData());
        imgFullName = intent.getStringExtra(ClipParam.IMG_PATH);
        String funtion = intent.getStringExtra(ClipParam.FUNCTION);
        Bitmap bitmap = null;
        if ("1".equals(funtion)) {
//            srcPic.setImageBitmap(BitmapUtil.decodeUriAsBitmap(this, intent.getData()));
            bitmap = BitmapUtil.saveBitmap(this, intent.getData(), imgFullName);
            if (bitmap != null) {
                srcPic.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "该照片过大无法加载", Toast.LENGTH_SHORT).show();
                finish();
            }

        } else if ("2".equals(funtion)) {
            bitmap = BitmapUtil.saveBitmap(imgFullName);
            srcPic.setImageBitmap(bitmap);
        }
//        srcPic.setImageBitmap(BitmapUtil.decodeUriAsBitmap(this,intent.getData()));
        //init the location of the src
        DisplayMetrics dm = getResources().getDisplayMetrics();
        matrix.postTranslate((dm.widthPixels - bitmap.getWidth()) / 2, (dm.heightPixels - bitmap.getHeight()) / 2);
        srcPic.setImageMatrix(matrix);
    }

    /*这里实现了多点触摸放大缩小，和单点移动图片的功能，参考了论坛的代码*/
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                // 設置初始點位置
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG");
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    // ...
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY()
                            - start.y);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix);
        return true; // indicate event was handled
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        float count = x * x + y * y;
//		return FloatMath.sqrt(count);
        return (float) Math.sqrt(count);


    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /*点击进入预览*/
    public void onClick(View v) {
        boolean isMk = false;
        Bitmap fianBitmap = getBitmap();
        try {

            File imgFile = new File(imgFullName);

            if (imgFile.exists()) {
                imgFile.delete();
            }

            Uri uri = BitmapUtil.saveBitmap(fianBitmap, Uri.fromFile(imgFile));
            Intent intent = new Intent();
            intent.setData(uri);
            this.setResult(this.RESULT_OK, intent);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /*获取矩形区域内的截图*/
    private Bitmap getBitmap() {
        getBarHeight();
        Bitmap screenShoot = takeScreenShot();

        clipview = (ClipView) this.findViewById(R.id.clipview);
        int width = clipview.getWidth();
        int height = clipview.getHeight();
//        Bitmap finalBitmap = Bitmap.createBitmap(screenShoot,
//                (width - height / 3) / 2, height / 3 + titleBarHeight + statusBarHeight, height / 3, height / 3);
        Bitmap finalBitmap = Bitmap.createBitmap(screenShoot, 0, titleBarHeight + statusBarHeight + (height - width) / 2, width, width);
        return finalBitmap;
    }

    int statusBarHeight = 0;
    int titleBarHeight = 0;

    private void getBarHeight() {
        // 获取状态栏高度
        Rect frame = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        statusBarHeight = frame.top;

        int contenttop = this.getWindow()
                .findViewById(Window.ID_ANDROID_CONTENT).getTop();
        // statusBarHeight是上面所求的状态栏的高度
        titleBarHeight = contenttop - statusBarHeight;

        Log.v(TAG, "statusBarHeight = " + statusBarHeight
                + ", titleBarHeight = " + titleBarHeight);
    }

    // 获取Activity的截屏
    private Bitmap takeScreenShot() {
        View view = this.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

}