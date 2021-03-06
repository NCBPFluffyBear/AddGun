package com.programmerdan.minecraft.addgun.guns;

import com.programmerdan.minecraft.addgun.AddGun;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import com.programmerdan.minecraft.addgun.ArmorType;
import com.programmerdan.minecraft.addgun.ammo.AmmoType;

/**
 * Collection of static utilities to help navigate the world of guns.
 * 
 * @author ProgrammerDan
 *
 */
public class Utilities {

	private static final NamespacedKey AMMO_KEY = new NamespacedKey(AddGun.getPlugin(), "ammo");
	private static final NamespacedKey MAG_KEY = new NamespacedKey(AddGun.getPlugin(), "magazine");
	private static final NamespacedKey ROUNDS_KEY = new NamespacedKey(AddGun.getPlugin(), "rounds");
	private static final NamespacedKey TYPE_KEY = new NamespacedKey(AddGun.getPlugin(), "type");
	private static final NamespacedKey LIFETIME_SHOTS_KEY = new NamespacedKey(AddGun.getPlugin(), "lifetimeShots");
	private static final NamespacedKey HEALTH_KEY = new NamespacedKey(AddGun.getPlugin(), "health");
	private static final NamespacedKey OWNER_KEY = new NamespacedKey(AddGun.getPlugin(), "owner");
	private static final NamespacedKey GROUP_KEY = new NamespacedKey(AddGun.getPlugin(), "group");
	private static final NamespacedKey UNID_KEY = new NamespacedKey(AddGun.getPlugin(), "unid");

	
	/**
	 * Using a Minecraft core utility, this determines where against a hitbox impact occurred, if at all.
	 * 
	 * See hitInformation() for more details.
	 * 
	 * @param origin The starting point of travel
	 * @param velocity The travel velocity
	 * @param entity The entity to test hit box against
	 * @return Location of impact, or the _origin_ if no hit detected.
	 */
	/*
	public static Location approximateHitBoxLocation(Location origin, Vector velocity, Entity entity) {
		
		Vec3D hit = hitInformation(origin, velocity, entity);

		if (hit == null) {
			return origin;
		} else {
			return new Location(origin.getWorld(), hit.getX(), hit.getY(), hit.getZ(), origin.getYaw(), origin.getPitch());
		}
	}
	 */
	
	public static HitDigest detailedHitBoxLocation(Location origin, Vector velocity, Entity entity) {

		if (entity == null) { // No entity was hit
			return new HitDigest(HitPart.MISS, origin);
		} else {
			HitPart part = HitPart.MISS;
			double locY = entity.getLocation().getY();
			double hitY = origin.getY();
			double height = entity.getHeight();
			double head = entity.getHeight() - 0.25; // Approximation of head location
			double midsection = (height - head > 0) ? (height - head) / 2 : height / 2;
			double legs = (height - head > 0) ? (height - head) / 5 : height / 5;

			if (hitY < locY + height && hitY >= locY + head) {
				part = HitPart.HEAD;
			} else if (hitY < locY + head && hitY >= locY + midsection) {
				part = HitPart.BODY;
			} else if (hitY < locY + midsection && hitY >= locY + legs) {
				part = HitPart.LEGS;
			} else if (hitY < locY + legs && hitY >= locY) {
				part = HitPart.FEET;
			}
			
			return new HitDigest(part, new Location(origin.getWorld(), origin.getX(), origin.getY(), origin.getZ(), origin.getYaw(), origin.getPitch()));
		}
	}
	
	/**
	 * Soft wrapper to convert Bukkit objects on vector and impact into a MovingObjectPosition representing
	 * if the passed entity was intersected by a vector "velocity" with origin of "origin". This assumes a 
	 * no-size projectile. Use other techniques for entity on entity boxtests.
	 * 
	 * @param origin Starting point of travel
	 * @param velocity Travel path
	 * @param entity The entity to test intersection against.
	 * @return a MovingObjectPosition with intersection details, or null.
	 */
	/*
	public static Vec3D hitInformation(Location origin, Vector velocity, Entity entity) {
		
		// we have the bullet's last tick location, its velocity, and the barycenter of the object it hit, and that
		// object's hitbox. We also know for sure that the object was intersected with.
		AxisAlignedBB boundingBox = ((CraftEntity) entity).getHandle().getBoundingBox();
		Vec3D origLocation = new Vec3D(origin.getX(), origin.getY(), origin.getZ());
		Vec3D origVector = new Vec3D(origin.getX() + velocity.getX(), origin.getY() + velocity.getY(), origin.getZ() + velocity.getZ());
		
		Optional<Vec3D> intersectLoc = boundingBox.b(origLocation, origVector);
		return intersectLoc.isPresent() ? intersectLoc.get() : null;
	}
	 */


	/**
	 * Originally adapted from 1.8 computation TODO: doublecheck still valid.
	 * 
	 * @param e The entity  whose current XP to compute
	 * @return the number of XP in hotbar right now.
	 */
	public static int computeTotalXP(LivingEntity e) {
		if (e instanceof Player) {
			Player p = (Player) e;
	        float cLevel = (float) p.getLevel();
	        float progress = p.getExp();
	        float a = 1f, b = 6f, c = 0f, x = 2f, y = 7f;
	        if (cLevel > 16 && cLevel <= 31) {
	                a = 2.5f; b = -40.5f; c = 360f; x = 5f; y = -38f;
	        } else if (cLevel >= 32) {
	                a = 4.5f; b = -162.5f; c = 2220f; x = 9f; y = -158f;
	        }
	        return (int) Math.floor(a * cLevel * cLevel + b * cLevel + c + progress * (x * cLevel + y));
		} else { 
			return 0; //TODO perhaps some fixed amount?
		}
	}
	
	/**
	 * Estimates the XP this entity has in inventory.
	 * 
	 * @param entity the entity to check
	 * @return how much XP is held
	 */
	public static int getInvXp(LivingEntity entity) {
		if (entity == null)
			return 0;

		ItemStack[] inv;
		if (entity instanceof InventoryHolder) {
			// complex inventory
			InventoryHolder holder = (InventoryHolder) entity;
			inv = holder.getInventory().getContents();
		} else {
			// simple inventory
			inv = entity.getEquipment().getArmorContents();
		}

		int total = 0;
		if (inv != null) {
			for (ItemStack item : inv) {
				if (Material.EXPERIENCE_BOTTLE == item.getType()) {
					total += item.getAmount();
				}
			}
		}
		return total;
	}


	/**
	 * Attempts to reduce the complexity of all materials to a more
	 * manageable pile of enumeration
	 * 
	 * @param material any material
	 * @return the rough armor grade, if any
	 */
	public static ArmorType getArmorType(Material material) {
		switch(material) {
		case IRON_HORSE_ARMOR:
			return ArmorType.IRON_BARDING;
		case GOLDEN_HORSE_ARMOR:
			return ArmorType.GOLD_BARDING;
		case DIAMOND_HORSE_ARMOR:
			return ArmorType.DIAMOND_BARDING;
		case LEATHER_BOOTS:
		case LEATHER_HELMET:
		case LEATHER_CHESTPLATE:
		case LEATHER_LEGGINGS:
			return ArmorType.LEATHER;
		case IRON_BOOTS:
		case IRON_HELMET:
		case IRON_CHESTPLATE:
		case IRON_LEGGINGS:
			return ArmorType.IRON;
		case GOLDEN_BOOTS:
		case GOLDEN_HELMET:
		case GOLDEN_CHESTPLATE:
		case GOLDEN_LEGGINGS:
			return ArmorType.GOLD;
		case DIAMOND_BOOTS:
		case DIAMOND_HELMET:
		case DIAMOND_CHESTPLATE:
		case DIAMOND_LEGGINGS:
			return ArmorType.DIAMOND;
		case CHAINMAIL_BOOTS:
		case CHAINMAIL_CHESTPLATE:
		case CHAINMAIL_HELMET:
		case CHAINMAIL_LEGGINGS:
			return ArmorType.CHAIN;
		case SHIELD:
			return ArmorType.SHIELD;
		case ELYTRA:
			return ArmorType.WINGS;
		default:
			return ArmorType.NONE;
		}
	}
	
	/**
	 * Supported data right now:
	 * 
	 * "ammo": String with unique Bullet name of the ammo loaded
	 * "magazine": String with unique magazine name of the magazine used, if applicable
	 * "rounds": Integer with # of bullets or size of magazine
	 * "type": AmmoType; stored as a string, but we convert for you
	 * "lifetimeShots": total shots fired over whole life, a Long
	 * "health": remaining shots until 0 health, basically a hidden durability.
	 * "owner": UUID of last user.
	 * "group": String value describing the Citadel group this gun is locked to, if supported. (TODO)
	 *
	 * This rewrite converts NMS data storage to PersistentDataContainers
	 *
	 * @param gun
	 * @return
	 */
	public static Map<String, Object> getGunData(ItemStack gun) {
		Map<String, Object> gunMap = new HashMap<>();
		PersistentDataContainer pdc = gun.getItemMeta().getPersistentDataContainer();
		if (!pdc.isEmpty()) {

			if (pdc.has(AMMO_KEY)) {
				gunMap.put("ammo", pdc.get(AMMO_KEY, PersistentDataType.STRING));
			}

			if (pdc.has(MAG_KEY)) {
				gunMap.put("magazine", pdc.get(MAG_KEY, PersistentDataType.STRING));
			}

			if (pdc.has(ROUNDS_KEY)) {
				gunMap.put("rounds", pdc.get(ROUNDS_KEY, PersistentDataType.INTEGER));
			}

			if (pdc.has(TYPE_KEY)) {
				gunMap.put("type", AmmoType.valueOf(pdc.get(TYPE_KEY, PersistentDataType.STRING)));
			}

			if (pdc.has(LIFETIME_SHOTS_KEY)) {
				gunMap.put("lifetimeShots", pdc.get(LIFETIME_SHOTS_KEY, PersistentDataType.LONG));
			}

			if (pdc.has(HEALTH_KEY)) {
				gunMap.put("health", pdc.get(HEALTH_KEY, PersistentDataType.INTEGER));
			}

			if (pdc.has(OWNER_KEY)) {
				gunMap.put("owner", UUID.fromString(pdc.getOrDefault(OWNER_KEY, PersistentDataType.STRING, "")));
			}

			if (pdc.has(GROUP_KEY)) {
				gunMap.put("group", pdc.get(GROUP_KEY, PersistentDataType.STRING));
			}

			if (pdc.has(UNID_KEY)) {
				gunMap.put("unid", pdc.get(UNID_KEY, PersistentDataType.STRING));
			}
		}
		return gunMap;
	}
	
	/**
	 * This will update the fields passed in via the map, leaving other data unmodified.
	 * 
	 * @param gun the gun item to update
	 * @param update the limited set of data to update, fields not in the map are unchanged. A field in the map with NULL as value is removed.
	 * @return the ItemStack, augmented
	 */
	public static ItemStack updateGunData(ItemStack gun, Map<String, Object> update) {
		ItemMeta gunMeta = gun.getItemMeta();
		PersistentDataContainer pdc = gunMeta.getPersistentDataContainer();

		if (update.containsKey("ammo")) {
			String value = (String) update.get("ammo");
			pdc.set(AMMO_KEY, PersistentDataType.STRING, Objects.requireNonNullElse(value, ""));
		}
		
		if (update.containsKey("magazine")) {
			String value = (String) update.get("magazine");
			pdc.set(MAG_KEY, PersistentDataType.STRING, Objects.requireNonNullElse(value, ""));
		}
		
		if (update.containsKey("rounds")) {
			Integer value = (Integer) update.get("rounds");
			pdc.set(ROUNDS_KEY, PersistentDataType.INTEGER, Objects.requireNonNullElse(value, 0));
		}
		
		if (update.containsKey("type")) {
			AmmoType value = (AmmoType) update.get("type");
			pdc.set(TYPE_KEY, PersistentDataType.STRING, Objects.requireNonNullElse(value.toString(), ""));
		}
		
		if (update.containsKey("lifetimeShots")) {
			Long value = (Long) update.get("lifetimeShots");
			pdc.set(LIFETIME_SHOTS_KEY, PersistentDataType.LONG, Objects.requireNonNullElse(value, 0L));
		}
		
		if (update.containsKey("health")) {
			Integer value = (Integer) update.get("health");
			pdc.set(HEALTH_KEY, PersistentDataType.INTEGER, Objects.requireNonNullElse(value, 0));
		}
		
		if (update.containsKey("owner")) {
			UUID value = (UUID) update.get("owner");
			pdc.set(OWNER_KEY, PersistentDataType.STRING, Objects.requireNonNullElse(value.toString(), ""));
		}
		
		if (update.containsKey("group")) {
			String value = (String) update.get("group");
			pdc.set(GROUP_KEY, PersistentDataType.STRING, Objects.requireNonNullElse(value, ""));
		}
		
		if (update.containsKey("unid")) {
			String value = (String) update.get("unid");
			pdc.set(UNID_KEY, PersistentDataType.STRING, Objects.requireNonNullElse(value, ""));
		}

		ItemStack gunClone = gun.clone();
		gunClone.setItemMeta(gunMeta);
		
		return gunClone;
	}
	
	
	
	
	/**
	 * Supported data right now:
	 * 
	 * "ammo": String with unique Bullet name of the ammo loaded
	 * "rounds": Integer with # of bullets in magazine
	 * 
	 * @param magazine
	 * @return
	 */
	public static Map<String, Object> getMagazineData(ItemStack magazine) {
		Map<String, Object> magazineMap = new HashMap<>();
		PersistentDataContainer pdc = magazine.getItemMeta().getPersistentDataContainer();

		magazineMap.put("ammo", pdc.getOrDefault(AMMO_KEY, PersistentDataType.STRING, "None"));
		magazineMap.put("magazine", pdc.getOrDefault(MAG_KEY, PersistentDataType.STRING, "None"));
		magazineMap.put("rounds", pdc.getOrDefault(ROUNDS_KEY, PersistentDataType.INTEGER, 0));

		return magazineMap;
	}
	
	/**
	 * This will update the fields passed in via the map, leaving other data unmodified.
	 * 
	 * @param magazine the magazine item to update
	 * @param update the limited set of data to update, fields not in the map are unchanged. A field in the map with NULL as value is removed.
	 * @return the ItemStack, augmented
	 */
	public static ItemStack updateMagazineData(ItemStack magazine, Map<String, Object> update) {
		ItemMeta magMeta = magazine.getItemMeta();
		PersistentDataContainer pdc = magMeta.getPersistentDataContainer();

		if (update.containsKey("ammo")) {
			String value = (String) update.get("ammo");
			pdc.set(AMMO_KEY, PersistentDataType.STRING, Objects.requireNonNullElse(value, ""));
		}

		if (update.containsKey("magazine")) {
			String value = (String) update.get("magazine");
			pdc.set(MAG_KEY, PersistentDataType.STRING, Objects.requireNonNullElse(value, ""));
		}

		if (update.containsKey("rounds")) {
			Integer value = (Integer) update.get("rounds");
			pdc.set(ROUNDS_KEY, PersistentDataType.INTEGER, Objects.requireNonNullElse(value, 0));
		}

		ItemStack magClone = magazine.clone();
		magClone.setItemMeta(magMeta);

		return magClone;
	}
	
	/**
	 * private function to compute a soft sigmoid. Originally designed for time functions, can be used elsewhere
	 * 
	 * @param elapsed in fractions of a second
	 * @param asymptote fraction of a second of inflection
	 * @param y expansion factor, 0.25 for [0,.5] 0.5 for [0,1]
	 * @param spread smoothness of sigmoid, larger is a smoother curve (but high minimum) (2.5 - 5 is a good range)
	 * @return
	 */
	public static double sigmoid(double elapsed, double asymptote, double y, double spread) {
		double term = (elapsed - asymptote) / spread;
		return y + y * (term / Math.sqrt(1.0 + term * term));
	}
}
