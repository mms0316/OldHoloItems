package com.klin.holoItems.abstractClasses;

import com.klin.holoItems.Item;
import com.klin.holoItems.utility.Utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class Enchant extends Item {
    private static final int quantity = 1;
    public static final boolean stackable = false;

    public static final int ENCHANTMENT_LEVEL_REQUIREMENT = 39;

    public final Set<String> acceptedIds;
    public final Set<Material> acceptedTypes;
    public final Set<Enchantment> exclusive;
    public final int expCost;

    public Enchant(String name, Set<Enchantment> accepted, Material material, String lore, int durability, boolean shiny, int cost, Set<String> acceptedIds, Set<Material> acceptedTypes, Set<Enchantment> exclusive, int expCost){
        super(name, accepted, material, quantity, lore, durability, stackable, shiny, cost);
        this.acceptedIds = acceptedIds;
        this.acceptedTypes = acceptedTypes;
        this.exclusive = exclusive;
        this.expCost = expCost;
    }

    public Enchant(String name, Set<Enchantment> accepted, Material material, String lore, int durability, boolean shiny, int cost, Set<String> acceptedIds, Set<Material> acceptedTypes, int expCost){
        this(name, accepted, material, lore, durability, shiny, cost, acceptedIds, acceptedTypes, null, expCost);
    }

    public Enchant(String name, Material material, String lore, int durability, boolean shiny, int cost, Set<String> acceptedIds, Set<Material> acceptedTypes, int expCost){
        this(name, null, material, lore, durability, shiny, cost, acceptedIds, acceptedTypes, expCost);
    }

    public static boolean isHoloItemEnchant(ItemStack item) {
        Item holoItemEnchant = Utility.findItem(item, Item.class);
        return holoItemEnchant instanceof Enchant;
    }

    public static Optional<Enchant> asHoloItemEnchant(ItemStack item) {
        if (item == null)
            return Optional.empty();

        Item holoItemEnchant = Utility.findItem(item, Item.class);
        if (holoItemEnchant instanceof Enchant enchant) {
            return Optional.of(enchant);
        }
        return Optional.empty();
    }

    /*
     * Checks if this HoloItem enchantment can be applied to the given itemStack
     */
    public boolean accepts(ItemStack itemStack) {
        Item holoItem = Utility.findItem(itemStack, Item.class);

        // Disallow enchanting twice
        List<String> existingEnchants = getHoloEnchantmentIds(itemStack);
        if (existingEnchants.contains(name)) {
            return false;
        }

        // Check acceptedIds and acceptedTypes rules        
        if (acceptedIds == null && acceptedTypes == null) {
            return true;
        }

        // Check if type matches (this applies only to non-HoloItems)
        if (acceptedTypes != null && holoItem == null) {
            if (!acceptedTypes.contains(itemStack.getType()))
                return false; // Type doesn't match
        }

        // Check if ID matches (this applies only to HoloItems)
        if (acceptedIds != null && holoItem != null) {
            if (!acceptedIds.contains(holoItem.name))
                return false; // Target doesn't match
        }

        return true;
    }

    /*
     * Returns a mutable List with the HoloItem Enchantments names of an Item Stack
     */
    public static List<String> getHoloEnchantmentIds(ItemStack itemStack) {
        if (itemStack == null) return new ArrayList<>();

        final var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return new ArrayList<>();

        String serializedEnchantments = itemMeta.getPersistentDataContainer().get(Utility.enchant, PersistentDataType.STRING);
        if (serializedEnchantments == null) return new ArrayList<>();

        return new ArrayList<>(Arrays.asList(serializedEnchantments.split(" ")));
    }

    public static void setHoloEnchantmentIds(ItemStack itemStack, List<String> enchantmentIds) {
        if (itemStack == null) return;

        final var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;

        String serializedEnchantments = String.join(" ", enchantmentIds);
        itemMeta.getPersistentDataContainer().set(Utility.enchant, PersistentDataType.STRING, serializedEnchantments);
        itemStack.setItemMeta(itemMeta);
    }

    public static Set<Enchant> getHoloEnchantments(ItemStack itemStack) {
        Set<Enchant> enchantments = new HashSet<>();

        if (itemStack == null) return enchantments;

        final var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return enchantments;

        String serializedEnchantments = itemMeta.getPersistentDataContainer().get(Utility.enchant, PersistentDataType.STRING);
        if (serializedEnchantments == null) return enchantments;

        for (String enchantmentId : serializedEnchantments.split(" ")) {
            Enchant reactantEnchant = Utility.findItem(enchantmentId, Enchant.class);
            if (reactantEnchant != null) {
                enchantments.add(reactantEnchant);
            }
        }

        return enchantments;
    }

    /*
     * Applies this HoloItem enchantment to the given itemStack
     *
     * This should be done after checking:
     * - if this HoloItem enchantment can be applied to the given itemStack
     * - if there are other costs, such as experience levels
     */
    public ItemStack apply(@NonNull ItemStack itemStack) {
        ItemStack result = itemStack.clone();

        // Remove conflicting enchantments
        if (exclusive != null) {
            for (Enchantment enchantment : result.getEnchantments().keySet()) {
                if (exclusive.contains(enchantment))
                    result.removeEnchantment(enchantment);
            }
        }

        // Register this enchantment as a HoloItem enchantment in the item's PersistentDataContainer
        List<String> enchantments = getHoloEnchantmentIds(result);
        enchantments.add(name);
        Enchant.setHoloEnchantmentIds(result, enchantments);

        // Cosmetic changes
        result.editMeta(meta -> {

            // Add golden color
            // Component.text by default adds Italic, and that's unwanted
            Component displayName;
            if (meta.hasDisplayName()) {
                displayName = meta.displayName();
            } else {
                displayName = Component.text(Utility.formatType(result.getType())).decoration(TextDecoration.ITALIC, false);
            }

            meta.displayName(displayName.color(NamedTextColor.GOLD)); //§6

            List<Component> lore;

            if (meta.hasLore()) {
                lore = meta.lore();

                // Prepend abilities (AQUA) with a new line
                if (lore.get(0).color() == NamedTextColor.AQUA) { //§b
                    lore.add(0, Component.empty());
                }
            } else if (meta.isUnbreakable()) {
                // Separates durability lore from the rest
                lore = new ArrayList<>(List.of(Component.empty()));
            } else {
                lore = new ArrayList<>();
            }

            // Add enchant name as the first line of the lore (after abilities and/or the empty line)
            lore.add(0, Component.text(Utility.formatName(name), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)); //§7

            meta.lore(lore);
        });

        // Original code checked for a 2nd time for preexisting enchantments that conflict with this HoloItem Enchantment.
        // This has been removed

        Item holoItem = Utility.findItem(result, Item.class);
        if (holoItem == null) {
            result.editMeta(meta -> {
                // Grants infinite durability to non-HoloItems
                if (meta instanceof Damageable) {
                    meta.setUnbreakable(true);
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
                }

                // TODO: remove all durability lore
                List<Component> lore = meta.lore();
                // Create durability lore if it doesn't exist
                final String lastLine = PlainTextComponentSerializer.plainText().serialize(lore.get(lore.size() - 1));
                if (!lastLine.startsWith("Durability: ")) {
                    int maxDurability = result.getType().getMaxDurability();
                    int currDurability = maxDurability - ((Damageable) meta).getDamage();
                    lore.add(Component.empty());
                    lore.add(Component.text("Durability: " + currDurability + "/" + maxDurability, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                }
            });
        }

        return result;
    }
}
