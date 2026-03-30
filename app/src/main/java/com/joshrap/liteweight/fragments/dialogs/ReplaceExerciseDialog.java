package com.joshrap.liteweight.fragments.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.managers.CurrentUserModule;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.models.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SuppressLint("NotifyDataSetChanged")
public class ReplaceExerciseDialog extends DialogFragment {
    private static final String TITLE = "title";
    private static final String POSITIVE_BTN = "positive_btn";
    private static final String NEGATIVE_BTN = "negative_btn";
    private static final String INITIAL_EXERCISE_ID = "initial_exercise_id";

    private Callbacks callbacks;
    private OwnedExercise selectedExercise;
    private HashMap<String, List<OwnedExercise>> focusToExercise;
    private EditText searchExerciseInput;
    private boolean isSearchingExercises;
    private String selectedFocus;
    private SelectExerciseAdapter adapter;
    private CreateExerciseDialog createExerciseDialog;
    private RecyclerView exerciseRecyclerView;

    private final String AllFocus = "All";

    @Inject
    CurrentUserModule currentUserModule;

    public interface Callbacks {
        void submit(OwnedExercise ownedExercise);

        void exerciseCreated(OwnedExercise exercise);
    }

    public static final class Builder {
        private String title = "";
        private String initialExerciseId;
        private Callbacks callbacks = null;

        public Builder title(@NonNull String title) {
            this.title = title;
            return this;
        }

        public Builder initialExerciseId(@NonNull String initialExerciseId) {
            this.initialExerciseId = initialExerciseId;
            return this;
        }

        public Builder callbacks(@Nullable Callbacks callbacks) {
            this.callbacks = callbacks;
            return this;
        }

        public ReplaceExerciseDialog build() {
            ReplaceExerciseDialog fragment = new ReplaceExerciseDialog();

            Bundle args = new Bundle();
            args.putString(TITLE, title);
            args.putString(POSITIVE_BTN, "Save");
            args.putString(NEGATIVE_BTN, "Cancel");
            args.putString(INITIAL_EXERCISE_ID, initialExerciseId);
            fragment.setArguments(args);

            fragment.callbacks = callbacks;
            return fragment;
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (createExerciseDialog != null && createExerciseDialog.isVisible()) {
            createExerciseDialog.dismiss();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Injector.getInjector(requireContext()).inject(this);

        Bundle args = requireArguments();
        User user = currentUserModule.getUser();
        List<OwnedExercise> allExercises = user.getExercises().stream().map(OwnedExercise::new).sorted().collect(Collectors.toList());

        String title = args.getString(TITLE, "");
        String positiveBtn = args.getString(POSITIVE_BTN, "Save");
        String negativeBtn = args.getString(NEGATIVE_BTN, "Cancel");
        String initialExerciseId = args.getString(INITIAL_EXERCISE_ID);
        selectedExercise = allExercises.stream().filter(x -> x.getId().equals(initialExerciseId)).findFirst().orElse(null);

        View view = getLayoutInflater().inflate(R.layout.popup_replace_exercise, null);
        exerciseRecyclerView = view.findViewById(R.id.pick_exercise_list_view);
        Spinner focusSpinner = view.findViewById(R.id.focus_spinner);

        LinearLayoutManager weekLayoutManager = new LinearLayoutManager(getActivity());
        adapter = new SelectExerciseAdapter(allExercises, new ArrayList<>(allExercises));
        exerciseRecyclerView.setAdapter(adapter);
        exerciseRecyclerView.setLayoutManager(weekLayoutManager);
        exerciseRecyclerView.scrollToPosition(allExercises.indexOf(selectedExercise));

        focusToExercise = new HashMap<>();
        List<String> focusList = new ArrayList<>(Variables.FOCUS_LIST);
        for (String focus : focusList) {
            focusToExercise.put(focus, new ArrayList<>());
        }

        for (OwnedExercise exercise : allExercises) {
            List<String> focusesOfExercise = exercise.getFocuses();
            for (String focus : focusesOfExercise) {
                if (!focusToExercise.containsKey(focus)) {
                    // focus somehow hasn't been added before
                    focusList.add(focus);
                    focusToExercise.put(focus, new ArrayList<>());
                }
                focusToExercise.get(focus).add(exercise);
            }
        }

        searchExerciseInput = view.findViewById(R.id.search_exercises_input);
        TextInputLayout searchExerciseInputLayout = view.findViewById(R.id.search_exercises_input_layout);
        ImageButton searchButton = view.findViewById(R.id.search_icon_button);

        searchButton.setOnClickListener(v -> {
            isSearchingExercises = !isSearchingExercises;
            if (isSearchingExercises) {
                searchButton.setImageResource(R.drawable.close_icon);

                // populate the list view with all exercises
                ArrayList<OwnedExercise> sortedExercises = new ArrayList<>();
                for (String focus : focusToExercise.keySet()) {
                    for (OwnedExercise exercise : focusToExercise.get(focus)) {
                        if (!sortedExercises.contains(exercise)) {
                            sortedExercises.add(exercise);
                        }
                    }
                }
                Collections.sort(sortedExercises);
                adapter.updateDisplayExercises(sortedExercises);

                focusSpinner.setVisibility(View.INVISIBLE);
                searchExerciseInputLayout.setVisibility(View.VISIBLE);
                searchExerciseInput.requestFocus();

                // android is so beautiful. Show keyboard after requesting focus
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchExerciseInput, 0);
            } else {
                // reset all search views
                searchExerciseInput.clearFocus();

                // can't use shared hide keyboard method since this is in an alertdialog apparently
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchExerciseInput.getWindowToken(), 0);

                searchButton.setImageResource(R.drawable.search_icon);

                focusSpinner.setVisibility(View.VISIBLE);
                searchExerciseInputLayout.setVisibility(View.INVISIBLE);
                updateExerciseChoices();
            }
        });

        searchExerciseInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchExerciseInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        focusList.sort(String.CASE_INSENSITIVE_ORDER);
        focusList.add(0, AllFocus);

        ArrayAdapter<String> focusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, focusList);
        focusSpinner.setAdapter(focusAdapter);
        focusSpinner.setOnItemSelectedListener(new FocusSpinnerListener());
        // initially select first item from spinner. Note this auto calls the method to update exercises for this focus
        focusSpinner.setSelection(0);
        exerciseRecyclerView.scrollToPosition(adapter.displayedExercises.indexOf(selectedExercise));

        return new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(view)
                .setPositiveButton(positiveBtn, (dialog, which) -> {
                    if (callbacks != null) {
                        callbacks.submit(selectedExercise);
                    }
                })
                .setNegativeButton(negativeBtn, (dialog, which) -> dialog.dismiss())
                .create();
    }

    private void onExerciseSelected(OwnedExercise exercise) {
        selectedExercise = exercise;
        adapter.notifyDataSetChanged();
    }

    private void updateExerciseChoices() {
        List<OwnedExercise> sortedExercises = new ArrayList<>();
        if (selectedFocus.equals(AllFocus)) {
            Set<OwnedExercise> ownedExercisesSet = new HashSet<>();
            for (String focus : focusToExercise.keySet()) {
                List<OwnedExercise> exercises = focusToExercise.get(focus);
                ownedExercisesSet.addAll(exercises);
            }
            sortedExercises.addAll(ownedExercisesSet);
        } else {
            sortedExercises.addAll(focusToExercise.get(selectedFocus));
        }
        Collections.sort(sortedExercises);
        adapter.updateDisplayExercises(sortedExercises);
        exerciseRecyclerView.scrollToPosition(0);
    }

    private class SelectExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int FOOTER_VIEW = 1;

        public class ViewHolder extends RecyclerView.ViewHolder {
            final TextView exerciseTv;

            ViewHolder(View itemView) {
                super(itemView);
                exerciseTv = itemView.findViewById(android.R.id.text1);
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {
            private final Button createExerciseBtn;

            FooterViewHolder(View itemView) {
                super(itemView);
                createExerciseBtn = itemView.findViewById(R.id.create_exercise_btn);
            }
        }

        private final List<OwnedExercise> allExercises;
        private final List<OwnedExercise> displayedExercises;


        public SelectExerciseAdapter(@NonNull List<OwnedExercise> allExercises, @NonNull List<OwnedExercise> displayedExercises) {
            this.allExercises = allExercises;
            this.displayedExercises = displayedExercises;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            if (viewType == FOOTER_VIEW) {
                View exerciseView = inflater.inflate(R.layout.exercise_not_found_footer, parent, false);
                return new SelectExerciseAdapter.FooterViewHolder(exerciseView);
            } else {
                View focusView = inflater.inflate(android.R.layout.simple_list_item_activated_1, parent, false);
                return new SelectExerciseAdapter.ViewHolder(focusView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof SelectExerciseAdapter.ViewHolder) {
                SelectExerciseAdapter.ViewHolder viewHolder = (SelectExerciseAdapter.ViewHolder) holder;
                OwnedExercise exercise = displayedExercises.get(position);
                TextView exerciseTv = viewHolder.exerciseTv;
                exerciseTv.setText(exercise.getName());

                exerciseTv.setOnClickListener(v -> onExerciseSelected(exercise));
                holder.itemView.setActivated(exercise.getId().equals(selectedExercise.getId()));
            } else if (holder instanceof SelectExerciseAdapter.FooterViewHolder) {
                SelectExerciseAdapter.FooterViewHolder viewHolder = (SelectExerciseAdapter.FooterViewHolder) holder;
                viewHolder.createExerciseBtn.setOnClickListener(view -> popupCreateExercise());
            }
        }

        public void updateDisplayExercises(List<OwnedExercise> displayedExercises) {
            this.displayedExercises.clear();
            this.displayedExercises.addAll(displayedExercises);
            notifyDataSetChanged();
        }

        private void onExerciseCreated(OwnedExercise exercise) {
            callbacks.exerciseCreated(exercise);

            for (String focus : exercise.getFocuses()) {
                if (focusToExercise.containsKey(focus)) {
                    focusToExercise.get(focus).add(exercise);
                }
            }
            updateExerciseChoices();

            // go ahead and select it since they just created it
            onExerciseSelected(exercise);
            exerciseRecyclerView.scrollToPosition(adapter.displayedExercises.indexOf(selectedExercise));
        }

        private void popupCreateExercise() {
            createExerciseDialog = new CreateExerciseDialog.Builder()
                    .title("Create Exercise")
                    .initialExerciseId(searchExerciseInput != null ? searchExerciseInput.getText().toString().trim() : "")
                    .onSubmit(this::onExerciseCreated)
                    .build();
            createExerciseDialog.show(getChildFragmentManager(), "create-exercise");
        }

        public void filter(String text) {
            this.displayedExercises.clear();
            if (text.isEmpty()) {
                this.displayedExercises.addAll(this.allExercises);
            } else {
                text = text.toLowerCase();
                for (OwnedExercise item : this.allExercises) {
                    if (item.getName().toLowerCase().contains(text)) {
                        this.displayedExercises.add(item);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (position == displayedExercises.size()) {
                return FOOTER_VIEW;
            }

            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            if (displayedExercises.isEmpty()) {
                // always want one item for the footer
                return 1;
            }
            return displayedExercises.size() + 1;
        }
    }

    private class FocusSpinnerListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            selectedFocus = parent.getItemAtPosition(pos).toString();
            updateExerciseChoices(); // update choices for exercise based on this newly selected focus
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }
}
