using AutoMapper;
using LiteWeightAPI.Api.ReceivedWorkouts.Responses;
using LiteWeightAPI.Domain.ReceivedWorkouts;

namespace LiteWeightAPI.AutoMapper;

public class ReceivedWorkoutMaps : Profile
{
	public ReceivedWorkoutMaps()
	{
		CreateMap<ReceivedWorkout, ReceivedWorkoutResponse>();
		CreateMap<ReceivedWorkoutDistinctExercise, ReceivedWorkoutDistinctExerciseResponse>();
		CreateMap<ReceivedRoutine, ReceivedRoutineResponse>();
		CreateMap<ReceivedWeek, ReceivedWeekResponse>();
		CreateMap<ReceivedDay, ReceivedDayResponse>();
		CreateMap<ReceivedExercise, ReceivedExerciseResponse>();
	}
}