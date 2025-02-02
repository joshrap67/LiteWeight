using AutoMapper;
using LiteWeightAPI.Api.Self.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Imports;
using LiteWeightAPI.Services;

namespace LiteWeightAPI.Commands.Self;

public class CreateSelf : ICommand<UserResponse>
{
	public required string UserId { get; set; }

	public required string UserEmail { get; set; }

	public required string Username { get; set; }

	public byte[]? ProfilePictureData { get; set; }

	public bool MetricUnits { get; set; }
}

public class CreateSelfHandler : ICommandHandler<CreateSelf, UserResponse>
{
	private readonly IRepository _repository;
	private readonly IStorageService _storageService;
	private readonly IMapper _mapper;

	public CreateSelfHandler(IRepository repository, IStorageService storageService, IMapper mapper)
	{
		_repository = repository;
		_storageService = storageService;
		_mapper = mapper;
	}

	public async Task<UserResponse> HandleAsync(CreateSelf command)
	{
		var userByUsername = await _repository.GetUserByUsername(command.Username);
		if (userByUsername != null)
		{
			throw new AlreadyExistsException("User already exists with this username");
		}

		var userByEmail = await _repository.GetUserByEmail(command.UserEmail);
		if (userByEmail != null)
		{
			throw new AlreadyExistsException("There is already an account associated with this email");
		}

		// whenever a user is created, give them a unique UUID file path that will always get updated
		var fileName = Guid.NewGuid().ToString();
		if (command.ProfilePictureData != null && command.ProfilePictureData.Length > 0)
		{
			await _storageService.UploadProfilePicture(command.ProfilePictureData, fileName);
		}
		else
		{
			await _storageService.UploadDefaultProfilePicture(fileName);
		}

		var userPreferences = new UserSettings
		{
			MetricUnits = command.MetricUnits,
			PrivateAccount = false,
			UpdateDefaultWeightOnRestart = true,
			UpdateDefaultWeightOnSave = true
		};
		var user = new User
		{
			Id = command.UserId,
			Email = command.UserEmail,
			ProfilePicture = fileName,
			Username = command.Username
				.ToLowerInvariant(), // really dumb. But firestore doesn't allow for case insensitive searching
			Settings = userPreferences,
			Exercises = Defaults.GetDefaultExercises()
		};
		await _repository.CreateUser(user);

		var retVal = _mapper.Map<UserResponse>(user);
		return retVal;
	}
}