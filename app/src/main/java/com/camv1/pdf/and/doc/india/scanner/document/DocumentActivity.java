package com.camv1.pdf.and.doc.india.scanner.document;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.pdf.and.doc.india.scanner.DynamicAdapter;
import com.cam.pdf.and.doc.india.scanner.OnChangeDynamic;
import com.cam.pdf.and.doc.india.scanner.OnLongClickItem;
import com.cam.pdf.and.doc.india.scanner.R;
import com.cam.pdf.and.doc.india.scanner.base.BaseActivity;
import com.cam.pdf.and.doc.india.scanner.camscanner.MainActivity;
import com.cam.pdf.and.doc.india.scanner.util.Const;
import com.cam.pdf.and.doc.india.scanner.util.ImageUtils;
import com.camv1.pdf.and.doc.india.scanner.Config.AdsTask;
import com.camv1.pdf.and.doc.india.scanner.activities.SimpleDocumentScannerActivity;
import com.camv1.pdf.and.doc.india.scanner.handle.HandleActivity;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import org.askerov.dynamicgrid.DynamicGridView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.camv1.pdf.and.doc.india.scanner.PresenterScanner.FOLDER_NAME;


public class DocumentActivity extends BaseActivity implements DocumentContract.DocumentView, OnLongClickItem, OnChangeDynamic, EasyPermissions.PermissionCallbacks {
    private DocumentContract.IDocumentPresenter presenter;
    private DocumentAdapter adapter;
    private DynamicAdapter dynamicAdapter;
    @BindView(R.id.rcView)
    RecyclerView rcView;
    @BindView(R.id.imgBack)
    ImageView imgBack;
    @BindView(R.id.imgMenu)
    ImageView imgMenu;
    @BindView(R.id.edtName)
    EditText editName;
    @BindView(R.id.imgDone)
    ImageView imgDone;
    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.dynamic_grid)
    DynamicGridView gridView;
    @BindView(R.id.tv_suggest)
    TextView tvSuggest;

    @BindView(R.id.menu_item_camera)
    FloatingActionButton fabCamera;
    @BindView(R.id.menu_item_gallery)
    FloatingActionButton fabGallery;
    private String folder;
    private static final int TYPE_NEW_CREATE = 1;
    private int type = 0;
    public static final int REQUEST_IMPORT = 100;
    private String pathCamera = "";
    private int REQUEST_CAMERA = 102;
    private int REQUEST_CAMERA_PERMISSION = 201;
    boolean isSave = false;
    private AdsTask adsTask;

    //Upload File to Google Drive

    GoogleAccountCredential mCredential = null;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String[] SCOPES = {DriveScopes.DRIVE};
    private static final String PREF_ACCOUNT_NAME = "accountName";
    GoogleSignInAccount account;
    //  private ProgressBar mProgressBar;
    private int calledFrom = 0;
    java.io.File file2;
    public static String path;
    Handler driveHandler;
    Runnable driveRunnable;
    public static String pdfComplete = "false";
    public static String fileName = "";

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_document;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initData() {
        adsTask = new AdsTask(this);
        folder = getIntent().getStringExtra("folder");
        type = getSharedPreferences("BVH", MODE_PRIVATE).getInt("type", 1);

        adapter.loadData(presenter.getListDocument(folder));
        dynamicAdapter = new DynamicAdapter(this, presenter.getListDocument(folder), 2);
        dynamicAdapter.setPresenter(presenter);
        dynamicAdapter.setOnLongClickItem(this);
        dynamicAdapter.setOnChangeDynamic(this);

        gridView.setAdapter(dynamicAdapter);
        File file = new File(folder);
        if (type == TYPE_NEW_CREATE) {
            editName.setVisibility(View.VISIBLE);
            imgDone.setVisibility(View.VISIBLE);
            tvName.setVisibility(View.GONE);
            imgMenu.setVisibility(View.GONE);
            editName.setText(file.getName());
            setNameFile(file.getName());
            editName.setSelection(editName.getText().toString().length());
        } else {
            imgDone.setVisibility(View.GONE);
            editName.setVisibility(View.GONE);
            tvName.setVisibility(View.VISIBLE);
            imgMenu.setVisibility(View.VISIBLE);
            tvName.setText(file.getName());
        }

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    @Override
    protected void initView() {
        getSupportActionBar().hide();

        presenter = new com.camv1.pdf.and.doc.india.scanner.document.DocumentPresenter(this, this);
        rcView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new DocumentAdapter(this, presenter);
//        rcView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position) {
        HandleActivity.startHandle(DocumentActivity.this, position, folder);
    }

    @Override
    public String getDefaultName() {
        return folder;
    }

    @Override
    public void setFolderName(String folderName) {
        this.folder = folderName;
    }

    @Override
    public void setNameFile(String nameFile) {
        tvName.setText(nameFile);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMPORT && resultCode == RESULT_OK) {
//            Log.e("bvh", data.toString());
//            adapter.loadData(presenter.getListDocument(folder));
            dynamicAdapter = new DynamicAdapter(this, presenter.getListDocument(folder), 2);
            dynamicAdapter.setPresenter(presenter);
            dynamicAdapter.setOnLongClickItem(this);
            dynamicAdapter.setOnChangeDynamic(this);
            gridView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:
                            if (gridView.isEditMode()) {
                                gridView.stopEditMode();
                                tvSuggest.setVisibility(View.GONE);
                            }
                            break;
                    }
                    return true;
                }
            });

            gridView.setAdapter(dynamicAdapter);
        } else if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            pathCamera = getSharedPreferences("BVH", MODE_PRIVATE).getString("path", pathCamera);
            File f = new File(pathCamera);
            if (f.exists()) {
//                Toast.makeText(this, "exist", Toast.LENGTH_SHORT).show();
//                SimpleDocumentScannerActivity.startScanner(DocumentActivity.this, pathCamera, "");
                getSharedPreferences("BVH", MODE_PRIVATE).edit().putInt("type", 0).commit();
                SimpleDocumentScannerActivity.startScanner(DocumentActivity.this, pathCamera, folder);
//                dynamicAdapter = new DynamicAdapter(this, presenter.getListDocument(folder), 2);
//                dynamicAdapter.setPresenter(presenter);
//                dynamicAdapter.setOnLongClickItem(this);
//                dynamicAdapter.setOnChangeDynamic(this);
                finish();
//                gridView.setAdapter(dynamicAdapter);
            } else {
                imgDone.setVisibility(View.GONE);
                imgMenu.setVisibility(View.VISIBLE);
                Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_GOOGLE_PLAY_SERVICES) {
            if (resultCode != RESULT_OK) {
                Log.e(this.toString(), "This app requires Google Play Services. Please install " +
                        "Google Play Services on your device and relaunch this app.");

            } else {
                getResultsFromApi();
            }
        } else if (requestCode == REQUEST_ACCOUNT_PICKER) {
            if (resultCode == RESULT_OK && data != null &&
                    data.getExtras() != null) {
                String accountName =
                        data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                if (accountName != null) {
                    SharedPreferences settings =
                            getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(PREF_ACCOUNT_NAME, accountName);
                    editor.apply();
                    mCredential.setSelectedAccountName(accountName);
                    getResultsFromApi();
                }
            }
        } else if (requestCode == REQUEST_AUTHORIZATION) {
            if (resultCode == RESULT_OK) {
                getResultsFromApi();
            }
        }
//
    }

    @Override
    protected void onResume() {
//        adapter.loadData(presenter.getListDocument(folder));
        super.onResume();
    }

    @OnClick(R.id.imgBack)
    public void onBack() {
//        startActivity(new Intent(DocumentActivity.this, MainActivity.class));
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        if (!isSave) {
//            isSave = true;
//            SaveData saveData = new SaveData();
//            saveData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        }

    }

    class SaveData extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DocumentActivity.this);
            progressDialog.setMessage("Save file");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<Object> arrModel = dynamicAdapter.getItems();
            int po = 0;
            for (int i = 0; i < arrModel.size(); i++) {
                DocumentModel documentModel = (DocumentModel) arrModel.get(i);
                String path = documentModel.getPath();

                int index = path.lastIndexOf(File.separator);
                String newPath = path.substring(0, index) + File.separator + "A" + System.currentTimeMillis() + po + ".jpg";
                Log.e("bvh", "check path new : " + newPath);
                Log.e("bvh", "check path old : " + newPath);
                po++;

                renameFile(documentModel.getPath(), newPath);
            }
            return null;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();

            dynamicAdapter = new DynamicAdapter(DocumentActivity.this, presenter.getListDocument(folder), 2);
            dynamicAdapter.setPresenter(presenter);
            dynamicAdapter.setOnLongClickItem(DocumentActivity.this);
            dynamicAdapter.setOnChangeDynamic(DocumentActivity.this);
            gridView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:
                            while (gridView.isEditMode()) {
                                gridView.stopEditMode();
                                tvSuggest.setVisibility(View.GONE);
                                if (!isSave) {
                                    isSave = true;
                                    SaveData saveData = new SaveData();
                                    saveData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            }
                            break;
                    }
                    return true;
                }
            });

            gridView.setAdapter(dynamicAdapter);
            isSave = false;
        }
    }

    public boolean renameFile(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        if (newFile.exists()) {
            Log.e("bvh", "rename: false");
            return false;
        } else {

            if (oldFile.renameTo(newFile)) {
                Log.e("bvh", "rename: true");
                return true;
            } else {
                Log.e("bvh", "rename: true");
                return false;
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @OnClick(R.id.imgMenu)
    public void onMenu() {
        PopupMenu pop = new PopupMenu(this, imgMenu);
        pop.getMenuInflater().inflate(R.menu.menu_doc, pop.getMenu());

        pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                final File file = new File(folder);
                switch (menuItem.getItemId()) {
                    case R.id.menuRename:
                        showDialog();
                        break;
                    case R.id.menuImport:
                        SimpleDocumentScannerActivity.startScanner(DocumentActivity.this, "", folder);
                        finish();
                        break;
                    case R.id.menuOpen:
                        presenter.openWith(getDefaultName() + "/" + tvName.getText().toString() + ".pdf");
                        break;
//                    case R.id.menuShare:
//                        presenter.shareFile(getDefaultName() + "/" + tvName.getText().toString() + ".pdf");
//                        break;
                    case R.id.menuSaveToPDF:
                        Log.d("PDFUplaodChecker", "button pressed");
                        String pdfName = file.getName() + System.currentTimeMillis() + ".pdf";
                        List<String> myPath = new ArrayList<>();
                        List<Object> arrModel = dynamicAdapter.getItems();
                        for (Object o : arrModel) {
                            DocumentModel documentModel = (DocumentModel) o;
                            myPath.add(documentModel.getPath());
                        }
                        ImageUtils.convertImageToPdf(myPath, folder + "/" + pdfName, DocumentActivity.this);
                        path = folder + "/" + pdfName;
                        fileName = pdfName;
                        if (MainActivity.drive_check.equals("true")) {
                            Log.d("PDFUplaodChecker", "Sync Enable");
                            driveHandler = new Handler();
                            driveRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (pdfComplete.equals("true")) {
                                        Log.d("PDFUplaodChecker", "PDF Create Done");
                                        calledFrom = 2;
                                        pdfComplete = "false";
                                        getResultsFromApi();
                                        new DocumentActivity.MakeDriveRequestTask2(mCredential, DocumentActivity.this).execute();
                                    } else {
                                        driveHandler.postDelayed(driveRunnable, 500);
                                    }
                                }
                            };
                            driveHandler.post(driveRunnable);
                        }
                        break;
                    case R.id.menuDelete:
                        if (file.exists()) {
                            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DocumentActivity.this);
                            builder
                                    .setMessage("Delete this document folder?")
                                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            for (File file1 : file.listFiles())
                                                file1.delete();
                                            file.delete();
                                            Toast.makeText(DocumentActivity.this, "Deleted", Toast.LENGTH_LONG).show();
                                            adapter.notifyDataSetChanged();
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();

                                        }
                                    });
                            final android.app.AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                }
                return true;
            }
        });
        MenuPopupHelper menuHelper = new MenuPopupHelper(this, (MenuBuilder) pop.getMenu(), imgMenu);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
    }

    @OnClick(R.id.menu_item_gallery)
    public void importFromGallery() {
        getSharedPreferences("BVH", MODE_PRIVATE).edit().putInt("type", 0).commit();
        SimpleDocumentScannerActivity.startScanner(DocumentActivity.this, "", folder);
        finish();
    }

    @OnClick(R.id.menu_item_camera)
    public void importFromCamera() {
        if (ActivityCompat.checkSelfPermission(DocumentActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            callCamera();
        } else {
            ActivityCompat.requestPermissions(DocumentActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void callCamera() {
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String folderName = mSharedPref.getString("storage_folder", FOLDER_NAME);
//        File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//                + "/" + folderName);
//        if (!folder.exists()) {
//            folder.mkdirs();
//        }
        pathCamera = this.folder + "/.TEMP_CAMERA.xxx";
        getSharedPreferences("BVH", MODE_PRIVATE).edit().putString("path", pathCamera).commit();
        File f = new File(pathCamera);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri outputFileUri;
        if (Build.VERSION.SDK_INT < 24)
            outputFileUri = Uri.fromFile(f);
        else {
            outputFileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", f);
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CAMERA);
        }
    }

    @OnClick(R.id.imgDone)
    public void onDone() {
        String filePdfName = editName.getText().toString();
        presenter.reName(filePdfName);
        fileName = filePdfName;
        Const.hideKeyboardFrom(DocumentActivity.this, editName);
        imgDone.setVisibility(View.GONE);
        editName.setVisibility(View.GONE);
        tvName.setVisibility(View.VISIBLE);
        imgMenu.setVisibility(View.VISIBLE);
        if (MainActivity.drive_check.equals("true")) {
            Log.d("PDFUplaodChecker", "Sync Enable");
            driveHandler = new Handler();
            driveRunnable = new Runnable() {
                @Override
                public void run() {
                    if (pdfComplete.equals("true")) {
                        Log.d("PDFUplaodChecker", "PDF Create Done");
                        calledFrom = 2;
                        pdfComplete = "false";
                        getResultsFromApi();
                        new DocumentActivity.MakeDriveRequestTask2(mCredential, DocumentActivity.this).execute();
                    } else {
                        driveHandler.postDelayed(driveRunnable, 500);
                    }
                }
            };
            driveHandler.post(driveRunnable);
        }

    }

    private void showDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
        View mView = layoutInflaterAndroid.inflate(R.layout.dialog_rename, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(mView);

        final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
        userInputDialogEditText.setText(tvName.getText().toString());
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        presenter.reName(userInputDialogEditText.getText().toString());
                    }
                })

                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void longClickItem(int po) {
        gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        while (gridView.isEditMode()) {
                            gridView.stopEditMode();
                            tvSuggest.setVisibility(View.GONE);
                            if (!isSave) {
                                isSave = true;
                                SaveData saveData = new SaveData();
                                saveData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        }
                        break;
                }
                return false;
            }
        });
        tvSuggest.setVisibility(View.VISIBLE);
        gridView.startEditMode(po);
    }

    @Override
    public void onChangeItem() {
//        Toast.makeText(this, "changer", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {

            Toast.makeText(getApplicationContext(),
                    "No Network Connection Available", Toast.LENGTH_SHORT).show();
            Log.e(this.toString(), "No network connection available.");
        } else {
            //if everything is Ok
            if (calledFrom == 2) {
                new DocumentActivity.MakeDriveRequestTask2(mCredential, DocumentActivity.this).execute();//upload q and responses xlsx files
            }

        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, android.Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    android.Manifest.permission.GET_ACCOUNTS);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }


    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }


    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        Log.e(this.toString(), "Checking if device");
        return (networkInfo != null && networkInfo.isConnected());

    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                DocumentActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private class MakeDriveRequestTask2 extends AsyncTask<Void, Void, List<String>> {
        private Drive mService = null;
        private Exception mLastError = null;
        private Context mContext;


        MakeDriveRequestTask2(GoogleAccountCredential credential, Context context) {

            mContext = context;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("UsingDriveJavaApi")
                    .build();
            // TODO change the application name to the name of your applicaiton
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            Log.d("PDFUplaodChecker", "doinBackground");

            try {
                uploadFile();
            } catch (Exception e) {
                e.printStackTrace();
                mLastError = e;

                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {

                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            DocumentActivity.REQUEST_AUTHORIZATION);
                } else {
                    Log.e(this.toString(), "The following error occurred:\n" + mLastError.getMessage());
                }
                Log.e(this.toString(), e + "");
            }


            return null;
        }

        @Override
        protected void onPreExecute() {
            //   mProgressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onPostExecute(List<String> output) {
            // mProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            // mProgressBar.setVisibility(View.GONE);
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {

                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            DocumentActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(DocumentActivity.this, "The following error occurred:\n" + mLastError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DocumentActivity.this, "Request cancelled.", Toast.LENGTH_SHORT).show();
            }
        }

        private void uploadFile() throws IOException {
            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            if (fileName.equals("")) {
                fileMetadata.setName("CamScanner India Document" + new Date());
            } else {
                fileMetadata.setName(fileName);
            }

            // For mime type of specific file visit Drive Doucumentation

            file2 = new java.io.File(path);
            //   InputStream inputStream = getResources().openRawResource(R.raw.template);
//            try {
//                FileUtils.copyInputStreamToFile(inputStream, file2);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            Log.d("PDFUplaodChecker", "inside upload file, File Name: " + fileName + " \nPath: " + path);

            FileContent mediaContent = new FileContent("application/pdf", file2);

            com.google.api.services.drive.model.File file = mService.files().create(fileMetadata, mediaContent).execute();

            Log.e(this.toString(), "File Created with ID:" + file.getId());

            Toast.makeText(getApplicationContext(), "Sync to Google Drive Successful:" + file.getId(), Toast.LENGTH_SHORT).show();
        }


    }

}

