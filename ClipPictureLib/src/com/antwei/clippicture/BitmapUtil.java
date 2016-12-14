package com.antwei.clippicture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.MailTo;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtil {

    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        if (context == null || uri == null) return null;

        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    public static Bitmap saveBitmap(String fileName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        File imgFile = new File(fileName);

        int degree = readPictureDegree(fileName);
        Bitmap bitmap = BitmapFactory.decodeFile(fileName, options);

        if (degree > 0) {
            bitmap = toturn(bitmap);
        }

        int fileSize = (int) imgFile.length() / (1024 * 1024);
//        File f = new File(getSDcard() + "/.cache/upload.jpg");
        File f = new File(imgFile.getParent() + File.separator + ClipParam.IMG_TEMP + imgFile.getName());
        FileOutputStream fOut = null;
        try {
            f.createNewFile();
            fOut = new FileOutputStream(f);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);// 把Bitmap对象解析成流

            fOut.flush();
            fOut.close();
            bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeFile(f.getPath());
    }

    public static Bitmap saveBitmap(Context context, Uri uri, String imgFullName) {
        String realPath = "";
        int SDK_VERSION = Build.VERSION.SDK_INT;
        if (SDK_VERSION <= 11) {
            realPath = getRealPathFromURI_BelowAPI11(context, uri);
        } else if (SDK_VERSION > 11) {
            realPath = getRealPathFromURI_API11to18(context, uri);
        }

        int degree = readPictureDegree(realPath);

        File imgFile = new File(realPath);
        BitmapFactory.Options options = new BitmapFactory.Options();

        int fileSize = (int) imgFile.length() / (1024 * 1024);

        if (fileSize > 30) {
            options.inSampleSize = 10;
        } else if (fileSize > 20) {
            options.inSampleSize = 7;
        } else if (fileSize > 10) {
            options.inSampleSize = 5;
        } else if (fileSize > 0.5) {
            options.inSampleSize = 2;
        } else {
            options.inSampleSize = 1;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getPath(), options);
        if (degree > 0) {
            bitmap = toturn(bitmap);
        }
        if (bitmap == null) return null;
        File temFile = new File(imgFullName);
        FileOutputStream fos = null;
        try {
            temFile.createNewFile();
            fos = new FileOutputStream(temFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            fos.flush();
            fos.close();
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return BitmapFactory.decodeFile(temFile.getPath());
    }

    public static Bitmap saveBitmap2(Context context, Uri uri, String imgFullName) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) return null;
        cursor.moveToFirst();
        int cloumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        String path = cursor.getString(cloumnIndex);

        if (cursor != null) {
            cursor.close();
        }

        File imgFile = new File(path);
        BitmapFactory.Options options = new BitmapFactory.Options();

        int fileSize = (int) imgFile.length() / (1024 * 1024);
        if (fileSize > 30) {
            options.inSampleSize = 5;
        } else if (fileSize > 20) {
            options.inSampleSize = 4;
        } else if (fileSize > 10) {
            options.inSampleSize = 3;
        } else if (fileSize > 0.5) {
            options.inSampleSize = 2;
        } else {
            options.inSampleSize = 1;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (bitmap == null) return null;
        File temFile = new File(imgFullName);
        FileOutputStream fos = null;
        try {
            temFile.createNewFile();
            fos = new FileOutputStream(temFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            fos.flush();
            fos.close();
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return BitmapFactory.decodeFile(temFile.getPath());
    }

    public static Bitmap decodeUriAsBitmap(Context context, Uri uri) {

        File file = new File(uri.getPath());
        int fileSize = (int) file.length() / (1024 * 1024);
        if (fileSize > 5) {
            return null;
        }
        try {
            // 读取uri所在的图片
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Uri saveBitmap(Bitmap bitmap, Uri path) throws IOException {

        File outPutFile = new File(path.getPath());

        if (!outPutFile.getParentFile().exists()) {
            outPutFile.getParentFile().mkdirs();
        }

        if (outPutFile.exists()) {
            outPutFile.delete();
        }

        FileOutputStream fileOutputStream = new FileOutputStream(outPutFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);

        fileOutputStream.flush();
        fileOutputStream.close();
        bitmap.recycle();

        return Uri.fromFile(outPutFile);
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        if (cursor != null) {
            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
            return filePath;
        } else {
            return uri.getPath();
        }

    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        } else {
            result = contentUri.getPath();
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        if (cursor != null) {
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else {
            return contentUri.getPath();
        }

    }

    /**
     * 读取照片exif信息中的旋转角度
     *通常三星samsung 系列手机会自动旋转拍摄后的图片
     * @param path 照片路径
     * @return角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap toturn(Bitmap img) {
        Matrix matrix = new Matrix();
        matrix.postRotate(-90); /*逆时针旋转90度*/
        int width = img.getWidth();
        int height = img.getHeight();
        img = Bitmap.createBitmap(img, 0, 0, width, height, matrix, true);
        return img;
    }

    /**
     * 模糊图片
     *
     * @param sentBitmap
     * @param radius
     * @return
     */
    public static Bitmap fastblur(Bitmap sentBitmap, int radius) {


        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.i("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }


}