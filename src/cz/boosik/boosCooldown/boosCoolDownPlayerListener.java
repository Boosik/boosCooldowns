package cz.boosik.boosCooldown;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import util.boosChat;

//import org.bukkit.event.entity.EntityDamageEvent;

@SuppressWarnings("deprecation")
public class boosCoolDownPlayerListener extends PlayerListener {
	private final boosCoolDown plugin;
	private static ConcurrentHashMap<String, Location> playerloc = new ConcurrentHashMap<String, Location>();
	private static ConcurrentHashMap<String, String> playerworld = new ConcurrentHashMap<String, String>();

	public boosCoolDownPlayerListener(boosCoolDown instance) {
		plugin = instance;
	}

	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) {
			return;
		}
		String message = event.getMessage();
		Player player = event.getPlayer();
		boolean on = true;

		if (player.isOp()) {
			on = false;
		}
		if (boosCoolDown.isUsingPermissions()
				&& boosCoolDown.getPermissions().has(player,
						"booscooldowns.exception")) {
			on = false;
		} else if (player.isOp()) {
			on = false;
		} else {
			on = true;
		}

		if (on) {
			playerloc.put(player.getName() + "@", player.getLocation());
			playerworld.put(player.getName() + "@", player.getWorld().getName());
			int i = message.indexOf(' ');
			if (i < 0) {
				i = message.length();
			}

			String preCommand = message.substring(0, i);
			String messageCommand = message.substring(i, message.length());

			boolean onCooldown = this.checkCooldown(event, player, preCommand,
					messageCommand);

			if (!onCooldown && messageCommand.length() > 1) {
				int j = messageCommand.indexOf(' ', 1);
				if (j < 0) {
					j = messageCommand.length();
				}

				String preSub = messageCommand.substring(1, j);
				String messageSub = messageCommand.substring(j,
						messageCommand.length());
				preSub = preCommand + ' ' + preSub;
				onCooldown = this.checkCooldown(event, player, preSub,
						messageSub);
			}
		}
	}

	// Returns true if the command is on cooldown, false otherwise
	private boolean checkCooldown(PlayerCommandPreprocessEvent event,
			Player player, String pre, String message) {
		int warmUpSeconds = 0;
		if (boosCoolDown.isUsingPermissions()) {
			if (!boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup2")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.warmup3")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.warmup4")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.warmup5")) {
				warmUpSeconds = boosConfigManager.getWarmUp(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup2")) {
				warmUpSeconds = boosConfigManager.getWarmUp2(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup3")) {
				warmUpSeconds = boosConfigManager.getWarmUp3(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup4")) {
				warmUpSeconds = boosConfigManager.getWarmUp4(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup5")) {
				warmUpSeconds = boosConfigManager.getWarmUp5(player, pre);
			}
		} else {
			warmUpSeconds = boosConfigManager.getWarmUp(player, pre);
		}
		if (boosCoolDown.isUsingPermissions()) {
			if (warmUpSeconds > 0
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.nowarmup")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.nowarmup." + pre)) {
				if (!boosCoolDownManager.checkWarmUpOK(player, pre, message)) {
					if (boosCoolDownManager.checkCoolDownOK(player, pre,
							message)) {
						boosWarmUpManager.startWarmUp(this.plugin, player, pre,
								message, warmUpSeconds);
						event.setCancelled(true);
						return true;
					} else {
						event.setCancelled(true);
						return true;
					}
				} else {
					if (boosCoolDownManager.coolDown(player, pre, message)) {
						event.setCancelled(true);
						return true;
					} else {
						boosCoolDownManager
								.removeWarmUpOK(player, pre, message);
					}
				}
			} else {
				if (boosCoolDownManager.coolDown(player, pre, message)) {
					event.setCancelled(true);
					return true;
				}
			}
		} else {
			if (warmUpSeconds > 0) {
				if (!boosCoolDownManager.checkWarmUpOK(player, pre, message)) {
					if (boosCoolDownManager.checkCoolDownOK(player, pre,
							message)) {
						boosWarmUpManager.startWarmUp(this.plugin, player, pre,
								message, warmUpSeconds);
						event.setCancelled(true);
						return true;
					} else {
						event.setCancelled(true);
						return true;
					}
				} else {
					if (boosCoolDownManager.coolDown(player, pre, message)) {
						event.setCancelled(true);
						return true;
					} else {
						boosCoolDownManager
								.removeWarmUpOK(player, pre, message);
					}
				}
			} else {
				if (boosCoolDownManager.coolDown(player, pre, message)) {
					event.setCancelled(true);
					return true;
				}
			}
		}
		if (boosCoolDown.isUsingEconomy()) {
			if (boosConfigManager.getPrice(player, pre) > 0) {
				if (!boosCoolDown.getPermissions().has(player,
						"booscooldowns.noprice")
						&& !boosCoolDown.getPermissions().has(player,
								"booscooldowns.noprice." + pre)) {
					if (boosCoolDown.getEconomy().getBalance(player.getName()) >= boosConfigManager
							.getPrice(player, pre)) {
						boosPriceManager.payForCommand(player, pre, message);
					} else {
						boosPriceManager.payForCommand(player, pre, message);
						boosCoolDownManager.cancelCooldown(player, pre);
						event.setCancelled(true);
						return true;
					}
				}
			}
		}
		return false;
	}

	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		if (boosCoolDown.isUsingPermissions()) {
			if (player != null
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.nocancel.move")) {
				if (boosWarmUpManager.hasWarmUps(player) && hasMoved(player)) {
					clearLocWorld(player);
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getWarmUpCancelledByMoveMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		} else {
			if (player != null) {
				if (boosWarmUpManager.hasWarmUps(player) && hasMoved(player)) {
					clearLocWorld(player);
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getWarmUpCancelledByMoveMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		}
	}

	public static boolean hasMoved(Player player) {
		String curworld = player.getWorld().getName();
		String cmdworld = playerworld.get(player.getName() + "@");
		Location curloc = player.getLocation();
		Location cmdloc = playerloc.get(player.getName() + "@");
		if (curworld != cmdworld) {
			return true;
		} else if (cmdloc.distanceSquared(curloc) > 2) {
			return true;
		}

		return false;
	}
	
	public static void clearLocWorld(Player player){
		for (String key : playerloc.keySet()) {
			if (key.startsWith(player.getName() + "@")) {
				playerloc.remove(key);
			}
		}
		for (String key : playerworld.keySet()){
			if (key.startsWith(player.getName() + "@")){
				playerworld.remove(key);
			}
		}
	}

	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		if (boosCoolDown.isUsingPermissions()) {
			if (player != null
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.nocancel.sneak")) {
				if (boosWarmUpManager.hasWarmUps(player)) {
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getCancelWarmupOnSneakMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		} else {
			if (player != null) {
				if (boosWarmUpManager.hasWarmUps(player)) {
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getCancelWarmupOnSneakMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		}
	}

	public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		if (boosCoolDown.isUsingPermissions()) {
			if (player != null
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.nocancel.sprint")) {
				if (boosWarmUpManager.hasWarmUps(player)) {
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getCancelWarmupOnSprintMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		} else {
			if (player != null) {
				if (boosWarmUpManager.hasWarmUps(player)) {
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getCancelWarmupOnSprintMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		}
	}
}