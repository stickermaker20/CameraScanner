package com.camv1.pdf.and.doc.india.scanner;

import android.graphics.Bitmap;

/**
 * Created by Phí Văn Tuấn on 24/11/2018.
 */

public interface IViewScanner {

    void onResult(Bitmap bitmap);

    void chooseImage();

    void editImage(String path);
}
