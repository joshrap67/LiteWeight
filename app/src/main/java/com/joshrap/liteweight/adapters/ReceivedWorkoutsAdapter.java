package com.joshrap.liteweight.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.models.ReceivedWorkoutMeta;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReceivedWorkoutsAdapter extends RecyclerView.Adapter<ReceivedWorkoutsAdapter.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView workoutNameTV;
        TextView dateSentTV;
        TextView senderTV;

        ViewHolder(View itemView) {
            super(itemView);
            workoutNameTV = itemView.findViewById(R.id.workout_name_tv);
            dateSentTV = itemView.findViewById(R.id.date_sent_tv);
            senderTV = itemView.findViewById(R.id.sender_tv);
        }
    }

    private List<ReceivedWorkoutMeta> receivedWorkouts;

    public ReceivedWorkoutsAdapter(List<ReceivedWorkoutMeta> receivedWorkouts) {
        this.receivedWorkouts = receivedWorkouts;
    }


    @Override
    public ReceivedWorkoutsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View exerciseView = inflater.inflate(R.layout.row_received_workout, parent, false);
        return new ViewHolder(exerciseView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ReceivedWorkoutsAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        final ReceivedWorkoutMeta receivedWorkout = receivedWorkouts.get(position);

        final TextView workoutNameTV = holder.workoutNameTV;
        final TextView senderTV = holder.senderTV;
        final TextView dateSentTv = holder.dateSentTV;
        senderTV.setText(String.format("Sent by: %s", receivedWorkout.getSender()));

        DateFormat dateFormatInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);
        dateFormatInput.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = dateFormatInput.parse(receivedWorkout.getDateSent());

            // we have the date as a proper object, so now format it to the user's local timezone
            DateFormat dateFormatOutput = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH);
            dateFormatOutput.setTimeZone(TimeZone.getDefault());
            dateSentTv.setText(dateFormatOutput.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (!receivedWorkout.isSeen()) {
            // if unseen, make it underlined and bold to catch user's attention
            dateSentTv.setPaintFlags(dateSentTv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            dateSentTv.setTypeface(dateSentTv.getTypeface(), Typeface.BOLD);
        }
        workoutNameTV.setText(receivedWorkout.getWorkoutName());
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return receivedWorkouts.size();
    }
}