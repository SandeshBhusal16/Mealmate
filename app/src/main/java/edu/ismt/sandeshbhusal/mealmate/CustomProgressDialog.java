package edu.ismt.sandeshbhusal.mealmate;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.progressindicator.CircularProgressIndicator;

public class CustomProgressDialog {
    private Dialog dialog;
    private CircularProgressIndicator progressIndicator;

    public CustomProgressDialog(Context context) {
        dialog = new Dialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog, null);
        dialog.setContentView(view);
        dialog.setCancelable(false); // Prevent user from closing it manually
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Find Progress Indicator and Set Indeterminate Mode
        progressIndicator = view.findViewById(R.id.progressIndicator);
        progressIndicator.setIndeterminate(true);
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
