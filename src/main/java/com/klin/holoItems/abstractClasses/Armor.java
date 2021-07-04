package com.klin.holoItems.abstractClasses;

import com.klin.holoItems.Item;
import com.klin.holoItems.interfaces.Responsible;
import com.klin.holoItems.interfaces.Wearable;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Set;

public abstract class Armor extends Item implements Wearable, Responsible {
    private static final int quantity = 1;
    private static final boolean shiny = false;

    //4: zombie/skeleton main hand
    //5: helmet, 6: chestplate, 7: leggings, 8: boots
    public final int armorPiece;

    public Armor(String name, Set<Enchantment> accepted, Material material, String lore,
                 int durability, boolean stackable, int cost, int armorPiece, String id, char key){
        super(name, accepted, material, quantity, lore, durability, stackable, shiny, cost, id, key);
        this.armorPiece = armorPiece;
    }

    public void ability(PlayerInteractEvent event, Action action){
        if(!(action==Action.RIGHT_CLICK_AIR || action==Action.RIGHT_CLICK_BLOCK))
            return;
        event.setUseItemInHand(Event.Result.DENY);
        ItemStack item = event.getItem();
        ItemStack armor = item.clone();
        armor.setAmount(1);
        PlayerInventory inv = event.getPlayer().getInventory();
        switch(armorPiece){
            case 5:
                if(inv.getHelmet()==null)
                    inv.setHelmet(armor);
                else
                    return;
                break;
            case 6:
                if(inv.getChestplate()==null)
                    inv.setChestplate(armor);
                else
                    return;
                break;
            case 7:
                if(inv.getLeggings()==null)
                    inv.setLeggings(armor);
                else
                    return;
                break;
            case 8:
                if(inv.getBoots()==null)
                    inv.setBoots(armor);
                else
                    return;
                break;
            default:
                return;
        }
        item.setAmount(item.getAmount()-1);
        effect(event);
    }

    public void ability(InventoryClickEvent event, boolean current){
        if(current && event.getRawSlot()==armorPiece &&
                !event.getCurrentItem().containsEnchantment(Enchantment.BINDING_CURSE))
            removeEffect(event);
    }

    public boolean ability(PlayerDeathEvent event, ItemStack item){
        effect(event);
        return false;
    }

    public boolean ability(PlayerInteractEntityEvent event){
        Entity entity = event.getRightClicked();
        if(!(entity instanceof Zombie || entity instanceof Skeleton))
            return false;
        Player player = event.getPlayer();
        ItemStack item;
        if(event.getHand()== EquipmentSlot.HAND)
            item = player.getInventory().getItemInMainHand();
        else
            item = player.getInventory().getItemInOffHand();
        ItemStack armor = item.clone();
        armor.setAmount(1);
        EntityEquipment inv = ((LivingEntity) entity).getEquipment();
        if(inv==null)
            return false;

        switch(armorPiece){
            case 4:
                if(inv.getItemInMainHand().getType()==Material.AIR)
                    inv.setItemInMainHand(armor);
                else
                    return false;
                break;
            case 5:
                if(inv.getHelmet()==null || inv.getHelmet().getType()==Material.AIR)
                    inv.setHelmet(armor);
                else
                    return false;
                break;
            case 6:
                if(inv.getChestplate()==null || inv.getChestplate().getType()==Material.AIR)
                    inv.setChestplate(armor);
                else
                    return false;
                break;
            case 7:
                if(inv.getLeggings()==null || inv.getLeggings().getType()==Material.AIR)
                    inv.setLeggings(armor);
                else
                    return false;
                break;
            case 8:
                if(inv.getBoots()==null || inv.getBoots().getType()==Material.AIR)
                    inv.setBoots(armor);
                else
                    return false;
                break;
        }
        if(player.getGameMode()!=GameMode.CREATIVE)
            item.setAmount(item.getAmount()-1);
        return false;
    }

    public abstract void removeEffect(InventoryClickEvent event);
    public abstract void effect(PlayerInteractEvent event);
    public abstract void effect(PlayerDeathEvent event);
}
