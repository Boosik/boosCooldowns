package cz.boosik.boosCooldown;

import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.entity.Player;

public class boosWarmUpTimer extends TimerTask {
	
	private boosCoolDown bCoolDown;
	private Player player;
	private String pre;
	private String message;

	public boosWarmUpTimer(boosCoolDown bCoolDown, Timer timer, Player player,
			String pre, String message) {
		this.bCoolDown = bCoolDown;
		this.player = player;
		this.pre = pre;
		this.message = message;
	}

	public boosWarmUpTimer() {
	}

	@Override
	public void run() {
		bCoolDown.getServer().getScheduler().scheduleSyncDelayedTask(bCoolDown, new boosWarmUpRunnable());
	}
	
	public class boosWarmUpRunnable implements Runnable {
		@Override
		public void run() {
			if (player.isOnline() && !player.isDead() && boosWarmUpManager.hasWarmUps(player)) {
				boosCoolDownManager.setWarmUpOK(player, pre, message);
				boosWarmUpManager.removeWarmUpProcess(player.getName() + "@"
						+ pre);
				boosCoolDownPlayerListener.clearLocWorld(player);
				player.chat(pre + message);
			} else if (player.isOnline() && player.isDead() && boosWarmUpManager.hasWarmUps(player)){
				boosCoolDownManager.removeWarmUp(player, pre, message);
				boosWarmUpManager.removeWarmUpProcess(player.getName() + "@"
						+ pre);
				boosCoolDownPlayerListener.clearLocWorld(player);
			} else if (!player.isOnline() && boosWarmUpManager.hasWarmUps(player)){
				boosCoolDownManager.removeWarmUp(player, pre, message);
				boosWarmUpManager.removeWarmUpProcess(player.getName() + "@"
						+ pre);
				boosCoolDownPlayerListener.clearLocWorld(player);
			}
		}
	}
}