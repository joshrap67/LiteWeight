package com.joshrap.liteweight.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.adapters.ReceivedWorkoutsAdapter;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.ReceivedWorkoutMeta;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.network.repos.WorkoutRepository;
import com.joshrap.liteweight.widgets.ErrorDialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class ReceivedWorkoutsFragment extends Fragment implements FragmentWithDialog {
    private User user;
    private List<ReceivedWorkoutMeta> receivedWorkouts;
    private int currentBatch;
    private ProgressBar loadingIcon;
    private RecyclerView receivedWorkoutsRecyclerView;
    private ImageButton markAllReceivedWorkoutsSeen;
    private TextView emptyView;
    @Inject
    WorkoutRepository workoutRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.RECEIVED_WORKOUTS_TITLE);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);
        Injector.getInjector(getContext()).inject(this);
        // TODO injection or view model for these two???
        user = Globals.user;
        currentBatch = 0;
        receivedWorkouts = new ArrayList<>();
        final View view = inflater.inflate(R.layout.fragment_received_workouts, container, false);

        loadingIcon = view.findViewById(R.id.loading_icon);
        loadingIcon.setVisibility(View.VISIBLE);
        receivedWorkoutsRecyclerView = view.findViewById(R.id.recycler_view);
        receivedWorkoutsRecyclerView.setVisibility(View.GONE);
        emptyView = view.findViewById(R.id.empty_view);
        emptyView.setVisibility(View.GONE);
        markAllReceivedWorkoutsSeen = view.findViewById(R.id.mark_all_read_btn);
        markAllReceivedWorkoutsSeen.setVisibility(View.GONE);

        getReceivedWorkouts();
        return view;
    }

    private void getReceivedWorkouts() {
        loadingIcon.setVisibility(View.VISIBLE);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<List<ReceivedWorkoutMeta>> resultStatus = this.workoutRepository.getReceivedWorkouts(currentBatch);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (this.isResumed()) {
                    // not sure if this does what i think it does? But would prevent potential memory leaks
                    loadingIcon.setVisibility(View.GONE);
                    if (resultStatus.isSuccess()) {

                        receivedWorkouts = resultStatus.getData();
                        Collections.sort(receivedWorkouts, (r1, r2) -> {
                            // sanity sort based on received workout sent time
                            DateFormat dateFormattter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);
                            dateFormattter.setTimeZone(TimeZone.getTimeZone("UTC"));
                            int retVal = 0;
                            try {
                                Date date1 = dateFormattter.parse(r1.getDateSent());
                                Date date2 = dateFormattter.parse(r2.getDateSent());
                                retVal = date1 != null ? date2.compareTo(date1) : 0;
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return retVal;
                        });
                        boolean showMarkAllSeen = false;
                        for (ReceivedWorkoutMeta receivedWorkoutMeta : receivedWorkouts) {
                            if (!receivedWorkoutMeta.isSeen()) {
                                showMarkAllSeen = true;
                                break;
                            }
                        }
                        markAllReceivedWorkoutsSeen.setVisibility(showMarkAllSeen ? View.VISIBLE : View.GONE);
                        displayReceivedWorkouts();
                    } else {
                        ErrorDialog.showErrorDialog("Load Received Workouts Error", resultStatus.getErrorMessage(), getContext());
                    }
                }
            });
        });
    }


    private void displayReceivedWorkouts() {
        receivedWorkoutsRecyclerView.setVisibility(View.VISIBLE);
        ReceivedWorkoutsAdapter receivedWorkoutsAdapter = new ReceivedWorkoutsAdapter(receivedWorkouts);
        receivedWorkoutsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            // since google is stupid af and doesn't have a simple setEmptyView for recyclerView...
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }
        });
        receivedWorkoutsRecyclerView.setAdapter(receivedWorkoutsAdapter);
        receivedWorkoutsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        checkEmpty();
    }

    private void checkEmpty() {
        /*
            Used to check if there are any received workouts
         */
        emptyView.setVisibility(receivedWorkouts.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void hideAllDialogs() {

    }
}
