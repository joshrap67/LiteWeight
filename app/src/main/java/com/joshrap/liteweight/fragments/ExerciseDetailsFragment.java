package com.joshrap.liteweight.fragments;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.os.Looper.getMainLooper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.managers.CurrentUserModule;
import com.joshrap.liteweight.managers.SelfManager;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.Link;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.models.user.OwnedExerciseWorkout;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ExerciseUtils;
import com.joshrap.liteweight.utils.WeightUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class ExerciseDetailsFragment extends Fragment implements FragmentWithDialog {

    private AlertDialog alertDialog;
    private OwnedExercise exercise;
    private String exerciseId;
    private boolean metricUnits;
    private ClipboardManager clipboard;
    private TextView focusesTV;
    private final MutableLiveData<String> focusTitle = new MutableLiveData<>();

    @Inject
    AlertDialog loadingDialog;
    @Inject
    SelfManager selfManager;
    @Inject
    CurrentUserModule currentUserModule;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentActivity activity = requireActivity();
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Injector.getInjector(getContext()).inject(this);
        ((MainActivity) activity).toggleBackButton(true);
        ((MainActivity) activity).updateToolbarTitle(Variables.EXERCISE_DETAILS_TITLE);

        clipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
        if (this.getArguments() != null) {
            exerciseId = this.getArguments().getString(Variables.EXERCISE_ID);
        } else {
            return null;
        }

        User user = currentUserModule.getUser();
        exercise = new OwnedExercise(user.getExercise(exerciseId));
        metricUnits = user.getSettings().isMetricUnits();

        return inflater.inflate(R.layout.fragment_exercise_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<String> workoutList = new ArrayList<>(exercise.getWorkouts()).stream().map(OwnedExerciseWorkout::getWorkoutName).collect(Collectors.toList());
        List<String> selectedFocuses = new ArrayList<>(exercise.getFocuses());

        focusesTV = view.findViewById(R.id.focus_list_tv);
        focusTitle.setValue(ExerciseUtils.getFocusTitle(selectedFocuses));
        focusTitle.observe(getViewLifecycleOwner(), this::setFocusTextView);

        ImageButton exerciseOptionsBtn = view.findViewById(R.id.exercise_options_btn);
        PopupMenu dropDownMenu = getPopupMenu(exerciseOptionsBtn);
        exerciseOptionsBtn.setOnClickListener(v -> dropDownMenu.show());

        TextView exerciseNameTv = view.findViewById(R.id.exercise_name_tv);
        exerciseNameTv.setText(exercise.getName());

        TextView defaultsTv = view.findViewById(R.id.exercise_defaults_tv);
        double weight = WeightUtils.getConvertedWeight(metricUnits, exercise.getDefaultWeight());
        String formattedWeight = WeightUtils.getFormattedWeightWithUnits(weight, metricUnits);
        defaultsTv.setText(String.format("Default: %s %sx%s", formattedWeight, exercise.getDefaultSets(), exercise.getDefaultReps()));

        TextView workoutListTv = view.findViewById(R.id.workout_list_tv);
        if (workoutList.isEmpty()) {
            workoutListTv.setText(R.string.none);
        } else {
            workoutList.sort(Comparator.comparing(String::toLowerCase));
            StringBuilder workouts = getWorkoutsDisplay(workoutList);

            workoutListTv.setText(workouts.toString());
        }

        TextView notesTv = view.findViewById(R.id.exercise_notes_tv);
        notesTv.setText(exercise.getNotes());

        RecyclerView linksRecyclerView = view.findViewById(R.id.exercise_links_recycler_view);
        ExerciseLinkAdapter linksAdapter = new ExerciseLinkAdapter(exercise.getLinks());
        linksRecyclerView.setAdapter(linksAdapter);
        linksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private static StringBuilder getWorkoutsDisplay(List<String> workoutList) {
        StringBuilder workouts = new StringBuilder();
        int maxSize = 5; // only show 5 workouts
        if (workoutList.size() <= maxSize) {
            // don't append the "+ x more"
            for (int i = 0; i < workoutList.size(); i++) {
                // don't have comma at last entry
                workouts.append(workoutList.get(i)).append((i < workoutList.size() - 1) ? ", " : "");
            }
        } else {
            for (int i = 0; i < maxSize; i++) {
                // don't have comma at last entry
                workouts.append(workoutList.get(i)).append((i < maxSize - 1) ? ", " : "");
            }
            workouts.append("\n + ").append(workoutList.size() - maxSize).append(" more");
        }
        return workouts;
    }

    private void setFocusTextView(String title) {
        if (title == null) {
            focusesTV.setText(R.string.none);
        } else {
            focusesTV.setText(title);
        }
    }

    private PopupMenu getPopupMenu(ImageButton exerciseOptionsButton) {
        PopupMenu dropDownMenu = new PopupMenu(getContext(), exerciseOptionsButton);
        Menu menu = dropDownMenu.getMenu();
        final int editIndex = 0;
        final int deleteIndex = 1;

        menu.add(0, editIndex, 0, "Edit Exercise");
        menu.add(0, deleteIndex, 0, "Delete Exercise");

        dropDownMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case editIndex:
                    dropDownMenu.dismiss();
                    ((MainActivity) requireActivity()).goToEditExercise(exerciseId);
                    return true;
                case deleteIndex:
                    promptDelete();
                    return true;
            }
            return false;
        });
        return dropDownMenu;
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

    private void promptDelete() {
        // exercise name is italicized
        SpannableString span1 = new SpannableString("Are you sure you wish to permanently delete ");
        SpannableString span2 = new SpannableString(exercise.getName());
        SpannableString span3 = new SpannableString("?\n\nIf so, this exercise will be removed from ALL workouts that contain it.");
        span2.setSpan(new StyleSpan(Typeface.ITALIC), 0, span2.length(), 0);
        CharSequence title = TextUtils.concat(span1, span2, span3);

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Delete Exercise")
                .setMessage(title)
                .setPositiveButton("Yes", (dialog, which) -> deleteExercise())
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private void deleteExercise() {
        AndroidUtils.showLoadingDialog(loadingDialog, "Deleting...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.selfManager.deleteExercise(exerciseId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    ((MainActivity) requireActivity()).finishFragment();
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());

                }
            });
        });
    }

    private class ExerciseLinkAdapter extends RecyclerView.Adapter<ExerciseLinkAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView linkTv;
            final ImageButton copyLinkBtn;

            ViewHolder(View itemView) {
                super(itemView);
                linkTv = itemView.findViewById(R.id.link_tv);
                copyLinkBtn = itemView.findViewById(R.id.copy_link_icon_btn);
            }
        }

        private final List<Link> links;

        public ExerciseLinkAdapter(List<Link> links) {
            this.links = links;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View focusView = inflater.inflate(R.layout.row_exercise_link, parent, false);
            return new ViewHolder(focusView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Link link = links.get(position);
            TextView linkTv = holder.linkTv;
            ImageButton copyLinkBtn = holder.copyLinkBtn;
            String label = link.getUrl();
            if (link.getLabel() != null && !link.getLabel().isEmpty()) {
                label = link.getLabel();
            }

            // underline text
            SpannableString content = new SpannableString(label);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            linkTv.setText(content);

            linkTv.setOnClickListener(v -> {
                alertDialog = new AlertDialog.Builder(requireContext())
                        .setTitle("Launch Link")
                        .setMessage(R.string.launch_link_msg)
                        .setPositiveButton("Yes", (dialog, which) -> ExerciseUtils.launchLink(link.getUrl(), getContext()))
                        .setNegativeButton("No", null)
                        .create();
                alertDialog.show();
            });
            copyLinkBtn.setOnClickListener(v -> {
                clipboard.setPrimaryClip(new ClipData(ClipData.newPlainText("url", link.getUrl())));
                Toast.makeText(getContext(), "Link copied to clipboard.", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return links.size();
        }
    }
}
