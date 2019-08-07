package com.example.writingboard.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.writingboard.utils.DensityUtil;
import com.example.writingboard.view.widget.ColorPickerDialog;
import com.example.writingboard.R;
import com.example.writingboard.view.widget.SizeSelectorDialog;
import com.example.writingboard.view.widget.WritingBoard;
import com.example.writingboard.view.widget.WritingState;
import com.example.writingboard.presenter.LoadImagePresenter;

public class MainActivity extends BaseView<LoadImagePresenter> {

    private ImageView penSizeSelector;
    private ImageView penColorSelector;
    private ImageView buttonMulti;
    private ImageView penSelect;
    private WritingState state = WritingState.getInstance();
    private WritingBoard board;
    private static final int CHOOSE_PHOTO = 2;
    private ImageView insertImage;
    private ImageView eraserType;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    protected LoadImagePresenter getPresenter() {
        return new LoadImagePresenter(this);
    }

    private void initView() {
        board = findViewById(R.id.writing_board);
        penSizeSelector = findViewById(R.id.iv_pen_size);
        penSizeSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SizeSelectorDialog dialog = new SizeSelectorDialog(MainActivity.this,
                        state.getPenSize());
                dialog.addOnSizeSelectListener(new SizeSelectorDialog.OnSizeSelectListener() {
                    @Override
                    public void onSizeChange(int size) {
                        state.setPenSize(size);
                        board.update();
                    }
                });
                dialog.setTitle("选择粗细");
                dialog.show();
            }
        });

        penColorSelector = findViewById(R.id.iv_pen_color);
        penColorSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog dialog = new ColorPickerDialog(MainActivity.this,
                        board.getPaint().getColor(), new ColorPickerDialog.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        state.setPenColor(color);
                        board.update();
                    }
                });
                dialog.setTitle("选择颜色");
                dialog.show();
            }
        });

        penSelect = findViewById(R.id.iv_pen_select);
        penSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!board.isSelect()){
                    penSelect.setImageResource(R.drawable.ic_select_all_24dp);
                    board.setSelect(true);
                } else {
                    penSelect.setImageResource(R.drawable.ic_select_all_gray_24dp);
                    board.setSelect(false);
                    board.exitSelect();
                }
            }
        });

        insertImage = findViewById(R.id.iv_insert_img);
        insertImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndOpenAlbum();
            }
        });



        buttonMulti = findViewById(R.id.iv_multi);
        buttonMulti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (board.isMulti()) {
                    board.setMulti(false);
                    buttonMulti.setImageResource(R.drawable.ic_bubble_chart_gray_24dp);
                } else {
                    board.setMulti(true);
                    buttonMulti.setImageResource(R.drawable.ic_bubble_chart_24dp);
                }
            }
        });

        eraserType = findViewById(R.id.iv_eraser);
        eraserType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow();
            }
        });
    }


    //检查权限
    public void checkAndOpenAlbum() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
        } else {
            openAlbum();
        }
    }

    public void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, CHOOSE_PHOTO);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAlbum();
            } else {
                Toast.makeText(this, "授权失败，无法操作", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Log.d("图片路径", uri == null ? "空的" : uri.getPath());
                if (uri != null) {
                    displayImage(uri);
                }
            }
        }
    }


    private void displayImage(Uri path) {
        if (path != null) {
            board.addBitmap(loadImage(path, 500,500));
        } else {
            Toast.makeText(MainActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap loadImage(Uri uri, int w, int h) {
        return presenter.LoadImage(uri, w, h);
    }

    private void showPopupWindow() {
        //设置contentView
        View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popup_eraser, null);
        popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setContentView(contentView);

        RadioGroup eraserGroup = contentView.findViewById(R.id.eraser_group);
        RadioButton buttonAll = contentView.findViewById(R.id.eraser_all);
        RadioButton buttonPart = contentView.findViewById(R.id.eraser_part);
        if (board.getEraserType() == WritingBoard.EraserType.TYPE_ALL) {
            buttonAll.setChecked(true);
        } else {
            buttonPart.setChecked(true);
        }
        eraserGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.eraser_all:
                        board.setEraserType(WritingBoard.EraserType.TYPE_ALL);
                        break;
                    case R.id.eraser_part:
                        board.setEraserType(WritingBoard.EraserType.TYPE_PART);
                        break;
                }
            }
        });
        popupWindow.showAsDropDown(eraserType, -DensityUtil.dp2px(30),
                -DensityUtil.dp2px(120));

    }
}
