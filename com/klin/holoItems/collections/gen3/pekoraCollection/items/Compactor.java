package com.klin.holoItems.collections.gen3.pekoraCollection.items;

import com.klin.holoItems.HoloItems;
import com.klin.holoItems.abstractClasses.Wiring;
import com.klin.holoItems.collections.gen3.pekoraCollection.PekoraCollection;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static org.bukkit.Material.*;

public class Compactor extends Wiring {
    public static final String name = "compactor";
    private static Map<List<String>, Map<Map<Character, ItemStack>, ItemStack>> recipes = new HashMap<>();
    private static Map<Map.Entry<Set<ItemStack>, Integer>, ItemStack> shapeless = new HashMap<>();
    //    private static final Set<Material> exceptions = Stream.of().collect(Collectors.toCollection(HashSet::new));

    private static final Material material = BLAST_FURNACE;
    private static final String lore =
            "§6Ability" +"/n"+
                "Dispensers wired with this item will" +"/n"+
                "auto craft using its inventory as the grid" + "/n"+
                "Break the dispenser to retrieve";
    private static final boolean shiny = true;

    public static final int cost = 0;
    public static final char key = '2';
    public static final String id = ""+PekoraCollection.key+key;

    public Compactor(){
        super(name, material, lore, shiny, cost, id, key);
    }

    public void registerRecipes(){
        ShapedRecipe recipe =
                new ShapedRecipe(new NamespacedKey(HoloItems.getInstance(), name), item);
        recipe.shape("&*&","*%*","&*&");
        recipe.setIngredient('&', COBBLESTONE);
        recipe.setIngredient('*', CRAFTING_TABLE);
        recipe.setIngredient('%', NETHERITE_BLOCK);
        recipe.setGroup(name);
        Bukkit.getServer().addRecipe(recipe);
    }

    public void ability(BlockDispenseEvent event) {
        if(recipes.isEmpty()){
            Iterator<Recipe> recipeIterator = Bukkit.getServer().recipeIterator();
            while(recipeIterator.hasNext()){
                Recipe next = recipeIterator.next();
                if(!(next instanceof ShapedRecipe)) {
                    if(next instanceof ShapelessRecipe) {
                        List<ItemStack> ingredients = ((ShapelessRecipe) next).getIngredientList();
                        shapeless.put(new AbstractMap.SimpleEntry<>(new HashSet<>(ingredients), ingredients.size()), next.getResult());
                    }
                    continue;
                }

                ShapedRecipe shaped = (ShapedRecipe) next;
                //different arrays with the same content do not produce the same hash keys
                List<String> shape = Arrays.asList(shaped.getShape());
                Map<Map<Character, ItemStack>, ItemStack> ingredients =
                        recipes.computeIfAbsent(shape, k -> new HashMap<>());
                ingredients.put(shaped.getIngredientMap(), shaped.getResult());
            }
            additions();
            //to-include: wood types, stone types (cobble/black), certain shapeless
        }
        event.setCancelled(true);

        new BukkitRunnable(){
            public void run(){
                Inventory inv = ((InventoryHolder) event.getBlock().getState()).getInventory();
                ItemStack[] contents = inv.getStorageContents();

                int topLeft = 9;
                int bottomRight = -1;
                for(int i=0; i<contents.length; i++){
                    if (contents[i] != null) {
                        topLeft = Math.min(topLeft, Math.min(i, topLeft/3*3+i%3));
                        bottomRight = Math.max(i, Math.max(i/3*3+bottomRight%3, bottomRight/3*3+i%3));
                    }
                }
                if(topLeft == 9)
                    return;

                Map<Character, ItemStack> ingredientMap = new HashMap<>();
                int increment = 0;
                for(int i=0; i<contents.length; i++){
                    if(i/3<topLeft/3 || i/3>bottomRight/3 || i%3<topLeft%3 || i%3>bottomRight%3)
                        continue;
                    ItemStack ingredient = contents[i];
                    char key = (char)('a'+increment);
                    if(ingredient==null)
                        ingredientMap.put(key, null);
                    else{
                        ItemStack toMap = ingredient.clone();
                        toMap.setAmount(1);
                        ingredientMap.put(key, toMap);
                    }
                    increment++;
                }


                String[] shape = new String[(bottomRight-topLeft)/3+1];
                increment = 0;
                for(int j=0; j<shape.length; j++) {
                    for(int i=topLeft%3; i<=bottomRight%3; i++){
                        if(shape[j]==null)
                            shape[j] = "";
                        shape[j] += (char) ('a' + increment);
                        increment++;
                    }
                }

                Map<Map<Character, ItemStack>, ItemStack> shapes = recipes.get(Arrays.asList(shape));
                ItemStack result = null;
                if(shapes!=null)
                    result = shapes.get(ingredientMap);
                if(result==null) {
                    List<ItemStack> ingredients = new ArrayList<>(ingredientMap.values());
                    ingredients.removeAll(Collections.singleton(null));
                    result = shapeless.get(new AbstractMap.SimpleEntry<>(new HashSet<>(ingredients), ingredients.size()));
                    if(result==null)
                        return;
                }

                Block block = event.getBlock();
                Block relative = block.getRelative(((Dispenser) block.getBlockData()).getFacing());
                Location loc = relative.getLocation();
                World world = loc.getWorld();
                if(world==null)
                    return;
                BlockState container = relative.getState();
                ItemStack special = special(result.getType());
                if(container instanceof Container) {
                    Inventory output = ((Container) container).getInventory();
                    if(output.firstEmpty()==-1)
                        world.dropItemNaturally(loc, result);
                    else {
                        output.addItem(result);
                        if (special != null){
                            if(output.firstEmpty()==-1)
                                world.dropItemNaturally(loc, result);
                            else
                                output.addItem(special);
                        }
                    }
                }
                else {
                    world.dropItemNaturally(loc, result);
                    if(special!=null)
                        world.dropItemNaturally(loc, special);
                }

                for (ItemStack itemStack : contents) {
                    if(itemStack==null)
                        continue;
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }
            }
        }.runTask(HoloItems.getInstance());
    }

    private static ItemStack special(Material type){
        switch(type){
            case CAKE:
                return new ItemStack(BUCKET, 3);

            case HONEY_BLOCK:
                return new ItemStack(GLASS_BOTTLE, 4);

            default:
                return null;
        }
    }

    private static void additions(){
        List<String> squareKey = Arrays.asList("abc", "def", "ghi");

        Map<Character, ItemStack> nestedKelpBlock = new HashMap<>();
        ItemStack kelp = new ItemStack(DRIED_KELP);
        nestedKelpBlock.put('a', kelp);
        nestedKelpBlock.put('b', kelp);
        nestedKelpBlock.put('c', kelp);
        nestedKelpBlock.put('d', kelp);
        nestedKelpBlock.put('e', kelp);
        nestedKelpBlock.put('f', kelp);
        nestedKelpBlock.put('g', kelp);
        nestedKelpBlock.put('h', kelp);
        nestedKelpBlock.put('i', kelp);

        Map<Map<Character, ItemStack>, ItemStack> square = recipes.get(squareKey);
        square.put(nestedKelpBlock, new ItemStack(DRIED_KELP_BLOCK));
    }
}
