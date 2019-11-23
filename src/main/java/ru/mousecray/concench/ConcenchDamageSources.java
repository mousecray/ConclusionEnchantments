package ru.mousecray.concench;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;

public class ConcenchDamageSources {

	public static DamageSource causeCritDamage(Entity tool, @Nullable Entity damager) {
		return (new EntityDamageSourceIndirect("crit", tool, damager));
	}

	public static DamageSource causeCritDamageArrow(Entity arrow, @Nullable Entity damager) {
		return (new EntityDamageSourceIndirect("arrowCrit", arrow, damager)).setProjectile();
	}
	
	public static DamageSource causeCreativeDamage(Entity damager) {
		return (new EntityDamageSource("finalCreative", damager)).setDamageAllowedInCreativeMode();
	}
}