package me.letscode.minecraft.advanced_hopper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;

public class AdvancedHopperPlugin extends JavaPlugin {

    public static final String PLUGIN_KEY = "advancedhopper";
    public static final String FILTER_ITEM_KEY = "advanced_hopper.item.filter_hopper";
    public static final String RECIPE_KEY = "filter_hopper_recipe";
    private static final Predicate<Block> HOPPER_PREDICATE = (block) -> block.getState() instanceof Hopper;
    private NamespacedKey hopperDataKey;

    private Map<BlockPos, AdvancedHopper> hopperCache;

    @Override
    public void onLoad() {
        this.hopperDataKey = new NamespacedKey(this, "data");
        this.hopperCache = new HashMap<>();

        this.addFilterHopperRecipe();
        this.addPluginResourcePack();
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }

    @Override
    public void onDisable() { }


    public NamespacedKey getHopperDataKey() {
        return hopperDataKey;
    }

    public Map<BlockPos, AdvancedHopper> getHopperCache() {
        return hopperCache;
    }

    public ItemStack createFilterHopperItem() {
        ItemStack itemStack = new ItemStack(Material.HOPPER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.translatable(FILTER_ITEM_KEY, NamedTextColor.AQUA));
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(itemMeta);
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        return itemStack;
    }
    public void addFilterHopperRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(this, RECIPE_KEY);
        ShapelessRecipe recipe = new ShapelessRecipe(recipeKey, this.createFilterHopperItem());
        recipe.addIngredient(Material.NETHER_STAR);
        recipe.addIngredient(Material.HOPPER);

        getServer().addRecipe(recipe);
    }

    private void addPluginResourcePack() {
        // INFO: cannot load resource pack from plugin
    }

    public AdvancedHopper tryLoadFilterHopper(Hopper hopper) {
        var position = new BlockPos(hopper.getLocation());
        if (this.hopperCache.containsKey(position)) {
            return this.hopperCache.get(position);
        } else {
            var dataContainer = hopper.getPersistentDataContainer();
            if (dataContainer.has(this.hopperDataKey, PersistentDataType.TAG_CONTAINER)) {
                var container = dataContainer.get(this.hopperDataKey, PersistentDataType.TAG_CONTAINER);
                AdvancedHopper filterHopper = new AdvancedHopper(position);
                filterHopper.loadFrom(container);
                filterHopper.getInventoryHolder().updateInventory();
                this.hopperCache.put(position, filterHopper);
                return filterHopper;
            }
        }
        return null;
    }

    public void updateFilterHopper(AdvancedHopper filterHopper) {
        var position = filterHopper.getPosition().toLocation();
        if (position.getBlock().getState() instanceof Hopper hopper) {
            var dataContainer = hopper.getPersistentDataContainer();
            var hopperData = dataContainer.getAdapterContext().newPersistentDataContainer();
            filterHopper.saveTo(hopperData);
            dataContainer.set(this.hopperDataKey, PersistentDataType.TAG_CONTAINER, hopperData);
            hopper.update(true); // update initial
        }
    }

    public void placeFilterHopper(Block block) {
        var position = new BlockPos(block.getLocation());
        if (block.getState() instanceof Hopper hopper) {
            var dataContainer = hopper.getPersistentDataContainer();
            AdvancedHopper filterHopper = new AdvancedHopper(block.getLocation());
            var hopperData = dataContainer.getAdapterContext().newPersistentDataContainer();
            filterHopper.saveTo(hopperData);
            filterHopper.getInventoryHolder().updateInventory();
            dataContainer.set(this.hopperDataKey,PersistentDataType.TAG_CONTAINER, hopperData);
            hopper.update(true); // save initial
            this.hopperCache.put(position, filterHopper);
        }
    }

    public void unloadFilterHopper(Hopper hopper) {
        var position = new BlockPos(hopper.getLocation());
        AdvancedHopper filterHopper = this.hopperCache.remove(position);

        if (filterHopper != null) {
            var dataContainer = hopper.getPersistentDataContainer();
            var hopperData = dataContainer.getAdapterContext().newPersistentDataContainer();
            filterHopper.saveTo(hopperData);
            dataContainer.set(this.hopperDataKey,PersistentDataType.TAG_CONTAINER, hopperData);
            hopper.update(true); // save initial
        }

    }

    public boolean isFilterHopper(Location location) {
        if (location.getBlock().getState() instanceof Hopper hopper) {
            return hopper.getPersistentDataContainer().has(this.hopperDataKey, PersistentDataType.TAG_CONTAINER);
        }
        return false;
    }

    public boolean isFilterHopper(Block block) {
        if (block.getState() instanceof Hopper hopper) {
            return hopper.getPersistentDataContainer().has(this.hopperDataKey, PersistentDataType.TAG_CONTAINER);
        }
        return false;
    }

    public void loadFilterHoppers(Chunk chunk) {
        int count = 0;
        for (var hopper : chunk.getTileEntities(HOPPER_PREDICATE, true)) {
            if (this.isFilterHopper(hopper.getLocation())) {
                this.tryLoadFilterHopper((Hopper) hopper);
                count++;
            }
        }
        if (count > 0) {
            this.getLogger().log(Level.INFO, "Loaded {0} filter hoppers in chunk {1}/{2}", new Object[]{
                    count, chunk.getX(), chunk.getZ()});
        }
    }

    public void unloadFilterHoppers(Chunk chunk) {
        int count = 0;
        for (var hopper : chunk.getTileEntities(HOPPER_PREDICATE, true)) {
            if (this.isFilterHopper(hopper.getLocation())) {
                this.unloadFilterHopper((Hopper) hopper);
                count++;
            }
        }
        if (count > 0) {
            this.getLogger().log(Level.INFO, "Unloaded {0} filter hoppers in chunk {1}/{2}", new Object[] {
                    count, chunk.getX(), chunk.getZ() });
        }
    }


}
