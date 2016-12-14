package com.antwei.clippicture;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;

/**
 * Created by antwei on 2015/10/9.
 */
public class ClipParam implements Serializable {

    public static final String FUNCTION = "function";
    public static final int REQUEST_CROP = 1 << 2;
    public static final int PHOT_RETUN = 2 << 2;
    private Context context;
    public static final String IMG_PATH = "imgPath";
    public static final String IMG_TEMP = "tempImg";

    private String imgPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "ClipParam";
    private Uri imgUri;

    public ClipParam(Context context) {
        this.context = context;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public Uri getImgUri() {
        return imgUri;
    }

    public void setImgUri(Uri imgUri) {
        this.imgUri = imgUri;
    }

    /**
     * 获取拓展存储Cache的绝对路径
     *
     * @param context
     */
    private String getExternalCacheDir(Context context) {

        if (!isMounted()) {
            Toast.makeText(context, "sd 卡 没有挂载", Toast.LENGTH_SHORT).show();
            return null;
        }


        StringBuilder sb = new StringBuilder();

        File file = context.getExternalCacheDir();

        // In some case, even the sd card is mounted,
        // getExternalCacheDir will return null
        // may be it is nearly full.

        if (file != null) {
            sb.append(file.getAbsolutePath()).append(File.separator);
            Log.i("ClipParam", "获取sd 卡 File ok / dir=" + sb.toString());
        } else {
            sb.append(Environment.getExternalStorageDirectory().getPath()).append("/Android/data/").append(context.getPackageName())
                    .append("/cache/").append(File.separator).toString();

            Log.i("ClipParam", "获取sd 卡 File false / dir=" + sb.toString());
        }

        return sb.toString();
    }

    private static boolean isMounted() {
        return android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());
    }
}
