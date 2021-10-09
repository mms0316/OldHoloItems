package com.klin.holoItems.collections.id2.anyaCollection.items;

import com.klin.holoItems.HoloItems;
import com.klin.holoItems.Item;
import com.klin.holoItems.interfaces.Interactable;
import com.klin.holoItems.utility.Task;
import com.klin.holoItems.utility.Utility;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Set;

public class UnlimitedKrisWorks extends Item implements Interactable {
    public static final String name = "unlimitedKrisWorks";
    public static final Set<Enchantment> accepted = Set.of(Enchantment.DURABILITY, Enchantment.LOYALTY, Enchantment.MENDING);
    private final Map<Material, Integer> swords;

    private static final Material material = Material.GOLDEN_SWORD;
    private static final int quantity = 1;
    private static final String lore =
            "Right click to throw a sword from\n" +
            "your inventory";
    private static final int durability = 32;
    public static final boolean stackable = false;
    private static final boolean shiny = false;
    public static final int cost = 0;

    public UnlimitedKrisWorks(){
        super(name, accepted, material, quantity, lore, durability, stackable, shiny, cost);
        swords = Map.of(
                Material.WOODEN_SWORD, 4,
                Material.GOLDEN_SWORD, 4,
                Material.STONE_SWORD, 5,
                Material.IRON_SWORD, 6,
                Material.DIAMOND_SWORD, 7,
                Material.NETHERITE_SWORD, 8);
    }

    public void registerRecipes(){
        ShapedRecipe recipe0 =
                new ShapedRecipe(new NamespacedKey(HoloItems.getInstance(), name+"0"), item);
        recipe0.shape(" a","a "," b");
        recipe0.setIngredient('a', Material.NETHER_STAR);
        recipe0.setIngredient('b', Material.LIGHTNING_ROD);
        recipe0.setGroup(name);
        Bukkit.getServer().addRecipe(recipe0);

        ShapedRecipe recipe1 =
                new ShapedRecipe(new NamespacedKey(HoloItems.getInstance(), name+"1"), item);
        recipe1.shape("a "," a","b ");
        recipe1.setIngredient('a', Material.NETHER_STAR);
        recipe1.setIngredient('b', Material.LIGHTNING_ROD);
        recipe1.setGroup(name);
        Bukkit.getServer().addRecipe(recipe1);
    }

    public void ability(PlayerInteractEvent event, Action action){
        if(!(action==Action.RIGHT_CLICK_AIR || action==Action.RIGHT_CLICK_BLOCK) ||
                event.useInteractedBlock()==Event.Result.ALLOW)
            return;
        ItemStack sword = event.getItem();
        if(Utility.onCooldown(sword))
            return;
        Player player = event.getPlayer();
        if (player.getGameMode()!=GameMode.CREATIVE)
            Utility.addDurability(sword, -1, player);
        ItemMeta meta = sword.getItemMeta();
        if(meta==null)
            return;
        boolean loyalty =  meta.hasEnchant(Enchantment.LOYALTY);
        for(ItemStack item : player.getInventory().getStorageContents()){
            if(item==null || sword.equals(item))
                continue;
            if(swords.containsKey(item.getType()) && !Utility.onCooldown(item)){
                sword = item;
                break;
            }
        }
        if(loyalty)
            Utility.cooldown(sword);
        else {
            PlayerInventory inv = player.getInventory();
            if(sword.equals(inv.getItemInOffHand()))
                inv.setItemInOffHand(null);
            else
                inv.removeItem(sword);
        }

        double damage = swords.get(sword.getType()) + 3 * (Utility.checkPotionEffect(player, PotionEffectType.INCREASE_DAMAGE) -
                Utility.checkPotionEffect(player, PotionEffectType.WEAKNESS));
        Location location = player.getEyeLocation();
        World world = player.getWorld();
        boolean hand = event.getHand()==EquipmentSlot.HAND;

        double distance = 50;
        Vector dir = location.getDirection().multiply(3);
        RayTraceResult result = world.rayTrace(location, dir, 50, FluidCollisionMode.NEVER, true, 0.5,
                entity -> (entity != player && entity instanceof LivingEntity && !(entity instanceof ArmorStand)));
        LivingEntity entity = null;
        if (result != null) {
            entity = (LivingEntity) result.getHitEntity();
            if (entity != null)
                distance = location.distance(result.getHitEntity().getLocation());
            else if (result.getHitBlock() != null)
                distance = location.distance(result.getHitBlock().getLocation());
        }
        double iterations = distance/3;

        ArmorStand stand = world.spawn(location.clone().add(0, -1, 0), ArmorStand.class);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setCanPickupItems(false);
        stand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING);
        stand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING);
        stand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING);
        stand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING);
        stand.getPersistentDataContainer().set(Utility.key, PersistentDataType.STRING, "hI");
        if(hand) {
            stand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING);
            stand.getEquipment().setItemInMainHand(sword);
        }
        else {
            stand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING);
            stand.getEquipment().setItemInOffHand(sword);
        }
        stand.setRightArmPose(new EulerAngle(-0.142+Math.PI*location.getPitch()/180, 0.04, 0.0145));

        double height = player.getLocation().getY();
        ItemStack item = sword;
        LivingEntity target = entity;
        new Task(HoloItems.getInstance(), 1, 1){
            double increment = 0;
            final boolean crit = player.getLocation().getY()<height;
            public void run(){
                if(increment > iterations) {
                    stand.remove();
                    if(target!=null && target.isValid() && (!(target instanceof Player) || !((Player) target).isBlocking()))
                            Utility.damage(item, damage, crit, player, target, false, true, false);
                    if(loyalty)
                        Utility.removeCooldown(item);
                    else
                        world.dropItemNaturally(stand.getLocation(), item);
                    cancel();
                    return;
                }
                stand.teleport(stand.getLocation().add(dir));
                increment++;
            }
        };
    }
}
