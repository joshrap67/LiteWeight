package com.joshrap.liteweight.fragments;

import static com.joshrap.liteweight.utils.ValidatorUtils.passwordNotMatchingMsg;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;

import javax.inject.Inject;

public class ChangePasswordFragment extends Fragment {

    private EditText newPasswordInput;
    private EditText confirmNewPasswordInput;
    private EditText existingPasswordInput;
    private TextInputLayout existingPasswordLayout;
    private TextInputLayout newPasswordLayout;
    private TextInputLayout confirmNewPasswordLayout;

    @Inject
    AlertDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Injector.getInjector(getContext()).inject(this);

        View view = inflater.inflate(R.layout.fragment_change_password, container, false);
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        ((MainActivity) requireActivity()).toggleBackButton(true);
        ((MainActivity) requireActivity()).updateToolbarTitle(Variables.CHANGE_PASSWORD);

        Button saveButton = view.findViewById(R.id.save_btn);

        existingPasswordInput = view.findViewById(R.id.existing_password_input);
        existingPasswordLayout = view.findViewById(R.id.existing_password_input_layout);
        newPasswordInput = view.findViewById(R.id.new_password_input);
        newPasswordLayout = view.findViewById(R.id.new_password_input_layout);
        confirmNewPasswordInput = view.findViewById(R.id.confirm_new_password_input);
        confirmNewPasswordLayout = view.findViewById(R.id.confirm_new_password_input_layout);

        existingPasswordInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(existingPasswordLayout));

        newPasswordInput.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            if (v.hasFocus()) {
                String errorMessage = ValidatorUtils.validNewPassword(newPasswordInput.getText().toString().trim());
                newPasswordLayout.setError(errorMessage);
            }
        });

        AndroidUtils.setPasswordRequirementsWatcher(newPasswordInput, newPasswordLayout, confirmNewPasswordInput, confirmNewPasswordLayout);

        confirmNewPasswordInput.setOnKeyListener((View v, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // if all valid input, try to sign up after user hits enter button
                if (validInput()) {
                    changePassword();
                }
                return true;
            }
            return false;
        });

        saveButton.setOnClickListener(v -> {
            if (validInput()) {
                changePassword();
            }
        });
        return view;

    }

    private void changePassword() {
        hideKeyboard();
        AndroidUtils.showLoadingDialog(loadingDialog, "Changing password...");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), existingPasswordInput.getText().toString().trim());

        user.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {
            if (reAuthTask.isSuccessful()) {
                user.updatePassword(newPasswordInput.getText().toString().trim()).addOnCompleteListener(updatePwTask -> {
                    if (updatePwTask.isSuccessful()) {
                        Toast.makeText(getContext(), "Password successfully reset.", Toast.LENGTH_SHORT).show();
                        ((MainActivity) requireActivity()).finishFragment();
                    } else {
                        AndroidUtils.showErrorDialog("There was a problem changing your password.", getContext());
                    }
                    loadingDialog.dismiss();
                });
            } else {
                loadingDialog.dismiss();
                AndroidUtils.showErrorDialog("Invalid credentials.", getContext());
            }
        });
    }

    private boolean validInput() {
        boolean validInput = true;
        String existingPassword = existingPasswordInput.getText().toString().trim();
        if (existingPassword.isEmpty()) {
            existingPasswordLayout.setError("Required");
            existingPasswordLayout.startAnimation(AndroidUtils.shakeError(2));
            validInput = false;
        }
        String passwordErrorMsg = ValidatorUtils.validNewPassword(newPasswordInput.getText().toString().trim());
        if (passwordErrorMsg != null) {
            newPasswordLayout.setError(passwordErrorMsg);
            newPasswordLayout.startAnimation(AndroidUtils.shakeError(2));
            validInput = false;
        }
        String passwordConfirmErrorMsg = ValidatorUtils.validNewPassword(confirmNewPasswordInput.getText().toString().trim());
        if (passwordConfirmErrorMsg != null) {
            confirmNewPasswordLayout.setError(passwordConfirmErrorMsg);
            confirmNewPasswordLayout.startAnimation(AndroidUtils.shakeError(2));
            validInput = false;
        }
        // make sure that the passwords match assuming they are actually valid
        if (passwordsDoNotMatch()) {
            newPasswordLayout.setError(passwordNotMatchingMsg);
            newPasswordLayout.startAnimation(AndroidUtils.shakeError(2));
            confirmNewPasswordLayout.setError(passwordNotMatchingMsg);
            confirmNewPasswordLayout.startAnimation(AndroidUtils.shakeError(2));
            validInput = false;
        }

        return validInput;
    }

    private boolean passwordsDoNotMatch() {
        String password = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmNewPasswordInput.getText().toString().trim();
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            return false;
        } else {
            return !password.equals(confirmPassword);
        }
    }

    private void hideKeyboard() {
        FragmentActivity activity = requireActivity();
        if (activity.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

}