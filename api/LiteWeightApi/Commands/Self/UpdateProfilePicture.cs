using LiteWeightAPI.Domain;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Services;

namespace LiteWeightAPI.Commands.Self;

public class UpdateProfilePicture : ICommand<bool>
{
	public required string UserId { get; set; }
	public required byte[] ImageData { get; set; }
}

public class UpdateProfilePictureHandler : ICommandHandler<UpdateProfilePicture, bool>
{
	private readonly IRepository _repository;
	private readonly IStorageService _storageService;

	public UpdateProfilePictureHandler(IRepository repository, IStorageService storageService)
	{
		_repository = repository;
		_storageService = storageService;
	}

	public async Task<bool> HandleAsync(UpdateProfilePicture command)
	{
		var user = await _repository.GetUser(command.UserId);
		if (user == null)
		{
			throw new ResourceNotFoundException("User");
		}

		await _storageService.UploadProfilePicture(command.ImageData, user.ProfilePicture);
		return true;
	}
}