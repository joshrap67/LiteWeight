namespace LiteWeightAPI.Commands.Workouts;

public class SetRoutine
{
	public IList<SetRoutineWeek> Weeks { get; set; } = new List<SetRoutineWeek>();
}

public class SetRoutineWeek
{
	public IList<SetRoutineDay> Days { get; set; } = new List<SetRoutineDay>();
}

public class SetRoutineDay
{
	public string Tag { get; set; }
	public IList<SetRoutineExercise> Exercises { get; set; } = new List<SetRoutineExercise>();
}

public class SetRoutineExercise
{
	public string ExerciseId { get; set; }
	public bool Completed { get; set; }
	public double Weight { get; set; }
	public int Sets { get; set; }
	public int Reps { get; set; }
	public string Details { get; set; }
}