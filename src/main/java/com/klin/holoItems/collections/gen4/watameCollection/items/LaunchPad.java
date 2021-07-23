package com.klin.holoItems.collections.gen4.watameCollection.items;

import com.klin.holoItems.HoloItems;
import com.klin.holoItems.abstractClasses.Crate;
import com.klin.holoItems.collections.gen4.watameCollection.WatameCollection;
import com.klin.holoItems.interfaces.Placeable;
import com.klin.holoItems.interfaces.Punchable;
import com.klin.holoItems.utility.Task;
import com.klin.holoItems.utility.Utility;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.HashSet;

public class LaunchPad extends Crate implements Placeable, Punchable {
    public static final String name = "launchPad";
    public static final HashSet<Enchantment> accepted = null;
    private static final Vector vertical = new Vector(0, 1, 0);

    private static final Material material = Material.SMOKER;
    private static final int quantity = 1;
    private static final String lore =
            "§6Ability" +"/n"+
                    "Light to launch a package";
    private static final int durability = 0;
    public static final boolean stackable = false;
    private static final boolean shiny = false;

    public static final int cost = 0;
    public static final char key = '1';

    public LaunchPad(){
        super(name, material, quantity, lore, durability, stackable, shiny, cost, ""+WatameCollection.key+key, key);
    }

    public void registerRecipes(){
        ShapedRecipe recipe =
                new ShapedRecipe(new NamespacedKey(HoloItems.getInstance(), name), item);
        recipe.shape("a a","bcb","ded");
        recipe.setIngredient('a', Material.SMOOTH_STONE_SLAB);
        recipe.setIngredient('b', Material.GLASS);
        recipe.setIngredient('c', Material.BARREL);
        recipe.setIngredient('d', Material.LODESTONE);
        recipe.setIngredient('e', Material.CAMPFIRE);
        recipe.setGroup(name);
        Bukkit.getServer().addRecipe(recipe);
    }

    public void ability(BlockPlaceEvent event){
        event.setCancelled(false);
        Block block = event.getBlock();
        TileState state = (TileState) block.getState();
        state.getPersistentDataContainer().set(Utility.key, PersistentDataType.STRING, id);
        state.update();
    }

    public void ability(PlayerInteractEvent event, Action action){
        if(action!=Action.RIGHT_CLICK_BLOCK)
            return;
        event.setCancelled(true);
        ItemStack fns = event.getItem();
        if(fns==null || fns.getType()!=Material.FLINT_AND_STEEL)
            return;
        Block clicked = event.getClickedBlock();
        World world = clicked.getWorld();
        Block block = world.getHighestBlockAt(clicked.getLocation());
        BlockState state = block.getState();
        Player player = event.getPlayer();
        if(!(state instanceof Barrel)) {
            player.sendMessage("Improper launch set up");
            return;
        }
        Barrel barrel = (Barrel) state;
        if(!UberSheepPackage.id.equals(barrel.getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING))) {
            player.sendMessage("Improper launch set up");
            return;
        }

        String name = barrel.getCustomName();
        if(name==null) {
            player.sendMessage("Destination set incorrectly");
            return;
        }
        Inventory inv = barrel.getInventory();
        ItemStack[] contents;
        if(inv.isEmpty()) {
            player.sendMessage("Delivery is empty");
            return;
        }
        Location dest = block.getLocation();
        boolean first = true;
        for(String coord : name.split(" ")){
            try {
                if(first)
                    dest.setX(Integer.parseInt(coord)+0.5);
                else
                    dest.setZ(Integer.parseInt(coord)+0.5);
                first = false;
            } catch(NumberFormatException e){
                player.sendMessage("Destination set incorrectly");
                return;
            }
        }
        Chunk chunk = dest.getChunk();
        if(!chunk.isLoaded())
            chunk.load();
        state = world.getHighestBlockAt(dest).getState();
        if(!(state instanceof Smoker) || !id.equals(((TileState) state).getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING))) {
            player.sendMessage("No receiving launch pad identified");
            return;
        }
        contents = inv.getContents().clone();
        inv.setContents(new ItemStack[27]);
        BlockData data = block.getBlockData();
        Location spawn = block.getLocation().add(0.5, 0.5, 0.5);
        FallingBlock drone = world.spawnFallingBlock(spawn, data);
        block.setType(Material.AIR);

        Smoker smoker = (Smoker) clicked.getState();
        smoker.setBurnTime((short) 20);
        smoker.update();
        world.spawnParticle(Particle.END_ROD, spawn, 4, 0, 0, 0, 0.05);

        new Task(HoloItems.getInstance(), 0, 1){
            int increment = 0;
            public void run() {
                if (increment >= 20 || !drone.isValid() || drone.getLocation().getY() > 256) {
                    drone.remove();
                    boolean observers = false;
                    for(Entity entity : world.getNearbyEntities(dest, 32, 32, 32)){
                        if(entity instanceof Player){
                            observers = true;
                            break;
                        }
                    }
                    if(observers) {
                        new Task(HoloItems.getInstance(), 1, 1) {
                            final FallingBlock drone = world.spawnFallingBlock(dest.clone().add(0, 256-dest.getY(), 0), data);
                            public void run() {
                                boolean observers = false;
                                for (Entity entity : world.getNearbyEntities(dest, 32, 32, 32)) {
                                    if (entity instanceof Player) {
                                        observers = true;
                                        break;
                                    }
                                }
                                if (!observers || increment >= 240 || !drone.isValid()) {
                                    if (drone != null)
                                        drone.remove();
                                    Block drop = world.getHighestBlockAt(dest);
                                    if (drop.getType() == Material.BARREL || !observers) {
                                        if (!observers) {
                                            drop = drop.getRelative(BlockFace.UP);
                                            drop.setType(Material.BARREL);
                                        }
                                        Barrel barrel = (Barrel) drop.getState();
                                        barrel.getPersistentDataContainer().set(Utility.key, PersistentDataType.STRING, UberSheepPackage.id);
                                        barrel.setCustomName(block.getX() + " " + block.getZ());
                                        barrel.update();

                                        barrel.getInventory().setContents(contents);
                                    } else {
                                        for (ItemStack content : contents) {
                                            if (content != null && content.getType() != Material.AIR)
                                                world.dropItemNaturally(drop.getRelative(BlockFace.UP).getLocation(), content);
                                        }
                                    }
                                    cancel();
                                    return;
                                }
                                increment++;
                            }
                        };
                    }
                    else{
                        Block drop = world.getHighestBlockAt(dest).getRelative(BlockFace.UP);
                        drop.setType(Material.BARREL);
                        Barrel barrel = (Barrel) drop.getState();
                        barrel.getPersistentDataContainer().set(Utility.key, PersistentDataType.STRING, UberSheepPackage.id);
                        barrel.setCustomName(block.getX() + " " + block.getZ());
                        barrel.update();

                        barrel.getInventory().setContents(contents);
                    }
                    cancel();
                    return;
                }
                drone.setVelocity(drone.getVelocity().add(vertical));
                increment++;
            }
        };
    }

    public void ability(BlockBreakEvent event) {
        event.setDropItems(false);
        super.ability(event);
    }
}