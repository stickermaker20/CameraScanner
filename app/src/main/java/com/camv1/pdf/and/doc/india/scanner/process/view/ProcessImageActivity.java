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

import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.cam.pdf.and.doc.india.scanner.base.BaseActivity;
import com.cam.pdf.and.doc.india.scanner.util.ImageUtils;
import com.camv1.pdf.and.doc.india.scanner.Config.AdsTask;
import com.camv1.pdf.and.doc.india.scanner.activities.SimpleDocumentScannerActivity;
import com.camv1.pdf.and.doc.india.scanner.document.DocumentActivity;
import com.camv1.pdf.and.doc.india.scanner.process.presenter.ProcessPresenter;
import com.camv1.pdf.and.doc.india.scanner.PresenterScanner;
import com.joshuabutton.queenscanner.process.adapter.ProcessAdapter;
import com.joshuabutton.queenscanner.process.model.FilterModel;
import com.joshuabutton.queenscanner.process.presenter.IProcessPresenter;
import com.joshuabutton.queenscanner.process.view.IProcessView;
import com.cam.pdf.and.doc.india.scanner.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import it.chengdazhi.styleimageview.Styler;

public class ProcessImageActivity extends BaseActivity implements IProcessView, SeekBar.OnSeekBarChangeListener {
    @BindView(R.id.imgSave)
    ImageView imgSave;
    @BindView(R.id.imgRotate)
    ImageView imgRotate;
    @BindView(R.id.rcView)
    RecyclerView rcView;
    @BindView(R.id.imgView2)
    ImageView imageView;
    @BindView(R.id.sbFilter)
    SeekBar sbFilter;
    private IProcessPresenter presenter;
    private ProcessAdapter adapter;
    private Bitmap bitmap;
    public static Bitmap bitMapSource;
    private String folderPath;
    private static int REQUEST_SIGN = 1;

    private Styler styler;
    AdsTask adsTask;


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
        sbFilter.setOnSeekBarChangeListener(this);
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


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            Log.d("ThresholdChecker", ""+progress);
            if (progress > 10) {
                Mat imageMat = new Mat();
                Utils.bitmapToMat(bitmap, imageMat);
                applyThreshold(imageMat, progress);
                // Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);
//                Imgproc.adaptiveThreshold(imageMat, imageMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 9, progress);
//                Utils.matToBitmap(imageMat, bitmap);
                //  styler.updateStyle();
            }
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


    private Bitmap applyThreshold(Mat src, int value) {
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 9, value);
        // Imgproc.threshold(src, src, value, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        //  Imgproc.threshold(src, src, 10, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        //  Bitmap bm = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888);
        // org.opencv.android.Utils.matToBitmap(src, bitmap);

        return bitmap;
    }
}
