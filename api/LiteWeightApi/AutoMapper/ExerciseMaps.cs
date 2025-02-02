using AutoMapper;
using LiteWeightAPI.Api.Exercises.Requests;
using LiteWeightAPI.Api.Exercises.Responses;
using LiteWeightAPI.Commands.Exercises;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.ExtensionMethods;

namespace LiteWeightAPI.AutoMapper;

public class ExerciseMaps : Profile
{
	public ExerciseMaps()
	{
		CreateMap<SetExerciseRequest, CreateExercise>().Ignore(x => x.UserId);
		CreateMap<SetExerciseRequest, UpdateExercise>().Ignore(x => x.UserId).Ignore(x => x.ExerciseId);
		CreateMap<CreateExercise, OwnedExercise>().Ignore(x => x.Workouts).Ignore(x => x.Id);

		CreateMap<OwnedExercise, OwnedExerciseResponse>();
		CreateMap<OwnedExerciseWorkout, OwnedExerciseWorkoutResponse>();
	}
}