package com;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.cam.pdf.and.doc.india.scanner.R;
import com.cam.pdf.and.doc.india.scanner.camscanner.MainActivity;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

public class CustomDialogClass extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;

    public CustomDialogClass(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        MainActivity mainActivity = new MainActivity();

        LabeledSwitch labeledSwitch = findViewById(R.id.sync_drive);
        labeledSwitch.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if (isOn) {
                    mainActivity.signIn();
                    MainActivity.drive_check = "true";

                } else {
                    MainActivity.drive_check = "false";
                    mainActivity.signOut();
                }
                dismiss();

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            default:
                break;
        }
        dismiss();
    }
}
