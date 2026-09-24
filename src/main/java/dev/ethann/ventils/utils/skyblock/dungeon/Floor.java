package dev.ethann.ventils.utils.skyblock.dungeon;

public enum Floor {
	E(0),
	F1(1),
	F2(2),
	F3(3),
	F4(4),
	F5(5),
	F6(6),
	F7(7),
	M1(1),
	M2(2),
	M3(3),
	M4(4),
	M5(5),
	M6(6),
	M7(7);

	private final int floorNumber;

	Floor(int floorNumber) {
		this.floorNumber = floorNumber;
	}

	public int floorNumber() {
		return floorNumber;
	}

	public boolean masterMode() {
		return name().startsWith("M");
	}

	public static Floor fromScoreboard(String token) {
		try {
			return Floor.valueOf(token);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}
}
