package ru.mousecray.concench;

public enum EnchType {
	KNOCKBACK,
	TP,
	FIRE,
	DAMAGE_SELF,
	LIGHTBOLT,
	CRIT,
	SHOW,
	SAND,
	WATER,
	SUMMON,
	SETBLOCK,
	FILL,
	GIVE,
	EXPLOSION,
	PRINT,
	KILL,
	SOUND,
	SOUND_RANDOM,
	DROP_ITEM_TARGET,
	DROP_ITEM_SELF,
	EFFECT,
	EFFECT_CLEAR,
	EFFECT_RANDOM,
	FUNCTION,
	CREATIVE;
	
	public static EnchType fromString(String string) {
		return EnchType.valueOf(string.toUpperCase());
	}
	
	public static boolean isEqual(String source, EnchType predicate) {
		return fromString(source) == predicate;
	}
	
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}