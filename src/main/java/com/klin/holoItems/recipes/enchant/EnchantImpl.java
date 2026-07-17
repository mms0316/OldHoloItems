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
import org.jspecify.annotations.NonNull;

import com.klin.holoItems.HoloItems;
import com.klin.holoItems.abstractClasses.Enchant;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EnchantImpl implements Listener {
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
            clickType == ClickType.DOUBLE_CLICK || clickType == ClickType.UNKNOWN ||
            clickType == ClickType.WINDOW_BORDER_LEFT || clickType == ClickType.WINDOW_BORDER_RIGHT)
        {
            event.setCancelled(true);
            return;
        }

        if (event.isShiftClick()) { // (Must be handled before left / right clicks)
            handleShiftClick(event, player, enchantInvHolder);
            return;
        }
        if (event.isLeftClick()) {
            handleLeftClick(event, player, enchantInvHolder);
            return;
        }
        if (event.isRightClick()) {
            handleRightClick(event, player, enchantInvHolder);
            return;
        }

        if (clickType.isKeyboardClick()) {
            handleHotkey(event, player, enchantInvHolder);
            return;
        }

        // Unknown future event?
        event.setCancelled(true);
    }

    private void handleLeftClick(InventoryClickEvent event, Player player, EnchantInventoryHolder enchantInvHolder) {
        if (event.getClickedInventory() == event.getView().getBottomInventory()) {
            //Allow anything done in player's inventory
            return;
        }
        else if (event.getClickedInventory() == event.getView().getTopInventory()) {
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

            final ItemStack cursor = event.getCursor();
            final ItemStack target = event.getCurrentItem();

            if (cursor == null && target == null)
                return;

            if (cursor == null && target != null) {
                // Taking out item
                updateActionSlotLater(event, player, enchantInvHolder);
                return;
            }

            if (cursor != null) {
                handleSwap(event, player, enchantInvHolder, cursor);
            }
        }
    }

    private void handleRightClick(InventoryClickEvent event, Player player, EnchantInventoryHolder enchantInvHolder) {
        if (event.getClickedInventory() == event.getView().getBottomInventory()) {
            //Allow anything done in player's inventory
            return;
        }
        else if (event.getClickedInventory() == event.getView().getTopInventory()) {
            final int rawSlot = event.getRawSlot();

            if (!EnchantInventoryHolder.isSlotValid(rawSlot)) {
                // Disallow clicking on placeholder slots
                event.setCancelled(true);
                return;
            }

            if (rawSlot == EnchantInventoryHolder.SLOT_ACTION) {
                // Disallow right-clicking the action slot
                event.setCancelled(true);
                return;
            }

            final ItemStack cursor = event.getCursor();
            final ItemStack target = event.getCurrentItem();

            if (cursor == null && target == null)
                return;

            if (cursor == null && target != null) {
                // Taking out item
                updateActionSlotLater(event, player, enchantInvHolder);
                return;
            }

            if (cursor != null) {
                handleSwap(event, player, enchantInvHolder, cursor);
            }
        }
    }

    /*
     * Swap if ItemStacks are different, or move item to target
     */
    private void handleSwap(InventoryClickEvent event, Player player, EnchantInventoryHolder enchantInvHolder, @NonNull ItemStack swapWithItemStack) {
        final int rawSlot = event.getRawSlot();

        if (rawSlot == EnchantInventoryHolder.SLOT_ENCHANTMENT) {
            // A HoloItem enchantment can only go in the enchantment slot
            final var optEnchant = Enchant.asHoloItemEnchant(swapWithItemStack);
            if (optEnchant.isEmpty()) {
                event.setCancelled(true);
                return;
            }

            if (enchantInvHolder.getBaseSlot() != null) {
                // Base slot already has an item

                // Check compatibility
                if (!optEnchant.get().accepts(swapWithItemStack)) {
                    event.setCancelled(true);
                    return;
                }
            }

            // Let the default event handle swap / move one
            updateActionSlotLater(event, player, enchantInvHolder);
            return;
        }
        if (rawSlot == EnchantInventoryHolder.SLOT_BASE) {
            // A HoloItem enchantment can only go in the enchantment slot
            if (Enchant.isHoloItemEnchant(swapWithItemStack)) {
                event.setCancelled(true);
                return;
            }

            ItemStack enchantItemStack = enchantInvHolder.getEnchantmentSlot();
            if (enchantItemStack != null) {
                // Enchantment slot already has an item
                final var optEnchant = Enchant.asHoloItemEnchant(enchantItemStack);
                if (optEnchant.isEmpty()) {
                    // This shouldn't happen
                    event.setCancelled(true);
                    return;
                }

                // Check compatibility
                if (!optEnchant.get().accepts(swapWithItemStack)) {
                    event.setCancelled(true);
                    return;
                }
            }

            // Let the default event handle swap / move one
            // Note: this can't be manually done, because event.setCursor() is deprecated for causing inconsistencies
            updateActionSlotLater(event, player, enchantInvHolder);
            return;
        }

        // For other slots, cancel
        event.setCancelled(true);
    }

    private void handleShiftClick(InventoryClickEvent event, Player player, EnchantInventoryHolder enchantInvHolder) {
        final ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null)
            return;

        final int rawSlot = event.getRawSlot();

        if (event.getClickedInventory() == event.getView().getBottomInventory()) {
            // Shift-clicking from player inventory

            // Do actions manually
            event.setCancelled(true);

            // A HoloItem enchantment can only go in the enchantment slot
            var optEnchant = Enchant.asHoloItemEnchant(clickedItem);
            if (optEnchant.isPresent()) {

                // if slot is occupied, do nothing
                if (enchantInvHolder.getEnchantmentSlot() != null)
                    return;

                ItemStack baseItemStack = enchantInvHolder.getBaseSlot();
                if (baseItemStack != null) {
                    // Base slot already has an item

                    // Check compatibility
                    if (!optEnchant.get().accepts(baseItemStack))
                        return;
                }

                // Move the clicked item to the enchantment slot
                enchantInvHolder.setEnchantmentSlot(clickedItem);
                event.setCurrentItem(null);
                updateActionSlotLater(event, player, enchantInvHolder);
                return;
            }

            // Otherwise, try to move to base item slot

            // If slot is occupied, do nothing
            if (enchantInvHolder.getBaseSlot() != null)
                return;

            // If enchantment slot is occupied, check compatibility
            optEnchant = Enchant.asHoloItemEnchant(enchantInvHolder.getEnchantmentSlot());
            if (optEnchant.isPresent()) {
                // Check compatibility
                if (!optEnchant.get().accepts(clickedItem))
                    return;
            }

            // Move the clicked item to base slot
            enchantInvHolder.setBaseSlot(clickedItem);
            event.setCurrentItem(null);
            updateActionSlotLater(event, player, enchantInvHolder);
            return;
        } else if (event.getClickedInventory() == event.getView().getTopInventory()) {
            // Shift-clicking from enchantment inventory

            // Do actions manually
            event.setCancelled(true);

            if (rawSlot == EnchantInventoryHolder.SLOT_BASE || rawSlot == EnchantInventoryHolder.SLOT_ENCHANTMENT) {
                // Take the item back to player inventory
                final var excess = player.getInventory().addItem(clickedItem);
                event.setCurrentItem(null);
                for (ItemStack item : excess.values()) {
                    player.getWorld().dropItem(player.getLocation(), item);
                }
                updateActionSlotLater(event, player, enchantInvHolder);
            } else if (rawSlot == EnchantInventoryHolder.SLOT_ACTION) {
                // Try to enchant
                handleActionSlot(player, enchantInvHolder);
            }

            return;
        }
    }

    /*
     *  Handle 1-9 (hotbar), F (offhand), Q (drop) hotkeys
     */  
    private void handleHotkey(InventoryClickEvent event, Player player, EnchantInventoryHolder enchantInvHolder) {
        if (event.getClickedInventory() == event.getView().getBottomInventory()) {
            //Allow anything done in player's inventory
            return;
        }
        else if (event.getClickedInventory() == event.getView().getTopInventory()) {
            final int rawSlot = event.getRawSlot();

            if (!EnchantInventoryHolder.isSlotValid(rawSlot)) {
                // Disallow dropping/swapping placeholder slots
                event.setCancelled(true);
                return;
            }

            if (rawSlot == EnchantInventoryHolder.SLOT_ACTION) {
                // Disallow dropping/swapping the action slot
                event.setCancelled(true);
                return;
            }

            final ClickType clickType = event.getClick();
            ItemStack itemToSwap;

            if (clickType == ClickType.DROP || clickType == ClickType.CONTROL_DROP) {
                itemToSwap = null;
            }
            else if (clickType == ClickType.NUMBER_KEY) {
                int hotkey = event.getHotbarButton();
                if (hotkey < 0) {
                    //Supposedly swap between offhand and hotbar. Clicked Inventory should've been the bottom inventory?
                    event.setCancelled(true);
                    return;
                }
                itemToSwap = player.getInventory().getItem(hotkey);
            }
            else if (clickType == ClickType.SWAP_OFFHAND) {
                itemToSwap = player.getInventory().getItemInOffHand();
            }
            else {
                // Unknown future hotkey?
                event.setCancelled(true);
                return;
            }

            if (itemToSwap == null) {
                // Taking out item
                updateActionSlotLater(event, player, enchantInvHolder);
                return;
            } else {
                handleSwap(event, player, enchantInvHolder, itemToSwap);
                return;
            }
        }
    }

    /*
     * After the event is processed, update action slot accordingly
     */
    private void updateActionSlotLater(InventoryClickEvent event, Player player, EnchantInventoryHolder enchantInvHolder) {
        Bukkit.getScheduler().runTaskLater(HoloItems.getInstance(), () -> {
            final ItemStack baseItemStack = enchantInvHolder.getBaseSlot();
            final ItemStack enchantItemStack = enchantInvHolder.getEnchantmentSlot();

            // If there aren't input items, reset action slot
            if (baseItemStack == null || enchantItemStack == null) {
                enchantInvHolder.setActionSlot(false, false, 0);
                return;
            }

            // Check if player has cost
            final var optEnchant = Enchant.asHoloItemEnchant(enchantItemStack);
            if (optEnchant.isEmpty()) {
                // Shouldn't happen
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
