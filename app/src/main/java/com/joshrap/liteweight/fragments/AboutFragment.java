package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import android.os.Handler;
import android.text.InputFilter;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.imports.BackendConfig;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class AboutFragment extends Fragment implements FragmentWithDialog {

    private AlertDialog alertDialog;
    private int rotationAngle;
    @Inject
    UserRepository userRepository;
    @Inject
    ProgressDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Injector.getInjector(getContext()).inject(this);

        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ((WorkoutActivity) getActivity()).toggleBackButton(false);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.ABOUT_TITLE);
        String version = null;
        try {
            version = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (version != null) {
            TextView versionTV = view.findViewById(R.id.version_number);
            String displayText = getResources().getString(R.string.version_number) + " " + version;
            versionTV.setText(displayText);
        }
        TextView faqTV = view.findViewById(R.id.faq_tv);
        faqTV.setOnClickListener(view1 -> ((WorkoutActivity) getActivity()).goToFaq());

        TextView termsConditionsTV = view.findViewById(R.id.terms_conditions_tv);
        termsConditionsTV.setOnClickListener(view1 -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BackendConfig.termsConditionsUrl));
            startActivity(browserIntent);
        });
        TextView privacyPolicyTV = view.findViewById(R.id.privacy_policy_tv);
        privacyPolicyTV.setOnClickListener(view1 -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BackendConfig.privacyPolicyUrl));
            startActivity(browserIntent);
        });

        RelativeLayout acknowledgementsLayout = view.findViewById(R.id.acknowledgements_layout);
        TextView acknowledgements = view.findViewById(R.id.acknowledgements);
        ImageButton acknowledgementIcon = view.findViewById(R.id.acknowledgements_icon);
        acknowledgements.setMovementMethod(LinkMovementMethod.getInstance()); // makes links clickable

        View.OnClickListener acknowledgementLayoutClicked = v -> {
            boolean visible = acknowledgements.getVisibility() == View.VISIBLE;
            acknowledgements.setVisibility(visible ? View.GONE : View.VISIBLE);
            rotationAngle = rotationAngle == 0 ? 180 : 0;
            acknowledgementIcon.animate().rotation(rotationAngle).setDuration(400).start();
            TransitionManager.beginDelayedTransition(container, new AutoTransition());
        };

        acknowledgementsLayout.setOnClickListener(acknowledgementLayoutClicked);
        acknowledgementIcon.setOnClickListener(acknowledgementLayoutClicked);

        TextView feedbackTV = view.findViewById(R.id.feedback_tv);
        feedbackTV.setOnClickListener(view1 -> {
            View popupView = getLayoutInflater().inflate(R.layout.popup_send_feedback, null);
            EditText feedbackInput = popupView.findViewById(R.id.feedback_input);
            TextInputLayout feedbackInputLayout = popupView.findViewById(R.id.feedback_input_layout);

            feedbackInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(feedbackInputLayout));
            feedbackInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_FEEDBACK)});
            alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                    .setTitle("Send Feedback")
                    .setView(popupView)
                    .setPositiveButton("Send", null)
                    .setNegativeButton("Cancel", null)
                    .create();
            alertDialog.setOnShowListener(dialogInterface -> {
                Button sendButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                sendButton.setOnClickListener(view2 -> {
                    String feedback = feedbackInput.getText().toString().trim();
                    String errorMsg = ValidatorUtils.validFeedback(feedback);
                    if (errorMsg != null) {
                        feedbackInputLayout.setError(errorMsg);
                    } else {
                        // no problems so go ahead and send
                        alertDialog.dismiss();
                        sendFeedback(feedback);
                    }
                });
            });
            alertDialog.show();
        });
        return view;
    }

    @Override
    public void hideAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        hideAllDialogs();
        super.onPause();
    }

    private void sendFeedback(String feedback) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M-d-yyyy, HH:mm:ss z", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
        String feedbackTime = simpleDateFormat.format(new Date(System.currentTimeMillis()));

        AndroidUtils.showLoadingDialog(loadingDialog, "Sending...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<String> resultStatus = this.userRepository.sendFeedback(feedback, feedbackTime);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    Toast.makeText(getContext(), "Feedback successfully sent. Thank you!", Toast.LENGTH_LONG).show();
                } else {
                    AndroidUtils.showErrorDialog("Send Feedback Error", resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }
}
