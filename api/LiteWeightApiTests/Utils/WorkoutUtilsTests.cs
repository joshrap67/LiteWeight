using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Utils;

namespace LiteWeightApiTests.Utils;

public class WorkoutUtilsTests : BaseTest
{
	[Theory]
	[InlineData(-1, 5, 5, 0, 4, 0)]
	[InlineData(-1, 5, 5, 4, 4, 3)]
	[InlineData(5, 5, 5, 0, 4, 0)]
	[InlineData(5, 5, 5, 4, 4, 3)]
	[InlineData(5, 5, 0, 0, 0, 0)]
	[InlineData(4, 5, 5, 4, 4, 3)]
	[InlineData(4, 6, 5, 4, 4, 3)]
	[InlineData(4, -1, 5, 4, 4, 3)]
	[InlineData(4, -1, 5, 0, 4, 0)]
	public void Should_Fix_Current_Day_And_Week(int currentWeek, int currentDay, int numberOfWeeks, int numberOfDays,
		int expectedCurrentWeek, int expectedCurrentDay)
	{
		var days = Enumerable.Range(0, numberOfDays)
			.Select(_ => Fixture.Create<RoutineDay>())
			.ToList();
		var weeks = Enumerable.Range(0, numberOfWeeks)
			.Select(_ => Fixture.Build<RoutineWeek>().With(x => x.Days, days).Create())
			.ToList();
		var routine = Fixture.Build<Routine>().With(x => x.Weeks, weeks).Create();
		var workout = Fixture.Build<Workout>().With(x => x.Routine, routine).Create();
		var workoutInfo = Fixture.Build<WorkoutInfo>()
			.With(x => x.CurrentWeek, currentWeek)
			.With(x => x.CurrentDay, currentDay)
			.Create();

		WorkoutUtils.FixCurrentDayAndWeek(workout, workoutInfo);
		Assert.Equal(expectedCurrentWeek, workoutInfo.CurrentWeek);
		Assert.Equal(expectedCurrentDay, workoutInfo.CurrentDay);
	}
	
	[Theory]
	[InlineData(4, 0, 5, 1, 4, 0)]
	[InlineData(4, 2, 5, 3, 4, 2)]
	public void Already_Valid_Current_Day_And_Week(int currentWeek, int currentDay, int numberOfWeeks, int numberOfDays,
		int expectedCurrentWeek, int expectedCurrentDay)
	{
		var days = Enumerable.Range(0, numberOfDays)
			.Select(_ => Fixture.Create<RoutineDay>())
			.ToList();
		var weeks = Enumerable.Range(0, numberOfWeeks)
			.Select(_ => Fixture.Build<RoutineWeek>().With(x => x.Days, days).Create())
			.ToList();
		var routine = Fixture.Build<Routine>().With(x => x.Weeks, weeks).Create();
		var workout = Fixture.Build<Workout>().With(x => x.Routine, routine).Create();
		var workoutInfo = Fixture.Build<WorkoutInfo>()
			.With(x => x.CurrentWeek, currentWeek)
			.With(x => x.CurrentDay, currentDay)
			.Create();

		WorkoutUtils.FixCurrentDayAndWeek(workout, workoutInfo);
		Assert.Equal(expectedCurrentWeek, workoutInfo.CurrentWeek);
		Assert.Equal(expectedCurrentDay, workoutInfo.CurrentDay);
	}
}