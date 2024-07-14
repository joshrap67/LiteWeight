package com.joshrap.liteweight.messages.activitymessages;

import lombok.Getter;

@Getter
public class TimerRestartMessage {

    private final long startTimeAbsolute; // in SI units of milliseconds (UNIX Timestamp)
    private final long timeRemaining; // in SI units of milliseconds

    public TimerRestartMessage(long startTimeAbsolute, long timeRemaining){
        this.startTimeAbsolute = startTimeAbsolute;
        this.timeRemaining = timeRemaining;
    }
}
