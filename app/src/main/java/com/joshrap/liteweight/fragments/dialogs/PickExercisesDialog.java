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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.Spinner;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

@SuppressLint("NotifyDataSetChanged")
public class PickExercisesDialog extends DialogFragment {
    private static final String TITLE = "title";
    private static final String POSITIVE_BTN = "positive_btn";
    private static final String NEGATIVE_BTN = "negative_btn";

    private PickExercisesDialog.Listener listener;
    private CreateExerciseDialog createExerciseDialog;
    private RecyclerView pickExerciseRecyclerView;
    private String selectedFocus;
    private HashMap<String, List<OwnedExercise>> focusToExercise;
    private boolean isSearchingExercises;
    private AddExerciseAdapter addExerciseAdapter;
    private EditText searchExerciseInput;
    private List<OwnedExercise> exercisesToAdd;

    private final String AllFocus = "All";

    @Inject
    CurrentUserModule currentUserModule;

    public interface Listener {
        void submit(List<OwnedExercise> pickedExercises);

        void exerciseCreated(OwnedExercise createdExercise);
    }

    public static final class Builder {
        private String title = "";
        private PickExercisesDialog.Listener listener = null;

        public PickExercisesDialog.Builder title(@NonNull String title) {
            this.title = title;
            return this;
        }

        public PickExercisesDialog.Builder listener(@Nullable PickExercisesDialog.Listener listener) {
            this.listener = listener;
            return this;
        }

        public PickExercisesDialog build() {
            PickExercisesDialog fragment = new PickExercisesDialog();

            Bundle args = new Bundle();
            args.putString(TITLE, title);
            args.putString(POSITIVE_BTN, "Save");
            args.putString(NEGATIVE_BTN, "Cancel");
            fragment.setArguments(args);

            fragment.listener = listener;
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
        exercisesToAdd = new ArrayList<>();

        String title = args.getString(TITLE, "");
        String positiveBtn = args.getString(POSITIVE_BTN, "Replace");
        String negativeBtn = args.getString(NEGATIVE_BTN, "Cancel");

        View popupView = getLayoutInflater().inflate(R.layout.popup_pick_exercise, null);
        pickExerciseRecyclerView = popupView.findViewById(R.id.pick_exercises_recycler_view);
        Spinner focusSpinner = popupView.findViewById(R.id.focus_spinner);

        focusToExercise = new HashMap<>();
        List<String> focusList = new ArrayList<>(Variables.FOCUS_LIST);
        for (String focus : focusList) {
            focusToExercise.put(focus, new ArrayList<>());
        }

        for (OwnedExercise exercise : currentUserModule.getUser().getExercises()) {
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

        // albeit more verbose than SearchView, but this allows more granular control
        searchExerciseInput = popupView.findViewById(R.id.search_exercises_input);
        TextInputLayout searchExerciseInputLayout = popupView.findViewById(R.id.search_exercises_input_layout);
        ImageButton searchButton = popupView.findViewById(R.id.search_icon_button);

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
                addExerciseAdapter = new AddExerciseAdapter(sortedExercises);
                pickExerciseRecyclerView.setAdapter(addExerciseAdapter);
                pickExerciseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
                addExerciseAdapter.getFilter().filter(charSequence);
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
        // initially select first item from spinner, then always select the one the user last clicked. Note this auto calls the method to update exercises for this focus
        focusSpinner.setSelection((selectedFocus == null) ? 0 : focusList.indexOf(selectedFocus));

        return new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(popupView)
                .setPositiveButton(positiveBtn, (dialog, which) -> {
                    if (listener != null) {
                        listener.submit(exercisesToAdd);
                    }
                })
                .setNegativeButton(negativeBtn, (dialog, which) -> dialog.dismiss())
                .create();
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
        addExerciseAdapter = new AddExerciseAdapter(sortedExercises);
        pickExerciseRecyclerView.setAdapter(addExerciseAdapter);
        pickExerciseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void onExerciseCreated(OwnedExercise newExercise) {
        exercisesToAdd.add(newExercise);
        for (String focus : newExercise.getFocuses()) {
            if (focusToExercise.containsKey(focus)) {
                focusToExercise.get(focus).add(newExercise);
            }
        }

        updateExerciseChoices();
        listener.exerciseCreated(newExercise);
    }

    private void popupCreateExercise() {
        createExerciseDialog = new CreateExerciseDialog.Builder()
                .title("Create Exercise")
                .initialExerciseId(searchExerciseInput != null ? searchExerciseInput.getText().toString().trim() : "")
                .onSubmit(this::onExerciseCreated)
                .build();
        createExerciseDialog.show(getChildFragmentManager(), "create-exercise");
    }

    private class FocusSpinnerListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            selectedFocus = parent.getItemAtPosition(pos).toString();
            updateExerciseChoices(); // update choices for exercise based on this newly selected focus
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }

    private class AddExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
        private static final int FOOTER_VIEW = 1;

        class AddExerciseViewHolder extends RecyclerView.ViewHolder {
            private final CheckBox exerciseCheckbox;

            AddExerciseViewHolder(View itemView) {
                super(itemView);
                exerciseCheckbox = itemView.findViewById(R.id.exercise_checkbox);
            }
        }

        class FooterViewHolder extends RecyclerView.ViewHolder {
            private final Button createExerciseBtn;

            FooterViewHolder(View itemView) {
                super(itemView);
                createExerciseBtn = itemView.findViewById(R.id.create_exercise_btn);
            }
        }

        private final List<OwnedExercise> allExercises;
        private final List<OwnedExercise> displayList;

        AddExerciseAdapter(List<OwnedExercise> exercises) {
            this.allExercises = exercises;
            displayList = new ArrayList<>(this.allExercises);
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            if (viewType == FOOTER_VIEW) {
                View exerciseView = inflater.inflate(R.layout.exercise_not_found_footer, parent, false);
                return new AddExerciseAdapter.FooterViewHolder(exerciseView);
            } else {
                View exerciseView = inflater.inflate(R.layout.row_add_exercise, parent, false);
                return new AddExerciseAdapter.AddExerciseViewHolder(exerciseView);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == displayList.size()) {
                return FOOTER_VIEW;
            }

            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            if (displayList.isEmpty()) {
                // always want one item for the footer
                return 1;
            }
            return displayList.size() + 1;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof AddExerciseAdapter.AddExerciseViewHolder) {
                AddExerciseAdapter.AddExerciseViewHolder viewHolder = (AddExerciseAdapter.AddExerciseViewHolder) holder;
                final OwnedExercise ownedExercise = displayList.get(position);
                CheckBox exerciseCheckbox = viewHolder.exerciseCheckbox;
                exerciseCheckbox.setText(ownedExercise.getName());
                boolean isChecked = exercisesToAdd.stream().anyMatch(x -> x.getId().equals(ownedExercise.getId()));
                exerciseCheckbox.setChecked(isChecked);

                exerciseCheckbox.setOnClickListener(v -> {
                    if (exerciseCheckbox.isChecked()) {
                        exercisesToAdd.add(ownedExercise);
                    } else {
                        exercisesToAdd.remove(ownedExercise);
                    }
                });
            } else if (holder instanceof AddExerciseAdapter.FooterViewHolder) {
                AddExerciseAdapter.FooterViewHolder viewHolder = (AddExerciseAdapter.FooterViewHolder) holder;
                viewHolder.createExerciseBtn.setOnClickListener(view -> popupCreateExercise());
            }
        }

        @Override
        public Filter getFilter() {
            return exerciseSearchFilter;
        }

        private final Filter exerciseSearchFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<OwnedExercise> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(allExercises);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (OwnedExercise ownedExercise : allExercises) {
                        if (ownedExercise.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(ownedExercise);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                displayList.clear();
                displayList.addAll((List<? extends OwnedExercise>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}
