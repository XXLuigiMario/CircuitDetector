package io.github.netdex.CircuitDetector;

import io.github.netdex.CircuitDetector.listeners.ExistenceListener;
import io.github.netdex.CircuitDetector.util.Utility;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * Introductory Comments
 * Are you tired of continuously looping circuits which lag the server,
 * and want to discover where those circuits are and even destroy them? 
 * Circuit Detector allows you to do all that and more.
 *
 * Features
 *
 * - Detect running redstone circuits
 * - Find the location of running redstone circuits
 * - Automatically destroy redstone circuits after they have reached a set threshold of loops within 60 seconds
 * 
 * Commands
 * - /cd log : Begins telling the player about redstone events occuring on the server,
 * such as when redstone torches toggle, redstone activates, repeaters activate etc.
 * - /cd unlog : Stops telling the player.
 * - /cd list : Lists all locations which have been causing redstone events on the server within the past 60 seconds.
 * - /cd kill <x> <y> <z> : Destroys any circuit connected to the block at xyz.
 * - /cd setthreshold <int> : Allows a circuit to loop <int> times before 
 * automatically destroying it using the same method as /cd kill. 
 * When the value is 0, circuits may loop indefinitely. Default value is 0.
 */

public class CircuitDetector extends JavaPlugin implements Listener {
	public static HashMap<String, Boolean> logging = new HashMap<String, Boolean>(); // Stores players who are logging
	public static HashMap<Location, Integer> violations = new HashMap<Location, Integer>(); // Stores violations
	protected static int threshold = 0; // The threshold at which to destroy circuits
	private FileConfiguration config;
	private Thread existenceListener;
	private ExistenceListener listener;
	private static int refreshTime = 60; // The time in seconds before all violations are wiped
	
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
		listener = new ExistenceListener(this);
		existenceListener = new Thread(listener);
		existenceListener.start();
		
		config = getConfig();
		
		if(config.get("threshold") != null){
			threshold = config.getInt("threshold");
		}
		if(config.get("refreshTime") != null){
			refreshTime = config.getInt("refreshTime");
		}
		
		this.getCommand("cd").setExecutor(new CommandManager()); // Register the CommandExecutor so commands are handled by the other class
		
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() { // Clear all violations every 60 seconds by default, or however configured
            @Override
            public void run() {
                try{
                	violations.clear();
                }catch(Exception e){
                	
                }
            	
            }
        }, 0L, refreshTime * 20L); 
	}
	public void onDisable(){
		listener.kill();
		config.set("threshold", threshold);
		config.set("refreshTime", refreshTime);
		saveConfig();
		
		violations = null;
		logging = null;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBlockRedstoneChange(BlockRedstoneEvent event){ // Handles the main use of this plugin, when a redstone event happens, log it as a violation
		Block b = event.getBlock();
		if(event.getOldCurrent() == 0){
			if(Utility.isRedstone(b)){
				if(violations.get(b.getLocation()) == null){ // If this violation is new, give it a count of 1
					violations.put(b.getLocation(), 1);
				}
				else{
					violations.put(b.getLocation(), violations.get(b.getLocation()) + 1); // Add 1 to the violation count
				}
				if(violations.get(b.getLocation()) > threshold && threshold != 0){ // If the threshold is passed, destroy the circuit
					Utility.destroyCircuit(b, true);
				}
				for(String s : logging.keySet()){
					if(logging.get(s)){ // Send a message to all players who have logging enabled
						Player player = Bukkit.getPlayer(s);
						
						String formattedLocation = Utility.formatLocation(b.getLocation());
						String msg = ChatColor.BLUE + Utility.getDate() + ChatColor.DARK_GRAY + " : " + ChatColor.AQUA + "\"" + ChatColor.ITALIC + b.getType().name() 
								+ ChatColor.AQUA + "\" at " + ChatColor.GRAY + formattedLocation 
								+ ChatColor.DARK_RED + " x" + violations.get(b.getLocation());
						player.sendMessage(msg);
					}
				}
			}
			
		}
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){ // Remove the violations if the violating block is broken, pretty much useless because of ExistenceListener
		Location loc = event.getBlock().getLocation();
		if(violations.containsKey(loc)){
			violations.remove(loc);
		}
	}
	
	/**
	 * Simple getter to save time
	 * @return a hashmap containing the violations
	 */
	public HashMap<Location, Integer> getViolations(){
		return violations;
	}
	
	
	
}
