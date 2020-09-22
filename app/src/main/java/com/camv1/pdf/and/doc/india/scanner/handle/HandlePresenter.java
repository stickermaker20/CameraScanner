package com.camv1.pdf.and.doc.india.scanner.handle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.camv1.pdf.and.doc.india.scanner.document.DocumentActivity;
import com.github.chrisbanes.photoview.PhotoView;
import com.cam.pdf.and.doc.india.scanner.util.ImageUtils;
import com.cam.pdf.and.doc.india.scanner.util.PathUtil;

import java.io.File;

/**
 * Created by Phí Văn Tuấn on 30/11/2018.
 */

public class HandlePresenter implements HandleContract.IHandlePresenter {
    private Context context;
    private HandleContract.IHandleView iView;

    public HandlePresenter(Context context, HandleContract.IHandleView iView) {
        this.context = context;
        this.iView = iView;
    }

    @Override
    public void sign() {
        iView.sign();
    }

    @Override
    public Bitmap getBitMapRotate(PhotoView photoView) {
        Bitmap bitmap = null;
        try {
            //BitmapDrawable drawable = (BitmapDrawable) photoView.getDrawable();
            bitmap = ((BitmapDrawable) photoView.getDrawable()).getBitmap();//drawable.getBitmap();
        } catch (Exception e) {
            e.printStackTrace();
            ///BitmapDrawable drawable = (BitmapDrawable) photoView.getDrawable();
            bitmap = ((BitmapDrawable) photoView.getDrawable()).getBitmap();//drawable.getBitmap();
        }
        return ImageUtils.rotate(bitmap, 90);
    }

    @Override
    public void merge() {
        iView.updateView();
        String folder = iView.getFolderPath();
        String namePdf = folder + "/" + new File(folder).getName() + ".pdf";
        ImageUtils.convertImageToPdf(PathUtil.getLstImagePath(folder), namePdf, context);
    }

    @Override
    public void createAnimationTop() {
        iView.createAnimationTop();
    }

    @Override
    public void createAnimationXia() {
        iView.createAnimationXia();
    }

    @Override
    public void share(String imagePath) {

    }

    @Override
    public void delete(String imagePath) {
        File file = new File(imagePath);
        if (file.exists()) {
            file.delete();
            iView.updateView();
//            this.merge();
        }
    }
}
