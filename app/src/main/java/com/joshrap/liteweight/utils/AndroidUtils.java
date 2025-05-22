package com.joshrap.liteweight.utils;

import static com.joshrap.liteweight.utils.ValidatorUtils.passwordNotMatchingMsg;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.Strings;
import com.google.firebase.FirebaseApp;
import com.joshrap.liteweight.BuildConfig;
import com.joshrap.liteweight.imports.BackendConfig;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.workout.RoutineExercise;

public class AndroidUtils {

    /**
     * Returns a TextWatcher that detects when error is present and hides it once user starts typing.
     *
     * @param layout layout that contains a given EditText
     * @return TextWatcher that does the detection.
     */
    public static TextWatcher hideErrorTextWatcher(TextInputLayout layout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout.isErrorEnabled()) {
                    layout.setErrorEnabled(false);
                    layout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    public static void setWeightTextWatcher(final EditText input, final RoutineExercise exercise, boolean metricUnits) {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String weight = input.getText().toString().trim();
                if (!weight.isEmpty() && weight.length() <= Variables.MAX_WEIGHT_DIGITS) {
                    double newWeight = Double.parseDouble(weight);
                    if (metricUnits) {
                        // convert back to imperial if in metric since weight is stored in imperial on backend
                        newWeight = WeightUtils.metricWeightToImperial(newWeight);
                    }
                    exercise.setWeight(newWeight);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        input.addTextChangedListener(textWatcher);
    }

    public static void setRepsTextWatcher(final EditText input, final RoutineExercise exercise) {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String reps = input.getText().toString().trim();
                if (!reps.isEmpty() && reps.length() <= Variables.MAX_REPS_DIGITS) {
                    int newReps = Integer.parseInt(reps);
                    exercise.setReps(newReps);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        input.addTextChangedListener(textWatcher);
    }

    public static void setSetsTextWatcher(final EditText input, final RoutineExercise exercise) {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String sets = input.getText().toString().trim();
                if (!sets.isEmpty() && sets.length() <= Variables.MAX_SETS_DIGITS) {
                    int newSets = Integer.parseInt(sets);
                    exercise.setSets(newSets);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        input.addTextChangedListener(textWatcher);
    }

    public static void setInstructionsTextWatcher(final EditText input, final RoutineExercise exercise) {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String instructions = input.getText().toString().trim();
                if (instructions.length() <= Variables.MAX_INSTRUCTIONS_LENGTH) {
                    exercise.setInstructions(instructions);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        input.addTextChangedListener(textWatcher);
    }

    public static void setPasswordRequirementsWatcher(EditText input1, TextInputLayout layout1, EditText input2, TextInputLayout layout2) {
        TextWatcher watcher1 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password1 = input1.getText().toString().trim();
                String password2 = input2.getText().toString().trim();
                String errorMessage = ValidatorUtils.validNewPassword(input1.getText().toString().trim());
                layout1.setError(errorMessage);

                if (passwordsDoNotMatch(password1, password2) && Strings.isNullOrEmpty(errorMessage)) {
                    layout1.setError(passwordNotMatchingMsg);
                } else if (passwordsDoNotMatch(password1, password2)) {
                    layout1.setError(errorMessage + passwordNotMatchingMsg);
                } else {
                    layout1.setError(errorMessage);
                    layout1.setErrorEnabled(errorMessage != null);
                }

                if (password2.isEmpty()) return;

                String confirmPasswordErrorMessage = ValidatorUtils.validNewPassword(password2);
                if (passwordsDoNotMatch(password1, password2) && confirmPasswordErrorMessage == null) {
                    layout2.setError(passwordNotMatchingMsg);
                } else if (passwordsDoNotMatch(password1, password2)) {
                    layout2.setError(confirmPasswordErrorMessage + passwordNotMatchingMsg);
                } else {
                    layout2.setError(confirmPasswordErrorMessage);
                    layout2.setErrorEnabled(confirmPasswordErrorMessage != null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        input1.addTextChangedListener(watcher1);

        TextWatcher watcher2 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password1 = input1.getText().toString().trim();
                String password2 = input2.getText().toString().trim();
                String errorMessage = ValidatorUtils.validNewPassword(input2.getText().toString().trim());
                layout2.setError(errorMessage);

                if (passwordsDoNotMatch(password1, password2) && Strings.isNullOrEmpty(errorMessage)) {
                    layout2.setError(passwordNotMatchingMsg);
                } else if (passwordsDoNotMatch(password1, password2)) {
                    layout2.setError(errorMessage + passwordNotMatchingMsg);
                } else {
                    layout2.setError(errorMessage);
                    layout2.setErrorEnabled(errorMessage != null);
                }

                if (password1.isEmpty()) return;

                String confirmPasswordErrorMessage = ValidatorUtils.validNewPassword(password1);
                if (passwordsDoNotMatch(password1, password2) && confirmPasswordErrorMessage == null) {
                    layout1.setError(passwordNotMatchingMsg);
                } else if (passwordsDoNotMatch(password1, password2)) {
                    layout1.setError(confirmPasswordErrorMessage + passwordNotMatchingMsg);
                } else {
                    layout1.setError(confirmPasswordErrorMessage);
                    layout1.setErrorEnabled(confirmPasswordErrorMessage != null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        input2.addTextChangedListener(watcher2);
    }

    private static boolean passwordsDoNotMatch(String password1, String password2) {
        if (password1.isEmpty() || password2.isEmpty()) {
            return false;
        } else {
            return !password1.equals(password2);
        }
    }


    public static void showLoadingDialog(AlertDialog loadingDialog, String message) {
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }

    public static TranslateAnimation shakeError(int shakeCycles) {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(350);
        shake.setInterpolator(new CycleInterpolator(shakeCycles));
        return shake;
    }

    public static void showErrorDialog(String msg, Context context) {
        if (context == null) {
            return;
        }
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(msg)
                .setPositiveButton("Ok", null)
                .create();
        alertDialog.show();
    }

    public static void initializeApp(Context context) {
        FirebaseApp.initializeApp(context);
        if (BuildConfig.FLAVOR.equalsIgnoreCase("sandbox")) {
            //http://10.0.2.2:5174/ for localhost. then remember to allow plaintext in android manifest
            BackendConfig.googleSignInClientId = "929931641278-88gnp9fmll3d8h2r557shbv0cij1crk9.apps.googleusercontent.com";
            BackendConfig.profilePictureBaseUrl = "https://storage.googleapis.com/liteweight-sandbox-profile-pictures/";
            BackendConfig.baseUrl = "https://liteweightapi-929931641278.us-central1.run.app/";
        } else if (BuildConfig.FLAVOR.equalsIgnoreCase("prod")) {
            BackendConfig.googleSignInClientId = "990471046455-g5s0mqhm6sm3b66ki7fle6n2ud0msim1.apps.googleusercontent.com";
            BackendConfig.profilePictureBaseUrl = "https://storage.googleapis.com/liteweight-profile-pictures/";
            BackendConfig.baseUrl = "https://liteweightapi-bxtag6fcfa-uc.a.run.app/";
        }
    }
}
