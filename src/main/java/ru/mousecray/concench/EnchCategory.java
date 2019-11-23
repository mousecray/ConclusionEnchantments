package ru.mousecray.concench;

public enum EnchCategory {
	NORMAL,
	RARE,
	EPIC;
	
	public static EnchCategory fromString(String string) {
		return EnchCategory.valueOf(string.toUpperCase());
	}
	
	public static boolean isEqual(String source, EnchCategory predicate) {
		return fromString(source) == predicate;
	}
	
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}