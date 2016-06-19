package me.lorinth.mounts;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LorinthsMountsMain extends JavaPlugin implements Listener{

	ConsoleCommandSender console;
	
	HashMap<Player, MountWindow> openMountWindows = new HashMap<Player, MountWindow>();
	HashMap<String, Mount> mounts = new HashMap<String, Mount>();
	public HashMap<Player, Horse> activeHorses = new HashMap<Player, Horse>();
	
	
	public ArrayList<Player> mountCooldowns = new ArrayList<Player>();
	public boolean notify = true;
	
	File config;
	YamlConfiguration configYml;
	
	File mount;
	YamlConfiguration mountYml;

	public String windowName;
	
	public long cooldownDelay;
	
	
	
	@Override
	public void onEnable(){
		console = Bukkit.getServer().getConsoleSender();
		
		LoadFiles();
		
		printLine("Has been enabled");
		
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
	}
	
	void LoadFiles(){
		//Load config
		//Load mounts
		LoadMounts();
	}
	
	void LoadMounts(){
		mount = new File(getDataFolder(), "mounts.yml");
		if(!mount.exists()){
			
			new File(this.getDataFolder() + "").mkdir();
			
			try {
				if(mount.createNewFile()){
					try {
						PrintWriter writer = new PrintWriter(mount);
						
						writer.write( "#Developer : Lorinthios \n"
								+"Config:\n"
								+"    Notify: true #shows mount/dismount messages to players\n"
								+"    Cooldown: 20 #Cooldown, in seconds, between summons, set to 0 if you don't want a cooldown\n"
								+"    WindowName: \"<name>'s Mounts\""
								+"Mounts:\n"
								+"    'IronSteed': #Use permission LMounts.IronSteed\n"
								+"        DisplayName: '&5Iron Steed' #Display name on item and above mounts head, can take colors\n"
								+"        Item: 417 #Display item in menu\n"
								+"        Lore: #This can take color codes\n"
								+"        - '&6This mount is clad in iron armor'\n"
								+"        - '&7Speed : 150%'\n"
								+"        - '&8Jump : 120%'\n"
								+"        Speed: 150 #% of player speed\n"
								+"        Jump: 0.7 #Scales between 0 and 2, 0.7 is average\n"
								+"        Health: 1 #1hp = 1 hit dead\n"
								+"        Armor: 417 #0=none, 417=iron, 418=gold, 419=diamond\n"
								+"        Variant: 0 #Between 0-4\n"
								+"        Color: 0 #Between 0-6\n"
								+"        Style: 0 #Between 0-4\n");
						writer.close();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println(mount.getAbsolutePath());
				e.printStackTrace();
			}
		}
		mountYml = YamlConfiguration.loadConfiguration(mount);
		boolean mountYmlChanged = false;
		
		Set<String> configSettings = mountYml.getConfigurationSection("Config").getKeys(false);
		
		notify = mountYml.getBoolean("Config.Notify");
		cooldownDelay = mountYml.getLong("Config.Cooldown");
		if(configSettings.contains("WindowName")){
			windowName = mountYml.getString("Config.WindowName");
		}
		else{
			mountYml.set("Config.WindowName", "<name>'s Mounts");
			windowName = "<name>'s Mounts";
			mountYmlChanged = true;
		}
		
		for(String name : mountYml.getConfigurationSection("Mounts").getKeys(false)){
			int item = mountYml.getInt("Mounts." + name + ".Item");
			String displayname = convertToMColors(mountYml.getString("Mounts." + name + ".DisplayName"));
			double speed = mountYml.getDouble("Mounts." + name + ".Speed");
			double jump = mountYml.getDouble("Mounts." + name + ".Jump");
			double hp = mountYml.getDouble("Mounts." + name + ".Health");
			List<String> lore = convertToMColors(mountYml.getStringList("Mounts." + name + ".Lore"));
			int armor = mountYml.getInt("Mounts." + name + ".Armor");
			int variant = mountYml.getInt("Mounts." + name + ".Variant");
			int color = mountYml.getInt("Mounts." + name + ".Color");
			int style = mountYml.getInt("Mounts." + name + ".Style");
			
			Mount m = new Mount(displayname, item, lore, speed, jump, hp, armor, variant, color, style);
			
			mounts.put(name, m);
		}
		
		if(mountYmlChanged){
			try {
				mountYml.save(mount);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
	}
		
	@Override
	public void onDisable(){
		printErrorLine("Has been disabled");
		
		for(Horse p : activeHorses.values()){
			p.remove();
		}
		
	}
	
	void printLine(String line){
		console.sendMessage(ChatColor.GREEN + "[LorsMounts] : " + line);
	}
	
	void printErrorLine(String line){
		console.sendMessage(ChatColor.RED + "[LorsMounts] : " + line);
	}
	
	void printWarningLine(String line){
		console.sendMessage(ChatColor.YELLOW + "[LorsMounts] : " + line);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void OnHorseHit(EntityDamageEvent e){
		if(e.getEntity() instanceof Horse){
			if(activeHorses.containsValue((Horse)e.getEntity())){
				activeHorses.remove(((Horse)e.getEntity()).getPassenger());
				((Horse)e.getEntity()).getInventory().clear();
				e.getEntity().remove();
				e.setDamage(0);
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void OnHorseDeath(EntityDeathEvent e){
		if(e.getEntity() instanceof Horse){
			if(activeHorses.containsValue((Horse)e.getEntity())){
				activeHorses.remove(((Horse)e.getEntity()).getPassenger());
				e.setDroppedExp(0);
				e.getDrops().clear();
			}
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){		
		if(sender instanceof Player){
			Player player = (Player) sender;
			if(commandLabel.equalsIgnoreCase("mounts")){
				MountWindow window = new MountWindow(this, getAvailableMounts(player), player);
				this.openMountWindows.put(player, window);
			}
		}
		return false;
	}
	
	@EventHandler
	public void OnWindowClick(InventoryClickEvent event){
		MountWindow win = openMountWindows.get(event.getWhoClicked());
		if(win != null){
			win.handleClick(event);
		}
	}
	
	@EventHandler
	public void OnWindowClose(InventoryCloseEvent event){
		Player p = (Player) event.getPlayer();
		MountWindow win = openMountWindows.get(p);
		if(win != null){
			openMountWindows.remove(p);
		}
	}
	
	@EventHandler
	public void OnHorseInventoryOpen(InventoryOpenEvent e){
		if(activeHorses.containsKey(e.getPlayer())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void OnHorseDismount(VehicleExitEvent event){
		if(event.getVehicle() instanceof Horse){
			Entity ent = event.getExited();
			if(ent instanceof Player){
				Horse h = activeHorses.get((Player) ent);
				if(h != null){
					activeHorses.remove((Player) ent);
					
					if(notify){
						((Player)ent).sendMessage(ChatColor.GRAY + "[Mount] : You have dismounted!");
					}
					
					h.remove();
				}
			}
		}
	}
	
	@EventHandler
	public void OnPlayerHit(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			if(activeHorses.containsKey(player)){
				Horse h = activeHorses.get(player);
				
				activeHorses.remove(player);
				
				if(notify){
					player.sendMessage(ChatColor.GRAY + "[Mount] : You have dismounted from combat!");
				}
				
				h.remove();
			}
		}
	}
	
	@EventHandler
	public void OnEntityDamage(EntityDamageByEntityEvent event){
		if(event.getDamager() instanceof Player){
			Player player = (Player) event.getDamager();
			if(activeHorses.containsKey(player)){
				Horse h = activeHorses.get(player);
				
				activeHorses.remove(player);
				
				if(notify){
					player.sendMessage(ChatColor.GRAY + "[Mount] : You have dismounted from combat!");
				}
				
				h.remove();
			}
		}
	}
	
	@EventHandler
	public void OnPlayerQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();
		if(activeHorses.containsKey(player)){
			Horse h = activeHorses.get(player);
			activeHorses.remove(player);
			h.remove();
		}
	}
	
	@EventHandler
	public void OnPlayerKicked(PlayerKickEvent event){
		Player player = event.getPlayer();
		if(activeHorses.containsKey(player)){
			Horse h = activeHorses.get(player);
			activeHorses.remove(player);
			h.remove();
		}
	}
	
	List<Mount> getAvailableMounts(Player player){
		List<Mount> available = new ArrayList<Mount>();
		
		for(String name : mounts.keySet()){
			if(player.hasPermission("LMounts." + name)){
				available.add(mounts.get(name));
			}
		}
		
		return available;
	}
	
	public String convertToMColors(String line){
		return line.replaceAll("&", "§");
	}
	
	public List<String> convertToMColors(List<String> lines){
		List<String> newLines = new ArrayList<String>();
		for(String line : lines){
			newLines.add(convertToMColors(line));
		}
		return newLines;
	}
	
}
