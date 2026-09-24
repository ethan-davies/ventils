package dev.ethann.ventils.utils.skyblock;

public enum Island {
	SINGLEPLAYER("Singleplayer"),
	DUNGEON("Catacombs"),
	DUNGEON_HUB("Dungeon Hub"),
	HUB("Hub"),
	PRIVATE_ISLAND("Private Island"),
	GARDEN("Garden"),
	UNKNOWN("(Unknown)");

	private final String displayName;

	Island(String displayName) {
		this.displayName = displayName;
	}

	public String displayName() {
		return displayName;
	}

	public static Island fromAreaLine(String area) {
		for (Island island : values()) {
			if (island == UNKNOWN || island == SINGLEPLAYER) {
				continue;
			}
			if (area.toLowerCase().contains(island.displayName.toLowerCase())) {
				return island;
			}
		}
		return UNKNOWN;
	}
}
