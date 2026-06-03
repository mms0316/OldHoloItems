package com.klin.holoItems.collections.gen2.shionCollection;

import com.klin.holoItems.Collection;
import com.klin.holoItems.HoloItems;
import com.klin.holoItems.collections.gen2.shionCollection.items.*;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Shion Collection, themed around enchanting and potion brewing.
 *
 * To create Shion as an entity, it's possible to:
 * - /summon minecraft:mannequin ~ ~ ~ {profile:murasakishion, CustomName:"Shion", hide_description:1b, Invulnerable:1b, immovable:1b}
 * - /acquire nameTag
 * - Use an anvil and name the NameTag "Shion"
 * - Right-click Shion entity while holding the NameTag
 * 
 * Note: Creative players can damage and move mannequins even though they have invulnerability and immovability tags
 * A plugin like Citizens may be required to make the entity completely invulnerable and immovable
 * 
 * Note: It's possible to handle immovability by Creative players with:
 * - /team add NoCollision
 * - /team modify NoCollision collisionRule never
 * - /team join NoCollision Shion
 * 
 * For testing with Bedrock + Paper + Geyser, besides having a Bedrock account, do:
 * - Install Geyser (https://geysermc.org/download/)
 * - Also install Floodgate (https://geysermc.org/wiki/floodgate/setup/) because otherwise you may need to reauthenticate with every login
 * - Run server once and stop so plugins/Geyser-Spigot/config.yml is created
 * - Edit java.auth-type to "floodgate"
 * - If using BlueStacks to run Bedrock, the server IP for reaching localhost is 10.0.2.2
 */
public class ShionCollection extends Collection {
    public static final String name = "Shion";
    public static final String desc = "NEEEEEEEEEEEEEEEEEEEEEEE";
    public static final String theme = "Ingredients mixed";
//    public static final String ign = "murasakishion";
//    public static final String uuid = "1ffbc76f-98ad-4c29-9fcd-8d7878b29248";
    public static final String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjkxZDE3NTJjMjVmNzVjYTIzZmE4MmFmYTUyNzEwMDEzYjBhMjNmNTdmNmI5ZjYyMjYyYmU2YzI1Yjc3MDI4YiJ9fX0=";

    public ShionCollection(){
        super(name, desc, theme, base64);
        collection.add(new WitchsBracelet());
        collection.add(new PotionSatchel());
        collection.add(new SecretBrew());
        collection.add(new SorceressTome());
        collection.add(new Fireball());
    }

    public Map<String, Integer> getStat(Player player){
        Map<String, Integer> stat = new LinkedHashMap<>();
        stat.put("Redstone infused", player.getStatistic(Statistic.BREAK_ITEM, Material.REDSTONE));
        stat.put("Glowstone infused", player.getStatistic(Statistic.BREAK_ITEM, Material.GLOWSTONE_DUST));
        stat.put("Fermented Spider Eye infused", player.getStatistic(Statistic.BREAK_ITEM, Material.FERMENTED_SPIDER_EYE));
        stat.put("Gunpowder infused", player.getStatistic(Statistic.BREAK_ITEM, Material.GUNPOWDER));
        stat.put("Dragon's Breath infused", player.getStatistic(Statistic.BREAK_ITEM, Material.DRAGON_BREATH));
        stat.put("Sugar infused", player.getStatistic(Statistic.BREAK_ITEM, Material.SUGAR));
        stat.put("Rabbit's Foot infused", player.getStatistic(Statistic.BREAK_ITEM, Material.RABBIT_FOOT));
        stat.put("Glistering Melon infused", player.getStatistic(Statistic.BREAK_ITEM, Material.GLISTERING_MELON_SLICE));
        stat.put("Spider Eye infused", player.getStatistic(Statistic.BREAK_ITEM, Material.SPIDER_EYE));
        stat.put("Pufferfish infused", player.getStatistic(Statistic.BREAK_ITEM, Material.PUFFERFISH));
        stat.put("Magma Cream infused", player.getStatistic(Statistic.BREAK_ITEM, Material.MAGMA_CREAM));
        stat.put("Golden Carrot infused", player.getStatistic(Statistic.BREAK_ITEM, Material.GOLDEN_CARROT));
        stat.put("Blaze Powder infused", player.getStatistic(Statistic.BREAK_ITEM, Material.BLAZE_POWDER));
        stat.put("Ghast Tear infused", player.getStatistic(Statistic.BREAK_ITEM, Material.GHAST_TEAR));
        stat.put("Turtle Shell infused", player.getStatistic(Statistic.BREAK_ITEM, Material.TURTLE_HELMET));
        stat.put("Phantom Membrane infused", player.getStatistic(Statistic.BREAK_ITEM, Material.PHANTOM_MEMBRANE));
        return stat;
    }

    public void inquire(Player player, ItemStack itemStack, PlayerInteractEntityEvent event) {
        HoloItems.getInstance().getEnchantImpl().openGUI(player, Component.text("HoloItem Enchant").decoration(TextDecoration.ITALIC, false));
    }
}
