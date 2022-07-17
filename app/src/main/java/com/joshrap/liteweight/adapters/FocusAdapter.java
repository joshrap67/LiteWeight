package com.joshrap.liteweight.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.utils.ExerciseUtils;

import java.util.List;

public class FocusAdapter extends RecyclerView.Adapter<FocusAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox focusCheckbox;

        ViewHolder(View itemView) {
            super(itemView);
            focusCheckbox = itemView.findViewById(R.id.focus_checkbox);
        }
    }

    private final List<String> focuses;
    private final List<String> selectedFocuses;
    private final MutableLiveData<String> focusTitle;

    public FocusAdapter(List<String> focuses, List<String> selectedFocuses, MutableLiveData<String> focusTitle) {
        this.focuses = focuses;
        this.selectedFocuses = selectedFocuses;
        this.focusTitle = focusTitle;
    }

    @NonNull
    @Override
    public FocusAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View focusView = inflater.inflate(R.layout.row_add_focus, parent, false);
        return new ViewHolder(focusView);
    }

    @Override
    public void onBindViewHolder(FocusAdapter.ViewHolder holder, int position) {
        String focus = focuses.get(position);
        CheckBox focusCheckbox = holder.focusCheckbox;
        focusCheckbox.setText(focus);
        focusCheckbox.setChecked(selectedFocuses.contains(focus));
        focusCheckbox.setOnClickListener(v -> {
            if (!focusCheckbox.isChecked()) {
                selectedFocuses.remove(focus);
            } else {
                selectedFocuses.add(focus);
            }
            if (focusTitle != null) {
                focusTitle.setValue(ExerciseUtils.getFocusTitle(selectedFocuses));
            }
        });
    }

    @Override
    public int getItemCount() {
        return focuses.size();
    }
}
