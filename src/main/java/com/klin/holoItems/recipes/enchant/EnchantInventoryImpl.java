package com.klin.holoItems.recipes.enchant;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.klin.holoItems.HoloItems;
import com.klin.holoItems.abstractClasses.Enchant;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EnchantInventoryImpl implements Listener {
    // Each player has their own inventory
    private final Map<UUID, EnchantInventoryHolder> enchantInventories = new HashMap<>();

    public void openGUI(Player player) {
        openGUI(player, null);
    }

    public void openGUI(Player player, Component component) {
        EnchantInventoryHolder enchantInventory = enchantInventories.computeIfAbsent(player.getUniqueId(), k -> new EnchantInventoryHolder(component));
        player.openInventory(enchantInventory.getInventory());
    }

    @EventHandler(priority = EventPriority.HIGHEST) // Run as late as possible
    public void onInventoryClick(InventoryClickEvent event) {
        final Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder(false) instanceof EnchantInventoryHolder enchantInvHolder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        final ClickType clickType = event.getClick();
        // Do not handle double-click, middle-click (creative) and clicking on GUI borders
        if (clickType == ClickType.MIDDLE || clickType == ClickType.CREATIVE ||
            clickType == ClickType.UNKNOWN ||
            clickType == ClickType.WINDOW_BORDER_LEFT || clickType == ClickType.WINDOW_BORDER_RIGHT)
        {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            final int rawSlot = event.getRawSlot();

            if (!EnchantInventoryHolder.isSlotValid(rawSlot)) {
                // Disallow clicking on placeholder slots
                event.setCancelled(true);
                return;
            }

            if (rawSlot == EnchantInventoryHolder.SLOT_ACTION) {
                // Try to enchant
                event.setCancelled(true);
                handleActionSlot(player, enchantInvHolder);
                return;
            }

            updateActionSlotLater(event, player, enchantInvHolder);
        }
    }

    /*
     * After the event is processed, update action slot accordingly
     */
    private void updateActionSlotLater(InventoryClickEvent event, Player player, EnchantInventoryHolder enchantInvHolder) {
        Bukkit.getScheduler().runTaskLater(HoloItems.getInstance(), () -> {
            final ItemStack baseItemStack = enchantInvHolder.getBaseSlot();
            final ItemStack enchantItemStack = enchantInvHolder.getEnchantmentSlot();

            // Remove base slot if it is a HoloItem enchantment
            if (Enchant.isHoloItemEnchant(baseItemStack)) {
                addOrDropItem(player, baseItemStack);
                enchantInvHolder.setBaseSlot(null);

                enchantInvHolder.setActionSlot(false, false, 0);
                return;
            }

            // Remove enchantment slot if it is not a HoloItem enchantment
            final var optEnchant = Enchant.asHoloItemEnchant(enchantItemStack);
            if (optEnchant.isEmpty()) {
                if (enchantItemStack != null) {
                    addOrDropItem(player, enchantItemStack);
                    enchantInvHolder.setEnchantmentSlot(null);
                }

                enchantInvHolder.setActionSlot(false, false, 0);
                return;
            }

            // If there aren't input items, reset action slot
            if (baseItemStack == null || enchantItemStack == null) {
                enchantInvHolder.setActionSlot(false, false, 0);
                return;
            }

            // Check if enchant can be applied to base item
            final var enchant = optEnchant.get();
            if (!enchant.accepts(baseItemStack)) {
                // Remove base item
                addOrDropItem(player, baseItemStack);
                enchantInvHolder.setBaseSlot(null);

                enchantInvHolder.setActionSlot(false, false, 0);
                return;
            }

            final Enchant holoItemEnchant = optEnchant.get();
            final boolean hasLevels = player.getLevel() >= holoItemEnchant.expCost;

            enchantInvHolder.setActionSlot(true, hasLevels, holoItemEnchant.expCost);
        }, 1);
    }

    /*
     * Handles clicking the action slot
     * This assumes the event has already been cancelled
     */
    private void handleActionSlot(Player player, EnchantInventoryHolder enchantInvHolder) {
        // Abort if there aren't items in both input slots
        if (!enchantInvHolder.hasInputSlots()) {
            return;
        }

        // Check if enchant slot is a HoloItem enchantment
        final var optEnchant = Enchant.asHoloItemEnchant(enchantInvHolder.getEnchantmentSlot());
        if (optEnchant.isEmpty())
            return;

        final Enchant enchant = optEnchant.get();

        // Check if player has enough levels
        if (player.getLevel() < enchant.expCost) {
            player.sendMessage(Component.text("You need at least " + enchant.expCost + " levels!").color(NamedTextColor.RED));
            return;
        }

        final ItemStack baseItemStack = enchantInvHolder.getBaseSlot();

        // Check for compatibility
        if (!enchant.accepts(baseItemStack)) {
            return;
        }

        // Finalize
        player.setLevel(player.getLevel() - enchant.expCost);

        final ItemStack enchantedItemStack = enchant.apply(baseItemStack);

        addOrDropItem(player, enchantedItemStack);

        enchantInvHolder.clearItems();
    }

    private void addOrDropItem(Player player, ItemStack itemStack) {
        if (itemStack == null) return;
        final var excess = player.getInventory().addItem(itemStack);
        excess.values().forEach(stack -> player.getWorld().dropItemNaturally(player.getLocation(), stack));
    }


    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        final Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder(false) instanceof EnchantInventoryHolder)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        // Cancel event if drag hits the enchanting inventory
        for (int slot : event.getRawSlots()) {
            if (EnchantInventoryHolder.isSlot(slot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        final Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder(false) instanceof EnchantInventoryHolder enchantInvHolder)) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        addOrDropItem(player, enchantInvHolder.getBaseSlot());
        addOrDropItem(player, enchantInvHolder.getEnchantmentSlot());

        enchantInvHolder.clearItems();

        Bukkit.getScheduler().runTaskLater(HoloItems.getInstance(), () -> {
            enchantInventories.remove(player.getUniqueId());
        }, 1);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();

        EnchantInventoryHolder enchantInvHolder = enchantInventories.get(player.getUniqueId());
        if (enchantInvHolder == null) return;

        addOrDropItem(player, enchantInvHolder.getBaseSlot());
        addOrDropItem(player, enchantInvHolder.getEnchantmentSlot());

        enchantInvHolder.clearItems();
        enchantInventories.remove(player.getUniqueId());
    }
}
