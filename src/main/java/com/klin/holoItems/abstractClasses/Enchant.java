package com.klin.holoItems.abstractClasses;

import com.klin.holoItems.Item;
import com.klin.holoItems.utility.Utility;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
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
        List<String> existingEnchants = getEnchantments(itemStack);
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
    public static List<String> getEnchantments(ItemStack itemStack) {
        if (itemStack == null) return new ArrayList<>();

        final var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return new ArrayList<>();

        String serializedEnchantments = itemMeta.getPersistentDataContainer().get(Utility.enchant, PersistentDataType.STRING);
        if (serializedEnchantments == null) return new ArrayList<>();

        return new ArrayList<>(Arrays.asList(serializedEnchantments.split(" ")));
    }

    public static void setEnchantments(ItemStack itemStack, List<String> enchantmentNames) {
        if (itemStack == null) return;

        final var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;

        String serializedEnchantments = String.join(" ", enchantmentNames);
        itemMeta.getPersistentDataContainer().set(Utility.enchant, PersistentDataType.STRING, serializedEnchantments);
        itemStack.setItemMeta(itemMeta);
    }

    /*
     * Applies this HoloItem enchantment to the given itemStack
     *
     * This should be done after checking:
     * - if this HoloItem enchantment can be applied to the given itemStack
     * - if there are other costs, such as experience levels
     */
    public ItemStack apply(@NonNull ItemStack itemStack) {
        ItemStack result = Utility.addEnchant(itemStack.clone(), this);

        // Original code checked for a 2nd time for preexisting enchantments that conflict with this HoloItem Enchantment.
        // This has been removed

        Item holoItem = Utility.findItem(result, Item.class);
        if (holoItem == null) {
            // Grants infinite durability to non-HoloItems
            var meta = result.getItemMeta();

            if (meta instanceof Damageable) {
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

            // TODO: remove all durability lore
            List<String> lore;
            if (meta.hasLore()) {
                lore = meta.getLore();
            } else {
                lore = new ArrayList<>();
            }

            if (!lore.get(lore.size() - 1).startsWith("§fDurability: ")) {
                int maxDurability = itemStack.getType().getMaxDurability();
                int currDurability = maxDurability - ((Damageable) meta).getDamage();
                lore.add("");
                lore.add("§fDurability: " + currDurability + "/" + maxDurability);
                meta.setLore(lore);
            }
            result.setItemMeta(meta);
        }

        return result;
    }
}
