using Google.Cloud.Firestore;
using LiteWeightAPI.Domain.Complaints;
using LiteWeightAPI.Domain.ReceivedWorkouts;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Options;
using Microsoft.Extensions.Options;

namespace LiteWeightAPI.Domain;

public interface IRepository
{
	Task<User?> GetUser(string userId);
	Task<User?> GetUserByUsername(string username);
	Task<User?> GetUserByEmail(string email);
	Task CreateUser(User user);
	Task PutUser(User user);
	Task DeleteUser(string userId);
	Task CreateComplaint(Complaint complaint);
	Task<Workout?> GetWorkout(string workoutId);
	Task CreateWorkout(Workout workout);
	Task PutWorkout(Workout workout);
	Task DeleteWorkout(string workoutId);
	Task<ReceivedWorkout?> GetReceivedWorkout(string receivedWorkoutId);
	Task DeleteReceivedWorkout(string workoutId);
	Task<Complaint?> GetComplaint(string complaintId);

	Task ExecuteBatchWrite(IList<Workout>? workoutsToPut = null, IList<User>? usersToPut = null,
		IList<ReceivedWorkout>? receivedWorkoutsToPut = null, IList<Workout>? workoutsToDelete = null,
		IList<User>? usersToDelete = null, IList<ReceivedWorkout>? receivedWorkoutsToDelete = null);
}

public class Repository : IRepository
{
	private readonly FirebaseOptions _firebaseOptions;

	private const string WorkoutsCollection = "workouts";
	private const string UsersCollection = "users";
	private const string ComplaintsCollection = "complaints";
	private const string ReceivedWorkoutsCollection = "receivedWorkouts";

	public Repository(IOptions<FirebaseOptions> firebaseOptions)
	{
		_firebaseOptions = firebaseOptions.Value;
	}

	private FirestoreDb GetDb()
	{
		return FirestoreDb.Create(_firebaseOptions.ProjectId);
	}

	public async Task<Complaint?> GetComplaint(string complaintId)
	{
		var db = GetDb();
		var docRef = db.Collection(ComplaintsCollection).Document(complaintId);
		var snapshot = await docRef.GetSnapshotAsync();

		if (!snapshot.Exists) return null;
		var complaint = snapshot.ConvertTo<Complaint>();
		return complaint;
	}

	public async Task ExecuteBatchWrite(IList<Workout>? workoutsToPut = null, IList<User>? usersToPut = null,
		IList<ReceivedWorkout>? receivedWorkoutsToPut = null, IList<Workout>? workoutsToDelete = null,
		IList<User>? usersToDelete = null, IList<ReceivedWorkout>? receivedWorkoutsToDelete = null)
	{
		var db = GetDb();
		var batch = db.StartBatch();

		foreach (var workout in workoutsToDelete ?? new List<Workout>())
		{
			var workoutsRef = db.Collection(WorkoutsCollection).Document(workout.Id);
			batch.Delete(workoutsRef);
		}

		foreach (var workout in workoutsToPut ?? new List<Workout>())
		{
			var workoutsRef = db.Collection(WorkoutsCollection).Document(workout.Id);
			batch.Set(workoutsRef, workout);
		}

		foreach (var user in usersToDelete ?? new List<User>())
		{
			var usersRef = db.Collection(UsersCollection).Document(user.Id);
			batch.Delete(usersRef);
		}

		foreach (var user in usersToPut ?? new List<User>())
		{
			var usersRef = db.Collection(UsersCollection).Document(user.Id);
			batch.Set(usersRef, user);
		}

		foreach (var receivedWorkout in receivedWorkoutsToDelete ?? new List<ReceivedWorkout>())
		{
			var receivedWorkoutRef =
				db.Collection(ReceivedWorkoutsCollection).Document(receivedWorkout.Id);
			batch.Delete(receivedWorkoutRef);
		}

		foreach (var receivedWorkout in receivedWorkoutsToPut ?? new List<ReceivedWorkout>())
		{
			var receivedWorkoutRef =
				db.Collection(ReceivedWorkoutsCollection).Document(receivedWorkout.Id);
			batch.Set(receivedWorkoutRef, receivedWorkout);
		}

		await batch.CommitAsync();
	}

	public async Task<User?> GetUser(string userId)
	{
		var db = GetDb();
		var docRef = db.Collection(UsersCollection).Document(userId);
		var snapshot = await docRef.GetSnapshotAsync();

		if (!snapshot.Exists) return null;
		var user = snapshot.ConvertTo<User>();
		return user;
	}

	public async Task<User?> GetUserByUsername(string username)
	{
		var db = GetDb();
		var usersRef = db.Collection(UsersCollection);
		var query = usersRef.WhereEqualTo("username", username.ToLowerInvariant());
		var querySnapshot = await query.GetSnapshotAsync();

		var user = querySnapshot.Documents.ToList().FirstOrDefault();
		return user?.ConvertTo<User>();
	}

	public async Task<User?> GetUserByEmail(string email)
	{
		var db = GetDb();
		var usersRef = db.Collection(UsersCollection);
		var query = usersRef.WhereEqualTo("email", email);
		var querySnapshot = await query.GetSnapshotAsync();

		var user = querySnapshot.Documents.ToList().FirstOrDefault();
		return user?.ConvertTo<User>();
	}

	public async Task CreateUser(User user)
	{
		var db = GetDb();
		var docRef = db.Collection(UsersCollection).Document(user.Id);
		await docRef.CreateAsync(user);
	}

	public async Task PutUser(User user)
	{
		var db = GetDb();
		var docRef = db.Collection(UsersCollection).Document(user.Id);
		await docRef.SetAsync(user);
	}

	public async Task DeleteUser(string userId)
	{
		var db = GetDb();
		var docRef = db.Collection(UsersCollection).Document(userId);
		await docRef.DeleteAsync();
	}

	public async Task CreateComplaint(Complaint complaint)
	{
		var db = GetDb();
		var docRef = db.Collection(ComplaintsCollection).Document(complaint.Id);
		await docRef.CreateAsync(complaint);
	}

	public async Task<Workout?> GetWorkout(string workoutId)
	{
		var db = GetDb();
		var docRef = db.Collection(WorkoutsCollection).Document(workoutId);
		var snapshot = await docRef.GetSnapshotAsync();

		if (!snapshot.Exists) return null;
		var workout = snapshot.ConvertTo<Workout>();
		return workout;
	}

	public async Task CreateWorkout(Workout workout)
	{
		var db = GetDb();
		var docRef = db.Collection(WorkoutsCollection).Document(workout.Id);
		await docRef.CreateAsync(workout);
	}

	public async Task PutWorkout(Workout workout)
	{
		var db = GetDb();
		var docRef = db.Collection(WorkoutsCollection).Document(workout.Id);
		await docRef.SetAsync(workout);
	}

	public async Task DeleteWorkout(string workoutId)
	{
		var db = GetDb();
		var docRef = db.Collection(WorkoutsCollection).Document(workoutId);
		await docRef.DeleteAsync();
	}

	public async Task<ReceivedWorkout?> GetReceivedWorkout(string receivedWorkoutId)
	{
		var db = GetDb();
		var docRef = db.Collection(ReceivedWorkoutsCollection).Document(receivedWorkoutId);
		var snapshot = await docRef.GetSnapshotAsync();

		if (!snapshot.Exists) return null;
		var receivedWorkout = snapshot.ConvertTo<ReceivedWorkout>();
		return receivedWorkout;
	}

	public async Task DeleteReceivedWorkout(string workoutId)
	{
		var db = GetDb();
		var docRef = db.Collection(ReceivedWorkoutsCollection).Document(workoutId);
		await docRef.DeleteAsync();
	}
}