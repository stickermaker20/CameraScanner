package com.cam.pdf.and.doc.india.scanner.listdoc;

import com.camv1.pdf.and.doc.india.scanner.document.DocumentModel;

import java.io.File;
import java.util.List;

/**
 * Created by Phí Văn Tuấn on 5/12/2018.
 */

public interface DocsContract {
    interface IDocsView {
        void onItemClick(File file);

        void onItemLongClick(File file);

    }

    interface IDocsPresenter {
        void onItemClick(File file);

        void onItemLongClick(File file);

        String bindLastModify(long time);

        String getPagePdf(File folder);

        int getNumberOfImage(File folder);

        String getImagePath(File folder);

        List<DocumentModel> getListDocs(String folder);
    }

    interface IDocsModel {
        List<String> getLstPathDoc();

    }
}
