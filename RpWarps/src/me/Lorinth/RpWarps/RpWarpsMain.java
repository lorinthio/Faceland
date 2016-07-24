package me.Lorinth.RpWarps;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.MainHand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class RpWarpsMain extends JavaPlugin implements Listener{

	ConsoleCommandSender console;
	int curID = 0;
	long autosave = 300;
	
	ArrayList<Warp> serverWarps = new ArrayList<Warp>();
	ArrayList<Warp> accessWarps = new ArrayList<Warp>();
	HashMap<String, Warp> playerWarps = new HashMap<String, Warp>();
	HashMap<Player, WarpWindow> warpWindows = new HashMap<Player, WarpWindow>();
	HashMap<Player, PlayerWarpProfile> profiles = new HashMap<Player, PlayerWarpProfile>();
	HashMap<World, HashMap<Vector,Warp>> locationWarps = new HashMap<World, HashMap<Vector,Warp>>();
	HashMap<Player, WarpConfirmWindow> playerConfirms = new HashMap<Player, WarpConfirmWindow>();
	
	File warps;
	File players;
	public YamlConfiguration warpsYml;
	public YamlConfiguration playersYml;
	
	@Override
	public void onEnable(){
		console = Bukkit.getServer().getConsoleSender();
		
		setupMaps();
		loadFiles();
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){

			@Override
			public void run() {
				SaveData();
			}
			
		}, autosave * 20, autosave * 20);
		
		for(Player p : Bukkit.getOnlinePlayers()){
			PlayerWarpProfile profile = new PlayerWarpProfile(p, this);
			profiles.put(p, profile);
		}
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		printLine("has been enabled!");
	}
	
	@Override
	public void onDisable(){
		SaveData();
		printErrorLine("has been disabled!");
	}
	
	void setupMaps(){
		for(World w : Bukkit.getServer().getWorlds()){
			locationWarps.put(w, new HashMap<Vector, Warp>());
		}
	}
	
	void loadFiles(){
		warps = new File(getDataFolder(), "warps.yml");
		players = new File(getDataFolder(), "players.yml");
		
		if(!warps.exists()){
			new File(this.getDataFolder() + "").mkdir();
			
			try {
				if(warps.createNewFile()){
					try {
						PrintWriter writer = new PrintWriter(warps);
						
						writer.write( "#Developer : Lorinthios \n"
								+ "#This file will be populated when the warps are created\n"
								+ "CurID: 0\n"
								+ "Autosave: 300 #The delay in seconds that the file will autosave");
						writer.close();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
		if(!players.exists()){
			new File(this.getDataFolder() + "").mkdir();
			
			try {
				if(players.createNewFile()){
					try {
						PrintWriter writer = new PrintWriter(players);
						
						writer.write( "#Developer : Lorinthios \n"+
						"#This file will be filled when players start learning warps");
						writer.close();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
		
		warpsYml = YamlConfiguration.loadConfiguration(warps);
		curID = warpsYml.getInt("CurID");
		autosave = warpsYml.getLong("Autosave");
		try{
			Set<String> serverWarpList = warpsYml.getConfigurationSection("ServerWarps").getKeys(false);
			for(String id : serverWarpList){
				Warp w = LoadWarp(id, true);
				HashMap<Vector, Warp> warps = locationWarps.get(w.loc.getWorld());
				if(warps.containsKey(w.loc.toVector().toBlockVector())) {
					DestroyWarp(w);
					System.out.println("Removed warp, " + id);
				}
				else{
					warps.put(w.loc.toVector().toBlockVector(), w);
					locationWarps.put(w.loc.getWorld(), warps);
					this.serverWarps.add(w);
				}
			}
		}
		catch(NullPointerException e){
			//Doesn't Exist yet
		}
		try{
			Set<String> playerWarpList = warpsYml.getConfigurationSection("PlayerWarps").getKeys(false);
			for(String id : playerWarpList){
				Warp w = LoadWarp(id, false);
				HashMap<Vector, Warp> warps = locationWarps.get(w.loc.getWorld());
				if(warps.containsKey(w.loc.toVector().toBlockVector())){
					DestroyWarp(w);
					System.out.println("Removed warp, " + id);
				}
				else{
					warps.put(w.loc.toVector().toBlockVector(), w);
					locationWarps.put(w.loc.getWorld(), warps);
					this.playerWarps.put(id, w);
				}
			}
		}
		catch(NullPointerException e){
			//Doesn't Exist Yet
		}
		try{
			Set<String> accessList = warpsYml.getConfigurationSection("AccessPoints").getKeys(false);
			for(String id : accessList){
				System.out.println("Loading warps...");
				Warp w = LoadAccess(id);
				System.out.println("Loading complete!");
				HashMap<Vector, Warp> warps = locationWarps.get(w.loc.getWorld());
				if(warps.containsKey(w.loc.toVector().toBlockVector())){
					DestroyWarp(w);
					System.out.println("Removed warp, " + id);
				}
				else{
					warps.put(w.loc.toVector().toBlockVector(), w);
					locationWarps.put(w.loc.getWorld(), warps);
				}
			}
		}
		catch(NullPointerException e){
			//Doesn't Exist Yet
		}
		playersYml = YamlConfiguration.loadConfiguration(players);
	}
	
	public void ConvertWarp(Player player){
		Block b = player.getTargetBlock((Set<Material>)null, 10);
		Warp w = this.locationWarps.get(b.getWorld()).get(b.getLocation().toVector().toBlockVector());
		
		if(w.isOwnedByPlayer(player) && player.hasPermission("LRWarps.convert")){
			w.ServerOwned = !w.ServerOwned;
			w.makeDisplayItem();
			
			if(w.ServerOwned){
				playerWarps.remove(w.ID);
				warpsYml.set("PlayerWarps." + w.ID, null);
				w.Save();
				for(Player p : Bukkit.getOnlinePlayers()){
					PlayerWarpProfile profile = this.profiles.get(p);
					profile.knownWarps.remove(w.ID);
				}
				serverWarps.add(w);
			}
			else{
				serverWarps.remove(w);
				playerWarps.put(w.ID, w);
				warpsYml.set("ServerWarps." + w.ID, null);
				w.Save();
			}
			
			player.sendMessage(ChatColor.GREEN + "Converted the warp point!");
		}
	}
	
	public void MakeAccessWarp(Player player){
		Block b = player.getTargetBlock((Set<Material>)null, 10);
		Warp w = this.locationWarps.get(b.getWorld()).get(b.getLocation().toVector().toBlockVector());
		
		if(w.isOwnedByPlayer(player) && player.hasPermission("LRWarps.admin")){
			if(w.ServerOwned){
				serverWarps.remove(w.ID);
				warpsYml.set("ServerWarps." + w.ID, null);
			}
			else{
				playerWarps.remove(w);
				warpsYml.set("PlayerWarps." + w.ID, null);
				for(Player p : Bukkit.getOnlinePlayers()){
					PlayerWarpProfile profile = this.profiles.get(p);
					profile.knownWarps.remove(w.ID);
				}
			}
			
			w.isAccessPoint = true;
			w.Save();
			player.sendMessage(ChatColor.GREEN + "Made the warp an access point!");
		}
	}
	
	public void CreateWarp(Location loc, Player p){
		String id = getNextId();
		Warp w = new Warp(this, id, loc, p);

		profiles.get(p).AddWarp(w);
		
		this.playerWarps.put(id, w);
		HashMap<Vector, Warp> warps = this.locationWarps.get(p.getWorld());
		warps.put(loc.toVector().toBlockVector(), w);
		locationWarps.put(p.getWorld(), warps);
	}
	
	public void DestroyWarp(Warp w){
		if(w.isAccessPoint){
			this.accessWarps.remove(w);
			warpsYml.set("AccessPoints." + w.ID, null);
		}
		
		if(w.ServerOwned){
			this.serverWarps.remove(w);
			warpsYml.set("ServerWarps." + w.ID, null);
		}
		else{
			for(Player p : Bukkit.getOnlinePlayers()){
				PlayerWarpProfile profile = this.profiles.get(p);
				profile.knownWarps.remove(w.ID);
			}
			warpsYml.set("PlayerWarps." + w.ID, null);
			this.playerWarps.remove(w.ID);
		}
	}
	
	public Warp LoadAccess(String key){
		Warp w = new Warp(this);
		String worldname = warpsYml.getString("AccessPoints." + key + ".Location.World");
		double x, y, z;
		x = warpsYml.getDouble("AccessPoints." + key + ".Location.X");
		y = warpsYml.getDouble("AccessPoints." + key + ".Location.Y");
		z = warpsYml.getDouble("AccessPoints." + key + ".Location.Z");
		w.loc = new Location(Bukkit.getWorld(worldname), x, y, z);
		w.ID = key;
		w.isAccessPoint = true;
		return w;
	}
	
	public Warp LoadWarp(String key, boolean isServerOwned){
		Warp w = new Warp(this);
		
		if(isServerOwned){
			w.ServerOwned = true;
			w.ID = key;
			w.owner = warpsYml.getString("ServerWarps." + key + ".Owner");
			w.name = warpsYml.getString("ServerWarps." + key + ".Name");
			String worldname = warpsYml.getString("ServerWarps." + key + ".Location.World");
			double x, y, z;
			x = warpsYml.getDouble("ServerWarps." + key + ".Location.X");
			y = warpsYml.getDouble("ServerWarps." + key + ".Location.Y");
			z = warpsYml.getDouble("ServerWarps." + key + ".Location.Z");
			w.loc = new Location(Bukkit.getWorld(worldname), x, y, z);
			w.mat = Material.getMaterial(warpsYml.getString("ServerWarps." + key + ".Material"));
			if(w.mat == Material.AIR){
				w.mat = Material.ENDER_PEARL;
			}
		}
		else{
			w.ID = key;
			w.owner = warpsYml.getString("PlayerWarps." + key + ".Owner");
			w.name = warpsYml.getString("PlayerWarps." + key + ".Name");
			String worldname = warpsYml.getString("PlayerWarps." + key + ".Location.World");
			double x, y, z;
			x = warpsYml.getDouble("PlayerWarps." + key + ".Location.X");
			y = warpsYml.getDouble("PlayerWarps." + key + ".Location.Y");
			z = warpsYml.getDouble("PlayerWarps." + key + ".Location.Z");
			w.loc = new Location(Bukkit.getWorld(worldname), x, y, z);
			w.mat = Material.getMaterial(warpsYml.getString("PlayerWarps." + key + ".Material"));
			if(w.mat == Material.AIR){
				w.mat = Material.ENDER_PEARL;
			}
		}

		w.makeDisplayItem();
		return w;
	}
	
	public void SaveData(){
		printLine("Saving data...");
		boolean error = false;
		
		for(Warp warp : serverWarps){
			warp.Save();
		}
		for(Warp warp : playerWarps.values()){
			warp.Save();
		}
		
		for(Player p : Bukkit.getOnlinePlayers()){
			PlayerWarpProfile profile = profiles.get(p);
			profile.Save();
		}
		
		warpsYml.set("CurID", curID);
		
		try {
			playersYml.save(players);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
		}
		try {
			warpsYml.save(warps);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
		}
		
		if(error){
			printErrorLine("Data save, was unsuccessful, check logs");
		}
		else{
			printLine("Success!");
		}
	}
	
	String getNextId(){
		String next = "" + curID;
		
		curID ++;
		return next;
	}
	
	//
	//
	// EVENTS
	//
	//
	@EventHandler
	public void OnBlockPlace(BlockPlaceEvent event){
		if(event.isCancelled()){
			return;
		}
		
		Player p = event.getPlayer();
		if(event.getBlock().getType() == Material.SEA_LANTERN){
			Location loc = event.getBlock().getLocation();
			boolean one = loc.clone().add(1, 0, 0).getBlock().getType() == Material.DOUBLE_STEP;
			boolean two = loc.clone().add(-1, 0, 0).getBlock().getType() == Material.DOUBLE_STEP;
			boolean three = loc.clone().add(0, 0, 1).getBlock().getType() == Material.DOUBLE_STEP;
			boolean four = loc.clone().add(0, 0, -1).getBlock().getType() == Material.DOUBLE_STEP;
			if(one && two && three && four){
				CreateWarp(loc, p);
			}
		}
	}
	
	@EventHandler
	public void OnBlockDestroy(BlockBreakEvent event){
		if(event.isCancelled()){
			return;
		}
		
		if(event.getBlock().getType() == Material.SEA_LANTERN){
			Block b = event.getBlock();
			BlockVector bv = b.getLocation().toVector().toBlockVector();
			Warp w = this.locationWarps.get(b.getWorld()).get(bv);
			if(w != null){
				if(w.isOwnedByPlayer(event.getPlayer())){
					DestroyWarp(w);
					event.getPlayer().sendMessage(ChatColor.RED + "Warp destroyed!");
				}
				else{
					event.setCancelled(true);
				}
			}
		}
		
	}
	
	@EventHandler
	public void OnPlayerClickInventory(InventoryClickEvent e){
		if(warpWindows.containsKey((Player)e.getWhoClicked())){
			WarpWindow win = warpWindows.get((Player)e.getWhoClicked());
			win.HandleClickEvent(e);
			
			e.setCancelled(true);
		}
		if(playerConfirms.containsKey((Player)e.getWhoClicked())){
			WarpConfirmWindow win = playerConfirms.get((Player) e.getWhoClicked());
			win.handleClickEvent(e);
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void OnWindowClose(InventoryCloseEvent e){
		if(this.warpWindows.containsKey(e.getPlayer())){
			warpWindows.remove(e.getPlayer());
		}
		if(this.playerConfirms.containsKey(e.getPlayer())){
			playerConfirms.remove(e.getPlayer());
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(sender instanceof Player){
			Player p = (Player) sender;
			if(commandLabel.equals("quickwarp")){
				if(args.length == 0){
					p.sendMessage(ChatColor.GREEN + "Use " + ChatColor.WHITE + "/quickwarp name <name> " + ChatColor.GREEN + "to name it");
					p.sendMessage(ChatColor.GREEN + "Use " + ChatColor.WHITE + "/quickwarp icon " + ChatColor.GREEN + "to change the icon");
					if(p.hasPermission("LRWarps.convert")){
						p.sendMessage(ChatColor.GOLD + "Use " + ChatColor.WHITE + "/quickwarp convert " + ChatColor.GOLD + "to convert this to a server warp");
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("name")){
					if(args.length < 1){
						p.sendMessage(ChatColor.RED + "Use the command, "+ ChatColor.WHITE + "/quickwarp name <name> ");
					}
					
					String name = "";
					for(int i = 1; i<args.length; i++){
						name = name + args[i] + " ";
					}
					
					RenameWarp(p, name);
				}
				if(args[0].equalsIgnoreCase("icon")){
					ChangeIcon(p);
				}
				if(args[0].equalsIgnoreCase("convert")){
					ConvertWarp(p);
				}
				if(args[0].equalsIgnoreCase("setaccess")){
					MakeAccessWarp(p);
				}
				else if(args[0].equalsIgnoreCase("remove")){
					RemoveWarp(p);
				}

			}
		}
		return false;
	}
	
	private void RemoveWarp(Player p) {
		Block b = p.getTargetBlock((Set<Material>)null, 10);
		Warp w = this.locationWarps.get(b.getWorld()).get(b.getLocation().toVector().toBlockVector());
		if(w != null){
			if(w.isOwnedByPlayer(p)){
				HashMap<Vector, Warp> warps = locationWarps.get(b.getWorld());
				warps.remove(w);
				locationWarps.put(b.getWorld(), warps);
				DestroyWarp(w);
			}
		}
	}

	public void ChangeIcon(Player p){
		Block b = p.getTargetBlock((Set<Material>)null, 10);
		Warp w = this.locationWarps.get(b.getWorld()).get(b.getLocation().toVector().toBlockVector());
		if(w != null){
			if(w.isOwnedByPlayer(p)){
				try{
					if(p.getEquipment().getItemInMainHand() != null){
						w.mat = p.getEquipment().getItemInMainHand().getType();
						if(w.mat == Material.AIR){
							w.mat = Material.ENDER_PEARL;
						}
						w.makeDisplayItem();
					
						p.sendMessage(ChatColor.GREEN + "Set the icon of this warp to, " + w.mat.toString());
					}
					else{
						p.sendMessage(ChatColor.RED + "You must be holding an item to make an icon!");
					}
				}
				catch(NullPointerException e){
					p.sendMessage(ChatColor.RED + "You must be holding an item to make an icon!");
				}
			}
			else{
				p.sendMessage(ChatColor.RED + "You are not the owner of that warp!");
			}
		}
	}
	
	public void RenameWarp(Player p, String name){
		Block b = p.getTargetBlock((Set<Material>)null, 10);
		Warp w = this.locationWarps.get(b.getWorld()).get(b.getLocation().toVector().toBlockVector());
		if(w != null){
			if(w.isOwnedByPlayer(p)){
				w.name = this.convertToMColors(name);
				w.makeDisplayItem();
				
				p.sendMessage(ChatColor.GREEN + "Set the name of this warp to, " + w.name);
			}
		}
	}
	
	@EventHandler
	public void OnPlayerConnect(PlayerJoinEvent event){
		Player p = event.getPlayer();
		PlayerWarpProfile profile = new PlayerWarpProfile(p, this);
		profiles.put(p, profile);
	}
	
	@EventHandler
	public void OnPlayerDisconnect(PlayerQuitEvent event){
		profiles.get(event.getPlayer()).Save();
	}
	
	@EventHandler
	public void OnPlayerKicked(PlayerKickEvent event){
		profiles.get(event.getPlayer()).Save();
	}
	
	@EventHandler
	public void OnBlockRightClick(PlayerInteractEvent event){
        if (event.getHand() != EquipmentSlot.OFF_HAND) {
            return;
        }
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
		}
        if (event.getClickedBlock().getType() != Material.SEA_LANTERN) {
            return;
        }
        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        HashMap<Vector, Warp> warps = this.locationWarps.get(p.getWorld());
				
        Warp w = warps.get(b.getLocation().toVector().toBlockVector());

        if(w != null){
            profiles.get(p).AddWarp(w);
            WarpWindow win = new WarpWindow(p, this);
            this.warpWindows.put(p, win);

            event.setCancelled(true);
        }
    }
	
	//
	//
	// UTILITY
	//
	//
	
	void printLine(String line){
		console.sendMessage(ChatColor.GREEN + "[RpWarps] : " + line);
	}
	
	void printErrorLine(String line){
		console.sendMessage(ChatColor.RED + "[RpWarps] : " + line);
	}
	
	void printWarningLine(String line){
		console.sendMessage(ChatColor.YELLOW + "[RpWarps] : " + line);
	}
	
	public String convertToMColors(String line){
		return line.replaceAll("&", "§");
	}
	
}
