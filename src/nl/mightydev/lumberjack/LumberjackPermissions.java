package nl.mightydev.lumberjack;

import org.bukkit.permissions.Permissible;


public class LumberjackPermissions {

	private final static String ALL = "lumberjack.all";
	
	public static boolean check(Permissible p) {
		if (!Plugin.manager.isPluginEnabled("PermissionsEx")) return true;
		else return p.hasPermission(ALL);
	}
}
