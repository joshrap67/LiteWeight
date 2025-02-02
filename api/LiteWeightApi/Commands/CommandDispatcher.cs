namespace LiteWeightAPI.Commands;

public interface ICommandDispatcher
{
	Task<TR> DispatchAsync<T, TR>(T command) where T : ICommand<TR>;
}

public class CommandDispatcher : ICommandDispatcher
{
	private readonly IServiceProvider _serviceProvider;

	public CommandDispatcher(IServiceProvider serviceProvider)
	{
		_serviceProvider = serviceProvider;
	}

	public async Task<TR> DispatchAsync<T, TR>(T command) where T : ICommand<TR>
	{
		var service = _serviceProvider.GetService(typeof(ICommandHandler<T, TR>)) as ICommandHandler<T, TR>;
		if (service == null)
		{
			throw new Exception("Missing service");
		}

		var response = await service.HandleAsync(command);
		return response;
	}
}