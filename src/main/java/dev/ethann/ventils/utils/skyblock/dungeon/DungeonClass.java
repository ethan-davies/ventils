package dev.ethann.ventils.utils.skyblock.dungeon;

public enum DungeonClass {
	ARCHER("Archer"),
	BERSERK("Berserk"),
	HEALER("Healer"),
	MAGE("Mage"),
	TANK("Tank");

	private final String label;

	DungeonClass(String label) {
		this.label = label;
	}

	public static DungeonClass fromLabel(String name) {
		if (name == null || name.isBlank()) {
			return null;
		}
		for (DungeonClass value : values()) {
			if (value.label.equalsIgnoreCase(name.trim())) {
				return value;
			}
		}
		return null;
	}

	public static DungeonClass fromLetter(char letter) {
		return switch (Character.toUpperCase(letter)) {
			case 'A' -> ARCHER;
			case 'B' -> BERSERK;
			case 'H' -> HEALER;
			case 'M' -> MAGE;
			case 'T' -> TANK;
			default -> null;
		};
	}

	@Override
	public String toString() {
		return label;
	}
}
