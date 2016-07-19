package me.Lorinth.BossApi;

import java.util.ArrayList;
import java.util.HashMap;

import me.Lorinth.BossApi.BossInstance.HealthSection;
import me.Lorinth.BossApi.Abilities.Ability;
import me.Lorinth.BossApi.Events.BossSpawnEvent;

import net.minecraft.server.v1_10_R1.EntityInsentient;

import net.minecraft.server.v1_10_R1.GenericAttributes;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class Boss {

	public EntityType type = EntityType.ZOMBIE;
	public String name;
	public double hp = 20;
	public double damage = 4;
	public double movespeed = 0.2;
	
	public String mountName = "";
	public boolean removeMountOnDeath = false;
	
	public int expReward = 0;
	
	double effectRadius = 2;
	double effectCount = 10;
	int effectData = 0;
	public Effect particle;
	
	public String key;
	
	public ItemStack held = new ItemStack(Material.AIR);
	public ItemStack helm = new ItemStack(Material.AIR);
	public ItemStack chest = new ItemStack(Material.AIR);
	public ItemStack legs = new ItemStack(Material.AIR);
	public ItemStack feet = new ItemStack(Material.AIR);
	
	public String lootTable = "";
	
	public HashMap<HealthSection, ArrayList<Ability>> onHit = new HashMap<HealthSection, ArrayList<Ability>>();
	public HashMap<HealthSection, ArrayList<Ability>> whenHit = new HashMap<HealthSection, ArrayList<Ability>>();
	public HashMap<HealthSection, ArrayList<Ability>> onEnter = new HashMap<HealthSection, ArrayList<Ability>>();
	
	public Boss(EntityType type, String key, String name, double hp, double damage, double movespeed){
		this.type = type;
		this.key = key;
		this.name = name;
		this.hp = hp;
		this.damage = damage;
		this.movespeed = movespeed;
	}
	
	@SuppressWarnings({ "deprecation" })
	public BossInstance spawn(Location loc, boolean spawnerLinked){
		loc.add(0.5, 0, 0.5);
		
		CraftWorld world = (CraftWorld) loc.getWorld();
		LivingEntity entity = (LivingEntity) world.spawnEntity(loc, type);
		entity.getEquipment().setItemInHand(held);
		entity.getEquipment().setItemInHandDropChance(0);
		entity.getEquipment().setHelmet(helm);
		entity.getEquipment().setHelmetDropChance(0);
		entity.getEquipment().setChestplate(chest);
		entity.getEquipment().setChestplateDropChance(0);
		entity.getEquipment().setLeggings(legs);
		entity.getEquipment().setLeggingsDropChance(0);
		entity.getEquipment().setBoots(feet);
		entity.getEquipment().setBootsDropChance(0);
		
		EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
		nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(movespeed);
		nmsEntity.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(damage);
		nmsEntity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(24);
		nmsEntity.goalSelector.a();
		nmsEntity.setCustomName(name);
		nmsEntity.setCustomNameVisible(true);
		
		entity.setRemoveWhenFarAway(true);
		entity.setMaxHealth(hp);
		entity.setHealth(hp);
		
		BossInstance bi = new BossInstance(this, entity);
		bi.onHit = onHit;
		bi.whenHit = whenHit;
		bi.onEnter = onEnter;
		
		try{
			if(!mountName.equalsIgnoreCase("")){
				BossInstance mount = BossApi.getPlugin().bossNames.get(mountName).spawn(loc, false);
				mount.bossEntity.setPassenger(entity);
				bi.mount = mount;
			}
		}
		catch(NullPointerException e){
			
		}
		
		//System.out.println(particle);
		
		if(particle != null){
			bi.effectCount = effectCount;
			bi.effectRadius = effectRadius;
			bi.particle = particle;
			bi.StartParticles();
		}
		
		
		
		//BossApi.getPlugin().OnBossSpawn(new BossSpawnEvent(bossInstance, entity));
		BossApi.getPlugin().OnBossSpawn(new BossSpawnEvent(bi, entity));
		
		return bi;
	}
	
	/* On Hit
	 * - When boss hits, chance to cast
	 * 
	 * When Hit
	 * - When boss is hit, chance to cast
	 * 
	 * On Enter new phase
	 * - When boss enters new phase, casts everytime
	
	*/
	
}
