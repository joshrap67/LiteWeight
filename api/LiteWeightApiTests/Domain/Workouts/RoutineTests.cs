using LiteWeightAPI.Domain.ReceivedWorkouts;
using LiteWeightAPI.Domain.Workouts;

namespace LiteWeightApiTests.Domain.Workouts;

public class RoutineTests : BaseTest
{
	[Fact]
	public void Should_Be_Created_From_Received_Routine()
	{
		var exerciseNameToId = new Dictionary<string, string>
		{
			{ "A", Fixture.Create<string>() },
			{ "B", Fixture.Create<string>() },
			{ "C", Fixture.Create<string>() },
			{ "D", Fixture.Create<string>() },
			{ "E", Fixture.Create<string>() }
		};
		var receivedRoutine = Fixture.Build<ReceivedRoutine>()
			.With(x => x.Weeks, new List<ReceivedWeek>
			{
				Fixture.Build<ReceivedWeek>().With(x => x.Days, new List<ReceivedDay>
				{
					Fixture.Build<ReceivedDay>().With(x => x.Exercises, new List<ReceivedExercise>
					{
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "A").Create(),
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "B").Create(),
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "C").Create()
					}).Create(),
					Fixture.Build<ReceivedDay>().With(x => x.Exercises, new List<ReceivedExercise>
					{
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "E").Create(),
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "B").Create()
					}).Create(),
					Fixture.Build<ReceivedDay>().With(x => x.Exercises, new List<ReceivedExercise>
					{
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "E").Create()
					}).Create()
				}).Create(),
				Fixture.Build<ReceivedWeek>().With(x => x.Days, new List<ReceivedDay>
				{
					Fixture.Build<ReceivedDay>().With(x => x.Exercises, new List<ReceivedExercise>
					{
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "A").Create(),
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "B").Create(),
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "C").Create(),
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "D").Create(),
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "E").Create()
					}).Create(),
					Fixture.Build<ReceivedDay>().With(x => x.Exercises, new List<ReceivedExercise>
					{
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "A").Create(),
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "B").Create()
					}).Create(),
					Fixture.Build<ReceivedDay>().With(x => x.Exercises, new List<ReceivedExercise>
					{
						Fixture.Build<ReceivedExercise>().With(x => x.ExerciseName, "D").Create()
					}).Create()
				}).Create()
			}).Create();

		var routine = new Routine(receivedRoutine, exerciseNameToId);
		Assert.Equal(receivedRoutine.Weeks.Sum(x => x.Days.Count), routine.TotalNumberOfDays);

		for (var i = 0; i < routine.Weeks.Count; i++)
		{
			for (var j = 0; j < routine.Weeks[i].Days.Count; j++)
			{
				var receivedRoutineDayTag = receivedRoutine.Weeks[i].Days[j].Tag;
				var routineDayTag = routine.Weeks[i].Days[j].Tag;
				Assert.Equal(receivedRoutineDayTag, routineDayTag);

				for (var k = 0; k < routine.Weeks[i].Days[j].Exercises.Count; k++)
				{
					var routineExerciseName = receivedRoutine.Weeks[i].Days[j].Exercises[k].ExerciseName;
					var exerciseId = routine.Weeks[i].Days[j].Exercises[k].ExerciseId;
					Assert.Equal(exerciseNameToId[routineExerciseName], exerciseId);
				}
			}
		}
	}
}