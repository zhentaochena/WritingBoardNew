package com.example.writingboard.view.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import com.example.writingboard.R;

public class SizeSelectorDialog extends AlertDialog implements SeekBar.OnSeekBarChangeListener{

    private SeekBar sizeSeek;
    private View rootView;
    private LineWidthVew lineWidthVew;
    private OnSizeSelectListener listener;
    int penSize;

    public SizeSelectorDialog(Context context, final int size) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        rootView = inflater.inflate(R.layout.dialog_size_selector, null, false);
        setView(rootView);
        sizeSeek = rootView.findViewById(R.id.seek_size);
        lineWidthVew = rootView.findViewById(R.id.show_width);
        sizeSeek.setProgress(size);
        this.penSize = size;
        sizeSeek.setOnSeekBarChangeListener(this);

        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == BUTTON_POSITIVE) {
                    if (listener != null) listener.onSizeChange(penSize);
                }
            }
        };

        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), onClickListener);
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), onClickListener);

    }

    public void addOnSizeSelectListener(OnSizeSelectListener listener) {
        this.listener = listener;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.seek_size) {
            penSize = progress;
            lineWidthVew.setPaintSize(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public interface OnSizeSelectListener{
        void onSizeChange(int size);
    }
}
