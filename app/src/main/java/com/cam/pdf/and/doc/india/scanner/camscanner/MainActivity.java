package com.cam.pdf.and.doc.india.scanner.camscanner;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.MainApplication;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.cam.pdf.and.doc.india.scanner.listdoc.DocsActivity;
import com.camv1.pdf.and.doc.india.scanner.Config.AdsTask;
import com.camv1.pdf.and.doc.india.scanner.activities.SimpleDocumentScannerActivity;
import com.cam.pdf.and.doc.india.scanner.R;
import com.camv2.pdf.and.doc.india.scanner.AccountActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;

import static com.camv1.pdf.and.doc.india.scanner.PresenterScanner.FOLDER_NAME;


/**
 * Created by HUNGDH. Edited by SonDD
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CardView cvCamera, cvFromGallery, cvGallery, cvPDF;
    private static final int REQUEST_STORAGE = 212;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private String pathCamera = null;
    private AdsTask adsTask;
    private LinearLayout llAds;
    int p = 0;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;
    public static String drive_check = "false";
    String token;
    public static final int RC_SIGN_IN = 420;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adsTask = new AdsTask(this);
        // Obtain the shared Tracker instance.
        MainApplication application = (MainApplication) getApplication();

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            initView();

//            initBannerAds();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        }
        initAds();

    }

    InterstitialAd mInterstitialAd;

    private void initAds() {
//        MobileAds.initialize(this, getResources().getString(R.string.admod_app_id));


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.id_interstitial_google));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                if (p == 1) {
                    startActivity(new Intent(MainActivity.this, DocsActivity.class));
                } else if (p == 2) {
                    startActivity(new Intent(MainActivity.this, MyPDFActivity.class));
                } else if (p == 3) {
                    getSharedPreferences("BVH", MODE_PRIVATE).edit().putInt("type", 1).commit();
                    SimpleDocumentScannerActivity.startScanner(MainActivity.this, "", "");
                } else if (p == 4) {
                    callCamera();
                }
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        adsTask.showInterstitialAds();
    }

    private void initView() {
        llAds = findViewById(R.id.ll_ads);
        cvCamera = (CardView) findViewById(R.id.cvCamera);
        cvFromGallery = (CardView) findViewById(R.id.cvFromGallery);
        cvGallery = (CardView) findViewById(R.id.cvGallery);
        cvPDF = (CardView) findViewById(R.id.cvPDF);
        LabeledSwitch labeledSwitch = findViewById(R.id.sync_drive);
        labeledSwitch.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if (isOn) {
                    drive_check = "true";
                    signIn();
                } else {
                    drive_check = "false";
                    signOut();
                }
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        /*In order to access files in drive the scope of the permission has to be specified.
        More info on scope is available in Google Drive Api Documentation*/

        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
        // If already signed in with the app it can be obtained here
//
//        if (account == null) {
//            Toast.makeText(getApplicationContext(), "You Need To Sign In First", Toast.LENGTH_SHORT).show();
//        }
        if (account != null) {
            drive_check = "true";
//            Intent intent = new Intent(MainActivity.this,DriveActivity.class);
//            intent.putExtra("ACCOUNT",account);
//            startActivity(intent);
        }
        cvCamera.setOnClickListener(this);
        cvFromGallery.setOnClickListener(this);
        cvGallery.setOnClickListener(this);
        cvPDF.setOnClickListener(this);

        adsTask.loadBannerAdsFacebook(llAds);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                initView();
//                initBannerAds();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);
            }
        }
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                callCamera();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        }

    }

    private void initBannerAds() {
//        final AdView mAdView = (AdView) findViewById(R.id.adView);
//
//        if (mAdView != null) {
//            mAdView.setAdListener(new AdListener() {
//                @Override
//                public void onAdFailedToLoad(int i) {
//                    super.onAdFailedToLoad(i);
//                    mAdView.setVisibility(View.GONE);
//                }
//
//                @Override
//                public void onAdLoaded() {
//                    super.onAdLoaded();
//                    mAdView.setVisibility(View.VISIBLE);
//                }
//            });
//
//            AdRequest adRequest = new AdRequest.Builder().build();
//            mAdView.loadAd(adRequest);
//        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cvCamera:
                /*Intent intent = new Intent(MainActivity.this, OpenNoteScannerActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                startActivity(intent);*/
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    if (mInterstitialAd.isLoaded() && mInterstitialAd != null) {
                        p = 4;
                        mInterstitialAd.show();
                    } else {
                        callCamera();
                    }


                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }
                break;
            case R.id.cvFromGallery:
                if (mInterstitialAd.isLoaded() && mInterstitialAd != null) {
                    p = 3;
                    mInterstitialAd.show();
                } else {
                    getSharedPreferences("BVH", MODE_PRIVATE).edit().putInt("type", 1).commit();
                    SimpleDocumentScannerActivity.startScanner(MainActivity.this, "", "");
                }


//                Intent iG = new Intent(MainActivity.this, SimpleDocumentScannerActivity.class);
//                iG.putExtra(SimpleDocumentScannerActivity.KEY_DOCUMENT, "");
//                startActivity(iG);
                break;
            case R.id.cvGallery:
                if (mInterstitialAd.isLoaded() && mInterstitialAd != null) {
                    p = 1;
                    mInterstitialAd.show();
                } else {
                    startActivity(new Intent(MainActivity.this, DocsActivity.class));
                }
                break;
            case R.id.cvPDF:
                if (mInterstitialAd.isLoaded() && mInterstitialAd != null) {
                    p = 2;
                    mInterstitialAd.show();
                } else {
                    startActivity(new Intent(MainActivity.this, MyPDFActivity.class));
                }

                break;
        }
    }

    private void callCamera() {
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String folderName = mSharedPref.getString("storage_folder", FOLDER_NAME);
        File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + "/" + folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        pathCamera = folder.getAbsolutePath() + "/.TEMP_CAMERA.xxx";
        getSharedPreferences("BVH", MODE_PRIVATE).edit().putString("path", pathCamera).commit();
        getSharedPreferences("BVH", MODE_PRIVATE).edit().putInt("type", 1).commit();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int i = 0;
        i++;
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            pathCamera = getSharedPreferences("BVH", MODE_PRIVATE).getString("path", pathCamera);
            File f = new File(pathCamera);
            if (f.exists()) {
                SimpleDocumentScannerActivity.startScanner(MainActivity.this, pathCamera, "");
//                Intent iC = new Intent(MainActivity.this, SimpleDocumentScannerActivity.class);
//                iC.putExtra(SimpleDocumentScannerActivity.KEY_DOCUMENT, pathCamera);
//                startActivity(iC);
            } else {
                Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
//            Intent intent = new Intent(this,DriveActivity.class);
//            intent.putExtra("ACCOUNT",account);
//            startActivity(intent);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, AccountActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_settings0) {
            callSettings();
            return true;
        }
        if (item.getItemId() == R.id.action_rate_app) {
            rateApp();
            return true;
        }
        if (item.getItemId() == R.id.action_share_app) {
            shareApp();
            return true;
        }
        if (item.getItemId() == R.id.action_more_app) {
            moreApp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void callSettings() {
        startActivity(new Intent(MainActivity.this, MySettingsActivity.class));
    }

    private void rateApp() {
        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void shareApp() {
        final String appPackageName = getPackageName();
        String myUrl = "https://play.google.com/store/apps/details?id=" + appPackageName;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, myUrl);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share)));
    }

    private void moreApp() {
        final String appPackageName = "Office+Utilities"; // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://developer?id=" + appPackageName)));
        } catch (ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=" + appPackageName)));
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.

            Log.e("Check", account.getEmail() + account.getGivenName() + account.getFamilyName());
            // If successful you can obtain the account info using the getter methods
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e("Sign-In", "signInResult:failed code=" + e.getStatusCode() + e);
            Toast.makeText(getApplicationContext(),
                    "Sign In Failed.Try again Later", Toast.LENGTH_LONG).show();
        }
    }

    public void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(),
                                "Signed Out", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
