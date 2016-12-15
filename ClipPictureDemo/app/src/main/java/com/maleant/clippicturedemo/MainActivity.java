package com.maleant.clippicturedemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.antwei.clippicture.ClipParam;
import com.antwei.clippicture.ClipPictureActivity;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private ImageView imvContent;
    private ClipParam clipParam = new ClipParam(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidget();
    }

    private void initWidget() {

        button = (Button) findViewById(R.id.button);
        imvContent = (ImageView) findViewById(R.id.imv_content);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File imgCacheFile = null;
                String imgDir = getSDFileDir("images");

                imgCacheFile = new File(imgDir + File.separator + System.currentTimeMillis() + ".jpg");
                try {
                    mkFile(imgCacheFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "SD 卡被占用，暂时无法使用！", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (imgCacheFile.exists()) {
                    imgCacheFile.delete();
                }

                clipParam.setImgPath(imgCacheFile.getPath());
                clipParam.setImgUri(Uri.fromFile(imgCacheFile));


                final String[] imgSeleStr = {"相册", "拍照"};
                AlertDialog photoPicDialog = new AlertDialog.Builder(MainActivity.this).setItems(imgSeleStr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (imgSeleStr[which].equals("相册")) {
                            Intent intent = new Intent(Intent.ACTION_PICK, null);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, clipParam.getImgUri());
                            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                            intent.putExtra("return-data", false);
                            startActivityForResult(intent, ClipParam.REQUEST_CROP);
                        } else if (imgSeleStr[which].equals("拍照")) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, clipParam.getImgUri());
                            intent.putExtra("return-data", false);
                            startActivityForResult(intent, ClipParam.REQUEST_CROP);
                        }
                    }
                }).create();


                photoPicDialog.show();
            }
        });


    }

    public String getSDFileDir(String folder) {
        String filePath = null;
        boolean isMake = false;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String fileDirStr = getSDcard() + File.separator + "clipdemo" + File.separator + folder;
            File fileDir = new File(fileDirStr);
            if (!fileDir.exists()) {
                isMake = fileDir.mkdirs();
                if (!isMake) {
                    Toast.makeText(this, "SD卡被占用，暂时无法使用", Toast.LENGTH_SHORT).show();
                }
            }
            filePath = fileDir.getAbsolutePath();
        }

        return filePath;
    }

    public String getSDcard() {
        // 获取SdCard状态
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Environment.getExternalStorageDirectory().canWrite()) {
                return Environment.getExternalStorageDirectory().getPath();
            }
        }
        return null;
    }

    public boolean mkFile(File file) throws IOException {
        return file.createNewFile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ClipParam.REQUEST_CROP:

                    Intent intent = new Intent(this, ClipPictureActivity.class);
//                    适配不同系统版本在返回时的两种参数情况
                    if (data != null) {
                        Log.i("DemoAcitivy", "intent data = " + data.getData());
                        intent.setData(data.getData());
                        intent.putExtra(ClipParam.IMG_PATH, clipParam.getImgPath());
                        intent.putExtra(ClipParam.FUNCTION, "1");
                    } else {
                        Log.i("DemoAcitivy", "ClipParam.getImgUri() = " + new File(clipParam.getImgUri().getPath()).exists());
                        intent.setData(clipParam.getImgUri());
                        intent.putExtra(ClipParam.IMG_PATH, clipParam.getImgPath());
                        intent.putExtra(ClipParam.FUNCTION, "2");
                    }
                    startActivityForResult(intent, ClipParam.PHOT_RETUN);
                    break;

                case ClipParam.PHOT_RETUN:
                    showImage(clipParam.getImgUri());
//                    在使用ImageLoader或者xUtil等网络加载框架时可以使用
//                    showImage(clipParam.getImgPath());
                    break;
            }
        }

    }

    private void showImage(Uri imgPath) {
        imvContent.setImageURI(imgPath);
    }
}
