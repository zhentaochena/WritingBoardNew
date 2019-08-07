package com.example.writingboard.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.example.writingboard.view.MainActivity;
import com.example.writingboard.model.LoadImageModel;

public class LoadImagePresenter extends BasePresenter<LoadImageModel, MainActivity>{

    public LoadImagePresenter(MainActivity view) {
        super(view);
    }

    @Override
    protected LoadImageModel getModel() {
        return new LoadImageModel();
    }

    public Bitmap LoadImage(Uri uri, int w, int h){
        return model.LoadImage(view, uri, w, h);
    }
}
