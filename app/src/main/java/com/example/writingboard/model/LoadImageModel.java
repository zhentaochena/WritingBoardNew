package com.example.writingboard.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class LoadImageModel extends BaseModel {
    public Bitmap LoadImage(Context context, Uri uri, int w, int h){
        try {
            Bitmap bitmap = null;
            InputStream fis = context.getContentResolver().openInputStream(uri);
            if (fis != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(fis, null, options);
                float srcWidth = options.outWidth;
                float srcHeight = options.outHeight;
                int inSampleSize = 1;
                if (srcHeight > h || srcWidth > w) {
                    if (srcWidth > srcHeight) {
                        inSampleSize = Math.round(srcHeight / h);
                    } else {
                        inSampleSize = Math.round(srcWidth / w);
                    }
                }
                options.inJustDecodeBounds = false;
                options.inSampleSize = inSampleSize;

                fis.close();
                fis = context.getContentResolver().openInputStream(uri);
                if (fis != null) {
                    bitmap = BitmapFactory.decodeStream(fis, null, options);
                    fis.close();
                }
            }
            return bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
