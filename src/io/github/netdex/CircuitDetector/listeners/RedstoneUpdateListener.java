package io.github.netdex.CircuitDetector.listeners;

import io.github.netdex.CircuitDetector.CircuitDetector;
import io.github.netdex.CircuitDetector.util.Utility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 * A listener which listens for changes in redstone events, and then adds a violation
 */
public class RedstoneUpdateListener implements Listener {
	
	@EventHandler
	public void onBlockRedstoneChange(BlockRedstoneEvent event){ // Handles the main use of this plugin, when a redstone event happens, log it as a violation
		Block b = event.getBlock();
		if(event.getOldCurrent() == 0){
			if(Utility.isRedstone(b)){
				if(CircuitDetector.violations.get(b.getLocation()) == null){ // If this violation is new, give it a count of 1
					CircuitDetector.violations.put(b.getLocation(), 1);
				}
				else{
					CircuitDetector.violations.put(b.getLocation(), CircuitDetector.violations.get(b.getLocation()) + 1); // Add 1 to the violation count
				}
				if(CircuitDetector.violations.get(b.getLocation()) > CircuitDetector.threshold && CircuitDetector.threshold != 0){ // If the threshold is passed, destroy the circuit
					Utility.destroyCircuit(b, true);
				}
				for(String s : CircuitDetector.logging.keySet()){
					if(CircuitDetector.logging.get(s)){ // Send a message to all players who have logging enabled
						Player player = Bukkit.getPlayer(s);
						
						String formattedLocation = Utility.formatLocation(b.getLocation());
						String msg = ChatColor.BLUE + Utility.getDate() + ChatColor.DARK_GRAY + " : " + ChatColor.AQUA + "\"" + ChatColor.ITALIC + b.getType().name() 
								+ ChatColor.AQUA + "\" at " + ChatColor.GRAY + formattedLocation 
								+ ChatColor.DARK_RED + " x" + CircuitDetector.violations.get(b.getLocation());
						player.sendMessage(msg);
					}
				}
			}
			
		}
	}

}
