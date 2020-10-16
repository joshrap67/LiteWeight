package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReceivedWorkoutMeta implements Model {

    public static final String WORKOUT_NAME = "workoutName";
    public static final String DATE_SENT = "dateSent";
    public static final String SEEN = "seen";
    public static final String SENDER = "sender";
    public static final String MOST_FREQUENT_FOCUS = "mostFrequentFocus";
    public static final String TOTAL_DAYS = "totalDays";
    public static final String WORKOUT_ID = "receivedWorkoutId";

    private String workoutName;
    private String workoutId;
    private String dateSent;
    private boolean seen;
    private String sender;
    private Integer totalDays;
    private String mostFrequentFocus;

    public ReceivedWorkoutMeta(Map<String, Object> json) {
        this.workoutName = (String) json.get(WORKOUT_NAME);
        this.workoutId = (String) json.get(WORKOUT_ID);
        this.dateSent = (String) json.get(DATE_SENT);
        this.mostFrequentFocus = (String) json.get(MOST_FREQUENT_FOCUS);
        this.seen = (boolean) json.get(SEEN);
        this.sender = (String) json.get(SENDER);
        this.totalDays = (Integer) json.get(TOTAL_DAYS);
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        retVal.put(WORKOUT_NAME, this.workoutName);
        retVal.put(DATE_SENT, this.dateSent);
        retVal.put(SEEN, this.seen);
        retVal.put(MOST_FREQUENT_FOCUS, this.mostFrequentFocus);
        retVal.put(SENDER, this.sender);
        retVal.put(TOTAL_DAYS, this.totalDays);
        return retVal;
    }
}
