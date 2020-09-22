package com.camv1.pdf.and.doc.india.scanner.process.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.cam.pdf.and.doc.india.scanner.base.BaseActivity;
import com.cam.pdf.and.doc.india.scanner.util.ImageUtils;
import com.camv1.pdf.and.doc.india.scanner.Config.AdsTask;
import com.camv1.pdf.and.doc.india.scanner.activities.SimpleDocumentScannerActivity;
import com.camv1.pdf.and.doc.india.scanner.document.DocumentActivity;
import com.camv1.pdf.and.doc.india.scanner.process.presenter.ProcessPresenter;
import com.camv1.pdf.and.doc.india.scanner.PresenterScanner;
import com.itextpdf.text.pdf.parser.Line;
import com.joshuabutton.queenscanner.process.adapter.ProcessAdapter;
import com.joshuabutton.queenscanner.process.model.FilterModel;
import com.joshuabutton.queenscanner.process.presenter.IProcessPresenter;
import com.joshuabutton.queenscanner.process.view.IProcessView;
import com.cam.pdf.and.doc.india.scanner.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import it.chengdazhi.styleimageview.Styler;
import ly.img.android.sdk.cropper.cropwindow.handle.Handle;

public class ProcessImageActivity extends BaseActivity implements IProcessView, SeekBar.OnSeekBarChangeListener {
    @BindView(R.id.imgSave)
    ImageView imgSave;
    @BindView(R.id.imgRotate)
    ImageView imgRotate;
    @BindView(R.id.rcView)
    RecyclerView rcView;
    @BindView(R.id.seekbar_layout)
    LinearLayout seekbar_layout;
    @BindView(R.id.imgView2)
    public ImageView imageView;
    SeekBar sb_constant;
    SeekBar sb_block;
    private IProcessPresenter presenter;
    private ProcessAdapter adapter;
    public Bitmap bitmap;
    public static Bitmap bitMapSource;
    private String folderPath;
    private static int REQUEST_SIGN = 1;
    int blockvalue = 21;
    int constantvalue = 9;
    private Styler styler;
    AdsTask adsTask;
    Handler bitmaphandler;
    Runnable bitmapRunnable;
    public static String position = "false";
    public static String enable = "true";

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_process_image;
    }

    @Override
    protected void initData() {
        adsTask = new AdsTask(this);
        bitmap = PresenterScanner.bitmapSelected;
        imageView.setImageBitmap(bitmap);
        folderPath = getIntent().getStringExtra("folderPath");
        styler = new Styler.Builder(imageView, Styler.Mode.NONE).enableAnimation(500).build();
        List<FilterModel> lst = presenter.getListModel();
        adapter.loadData(lst);
        styler.setMode(lst.get(0).getMode()).updateStyle();
        sb_constant = findViewById(R.id.sb_constant);
        sb_block = findViewById(R.id.sb_block);
        sb_block.setOnSeekBarChangeListener(this);
        sb_constant.setOnSeekBarChangeListener(this);
        bitmaphandler = new Handler();
        bitmapRunnable = new Runnable() {
            @Override
            public void run() {
                if (position.equals("true")) {
                    position = "false";
                    imageView.setImageBitmap(bitmap);
                }
                if (enable.equals("true")) {
                    enable = "";
                    Mat imageMat = new Mat();
                    Utils.bitmapToMat(bitmap, imageMat);
                    Bitmap mResult = applyThreshold(imageMat);
                    imageView.setImageBitmap(mResult);
                    seekbar_layout.setVisibility(View.VISIBLE);
                } else if (enable.equals("false")) {
                    enable = "";
                    seekbar_layout.setVisibility(View.GONE);
                }
                bitmaphandler.postDelayed(bitmapRunnable, 100);
            }
        };
        bitmaphandler.post(bitmapRunnable);
    }

    @Override
    protected void initView() {
        getSupportActionBar().hide();
        presenter = new ProcessPresenter(this, this);
        rcView.setLayoutManager(new LinearLayoutManager(rcView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new ProcessAdapter(this, presenter);
        rcView.setAdapter(adapter);
        DividerItemDecoration verticalDecoration = new DividerItemDecoration(rcView.getContext(),
                DividerItemDecoration.HORIZONTAL);
        Drawable verticalDivider = ContextCompat.getDrawable(this, R.drawable.horizontal_divider);
        verticalDecoration.setDrawable(verticalDivider);
        rcView.addItemDecoration(verticalDecoration);

    }

    @OnClick(R.id.imgBack)
    public void back() {
        onBackPressed();
    }

    @OnClick(R.id.imgRotate)
    public void onRotate() {
        Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        imgRotate.startAnimation(animFadein);
        imageView.setImageBitmap(rotateBitmap(bitmap, 90));
        styler.updateStyle();

    }

    @OnClick(R.id.imgSave)
    public void onSave() {
        Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        imgSave.startAnimation(animFadein);
        ProcessImageActivity.bitMapSource = styler.getBitmap();
        Log.d("pathh.....", "onSave...........: " + this.folderPath);
//        Toast.makeText(this, ""+this.folderPath, Toast.LENGTH_SHORT).show();
        String folderPath = presenter.getFolderPath(this.folderPath);
//        String imagePath = folderPath + "/" + getImagePath();
        String imagePath = folderPath + "/A" + System.currentTimeMillis() + ".jpg";

        ImageUtils.saveBitMap(styler.getBitmap(), imagePath);
        File file = new File(folderPath);
        String pdfName = file.getName() + ".pdf";
//        ImageUtils.convertImageToPdf(getLstImagePath(folderPath), folderPath + "/" + pdfName, ProcessImageActivity.this);
        Intent intent = new Intent(ProcessImageActivity.this, DocumentActivity.class);
        intent.putExtra("folder", folderPath);
        if (TextUtils.isEmpty(this.folderPath)) {
            finish();
            startActivity(intent);

        } else {
            setResult(RESULT_OK);
            finish();
            startActivity(intent);
        }

        adsTask.showInterstitialAds();

    }

    @Override
    public void onItemClick(FilterModel adjuster) {

        styler.setMode(adjuster.getMode()).updateStyle();
    }

    private Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    int lastvalue = 0;

    @Override
    public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
        if (fromUser) {
            Bitmap mResult = null;
            Mat imageMat = new Mat();
            Utils.bitmapToMat(bitmap, imageMat);
            switch (bar.getId()) {
                case R.id.sb_block:
                    if (progress < 10) {
                        blockvalue = 5;
                    } else if (progress < 15) {
                        blockvalue = 7;
                    } else if (progress < 20) {
                        blockvalue = 9;
                    } else if (progress < 25) {
                        blockvalue = 11;
                    } else if (progress < 30) {
                        blockvalue = 13;
                    } else if (progress < 35) {
                        blockvalue = 15;
                    } else if (progress < 40) {
                        blockvalue = 17;
                    } else if (progress < 45) {
                        blockvalue = 19;
                    } else if (progress < 50) {
                        blockvalue = 21;
                    } else if (progress < 55) {
                        blockvalue = 23;
                    } else if (progress < 60) {
                        blockvalue = 25;
                    } else if (progress < 65) {
                        blockvalue = 27;
                    } else if (progress < 70) {
                        blockvalue = 29;
                    } else if (progress < 75) {
                        blockvalue = 31;
                    } else if (progress < 80) {
                        blockvalue = 35;
                    } else if (progress < 85) {
                        blockvalue = 37;
                    } else if (progress < 90) {
                        blockvalue = 39;
                    } else if (progress < 95) {
                        blockvalue = 41;
                    } else if (progress < 101) {
                        blockvalue = 43;
                    }
                    Log.d("ThresholdChecker", "Block Value" + blockvalue);
                    mResult = applyThreshold(imageMat);
                    imageView.setImageBitmap(mResult);
                    break;

                case R.id.sb_constant:
                    Log.d("ThresholdChecker", "Constant Value" + progress);
                    constantvalue = progress;
                    mResult = applyThreshold(imageMat);
                    imageView.setImageBitmap(mResult);
                    break;
            }
            //  styler.updateStyle();
        }
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private String getImagePath() {
        File file = new File(folderPath);
        int position = 0;
        if (file.exists()) {
            for (File file1 : file.listFiles()) {
                if (file1.getAbsolutePath().endsWith(".jpg")) {
                    position = position + 1;
                }
            }
        }
        long second = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String dateString = formatter.format(new Date(second)) + position;
        return dateString + ".jpg";
    }

    private List<String> getLstImagePath(String folderPath) {
        List<String> lstPath = new ArrayList<>();
        File file = new File(folderPath);
        int position = 0;
        if (file.exists()) {
            for (File file1 : file.listFiles()) {
                if (file1.getAbsolutePath().endsWith(".jpg")) {
                    lstPath.add(file1.getAbsolutePath());
                }
            }
        }
        return lstPath;
    }

    public static void startProcess(Context context, String folderPath) {
        Intent intent = new Intent(context, ProcessImageActivity.class);
        intent.putExtra("folderPath", folderPath);
        if (TextUtils.isEmpty(folderPath)) {
            context.startActivity(intent);
        } else {
            ((SimpleDocumentScannerActivity) context).startActivityForResult(intent, DocumentActivity.REQUEST_IMPORT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adsTask.destroyBannerAds();
    }

    private Bitmap applyThreshold(Mat src) {

        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 1 * blockvalue, 1 * constantvalue);
        Imgproc.threshold(src, src, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        Bitmap bm = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(src, bm);
        return bm;
    }
}

