package com.camv1.pdf.and.doc.india.scanner.process.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.cam.pdf.and.doc.india.scanner.util.Const;
import com.joshuabutton.queenscanner.process.model.FilterModel;
import com.joshuabutton.queenscanner.process.view.IProcessView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Phí Văn Tuấn on 24/11/2018.
 */

public class ProcessPresenter implements com.joshuabutton.queenscanner.process.presenter.IProcessPresenter {
    private Context context;
    private IProcessView iProcessView;

    public ProcessPresenter(Context context, IProcessView iProcessView) {
        this.context = context;
        this.iProcessView = iProcessView;
    }

    @Override
    public void onItemClick(FilterModel adjuster) {
        iProcessView.onItemClick(adjuster);
    }

    @Override
    public List<FilterModel> getListModel() {
        return new FilterModel().getFilterModels();
    }

    @Override
    public String getFolderPath(String folderPath) {
        if (TextUtils.isEmpty(folderPath)) {
            String folder = "Document";
            String folder2 = folder;
            int position = 0;
            File f = new File(Const.FOLDER_DOC);
            if (!f.exists()) {
                f.mkdirs();
            }
//            try {
//
//                for (File file : f.listFiles()) {
//                    if (file.getName().equals(folder)) {
//                        position = position + 1;
//                        folder = folder2 + "(" + position + ")";
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            Calendar c = Calendar.getInstance();
            SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy hh:mm");
            String datetime = dateformat.format(c.getTime());

            File file = new File(Const.FOLDER_DOC + folder+datetime + "/");
            if (!file.exists()) {
                file.mkdirs();
            }
            return file.getAbsolutePath();
        } else {
            return folderPath;
        }

    }
}
