package ru.mousecray.concench;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = Concench.ID)
public class EnchEvents {

	@SubscribeEvent
	public static void onAnvilUpdate(AnvilUpdateEvent event) {
		ItemStack output = event.getLeft().copy();
		String name = output.getUnlocalizedName();
		if (Concench.enchantments.containsKey(name) && event.getRight().getItem() == Items.ENCHANTED_BOOK) {
			boolean flag = false;
			if (output.hasTagCompound()) {
				if (!output.getTagCompound().hasKey("concench")) {
					NBTTagCompound nbt = output.getOrCreateSubCompound("concench");
					genNBT(name, nbt, (output.getItem() instanceof ItemBow) ? true : false);
					flag = true;
				}
			} else {
				output.setTagCompound(new NBTTagCompound());
				NBTTagCompound nbt = output.getOrCreateSubCompound("concench");
				genNBT(name, nbt, (output.getItem() instanceof ItemBow) ? true : false);
				flag = true;
			}
			
			if (flag) {
				event.setCost(output.getRepairCost() + 2);
				output.setRepairCost(output.getRepairCost() + 2);
				event.setOutput(output);
			}
		}
	}

	private static void genNBT(String name, NBTTagCompound nbt, boolean arrow) {
		Optional<ConcenchObj> obj = getEnchantment(Concench.enchantments.get(name));
		if (obj.isPresent()) {
			ConcenchObj finals = obj.get();
			nbt.setString("name", finals.getName());
			nbt.setString("type", finals.getType().toString());
			nbt.setString("action", arrow ? (finals.getAction() == EnchAction.USE ? (Concench.random.nextInt(10) > 5 ? EnchAction.ATTACK.toString() : EnchAction.DEFENCE.toString()) : finals.getAction().toString()) : finals.getAction().toString());
			nbt.setString("value", finals.getValue());
			int rand = Concench.random.nextInt(100);
		}
	}

	private static Optional<ConcenchObj> getEnchantment(List<ConcenchObj> obj) {
		int rand = Concench.random.nextInt(100);
		Optional<ConcenchObj> outObj;
		if (rand < 60) {
			outObj = obj.stream().filter(z -> z.getCategory() == EnchCategory.NORMAL).findAny();
			if (!outObj.isPresent())
				outObj = obj.stream().filter(z -> z.getCategory() == EnchCategory.RARE).findAny();
			if (!outObj.isPresent())
				outObj = obj.stream().filter(z -> z.getCategory() == EnchCategory.EPIC).findAny();
		} else if (rand < 90) {
			outObj = obj.stream().filter(z -> z.getCategory() == EnchCategory.RARE).findAny();
			if (!outObj.isPresent())
				outObj = obj.stream().filter(z -> z.getCategory() == EnchCategory.NORMAL).findAny();
			if (!outObj.isPresent())
				outObj = obj.stream().filter(z -> z.getCategory() == EnchCategory.EPIC).findAny();
		} else {
			outObj = obj.stream().filter(z -> z.getCategory() == EnchCategory.EPIC).findAny();
			if (!outObj.isPresent())
				outObj = obj.stream().filter(z -> z.getCategory() == EnchCategory.RARE).findAny();
			if (!outObj.isPresent())
				outObj = obj.stream().filter(z -> z.getCategory() == EnchCategory.NORMAL).findAny();
		}
		return outObj;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onRenderTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("concench")) {
			NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("concench");
			Container container = event.getEntityPlayer().openContainer;
			if (!(container instanceof ContainerRepair
					&& ((ContainerRepair) container).getSlot(2).getStack().isItemEqual(stack))) {
				event.getToolTip().add(nbt.hasKey("name") ? "§dConcluded magic: " + nbt.getString("name")
						: "§dConcluded magic: " + "???");
				event.getToolTip().add("§9Magic type: " + nbt.getString("action"));
			} else {
				event.getToolTip().add("§dConcluded magic: " + "???");
				event.getToolTip().add("§9Magic type: " + "???");
			}
		}
	}

	@SubscribeEvent
	public static void onItemUseTick(LivingEntityUseItemEvent.Finish event) {
		ItemStack stack = event.getItem();
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("concench") && EnchAction.fromString(stack.getSubCompound("concench").getString("action")) == EnchAction.USE) {
			NBTTagCompound nbt = stack.getSubCompound("concench");
			generateUse(event.getEntityLiving(), EnchType.fromString(nbt.getString("type")), nbt.getString("value"));
		}
	}

	@SubscribeEvent
	public static void onEntityAttacked(LivingAttackEvent event) {
		EntityLivingBase living = event.getEntityLiving();
		if (event.getSource().getTrueSource() instanceof EntityLivingBase) {
			EntityLivingBase damager = (EntityLivingBase) event.getSource().getTrueSource();
			ItemStack stack = damager.getActiveItemStack();
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("concench")) {
				NBTTagCompound nbt = stack.getSubCompound("concench");
				if (EnchAction.fromString(stack.getSubCompound("concench").getString("action")) == EnchAction.ATTACK && EnchType.fromString(stack.getSubCompound("concench").getString("type")) != EnchType.CRIT) {
					EnchType type = EnchType.fromString(nbt.getString("type"));
					String value = nbt.getString("value");
					generateAttack(living, damager, type, value, event.getAmount());
				}
				else if (EnchAction.fromString(stack.getSubCompound("concench").getString("action")) == EnchAction.DEFENCE && living.getItemInUseCount() > 0 && !(living.getActiveItemStack().getItem() instanceof ItemBow)) {
					generateDefence(event.getEntityLiving().getEntityWorld(), living, event.getSource(), EnchType.fromString(nbt.getString("type")), nbt.getString("value"));
				}
			}
		}
	}

	@SubscribeEvent
	public static void onCritEvent(CriticalHitEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = player.getActiveItemStack();
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("concench")) {
			NBTTagCompound nbt = stack.getSubCompound("concench");
			if (EnchAction.fromString(stack.getSubCompound("concench").getString("action")) == EnchAction.ATTACK
					&& !(stack.getItem() instanceof ItemBow)
					&& EnchType.fromString(nbt.getString("type")) == EnchType.CRIT) {
				int value = Integer.parseInt(nbt.getString("value"));
				if (Concench.random.nextInt(100) < value) {
					event.setDamageModifier(2.5F);
					event.setResult(Result.ALLOW);
					System.out.println(event.getDamageModifier());
				}
			}
		}
	}

	@SubscribeEvent
	public static void onSpawnArrow(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityArrow) {
			EntityArrow arrow = (EntityArrow) event.getEntity();
			if (arrow.shootingEntity instanceof EntityLivingBase) {
				EntityLivingBase entity = (EntityLivingBase) arrow.shootingEntity;
				ItemStack stack = entity.getActiveItemStack();
				if (stack.hasTagCompound() && stack.getTagCompound().hasKey("concench")) {
					NBTTagCompound nbt = stack.getSubCompound("concench");
					if (EnchAction.fromString(stack.getSubCompound("concench").getString("action")) == EnchAction.ATTACK) {
						entity.addTag("concench$" + nbt.getString("type") + "$" + nbt.getString("value") + "$" + nbt.getString("action"));
					}
					else if (EnchAction.fromString(stack.getSubCompound("concench").getString("action")) == EnchAction.DEFENCE) {
						
					}
				}
			}

		}
	}

	@SubscribeEvent
	public static void onArrowImpact(ProjectileImpactEvent.Arrow event) {
		if (event.getRayTraceResult().typeOfHit == Type.ENTITY && event.getRayTraceResult().entityHit instanceof EntityLivingBase) {
			Optional<String> tag = event.getArrow().getTags().stream().filter(e -> e.startsWith("concench")).findFirst();
			if (tag.isPresent()) {
				String[] pars = tag.get().split("$");
				if (EnchAction.fromString(pars[3]) == EnchAction.ATTACK) {
					EnchType type = EnchType.fromString(pars[1]);
					String value = pars[2];
					generateArrowAttack((EntityLivingBase) event.getRayTraceResult().entityHit, event.getArrow().shootingEntity, type, value, event.getArrow().getDamage() * 2);
				}
			}
		}
		else if (event.getRayTraceResult().typeOfHit == Type.BLOCK) {
			Optional<String> tag = event.getArrow().getTags().stream().filter(e -> e.startsWith("concench")).findFirst();
			if (tag.isPresent()) {
				String[] pars = tag.get().split("$");
				if (EnchAction.fromString(pars[3]) == EnchAction.DEFENCE) {
					EnchType type = EnchType.fromString(pars[1]);
					String value = pars[2];
					generateDefence(event.getArrow().getEntityWorld(), null, null, type, value);
				}
			}
		}
	}

	private static void generateUse(EntityLivingBase entity, EnchType type, String value) {
		switch (type) {
		case CRIT:
			if (Concench.random.nextInt(100) < Integer.parseInt(value))
				entity.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, Concench.random.nextInt(3) + 1,
						Concench.random.nextInt(2) + 1));
			((EntityPlayer) entity).getCooldownTracker().setCooldown(entity.getActiveItemStack().getItem(),
					Concench.random.nextInt(30) + 30);
			break;
		case DAMAGE_SELF:
			break;
		case DROP_ITEM_SELF:
			break;
		case DROP_ITEM_TARGET:
			break;
		case EFFECT:
			break;
		case EFFECT_CLEAR:
			break;
		case EFFECT_RANDOM:
			break;
		case EXPLOSION:
			break;
		case FILL:
			break;
		case FIRE:
			break;
		case GIVE:
			break;
		case KILL:
			break;
		case KNOCKBACK:
			break;
		case LIGHTBOLT:
			break;
		case PRINT:
			break;
		case SAND:
			break;
		case SETBLOCK:
			break;
		case SHOW:
			break;
		case SOUND:
			break;
		case SOUND_RANDOM:
			break;
		case SUMMON:
			break;
		case TP:
			break;
		case WATER:
			break;
		case FUNCTION:
			break;
		case CREATIVE:
			break;
		}
	}

	private static void generateDefence(World world, @Nullable EntityLivingBase entity, @Nullable DamageSource source, EnchType type, String value) {
		switch (type) {
		case CRIT:
			break;
		case DAMAGE_SELF:
			break;
		case DROP_ITEM_SELF:
			break;
		case DROP_ITEM_TARGET:
			break;
		case EFFECT:
			break;
		case EFFECT_CLEAR:
			break;
		case EFFECT_RANDOM:
			break;
		case EXPLOSION:
			break;
		case FILL:
			break;
		case FIRE:
			break;
		case GIVE:
			break;
		case KILL:
			break;
		case KNOCKBACK:
			break;
		case LIGHTBOLT:
			break;
		case PRINT:
			break;
		case SAND:
			break;
		case SETBLOCK:
			break;
		case SHOW:
			break;
		case SOUND:
			break;
		case SOUND_RANDOM:
			break;
		case SUMMON:
			break;
		case TP:
			break;
		case WATER:
			break;
		case FUNCTION:
			break;
		case CREATIVE:
			break;
		}
	}

	private static void generateAttack(EntityLivingBase entity, Entity damager, EnchType type, String value, double amount) {
		switch (type) {
		case CRIT:
			break;
		case DAMAGE_SELF:
			break;
		case DROP_ITEM_SELF:
			break;
		case DROP_ITEM_TARGET:
			break;
		case EFFECT:
			break;
		case EFFECT_CLEAR:
			break;
		case EFFECT_RANDOM:
			break;
		case EXPLOSION:
			break;
		case FILL:
			break;
		case FIRE:
			break;
		case GIVE:
			break;
		case KILL:
			break;
		case KNOCKBACK:
			break;
		case LIGHTBOLT:
			break;
		case PRINT:
			break;
		case SAND:
			break;
		case SETBLOCK:
			break;
		case SHOW:
			break;
		case SOUND:
			break;
		case SOUND_RANDOM:
			break;
		case SUMMON:
			break;
		case TP:
			break;
		case WATER:
			break;
		case FUNCTION:
			break;
		case CREATIVE:
			break;
		}
	}
	
	private static void generateArrowAttack(EntityLivingBase entity, Entity damager, EnchType type, String value, double amount) {
		switch (type) {
		case CRIT:
			if (Concench.random.nextInt(100) < Integer.parseInt(value))
				entity.attackEntityFrom(ConcenchDamageSources.causeCritDamageArrow(entity, damager), (float) amount);
			break;
		case DAMAGE_SELF:
			break;
		case DROP_ITEM_SELF:
			break;
		case DROP_ITEM_TARGET:
			break;
		case EFFECT:
			break;
		case EFFECT_CLEAR:
			break;
		case EFFECT_RANDOM:
			break;
		case EXPLOSION:
			break;
		case FILL:
			break;
		case FIRE:
			break;
		case GIVE:
			break;
		case KILL:
			break;
		case KNOCKBACK:
			break;
		case LIGHTBOLT:
			break;
		case PRINT:
			break;
		case SAND:
			break;
		case SETBLOCK:
			break;
		case SHOW:
			break;
		case SOUND:
			break;
		case SOUND_RANDOM:
			break;
		case SUMMON:
			break;
		case TP:
			break;
		case WATER:
			break;
		case FUNCTION:
			break;
		case CREATIVE:
			break;
		}
	}
	
	private static void generateArrowDefencee(EntityLivingBase entity, Entity damager, EnchType type, String value, double amount) {
				
	}
}