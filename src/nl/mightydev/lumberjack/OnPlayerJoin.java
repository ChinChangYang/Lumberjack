package nl.mightydev.lumberjack;

import nl.mightydev.lumberjack.player_data.PlayerData;
import nl.mightydev.lumberjack.util.LumberjackConfiguration;
import nl.mightydev.lumberjack.util.Message;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoin implements Listener {

	public static OnPlayerJoin instance = new OnPlayerJoin();

	@EventHandler
	public void onPlayerJoin(
			PlayerJoinEvent event) {
		
		Player p = event.getPlayer();

		if (!p.hasPermission(LumberjackPermissions.ALL)) return;
		if (LumberjackConfiguration.showLoginMessage() == false)
			return;

		PlayerData d = PlayerData.get(p);

		String s = d.enabled() ? Message.good("enabled") : Message
				.bad("disabled");
		Message.send(p, "Lumberjack loaded and " + s);

	}
}
