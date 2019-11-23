package ru.mousecray.concench;

public class ConcenchObj {
	
	private final EnchType type;
	private final String name;
	private EnchCategory category;
	private String value;
	private EnchAction action;
	
	public ConcenchObj(EnchType type, String name, EnchCategory category, EnchAction action, String value) {
		this.type = type;
		this.name = name;
		this.category = category;
		this.value = value;
		this.action = action;
	}
	
	public ConcenchObj(EnchType type, EnchCategory category, EnchAction action, String value) {
		this(type, null, category, action, value);
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public EnchCategory getCategory() {
		return category;
	}
	
	public void setCategory(EnchCategory category) {
		this.category = category;
	}
	
	public EnchType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public EnchAction getAction() {
		return action;
	}
	
	public void setAction(EnchAction action) {
		this.action = action;
	}
}