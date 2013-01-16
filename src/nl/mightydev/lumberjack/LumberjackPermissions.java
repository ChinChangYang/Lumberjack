package nl.mightydev.lumberjack;

import org.bukkit.permissions.Permissible;


public class LumberjackPermissions {

	private final static String ALL = "lumberjack.all";
	
	public static boolean check(Permissible p) {
		if (!isPermissionPluginEnabled()) return true;
		else return p.hasPermission(ALL);
	}
	
	private static boolean isPermissionPluginEnabled() {
		if (Plugin.manager.isPluginEnabled("PermissionsEx")) return true;
		if (Plugin.manager.isPluginEnabled("PermissionsBukkit")) return true;
		return false;
	}
}
