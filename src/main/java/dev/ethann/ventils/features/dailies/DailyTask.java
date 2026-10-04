package dev.ethann.ventils.features.dailies;

public enum DailyTask {
	NPC_FLIPS("NPC Flips"),
	FLETCHUR("Fletchur"),
	PUZZLER("Puzzler"),
	EXPERIMENTATION_TABLE("Experimentation table"),
	CRIMSON_ISLE_DAILIES("Crimson Isle Dailies"),
	HEAVY_PEARLS("Heavy Pearls"),
	HOTM_DAILIES("HOTM Dailies"),
	FORGE("Forge"),
	CHOCOLATE_FACTORY("Chocolate Factory"),
	PESTS("Pests"),
	VISITORS("Visitors"),
	GREENHOUSE("Greenhouse"),
	COMPOSTER("Composter"),
	CAKE_BUFFS("Cake buffs");

	private final String label;

	DailyTask(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}
}
