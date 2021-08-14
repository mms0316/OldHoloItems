package com.klin.holoItems.collections.en.watsonCollection.items;

import com.klin.holoItems.HoloItems;
import com.klin.holoItems.abstractClasses.BatteryPack;
import com.klin.holoItems.collections.en.watsonCollection.WatsonCollection;
import com.klin.holoItems.interfaces.Hitable;
import com.klin.holoItems.utility.Utility;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

public class Hourglass extends BatteryPack implements Hitable {
    public static final String name = "hourglass";

    private static final Material material = Material.SPLASH_POTION;
    private static final String lore =
            "§6Ability" +"/n"+
                "Shatter to start a timer with duration" +"/n"+
                "equal to the quantity of sand in the" +"/n"+
                "hourglass, return *here* when it ends";
    private static final int durability = 0;
    private static final boolean shiny = false;

    public static final Material content = Material.SAND;
    public static final double perFuel = 1;
    public static final int cap = 15;

    public static final int cost = 0;
    public static final char key = '3';
    public static final String id = ""+WatsonCollection.key+key;

    public Hourglass(){
        super(name, material, lore, durability, shiny, cost, content, perFuel, cap, id, key);
    }

    public void registerRecipes(){
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setColor(Color.ORANGE);
        item.setItemMeta(meta);

        ShapedRecipe recipe =
                new ShapedRecipe(new NamespacedKey(HoloItems.getInstance(), name), item);
        recipe.shape("*/*"," * ","*%*");
        recipe.setIngredient('*', Material.GLASS);
        recipe.setIngredient('/', Material.HOPPER);
        recipe.setIngredient('%', Material.DROPPER);
        recipe.setGroup(name);
        Bukkit.getServer().addRecipe(recipe);
    }

    public void ability(ProjectileHitEvent event) {
        ThrownPotion projectile = (ThrownPotion) event.getEntity();
        int charge = Utility.deplete(projectile.getItem());
        if(charge==-1)
            return;
        charge += 1;

        ProjectileSource shooter = projectile.getShooter();
        if(!(shooter instanceof Player))
            return;
        Player player = (Player) shooter;
        player.sendMessage("Returning in "+charge+" seconds");

        Location loc = player.getLocation();
        new BukkitRunnable(){
            public void run(){
                player.teleport(loc);
            }
        }.runTaskLater(HoloItems.getInstance(), charge*20L);
    }

    protected void effect(PlayerInteractEvent event) {}
}

