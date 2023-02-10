package me.letscode.minecraft.advanced_hopper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class AdvancedHopperPlugin extends JavaPlugin {

    public static final String PLUGIN_KEY = "advancedhopper";
    public static final String FILTER_ITEM_KEY = "advanced_hopper.item.filter_hopper";
    public static final String RECIPE_KEY = "filter_hopper_recipe";

    public static final boolean USE_BUILTIN_RESOURCE = true;

    private NamespacedKey hopperDataKey;
    private NamespacedKey hopperItemKey;


    private Map<BlockPos, AdvancedHopper> hopperCache;

    private ResourceBundle langBundle;

    @Override
    public void onLoad() {
        this.hopperDataKey = new NamespacedKey(this, "data");
        this.hopperItemKey = new NamespacedKey(this, "filter_hopper_item");

        this.hopperCache = new HashMap<>();

        this.addPluginResourcePack();
        this.addFilterHopperRecipe();
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
        // --- SPIGOT/PAPER IMPL ---
        // var component = new TranslatableComponent(FILTER_ITEM_KEY);
        // component.setColor(ChatColor.AQUA);
        // itemMeta.setDisplayName(ComponentSerializer.toString(component));
        // --- ONLY PAPER IMPL ---
        itemMeta.displayName(this.translateComponent(FILTER_ITEM_KEY).color(NamedTextColor.AQUA));
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.getPersistentDataContainer().set(this.hopperItemKey, PersistentDataType.BYTE, (byte) 1);
        itemStack.setItemMeta(itemMeta);
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        return itemStack;
    }

    public boolean isFilterHopperItem(ItemStack itemStack) {
        var persistentDataContainer = itemStack.getItemMeta().getPersistentDataContainer();
        return persistentDataContainer.getOrDefault(this.hopperItemKey, PersistentDataType.BYTE, (byte) 0) == 1;
    }

    public void addFilterHopperRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(this, RECIPE_KEY);
        ShapedRecipe shapedRecipe = new ShapedRecipe(recipeKey, this.createFilterHopperItem());
        shapedRecipe.shape("crc", "chc", "cac");
        shapedRecipe.setIngredient('c', Material.COPPER_INGOT);
        shapedRecipe.setIngredient('a', Material.AMETHYST_SHARD);
        shapedRecipe.setIngredient('h', Material.HOPPER);
        shapedRecipe.setIngredient('r', Material.COMPARATOR);

        getServer().addRecipe(shapedRecipe);
    }

    private void addPluginResourcePack() {
        Locale locale = Locale.getDefault();
        String definedLang = System.getProperty("advanced_hopper.lang");
        if (definedLang == null) {
            definedLang = System.getenv("ADVANCED_HOPPER_LANG");
        }
        if (definedLang != null) {
            locale = Locale.forLanguageTag(definedLang);
            if (locale.toString().isEmpty()) {
                locale = Locale.getDefault();
                this.getLogger().log(Level.INFO, "Unknown language tag {0}, fallback to default language {1}",
                                new Object[] { definedLang, locale });
            }
        }
        this.langBundle = ResourceBundle.getBundle("lang.translations", locale);
        this.getLogger().log(Level.INFO, "Using {0} as language bundle (desired {1})",
                new Object[] { this.langBundle.getLocale(), locale });
    }

    public Component translateComponent(String key, Component... args) {
        if (USE_BUILTIN_RESOURCE) {
            String translated = this.langBundle.getString(key);
            // split with delimiters
            String[] parts = translated.split("((?=%s)|(?<=%s))");
            var parentComponent = Component.text();
            int arg = 0;
            for (String part : parts) {
                if (part.equals("%s") && arg < args.length) {
                    parentComponent.append(args[arg++]);
                } else {
                    parentComponent.append(Component.text(part));
                }
            }
            return parentComponent.asComponent();
        } else {
            return Component.translatable(key, args);
        }
    }

    public AdvancedHopper tryLoadFilterHopper(Hopper hopper) {
        var position = new BlockPos(hopper.getLocation());
        if (this.hopperCache.containsKey(position)) {
            return this.hopperCache.get(position);
        } else {
            var dataContainer = hopper.getPersistentDataContainer();
            if (dataContainer.has(this.hopperDataKey, PersistentDataType.TAG_CONTAINER)) {
                var container = dataContainer.get(this.hopperDataKey, PersistentDataType.TAG_CONTAINER);
                AdvancedHopper filterHopper = new AdvancedHopper(this, position);
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
            AdvancedHopper filterHopper = new AdvancedHopper(this, block.getLocation());
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

    public void removeFilterHopper(Hopper hopper) {
        var position = new BlockPos(hopper.getLocation());
        this.hopperCache.remove(position);
    }

    public boolean isFilterHopper(Location location) {
        return this.isFilterHopper(location.getBlock().getState());
    }

    public boolean isFilterHopper(Block block) {
        return this.isFilterHopper(block.getState());
    }

    public boolean isFilterHopper(BlockState blockState) {
        if (blockState instanceof Hopper hopper) {
            return hopper.getPersistentDataContainer().has(this.hopperDataKey, PersistentDataType.TAG_CONTAINER);
        }
        return false;
    }

    public void loadFilterHoppers(Chunk chunk) {
        int count = 0;
        for (var tile : chunk.getTileEntities()) {
            if (tile instanceof Hopper hopper) {
                if (this.isFilterHopper(hopper.getLocation())) {
                    this.tryLoadFilterHopper(hopper);
                    count++;
                }
            }
        }
        if (count > 0) {
            this.getLogger().log(Level.INFO, "Loaded {0} filter hoppers in chunk {1}/{2}", new Object[]{
                    count, chunk.getX(), chunk.getZ()});
        }
    }

    public void unloadFilterHoppers(Chunk chunk) {
        int count = 0;
        for (var tile : chunk.getTileEntities()) {
            if (tile instanceof Hopper hopper) {
                if (this.isFilterHopper(hopper.getLocation())) {
                    this.unloadFilterHopper(hopper);
                    count++;
                }
            }
        }
        if (count > 0) {
            this.getLogger().log(Level.INFO, "Unloaded {0} filter hoppers in chunk {1}/{2}", new Object[] {
                    count, chunk.getX(), chunk.getZ() });
        }
    }


}
