using AutoMapper;
using LiteWeightAPI.Api.Self.Requests;
using LiteWeightAPI.Api.Self.Responses;
using LiteWeightAPI.Api.Users.Responses;
using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.ExtensionMethods;

namespace LiteWeightAPI.AutoMapper;

public class UserAndSelfMaps : Profile
{
	public UserAndSelfMaps()
	{
		CreateMap<WorkoutInfo, WorkoutInfoResponse>();
		CreateMap<Friend, FriendResponse>();
		CreateMap<FriendRequest, FriendRequestResponse>();
		CreateMap<ReceivedWorkoutInfo, ReceivedWorkoutInfoResponse>();
		CreateMap<UserSettings, UserSettingsResponse>();
		CreateMap<User, UserResponse>();
		CreateMap<User, SearchUserResponse>();
		
		CreateMap<CreateSelfRequest, CreateSelf>().Ignore(x => x.UserEmail).Ignore(x => x.UserId);
		CreateMap<UserSettingsResponse, SetSettings>().Ignore(x => x.UserId);
	}
}