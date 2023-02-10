package me.letscode.minecraft.advanced_hopper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class AdvancedHopper {

    private static final NamespacedKey ENABLED_KEY = new NamespacedKey(AdvancedHopperPlugin.PLUGIN_KEY, "enabled");
    private static final NamespacedKey ITEMS_KEY = new NamespacedKey(AdvancedHopperPlugin.PLUGIN_KEY, "items");
    private static final NamespacedKey FILTER_KEY = new NamespacedKey(AdvancedHopperPlugin.PLUGIN_KEY, "filter");
    private static final NamespacedKey INDEX_KEY = new NamespacedKey(AdvancedHopperPlugin.PLUGIN_KEY, "index");

    private final BlockPos position;

    private boolean enabled;
    private final Map<Integer, Material> filterItems;

    private final AdvancedHopperInv inventoryHolder;

    private final AdvancedHopperPlugin plugin;

    public AdvancedHopper(AdvancedHopperPlugin plugin, Location position) {
        this(plugin, new BlockPos(position));
    }

    public AdvancedHopper(AdvancedHopperPlugin plugin, BlockPos position) {
        this.plugin = plugin;
        this.position = position;
        this.enabled = false;
        this.filterItems = new HashMap<>();
        this.inventoryHolder = new AdvancedHopperInv(this);
    }

    public void loadFrom(PersistentDataContainer container) {
        this.enabled = container.getOrDefault(ENABLED_KEY, PersistentDataType.BYTE, (byte) 0) == 1;

        this.filterItems.clear();
        if (container.has(ITEMS_KEY, PersistentDataType.TAG_CONTAINER_ARRAY)) {
            var itemList = container.get(ITEMS_KEY, PersistentDataType.TAG_CONTAINER_ARRAY);
            for (var item : itemList) {
                var itemName = item.getOrDefault(FILTER_KEY, PersistentDataType.STRING, "");
                var slot = item.getOrDefault(INDEX_KEY, PersistentDataType.INTEGER, -1);
                if (slot >= 0 && slot <= 7) {
                    var resolved = Material.matchMaterial(itemName);
                    if (resolved != null) this.filterItems.put(slot, resolved);
                }

            }
        }
    }

    public AdvancedHopperPlugin getPlugin() {
        return plugin;
    }

    public BlockPos getPosition() {
        return position;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<Integer, Material> getFilterItems() {
        return filterItems;
    }

    public AdvancedHopperInv getInventoryHolder() {
        return inventoryHolder;
    }

    public void saveTo(PersistentDataContainer container) {
        container.set(ENABLED_KEY, PersistentDataType.BYTE, this.enabled ? (byte) 1 : (byte) 0);

        var list = new PersistentDataContainer[this.filterItems.size()];
        int i = 0;
        for (var entry : this.filterItems.entrySet()) {
            list[i] = container.getAdapterContext().newPersistentDataContainer();
            list[i].set(INDEX_KEY, PersistentDataType.INTEGER, entry.getKey());
            list[i].set(FILTER_KEY, PersistentDataType.STRING, entry.getValue().getKey().toString());
            i++;
        }
        container.set(ITEMS_KEY, PersistentDataType.TAG_CONTAINER_ARRAY, list);
    }

    public InventoryView openInventory(Player player) {
        return player.openInventory(this.inventoryHolder.getInventory());
    }

    public boolean willItemPassFilter(ItemStack item) {
        return this.isEnabled() && this.filterItems.containsValue(item.getType());
    }
}
