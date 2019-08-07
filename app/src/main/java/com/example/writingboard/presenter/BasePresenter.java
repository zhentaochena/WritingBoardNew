package com.example.writingboard.presenter;

import com.example.writingboard.view.BaseView;
import com.example.writingboard.model.BaseModel;

public abstract class BasePresenter<M extends BaseModel, V extends BaseView> {
    protected M model;
    protected V view;

    public BasePresenter(V view) {
        this.view = view;
        model = getModel();
    }

    protected abstract M getModel();

}


