package ru.mousecray.concench;

public enum EnchAction {
	
	ATTACK,
	DEFENCE,
	USE;

	public static EnchAction fromString(String string) {
		return EnchAction.valueOf(string.toUpperCase());
	}
	
	public static boolean isEqual(String source, EnchAction predicate) {
		return fromString(source) == predicate;
	}
	
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}