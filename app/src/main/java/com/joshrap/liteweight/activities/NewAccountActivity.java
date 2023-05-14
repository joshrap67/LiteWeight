package com.joshrap.liteweight.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.managers.UserManager;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.providers.CurrentUserAndWorkoutProvider;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class NewAccountActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private boolean metricUnits;

    @Inject
    UserManager userManager;
    @Inject
    CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Injector.getInjector(this).inject(this);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            launchSignInActivity();// todo pass in error message
            finish();
            return;
        }

        setContentView(R.layout.activity_create_account);

        Button logoutButton = findViewById(R.id.log_out_btn);
        Button createUserButton = findViewById(R.id.create_user_btn);
        TextView signedInAs = findViewById(R.id.signed_in_as_tv);
        signedInAs.setText(String.format("%s %s", getString(R.string.signed_in_as), user.getEmail()));

        SwitchCompat metricSwitch = findViewById(R.id.metric_switch);

        LinearLayout metricLayout = findViewById(R.id.metric_container);
        metricLayout.setOnClickListener(view1 -> metricSwitch.performClick());
        metricSwitch.setChecked(metricUnits);
        metricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> metricUnits = isChecked);

        EditText usernameInput = findViewById(R.id.username_input);
        TextInputLayout usernameInputLayout = findViewById(R.id.username_input_layout);
        usernameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_URL_LENGTH)});
        usernameInput.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String errorMsg = ValidatorUtils.validNewUsername(usernameInput.getText().toString().trim());
                if (errorMsg == null) {
                    usernameInputLayout.setError(null);
                    return true;
                } else {
                    usernameInputLayout.setError(errorMsg);
                }
            }
            return false;
        });
        usernameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(usernameInputLayout));

        createUserButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String usernameErrorMsg = ValidatorUtils.validNewUsername(username);
            if (usernameErrorMsg != null) {
                usernameInputLayout.setError(usernameErrorMsg);
                usernameInputLayout.startAnimation(AndroidUtils.shakeError(2));
                return;
            }

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                Result<User> userResult = this.userManager.createUser(username, metricUnits);
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    if (userResult.isSuccess() && userResult.getData() != null) {
                        launchMainActivity();
                    } else {
                        AndroidUtils.showErrorDialog(userResult.getErrorMessage(), this);
                    }
                });
            });
        });


        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            launchSignInActivity();
        });


        if (getIntent().getExtras() != null) {
            String errorMessage = getIntent().getExtras().getString(Variables.ERROR_MESSAGE);
            if (errorMessage != null) {
                AndroidUtils.showErrorDialog(errorMessage, this);
            }
        }
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void launchSignInActivity() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
}
