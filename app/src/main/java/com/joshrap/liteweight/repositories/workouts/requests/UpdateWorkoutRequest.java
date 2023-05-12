package com.joshrap.liteweight.repositories.workouts.requests;

import com.joshrap.liteweight.models.workout.Routine;
import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class UpdateWorkoutRequest extends BodyRequest {

   public int currentWeek;
   public int currentDay;
   public SetRoutineRequest routine;

   public UpdateWorkoutRequest(int currentWeek, int currentDay, Routine routine){
      this.currentWeek = currentWeek;
      this.currentDay = currentDay;
      this.routine = new SetRoutineRequest(routine);
   }
}
