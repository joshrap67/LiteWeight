using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;

namespace LiteWeightApiTests.Commands.Self;

public class SetPreferencesTests : BaseTest
{
	private readonly SetSettingsHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public SetPreferencesTests()
	{
		_mockRepository = new Mock<IRepository>();
		_handler = new SetSettingsHandler(_mockRepository.Object);
	}

	[Fact]
	public async Task Should_Set_Preferences()
	{
		var command = Fixture.Create<SetSettings>();

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await _handler.HandleAsync(command);

		Assert.Equal(command.MetricUnits, user.Settings.MetricUnits);
		Assert.Equal(command.PrivateAccount, user.Settings.PrivateAccount);
		Assert.Equal(command.UpdateDefaultWeightOnRestart, user.Settings.UpdateDefaultWeightOnRestart);
		Assert.Equal(command.UpdateDefaultWeightOnSave, user.Settings.UpdateDefaultWeightOnSave);
	}
}