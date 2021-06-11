package com.klin.holoItems.collections.gen4.kanataCollection.items;

import com.klin.holoItems.HoloItems;
import com.klin.holoItems.Item;
import com.klin.holoItems.collections.gen4.kanataCollection.KanataCollection;
import com.klin.holoItems.interfaces.Interactable;
import com.klin.holoItems.utility.Task;
import com.klin.holoItems.utility.Utility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Zipline extends Item implements Interactable {
    public static final String name = "zipline";
    public static final Set<Enchantment> accepted = null;
    private static final Map<BlockFace, BlockFace> opposites = Stream.of(new BlockFace[][] {
            { BlockFace.UP, BlockFace.DOWN },
            { BlockFace.DOWN, BlockFace.UP },
            { BlockFace.NORTH, BlockFace.SOUTH },
            { BlockFace.SOUTH, BlockFace.NORTH },
            { BlockFace.EAST, BlockFace.WEST },
            { BlockFace.WEST, BlockFace.EAST },
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
    private final Set<BlockFace> faces = Stream.of(
            BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST).
            collect(Collectors.toCollection(HashSet::new));
    private static final Set<BlockFace> vertical = Stream.of(BlockFace.UP, BlockFace.DOWN).
            collect(Collectors.toCollection(HashSet::new));
    private static final Set<Material> fences = Stream.of(
            Material.ACACIA_FENCE, Material.BIRCH_FENCE, Material.OAK_FENCE, Material.DARK_OAK_FENCE, Material.CRIMSON_FENCE, Material.JUNGLE_FENCE, Material.NETHER_BRICK_FENCE, Material.SPRUCE_FENCE, Material.WARPED_FENCE, Material.CHAIN, Material.IRON_BARS).
            collect(Collectors.toCollection(HashSet::new));

    private static final Material material = Material.STRING;
    private static final int quantity = 1;
    private static final String lore =
            "§6Ability" +"/n"+
                "Ride fences, iron bars, and chains";
    private static final int durability = 2;
    private static final boolean stackable = false;
    private static final boolean shiny = true;

    public static final int cost = 0;
    public static final char key = '1';

    public Zipline(){
        super(name, accepted, material, quantity, lore, durability, stackable, shiny, cost,
                ""+KanataCollection.key+key, key);
    }

    public void registerRecipes(){
        ShapedRecipe recipe =
                new ShapedRecipe(new NamespacedKey(HoloItems.getInstance(), name), item);
        recipe.shape("***","*&*","***");
        recipe.setIngredient('*', Material.IRON_NUGGET);
        recipe.setIngredient('&', Material.LEAD);
        recipe.setGroup(name);
        Bukkit.getServer().addRecipe(recipe);
    }

    public void ability(PlayerInteractEvent event, Action action){
        if(action!=Action.RIGHT_CLICK_BLOCK)
            return;
        Block clicked = event.getClickedBlock();
        Material fence = clicked.getType();
        if(!fences.contains(fence))
            return;
        Player player = event.getPlayer();
        if(!((LivingEntity) player).isOnGround())
            return;

        Utility.addDurability(event.getItem(), -1, player);
        BlockFace forward = player.getFacing();
        Set<BlockFace> faces = new HashSet<>(this.faces);
        faces.remove(opposites.get(forward));
        faces.remove(forward);

        Location loc = player.getLocation();
        Location dest = clicked.getLocation().add(0.5, -2, 0.5);
        dest.setYaw(loc.getYaw());
        dest.setPitch(loc.getPitch());
        player.teleport(dest);
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 160, 1));

        Location zero = new Location(loc.getWorld(), 0, 0, 0);
        new Task(HoloItems.getInstance(), 0, 1){
            int increment = 0;
            Block block = clicked;
            Location lastPos = player.getLocation();
            BlockFace alongVertical = null;
            BlockFace alongHorizontal = null;
            int oneTwo = 0;

            public void run(){
                Location to = block.getLocation().add(0.5, -2, 0.5);
                increment++;
                if(increment>160 || player.isSneaking() || !player.isValid()){
                    Location loc = player.getLocation();
                    to.setYaw(loc.getYaw());
                    to.setPitch(loc.getPitch());
                    player.teleport(to);
                    new BukkitRunnable(){
                        public void run(){
                            player.removePotionEffect(PotionEffectType.LEVITATION);
                        }
                    }.runTask(HoloItems.getInstance());
                    cancel();
                    return;
                }

                Location velocity = to.clone().subtract(player.getLocation());
                Location loc = player.getLocation();
                if(!velocity.equals(zero)) {
                    player.setVelocity(velocity.toVector().normalize());
                    if(loc.distance(lastPos)==0){
                        if(oneTwo<2)
                            oneTwo++;
                        else {
                            to.setYaw(loc.getYaw());
                            to.setPitch(loc.getPitch());
                            player.teleport(to);
                            oneTwo = 0;
                        }
                    }
                    else
                        oneTwo = 0;
                }
                lastPos = loc;
                block = block.getRelative(forward);
                if(block.getType()==fence)
                    return;

                if(alongHorizontal==null || alongVertical==null){
                    for(BlockFace face : faces){
                        Block relative = block.getRelative(face);
                        if(relative.getType()==fence) {
                            block = relative;
                            if(vertical.contains(face)) {
                                alongVertical = face;
                                Location dest = block.getLocation().add(0.5, -2, 0.5);
                                dest.setYaw(loc.getYaw());
                                dest.setPitch(loc.getPitch());
                                player.teleport(dest);
                            }
                            else
                                alongHorizontal = face;
                            faces.remove(opposites.get(face));
                            return;
                        }
                        if(!vertical.contains(face)){
                            for(BlockFace corner : vertical) {
                                if(!faces.contains(corner))
                                    continue;
                                Block cornered = relative.getRelative(corner);
                                if(cornered.getType()==fence) {
                                    block = relative;
                                    alongVertical = corner;
                                    alongHorizontal = face;
                                    Location dest = block.getLocation().add(0.5, -2, 0.5);
                                    dest.setYaw(loc.getYaw());
                                    dest.setPitch(loc.getPitch());
                                    player.teleport(dest);
                                    return;
                                }
                            }
                        }
                    }
                }
                else{
                    Block relative = block.getRelative(alongVertical);
                    if(block.getRelative(alongVertical).getType()==fence) {
                        block = relative;
                        Location dest = block.getLocation().add(0.5, -2, 0.5);
                        dest.setYaw(loc.getYaw());
                        dest.setPitch(loc.getPitch());
                        player.teleport(dest);
                        return;
                    }
                    relative = block.getRelative(alongHorizontal);
                    if(relative.getType()==fence) {
                        block = relative;
                        return;
                    }
                    relative = relative.getRelative(alongVertical);
                    if(relative.getType()==fence) {
                        block = relative;
                        Location dest = block.getLocation().add(0.5, -2, 0.5);
                        dest.setYaw(loc.getYaw());
                        dest.setPitch(loc.getPitch());
                        player.teleport(dest);
                        return;
                    }
                }

                to.setYaw(loc.getYaw());
                to.setPitch(loc.getPitch());
                player.teleport(to);
                new BukkitRunnable(){
                    public void run(){
                        player.removePotionEffect(PotionEffectType.LEVITATION);
                    }
                }.runTask(HoloItems.getInstance());
                cancel();
            }
        };
    }
}