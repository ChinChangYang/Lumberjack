package nl.mightydev.lumberjack;

import java.util.Random;
import nl.mightydev.lumberjack.player_data.PlayerData;
import nl.mightydev.lumberjack.util.LumberjackConfiguration;
import nl.mightydev.lumberjack.util.Message;
import nl.mightydev.lumberjack.util.PluginMessage;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class OnPlayerHit implements Listener {
	
	private Random random = new Random();
	
	public final static OnPlayerHit instance = new OnPlayerHit();

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		
		Block block = event.getBlock();
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		World world = block.getWorld();
		
		if(event.isCancelled()) return;		
		if(event instanceof LumberjackBlockBreakEvent) return;
		if(player.getGameMode() != GameMode.SURVIVAL) return;
		if(!LumberjackPermissions.check(player)) return;
		if(!data.enabled()) return;		
		if(block.getType() != Material.LOG) return;	

		// mcMMO support
		if(Plugin.manager.isPluginEnabled("mcMMO") && LumberjackConfiguration.mcMMOCheck()) {
			try {
				ClassLoader cl = Plugin.manager.getPlugin("mcMMO").getClass().getClassLoader();
				Class<?> c = Class.forName("com.gmail.nossr50.events.fake.FakeBlockBreakEvent", false, cl);
				if(c.isInstance(event)) return;
			} catch (ClassNotFoundException e) {
				PluginMessage.send("mcMMO's FakeBlockBreakEvent class not found, path might have been changed, contact Lumberjack author!");
				LumberjackConfiguration.setMcMMO(false);
				PluginMessage.send("mcMMO support is disabled.");
			}
		}

		MinecraftTree tree = MinecraftTree.getInstance(world, block, data);			
		if(!tree.isNatural()) return;
	
		if (data.silent() == false) {
			if (random.nextInt(8) > 0) {
				String message = getRandomMessage();
				Message.send(player, message);
			}
		}
		
		if(LumberjackConfiguration.breakFull()) {
			Block highest;
			while((highest = tree.removeTrunkTop()) != null) {
				if(block.getLocation().equals(highest.getLocation())) {
					continue;
				}
				fakeBlockBreak(highest, player, block.getLocation());
			}
		}
		else {
			Block highest = tree.removeTrunkTop();
			
			// no more blocks in tree
			if(highest == null) return;
			if(block.getLocation().equals(highest.getLocation())) {
				return;
			}
			
			BlockBreakEvent break_event = new LumberjackBlockBreakEvent(block, player);
			Plugin.manager.callEvent(break_event);
			if(break_event.isCancelled()) return;
			if(!block.getType().equals(Material.LOG)) return;
			
			fakeBlockBreak(highest, player, block.getLocation());
			event.setCancelled(true);
		}		
	}
	
	private String getRandomMessage() {
		switch(random.nextInt(7)) {
			case 0: return "Chop it like its hot";
			case 1: return "Do you feel the magic?";
			case 2: return "So easy...";
			case 3: return "Comfortably collecting wood yeah!";
			case 4: return "Be gone tree";
			case 5: return "May the axe be with you";
			case 6: return "Who needs an axe if you've got hands?";
			default: return "Who needs a hammer if you've got a workbe.. wait what?";
		}
	}
	
	private void fakeBlockBreak(Block block, Player player, Location breakLocation) {
		// reduce durability
		ItemStack item_in_hand = player.getItemInHand();
		int enchantmentLevel = 0;
		if (item_in_hand.containsEnchantment(Enchantment.DURABILITY)) {
			enchantmentLevel = item_in_hand.getEnchantmentLevel(Enchantment.DURABILITY);
		}
		
		if (random.nextInt(enchantmentLevel + 1) == 0) {
			short dur = (short) (item_in_hand.getDurability() + 1);
			item_in_hand.setDurability((short) dur);			
		}
		
		// drop item between player and wood
		Material material = block.getType();
		int amount = 1;
		byte data = (byte) (3 & block.getData());
		short damage = 0;
		ItemStack dropItem = new ItemStack(material, amount, damage, data);
		Location playerLocation = player.getLocation();
		Location dropLocation = playerLocation.add(breakLocation).multiply(0.5);
		dropLocation.setY(breakLocation.getY());
		block.getWorld().dropItemNaturally(dropLocation, dropItem);
		
		// destroy highest block
		block.setData((byte)0);
		block.setType(Material.AIR);
		
		return;
	}
}