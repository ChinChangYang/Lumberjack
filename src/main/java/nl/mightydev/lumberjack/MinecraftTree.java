package nl.mightydev.lumberjack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.mightydev.lumberjack.player_data.PlayerData;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class MinecraftTree {

	public static MinecraftTree getInstance(World world, Block b, PlayerData d) {
		MinecraftTree tree = d.lastTree();
		if(tree == null) {
			tree = new MinecraftTree(b);
			d.lastTree(tree);
		}
		else {
			tree.refresh(world);
			if(tree.isInTrunk(b) == false) {
				tree = new MinecraftTree(b);
				d.lastTree(tree);
			}
		}

		return tree;
	}

	private String type = "undetermined";
	private boolean natural = true;
	private int tree_height;
	private int leaf_height;
	private final List<Block> leaves = new ArrayList<Block>();
	private final List<Block> trunk = new ArrayList<Block>();

	public MinecraftTree(Block trunk) {
		completeTree(trunk);
	}

	public void debug() {
		Logger l = Logger.getLogger("Minecraft");
		l.info("---MinecraftTree---");
		l.info("type: " + type);
		l.info("natural: " + (natural ? "true" : "false"));
		l.info("tree height: " + tree_height);
		l.info("leaf height: " + leaf_height);
		l.info("number of trunk blocks: " + trunk.size());
		l.info("number of leaf blocks: " + leaves.size());
		l.info("-------------------");
	}

	public Block getTrunkBase() {
		if(trunk.size() < 1) return null;
		Block base = trunk.get(0);

		for(Block b : trunk) {
			if(b.getY() < base.getY()) base = b;
		}

		return base;
	}

	public Block getTrunkTop() {
		if(trunk.size() < 1) return null;
		Block top = trunk.get(0);

		for(Block b : trunk) {
			if(b.getY() > top.getY()) top = b;
		}

		return top;
	}

	public Block removeTrunkTop() {
		Block top = getTrunkTop();
		trunk.remove(top);
		return top;
	}

	public Block getLowestLeaf() {
		if(leaves.size() < 1) return null;
		Block lowest = leaves.get(0);

		for(Block b : leaves) {
			if(b.getY() < lowest.getY()) lowest = b;
		}

		return lowest;
	}

	public boolean isInTrunk(Block block) {
		return trunk.contains(block);
	}

	public boolean isNatural() {
		return natural;
	}

	private void completeTree(Block source) {
		int type_id = source.getData() & 3;
		if(source.getType().equals(Material.LOG)) {
			switch(type_id) {
			case 0:
				type = "oak";
				completeOakTree(source);
				break;
			case 1:
				type = "redwood";
				completeRedwoodTree(source);
				break;
			case 2:
				type = "birch";
				completeBirchTree(source);
				break;
			case 3:
				type = "jungle";
				completeJungleTree(source);
			}
		}
		else {
			switch(type_id) {
			case 0:
			case 2:
				type = "acacia";
				completeOakTree(source);
				break;
			case 1:
			case 3:
				type = "darkoak";
				completeJungleTree(source);
				break;
			}
		}

		if(natural == false) return;

		if(leaves.size() < 4) {
			natural = false;
			return;
		}

		Block base = getTrunkBase();
		Block top = getTrunkTop();
		Block ground = null;

		if(base != null && top != null) {
			tree_height = top.getY() - base.getY() + 1;

			ground = base.getWorld().getBlockAt(base.getX(), base.getY() - 1, base.getZ());
		}

		Block lowest_leaf = getLowestLeaf();

		if(lowest_leaf != null) {
			leaf_height = lowest_leaf.getY() - base.getY();
		}

		if(ground != null && ground.getType() != Material.DIRT) {
			natural = false;
			return;
		}
	}

	private void completeOakTree(Block source) {
		if(!natural) return;

		if((source.getType() == Material.LEAVES || source.getType().equals(Material.LEAVES_2)) && !leaves.contains(source)) {
			leaves.add(source);
			natural = isNaturalLeaf(source);
		}
		else if((source.getType() == Material.LOG || source.getType().equals(Material.LOG_2)) && !trunk.contains(source)) {
			trunk.add(source);
			List<Block> adjacent = getDiagonallyAdjacentBlocks(source);
			for(Block b : adjacent) {
				completeOakTree(b);
			}
		}
	}

	private void completeBirchTree(Block source) {
		if(!natural) return;

		int x = source.getX(); int y = source.getY(); int z = source.getZ();
		World w = source.getWorld();
		Block b = w.getBlockAt(x,y,z);
		while(b != null && (b.getType() == Material.LOG || b.getType().equals(Material.LOG_2))) {
			trunk.add(b);
			completeBirchTreeLeaves(b);
			b = w.getBlockAt(x,++y,z);
		}
		y = source.getY() - 1;
		b = w.getBlockAt(x,y,z);
		while(b != null && (b.getType() == Material.LOG || b.getType().equals(Material.LOG_2))) {
			trunk.add(b);
			completeBirchTreeLeaves(b);
			b = w.getBlockAt(x,--y,z);
		}
	}

	private void completeBirchTreeLeaves(Block source) {
		List<Block> possible_leaves = getHorizontalAdjacentBlocks(source);
		for(Block b : possible_leaves) {
			if(b != null && (b.getType() == Material.LEAVES || b.getType().equals(Material.LEAVES_2))&&
				isNaturalLeaf(b) && !leaves.contains(b)) {
				leaves.add(b);
			}
		}
	}

	private void completeRedwoodTree(Block source){
		completeBirchTree(source);
	}

	private void completeJungleTree(Block source) {
		completeOakTree(source);
	}


	/**
	 *
	 * @param source
	 * @return all 26 blocks around the source blocks
	 */
	private List<Block> getDiagonallyAdjacentBlocks(Block source) {
		List<Block> blocks = new ArrayList<Block>(26);
		int xmin = source.getX() - 1; int xmax = xmin + 2;
		int ymin = source.getY() - 1; int ymax = ymin + 2;
		int zmin = source.getZ() - 1; int zmax = zmin + 2;
		World w = source.getWorld();
		for(int x = xmin; x <= xmax; x++) {
			for(int y = ymin; y <= ymax; y++) {
				for(int z = zmin; z <= zmax; z++) {
					if(x == source.getX() && y == source.getY() && z == source.getZ()) continue;
					blocks.add(w.getBlockAt(x, y, z));
				}
			}
		}
		return blocks;
	}

	/**
	 *
	 * @param source
	 * @return all 8 blocks horizontally around the source block
	 */
	private List<Block> getHorizontalAdjacentBlocks(Block source) {
		List<Block> blocks = new ArrayList<Block>(8);
		int xmin = source.getX() - 1; int xmax = xmin + 2;
		int y = source.getY();
		int zmin = source.getZ() - 1; int zmax = zmin + 2;
		World w = source.getWorld();
		for(int x = xmin; x <= xmax; x++) {
			for(int z = zmin; z <= zmax; z++) {
				if(x == source.getX() && z == source.getZ()) continue;
				blocks.add(w.getBlockAt(x, y, z));
			}
		}
		return blocks;
	}

	private boolean isNaturalLeaf(Block leaf) {
		/*
		 * If bit 0x4 is set, the leaves are permanent and will never decay.
		 * This bit is set on player-placed leaf blocks and overrides the
		 * meaning of bit 0x8.
		 *
		 * If bit 0x8 is set, the leaves will be checked for decay. The bit will
		 * be cleared after the check if the leaves do not decay. The bit will
		 * be set again whenever a block adjacent to the leaves is changed.
		 *
		 * Reference: http://www.minecraftwiki.net/wiki/Data_values#Leaves
		 */
		return (leaf.getData() & (1<<2)) == 0;
	}

	public void refresh(World world) {
		for(int i = trunk.size() - 1; i >= 0; i--) {
			Block o = trunk.get(i);
			Block n = world.getBlockAt(o.getX(), o.getY(), o.getZ());
			if(n.getType() == Material.LOG || n.getType() == Material.LOG_2) {
				trunk.set(i, n);
			}
			else {
				trunk.remove(i);
			}
		}
	}

}
