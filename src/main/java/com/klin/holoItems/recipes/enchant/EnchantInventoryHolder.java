package com.klin.holoItems.recipes.enchant;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.klin.holoItems.utility.Utility;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.Component;

/*
 * This class is for combining HoloItems because PrepareAnvilEvent doesn't work for Bedrock players coming through Geyser:
 * - https://geysermc.org/wiki/geyser/current-limitations/
 * - https://github.com/GeyserMC/Geyser/issues/4706
 * 
 * Compared to the original implementation, this now only allows a base item and a HoloItem enchantment
 */
public class EnchantInventoryHolder implements InventoryHolder {
    private final Inventory inventory;

    public static final int SLOT_BASE = 2 + 9;
    public static final int SLOT_ENCHANTMENT = 4 + 9;
    public static final int SLOT_ACTION = 6 + 9;

    private static final Material ACTION_MATERIAL = Material.EXPERIENCE_BOTTLE;

    private static final Component ACTION_NONE_COMPONENT = Component.text("Add base item and HoloItem enchantment");
    private static final String ACTION_HAS_LEVELS_FMT = "Enchant for %d levels";
    private static final String ACTION_NEEDS_LEVELS_FMT = "Needs %d levels";

    private static final Material PLACEHOLDER_MATERIAL = Material.GRAY_STAINED_GLASS_PANE;

    public EnchantInventoryHolder() {
        this(null);
    }

    public EnchantInventoryHolder(Component component) {
        if (component == null) {
            inventory = Bukkit.createInventory(this, 27);
        } else {
            inventory = Bukkit.createInventory(this, 27, component);
        }

        ItemStack placeholderItemStack = new ItemStack(PLACEHOLDER_MATERIAL);
        placeholderItemStack.editMeta(meta -> meta.customName(Component.empty()));
        Utility.setUnstackableUtilityKey(placeholderItemStack);

        for (int i = 0; i < 27; i++) {
            if (isSlotPlaceholder(i)) {
                inventory.setItem(i, placeholderItemStack);
            }
        }

        ItemStack actionItemStack = new ItemStack(ACTION_MATERIAL);
        actionItemStack.editMeta(meta -> meta.customName(ACTION_NONE_COMPONENT));
        Utility.setUnstackableUtilityKey(actionItemStack);
        inventory.setItem(SLOT_ACTION, actionItemStack);

        // Note: the use of Utility.setUnstackableUtilityKey prevents the following:
        // 1) Get an experience bottle
        // 2) Use anvil and rename it "Add base item and HoloItem enchantment"
        // 3) Inside this inventory, double-click the experience bottle
        // Without this, the player would fetch the experience bottle
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public ItemStack getBaseSlot() {
        return inventory.getItem(SLOT_BASE);
    }

    public void setBaseSlot(ItemStack baseItem) {
        inventory.setItem(SLOT_BASE, baseItem);
    }

    public ItemStack getEnchantmentSlot() {
        return inventory.getItem(SLOT_ENCHANTMENT);
    }

    public void setEnchantmentSlot(ItemStack enchantmentItem) {
        inventory.setItem(SLOT_ENCHANTMENT, enchantmentItem);
    }

    public void clearItems() {
        setBaseSlot(null);
        setEnchantmentSlot(null);
        setActionSlot(false, false, 0);
    }

    public void setActionSlot(boolean isReady, boolean hasLevels, int cost) {
        ItemStack actionItemStack = inventory.getItem(SLOT_ACTION);
        if (actionItemStack == null) return; //Shouldn't ever happen

        Component actionComponent;

        if (!isReady) {
            actionComponent = ACTION_NONE_COMPONENT;
        } else {
            final String actionText = String.format(hasLevels ? ACTION_HAS_LEVELS_FMT : ACTION_NEEDS_LEVELS_FMT, cost);
            actionComponent = Component.text(actionText, hasLevels ? NamedTextColor.GREEN : NamedTextColor.RED);
        }

        actionItemStack.editMeta(meta -> meta.displayName(actionComponent));
    }

    public boolean hasInputSlots() {
        return getBaseSlot() != null && getEnchantmentSlot() != null;
    }

    public static boolean isSlotValid(int rawSlot) {
        return rawSlot == SLOT_BASE || rawSlot == SLOT_ENCHANTMENT || rawSlot == SLOT_ACTION;
    }

    public static boolean isSlotPlaceholder(int rawSlot) {
        return rawSlot >= 0 && rawSlot < 27 && !isSlotValid(rawSlot);
    }

    public static boolean isSlot(int rawSlot) {
        return rawSlot >= 0 && rawSlot < 27;
    }
}
