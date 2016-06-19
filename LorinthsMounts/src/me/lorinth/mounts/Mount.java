package me.lorinth.mounts;

import java.util.List;

import net.minecraft.server.v1_10_R1.GenericAttributes;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Mount {

	private double speed;
	private double jump;
	private double health;
	
	private String name;
	
	private Integer itemId;
	private List<String> lore;
	private Integer armorId;
	
	private int styleId;
	private int colorId;
	private int variantId;
	
	private ItemStack displayItem;
	
	public Mount(String name, int item, List<String> lore, double spd, double jump, double hp, int armor, int variantId, int colorId, int styleId){
		this.name = name;
		itemId = item;
		this.lore = lore;
		speed = spd;
		this.jump = jump;
		health = hp;
		armorId = armor;
		this.variantId = variantId;
		this.colorId = colorId;
		this.styleId = styleId;
		
		ItemStack display = new ItemStack(item);
		ItemMeta meta = display.getItemMeta();
		meta.setLore(lore);
		meta.setDisplayName(name);
		display.setItemMeta(meta);
		displayItem = display;
	}
	
	public String getName(){
		return name;
	}
	
	public Horse spawn(Player player){
		Horse h = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
		
		h.setTamed(true);
		h.setAdult();
		try{
			h.setVariant(Variant.values()[variantId]);
		}
		catch(IndexOutOfBoundsException e){
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[ERROR][Mounts] : Variant value, " + variantId + ", on mount, " + name + " is invalid use between 0-" + (Variant.values().length-1));
			h.setVariant(Variant.values()[0]);
		}
		try{
			h.setStyle(Style.values()[styleId]);
		}
		catch(IndexOutOfBoundsException e){
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[ERROR][Mounts] : Style value, " + styleId + ", on mount, " + name + " is invalid use between 0-" + (Style.values().length-1));
			h.setStyle(Style.values()[0]);
		}
		
		try{
			h.setColor(Color.values()[colorId]);
		}
		catch(IndexOutOfBoundsException e){
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[ERROR][Mounts] : Color value, " + colorId + ", on mount, " + name + " is invalid use between 0-" + (Color.values().length-1));
			h.setColor(Color.values()[0]);
		}
		h.setMaxHealth(health);
		h.setHealth(health);
		
		h.setCustomName(name);
		
		h.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		if(armorId != 0){
			h.getInventory().setArmor(new ItemStack(armorId));
		}
		h.setJumpStrength(jump);
		((CraftLivingEntity) h).getHandle().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.2 * (speed / 100.0));
		
		h.setPassenger(player);
		return h;
	}
	
	public ItemStack getDisplayItem(){
		return displayItem;
	}
	
}
