package com.joshrap.liteweight.messages.activitymessages;

import lombok.Getter;

public class TimerRestartMessage {

    @Getter
    private final long startTimeAbsolute; // in SI units of milliseconds (UNIX Timestamp)
    @Getter
    private final long timeRemaining; // in SI units of milliseconds

    public TimerRestartMessage(long startTimeAbsolute, long timeRemaining){
        this.startTimeAbsolute = startTimeAbsolute;
        this.timeRemaining = timeRemaining;
    }
}
