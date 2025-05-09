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
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=odhXwoS3mDA", Label = "Example Video" } },
				Focuses = new List<string> { "Shoulders", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Back Extension",
				Links = new List<Link> { new() { Url = "https://youtu.be/ph3pddpKzzw?t=32", Label = "Example Video" } },
				Focuses = new List<string> { "Back" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Barbell Curl",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=FAEWpmb9YQs", Label = "Example Video" } },
				Focuses = new List<string> { "Biceps", "Forearms", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Bench Press",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=gRVjAtPip0Y", Label = "Example Video" } },
				Focuses = new List<string> { "Chest", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Bent Over Row",
				Links = new List<Link> { new() { Url = "https://youtu.be/QFq5jdwWwX4", Label = "Example Video" } },
				Focuses = new List<string> { "Back" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Burpee",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=dZgVxmf6jkA", Label = "Example Video" } },
				Focuses = new List<string> { "Cardio", "Core" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Cable Fly",
				Links = new List<Link> { new() { Url = "https://youtu.be/Iwe6AmxVf7o?t=16", Label = "Example Video" } },
				Focuses = new List<string> { "Chest" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Calf Raise",
				Links = new List<Link> { new() { Url = "https://youtu.be/-M4-G8p8fmc?t=4", Label = "Example Video" } },
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Chin Up",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=qVztO-F-IwI", Label = "Example Video" } },
				Focuses = new List<string> { "Back", "Biceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Close Grip Bench",
				Links = new List<Link> { new() { Url = "https://youtu.be/nEF0bv2FW94?t=18", Label = "Example Video" } },
				Focuses = new List<string> { "Chest", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Concentration Curl",
				Links = new List<Link> { new() { Url = "https://youtu.be/Jvj2wV0vOYU?t=18", Label = "Example Video" } },
				Focuses = new List<string> { "Biceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Cycling",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=Ovlm9wWTk7Y", Label = "Example Video" } },
				Focuses = new List<string> { "Cardio" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Deadlift",
				Links = new List<Link> { new() { Url = "https://youtu.be/-4qRntuXBSc?t=7", Label = "Example Video" } },
				Focuses = new List<string> { "Back", "Core", "Legs", "Shoulders", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Decline Bench Press",
				Links = new List<Link> { new() { Url = "https://youtu.be/LfyQBUKR8SE?t=16", Label = "Example Video" } },
				Focuses = new List<string> { "Chest", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Dumbbell Curl",
				Links = new List<Link> { new() { Url = "https://youtu.be/ykJmrZ5v0Oo?t=7", Label = "Example Video" } },
				Focuses = new List<string> { "Biceps", "Forearms" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Dumbbell Fly",
				Links = new List<Link> { new() { Url = "https://youtu.be/Iwe6AmxVf7o?t=17", Label = "Example Video" } },
				Focuses = new List<string> { "Biceps", "Chest" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Dumbbell Kickback",
				Links = new List<Link> { new() { Url = "https://youtu.be/ZO81bExngMI?t=6", Label = "Example Video" } },
				Focuses = new List<string> { "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Dumbbell Chest Press",
				Links = new List<Link> { new() { Url = "https://youtu.be/VmB1G1K7v94?t=4", Label = "Example Video" } },
				Focuses = new List<string> { "Chest", "Strength Training", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Elliptical",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=E15Q3Z9J-Zg", Label = "Example Video" } },
				Focuses = new List<string> { "Cardio" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Farmer Walk",
				Links = new List<Link> { new() { Url = "https://youtu.be/Fkzk_RqlYig?t=7", Label = "Example Video" } },
				Focuses = new List<string> { "Forearms", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Front Deltoid Raise",
				Links = new List<Link> { new() { Url = "https://youtu.be/-t7fuZ0KhDA?t=6", Label = "Example Video" } },
				Focuses = new List<string> { "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Front Squat",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=VfBOBhwXbro", Label = "Example Video" } },
				Focuses = new List<string> { "Legs", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Good Morning",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=YA-h3n9L4YU", Label = "Example Video" } },
				Focuses = new List<string> { "Back" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Hack Squat",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=EdtaJRBqwes", Label = "Example Video" } },
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Hammer Curl",
				Links = new List<Link> { new() { Url = "https://youtu.be/zC3nLlEvin4?t=17", Label = "Example Video" } },
				Focuses = new List<string> { "Biceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Incline Bench Press",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=jPLdzuHckI8", Label = "Example Video" } },
				Focuses = new List<string> { "Chest", "Strength Training", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Incline Dumbbell Curl",
				Links = new List<Link> { new() { Url = "https://youtu.be/soxrZlIl35U?t=18", Label = "Example Video" } },
				Focuses = new List<string> { "Biceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Incline Dumbbell Fly",
				Links = new List<Link> { new() { Url = "https://youtu.be/bDaIL_zKbGs?t=15", Label = "Example Video" } },
				Focuses = new List<string> { "Biceps", "Chest" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Incline Dumbbell Press",
				Links = new List<Link> { new() { Url = "https://youtu.be/8iPEnn-ltC8?t=18", Label = "Example Video" } },
				Focuses = new List<string> { "Chest", "Strength Training", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Jogging",
				Links = new List<Link> { new() { Url = "https://youtu.be/vKNl8II2B-k?t=34", Label = "Example Video" } },
				Focuses = new List<string> { "Cardio" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Lat Pulldown",
				Links = new List<Link>
					{ new() { Url = "https://youtu.be/0oeIB6wi3es?t=122", Label = "Example Video" } },
				Focuses = new List<string> { "Back", "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Leg Curl",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=ELOCsoDSmrg", Label = "Example Video" } },
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Leg Extension",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=YyvSfVjQeL0", Label = "Example Video" } },
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Leg Raise",
				Links = new List<Link> { new() { Url = "https://youtu.be/JB2oyawG9KI?t=6", Label = "Example Video" } },
				Focuses = new List<string> { "Core" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Leg Press",
				Links = new List<Link> { new() { Url = "https://youtu.be/IZxyjW7MPJQ?t=14", Label = "Example Video" } },
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Lunge",
				Links = new List<Link> { new() { Url = "https://youtu.be/D7KaRcUTQeE?t=20", Label = "Example Video" } },
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Lying Side Deltoid Raise",
				Links = new List<Link> { new() { Url = "https://youtu.be/6I6AlMABLL8?t=20", Label = "Example Video" } },
				Focuses = new List<string> { "Back", "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Military Press",
				Links = new List<Link> { new() { Url = "https://youtu.be/2yjwXTZQDDI?t=20", Label = "Example Video" } },
				Focuses = new List<string> { "Shoulders", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "One Arm Dumbbell Row",
				Links = new List<Link> { new() { Url = "https://youtu.be/pYcpY20QaE8?t=15", Label = "Example Video" } },
				Focuses = new List<string> { "Back", "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "One Arm Triceps Extension",
				Links = new List<Link> { new() { Url = "https://youtu.be/_gsUck-7M74?t=5", Label = "Example Video" } },
				Focuses = new List<string> { "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Plank",
				Links = new List<Link> { new() { Url = "https://youtu.be/DHvSGdCIZyQ?t=11", Label = "Example Video" } },
				Focuses = new List<string> { "Core" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Power Clean",
				Links = new List<Link> { new() { Url = "https://youtu.be/O32-Ae8SNIc?t=4", Label = "Example Video" } },
				Focuses = new List<string> { "Core", "Legs", "Shoulders", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Preacher Curl",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=vngli9UR6Hw", Label = "Example Video" } },
				Focuses = new List<string> { "Biceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Prone Leg Curl",
				Links = new List<Link> { new() { Url = "https://youtu.be/MxJ0Tq6ldkU?t=10", Label = "Example Video" } },
				Focuses = new List<string> { "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Pull-Up",
				Links = new List<Link> { new() { Url = "https://youtu.be/nVJWf-Llf8k?t=11", Label = "Example Video" } },
				Focuses = new List<string> { "Back", "Biceps", "Shoulders", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Push-Up",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=_l3ySVKYVJ8", Label = "Example Video" } },
				Focuses = new List<string> { "Chest", "Strength Training", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Reverse Barbell Curl",
				Links = new List<Link> { new() { Url = "https://youtu.be/nRgxYX2Ve9w?t=4", Label = "Example Video" } },
				Focuses = new List<string> { "Biceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Reverse Grip Wrist Curl",
				Links = new List<Link> { new() { Url = "https://youtu.be/FW7URAaC-vE?t=17", Label = "Example Video" } },
				Focuses = new List<string> { "Forearms" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Reverse Shrug",
				Links = new List<Link> { new() { Url = "https://youtu.be/GaHtS9SUqh4?t=20", Label = "Example Video" } },
				Focuses = new List<string> { "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Rowing",
				Links = new List<Link> { new() { Url = "https://youtu.be/H0r_ZPXJLtg?t=7", Label = "Example Video" } },
				Focuses = new List<string> { "Back", "Cardio", "Core", "Legs" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Sprinting",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=6BmtGNjm7BE", Label = "Example Video" } },
				Focuses = new List<string> { "Cardio" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Shrug",
				Links = new List<Link> { new() { Url = "https://youtu.be/cJRVVxmytaM?t=18", Label = "Example Video" } },
				Focuses = new List<string> { "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Side Lateral Raise",
				Links = new List<Link> { new() { Url = "https://youtu.be/3VcKaXpzqRo?t=22", Label = "Example Video" } },
				Focuses = new List<string> { "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Sit-Up",
				Links = new List<Link> { new() { Url = "https://youtu.be/1fbU_MkV7NE?t=6", Label = "Example Video" } },
				Focuses = new List<string> { "Core" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Sitting Cable Row",
				Links = new List<Link> { new() { Url = "https://youtu.be/GZbfZ033f74?t=18", Label = "Example Video" } },
				Focuses = new List<string> { "Back", "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Skull Crusher",
				Links = new List<Link> { new() { Url = "https://youtu.be/d_KZxkY_0cM?t=17", Label = "Example Video" } },
				Focuses = new List<string> { "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Squat",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=MVMNk0HiTMg", Label = "Example Video" } },
				Focuses = new List<string> { "Legs", "Strength Training" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Swimming",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=5HLW2AI1Ink", Label = "Example Video" } },
				Focuses = new List<string> { "Cardio", "Core" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "T-Bar Row",
				Links = new List<Link> { new() { Url = "https://youtu.be/j3Igk5nyZE4?t=19", Label = "Example Video" } },
				Focuses = new List<string> { "Back", "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Triceps Dip",
				Links = new List<Link> { new() { Url = "https://youtu.be/0326dy_-CzM?t=10", Label = "Example Video" } },
				Focuses = new List<string> { "Chest", "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Triceps Pushdown",
				Links = new List<Link> { new() { Url = "https://youtu.be/2-LAMcpzODU?t=15", Label = "Example Video" } },
				Focuses = new List<string> { "Triceps" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Upright Row",
				Links = new List<Link>
					{ new() { Url = "https://www.youtube.com/watch?v=VIoihl5ZZzM", Label = "Example Video" } },
				Focuses = new List<string> { "Back", "Shoulders" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Weighted Oblique Twist",
				Links = new List<Link> { new() { Url = "https://youtu.be/pDTHSnoGoEc?t=6", Label = "Example Video" } },
				Focuses = new List<string> { "Core" }
			},
			new()
			{
				Id = Guid.NewGuid().ToString(),
				Name = "Weighted Sit-Up",
				Links = new List<Link> { new() { Url = "https://youtu.be/kZvSaq192cg?t=4", Label = "Example Video" } },
				Focuses = new List<string> { "Core", "Strength Training" }
			}
		};
		return defaultExercises;
	}
}