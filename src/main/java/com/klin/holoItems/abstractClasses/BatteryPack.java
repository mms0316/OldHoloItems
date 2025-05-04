package com.klin.holoItems.abstractClasses;

import com.klin.holoItems.utility.Utility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Set;

public abstract class BatteryPack extends Pack {
    private static  final int size = 9;
    public static final String title = "Charging. . .";
    public static final boolean display = false;

    public final Material content;
    public final double perCharge;
    public final int cap;

    public BatteryPack(String name, Set<Enchantment> accepted, Material material, String lore, int durability, boolean shiny, int cost, Material content, double perCharge, int cap){
        super(name, accepted, material, lore, durability, shiny, size, title, display, cost);
        this.content = content;
        this.perCharge = perCharge;
        this.cap = cap;
    }

    public BatteryPack(String name, Material material, String lore, int durability, boolean shiny, int cost, Material content, double perCharge, int cap){
        super(name, material, lore, durability, shiny, size, title, display, cost);
        this.content = content;
        this.perCharge = perCharge;
        this.cap = cap;
    }

    public int ability(Inventory inv, ItemStack item, Player player){
        int count = 0;
        Location loc = player.getLocation();
        World world = loc.getWorld();
        Material content = this.content;
        if(content==null)
            content = item.getType();
        for(ItemStack fuel : inv.getContents()) {
            if(fuel==null || fuel.getType()==Material.AIR)
                continue;
            if(fuel.getType()!=content) {
                world.dropItemNaturally(loc, fuel);
                continue;
            }
            count += fuel.getAmount();
        }
        count *= perCharge;
        int excess = count-cap;
        excess = (int) (excess / perCharge + (excess % perCharge > 0 ? 1 : 0));

        int stackSize = content.getMaxStackSize();
        while (excess > 0) {
            if (excess > stackSize) {
                world.dropItemNaturally(loc, new ItemStack(content, stackSize));
                excess -= stackSize;
            } else {
                world.dropItemNaturally(loc, new ItemStack(content, excess));
                excess = 0;
            }
        }

        int by = Math.min(cap, count);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Utility.pack, PersistentDataType.INTEGER, by);
        item.setItemMeta(meta);
        player.sendMessage("Charged "+meta.getDisplayName()+"§f by: "+by);
        return by;
    }

    protected void repack(ItemStack item, Inventory inv) {
        Integer amount = item.getItemMeta().
                getPersistentDataContainer().get(Utility.pack, PersistentDataType.INTEGER);
        if (amount != null && amount > 0) {
            Material content = this.content;
            if (content == null)
                content = item.getType();
            int stackSize = content.getMaxStackSize();
            amount = (int) (amount / perCharge);

            while (amount > 0) {
                if (amount > stackSize) {
                    inv.addItem(new ItemStack(content, stackSize));
                    amount -= stackSize;
                } else {
                    inv.addItem(new ItemStack(content, amount));
                    amount = 0;
                }
            }
        }
    }
}
