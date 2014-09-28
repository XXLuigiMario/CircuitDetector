package io.github.netdex.CircuitDetector.listeners;

import io.github.netdex.CircuitDetector.CircuitDetector;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * A listener which removes violations if their block is removed
 */
public class BlockBreakListener implements Listener {
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){ // Remove the violations if the violating block is broken, pretty much useless because of ExistenceListener
		Location loc = event.getBlock().getLocation();
		if(CircuitDetector.violations.containsKey(loc)){
			CircuitDetector.violations.remove(loc);
		}
	}
}
