using LiteWeightAPI.Domain.Users;

namespace LiteWeightAPI.Imports;

public static class Defaults
{
	public static List<OwnedExercise> GetDefaultExercises()
	{
		var defaultExercises = new List<OwnedExercise>
		{
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Arnold Press",
				VideoUrl = "https://www.youtube.com/watch?v=odhXwoS3mDA",
				Focuses = new List<string> { "Shoulders", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Back Extension",
				VideoUrl = "https://youtu.be/ph3pddpKzzw?t=32",
				Focuses = new List<string> { "Back" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Barbell Curl",
				VideoUrl = "https://www.youtube.com/watch?v=FAEWpmb9YQs",
				Focuses = new List<string> { "Biceps", "Forearms", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Bench Press",
				VideoUrl = "https://www.youtube.com/watch?v=gRVjAtPip0Y",
				Focuses = new List<string> { "Chest", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Bent Over Row",
				VideoUrl = "https://youtu.be/QFq5jdwWwX4",
				Focuses = new List<string> { "Back" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Burpee",
				VideoUrl = "https://www.youtube.com/watch?v=dZgVxmf6jkA",
				Focuses = new List<string> { "Cardio", "Core" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Cable Fly",
				VideoUrl = "https://youtu.be/Iwe6AmxVf7o?t=16",
				Focuses = new List<string> { "Chest" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Calf Raise",
				VideoUrl = "https://youtu.be/-M4-G8p8fmc?t=4",
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Chin Up",
				VideoUrl = "https://www.youtube.com/watch?v=qVztO-F-IwI",
				Focuses = new List<string> { "Back", "Biceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Close Grip Bench",
				VideoUrl = "https://youtu.be/nEF0bv2FW94?t=18",
				Focuses = new List<string> { "Chest", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Concentration Curl",
				VideoUrl = "https://youtu.be/Jvj2wV0vOYU?t=18",
				Focuses = new List<string> { "Biceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Cycling",
				VideoUrl = "https://www.youtube.com/watch?v=Ovlm9wWTk7Y",
				Focuses = new List<string> { "Cardio" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Deadlift",
				VideoUrl = "https://youtu.be/-4qRntuXBSc?t=7",
				Focuses = new List<string> { "Back", "Core", "Legs", "Shoulders", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Decline Bench Press",
				VideoUrl = "https://youtu.be/LfyQBUKR8SE?t=16",
				Focuses = new List<string> { "Chest", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Dumbbell Curl",
				VideoUrl = "https://youtu.be/ykJmrZ5v0Oo?t=7",
				Focuses = new List<string> { "Biceps", "Forearms" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Dumbbell Fly",
				VideoUrl = "https://youtu.be/Iwe6AmxVf7o?t=17",
				Focuses = new List<string> { "Biceps", "Chest" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Dumbbell Kickback",
				VideoUrl = "https://youtu.be/ZO81bExngMI?t=6",
				Focuses = new List<string> { "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Dumbbell Chest Press",
				VideoUrl = "https://youtu.be/VmB1G1K7v94?t=4",
				Focuses = new List<string> { "Chest", "Strength Training", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Elliptical",
				VideoUrl = "https://www.youtube.com/watch?v=E15Q3Z9J-Zg",
				Focuses = new List<string> { "Cardio" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Farmer Walk",
				VideoUrl = "https://youtu.be/Fkzk_RqlYig?t=7",
				Focuses = new List<string> { "Forearms", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Front Deltoid Raise",
				VideoUrl = "https://youtu.be/-t7fuZ0KhDA?t=6",
				Focuses = new List<string> { "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Front Squat",
				VideoUrl = "https://www.youtube.com/watch?v=VfBOBhwXbro",
				Focuses = new List<string> { "Legs", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Good Morning",
				VideoUrl = "https://www.youtube.com/watch?v=YA-h3n9L4YU",
				Focuses = new List<string> { "Back" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Hack Squat",
				VideoUrl = "https://www.youtube.com/watch?v=EdtaJRBqwes",
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Hammer Curl",
				VideoUrl = "https://youtu.be/zC3nLlEvin4?t=17",
				Focuses = new List<string> { "Biceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Incline Bench Press",
				VideoUrl = "https://www.youtube.com/watch?v=jPLdzuHckI8",
				Focuses = new List<string> { "Chest", "Strength Training", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Incline Dumbbell Curl",
				VideoUrl = "https://youtu.be/soxrZlIl35U?t=18",
				Focuses = new List<string> { "Biceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Incline Dumbbell Fly",
				VideoUrl = "https://youtu.be/bDaIL_zKbGs?t=15",
				Focuses = new List<string> { "Biceps", "Chest" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Incline Dumbbell Press",
				VideoUrl = "https://youtu.be/8iPEnn-ltC8?t=18",
				Focuses = new List<string> { "Chest", "Strength Training", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Jogging",
				VideoUrl = "https://youtu.be/vKNl8II2B-k?t=34",
				Focuses = new List<string> { "Cardio" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Lat Pulldown",
				VideoUrl = "https://youtu.be/0oeIB6wi3es?t=122",
				Focuses = new List<string> { "Back", "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Leg Curl",
				VideoUrl = "https://www.youtube.com/watch?v=ELOCsoDSmrg",
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Leg Extension",
				VideoUrl = "https://www.youtube.com/watch?v=YyvSfVjQeL0",
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Leg Raise",
				VideoUrl = "https://youtu.be/JB2oyawG9KI?t=6",
				Focuses = new List<string> { "Core" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Leg Press",
				VideoUrl = "https://youtu.be/IZxyjW7MPJQ?t=14",
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Lunge",
				VideoUrl = "https://youtu.be/D7KaRcUTQeE?t=20",
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Lying Side Deltoid Raise",
				VideoUrl = "https://youtu.be/6I6AlMABLL8?t=20",
				Focuses = new List<string> { "Back", "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Military Press",
				VideoUrl = "https://youtu.be/2yjwXTZQDDI?t=20",
				Focuses = new List<string> { "Shoulders", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "One Arm Dumbbell Row",
				VideoUrl = "https://youtu.be/pYcpY20QaE8?t=15",
				Focuses = new List<string> { "Back", "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "One Arm Triceps Extension",
				VideoUrl = "https://youtu.be/_gsUck-7M74?t=5",
				Focuses = new List<string> { "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Plank",
				VideoUrl = "https://youtu.be/DHvSGdCIZyQ?t=11",
				Focuses = new List<string> { "Core" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Power Clean",
				VideoUrl = "https://youtu.be/O32-Ae8SNIc?t=4",
				Focuses = new List<string> { "Core", "Legs", "Shoulders", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Preacher Curl",
				VideoUrl = "https://www.youtube.com/watch?v=vngli9UR6Hw",
				Focuses = new List<string> { "Biceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Prone Leg Curl",
				VideoUrl = "https://youtu.be/MxJ0Tq6ldkU?t=10",
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Pull-Up",
				VideoUrl = "https://youtu.be/nVJWf-Llf8k?t=11",
				Focuses = new List<string> { "Back", "Biceps", "Shoulders", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Push-Up",
				VideoUrl = "https://www.youtube.com/watch?v=_l3ySVKYVJ8",
				Focuses = new List<string> { "Chest", "Strength Training", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Reverse Barbell Curl",
				VideoUrl = "https://youtu.be/nRgxYX2Ve9w?t=4",
				Focuses = new List<string> { "Biceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Reverse Grip Wrist Curl",
				VideoUrl = "https://youtu.be/FW7URAaC-vE?t=17",
				Focuses = new List<string> { "Forearms" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Reverse Shrug",
				VideoUrl = "https://youtu.be/GaHtS9SUqh4?t=20",
				Focuses = new List<string> { "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Rowing",
				VideoUrl = "https://youtu.be/H0r_ZPXJLtg?t=7",
				Focuses = new List<string> { "Back", "Cardio", "Core", "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Sprinting",
				VideoUrl = "https://www.youtube.com/watch?v=6BmtGNjm7BE",
				Focuses = new List<string> { "Cardio" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Shrug",
				VideoUrl = "https://youtu.be/cJRVVxmytaM?t=18",
				Focuses = new List<string> { "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Side Lateral Raise",
				VideoUrl = "https://youtu.be/3VcKaXpzqRo?t=22",
				Focuses = new List<string> { "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Sit-Up",
				VideoUrl = "https://youtu.be/1fbU_MkV7NE?t=6",
				Focuses = new List<string> { "Core" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Sitting Cable Row",
				VideoUrl = "https://youtu.be/GZbfZ033f74?t=18",
				Focuses = new List<string> { "Back", "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Skull Crusher",
				VideoUrl = "https://youtu.be/d_KZxkY_0cM?t=17",
				Focuses = new List<string> { "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Squat",
				VideoUrl = "https://www.youtube.com/watch?v=MVMNk0HiTMg",
				Focuses = new List<string> { "Legs", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Swimming",
				VideoUrl = "https://www.youtube.com/watch?v=5HLW2AI1Ink",
				Focuses = new List<string> { "Cardio", "Core" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "T-Bar Row",
				VideoUrl = "https://youtu.be/j3Igk5nyZE4?t=19",
				Focuses = new List<string> { "Back", "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Triceps Dip",
				VideoUrl = "https://youtu.be/0326dy_-CzM?t=10",
				Focuses = new List<string> { "Chest", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Triceps Pushdown",
				VideoUrl = "https://youtu.be/2-LAMcpzODU?t=15",
				Focuses = new List<string> { "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Upright Row",
				VideoUrl = "https://www.youtube.com/watch?v=VIoihl5ZZzM",
				Focuses = new List<string> { "Back", "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Weighted Oblique Twist",
				VideoUrl = "https://youtu.be/pDTHSnoGoEc?t=6",
				Focuses = new List<string> { "Core" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Weighted Sit-Up",
				VideoUrl = "https://youtu.be/kZvSaq192cg?t=4",
				Focuses = new List<string> { "Core", "Strength Training" }
			}
		};
		return defaultExercises;
	}
}