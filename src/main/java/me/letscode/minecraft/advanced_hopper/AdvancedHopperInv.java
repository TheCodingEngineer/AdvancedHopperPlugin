package me.letscode.minecraft.advanced_hopper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class AdvancedHopperInv implements InventoryHolder {

    private final AdvancedHopper hopperParent;

    private final Inventory inventory;

    public AdvancedHopperInv(AdvancedHopper hopperParent) {
        this.hopperParent = hopperParent;
        this.inventory = this.createInventory();
    }

    private Inventory createInventory() {
        // --- SPIGOT/PAPER IMPL ---
        // var title = new TranslatableComponent("advanced_hopper.inventory.title");
        // var jsonString = ComponentSerializer.toString(title);
        // return Bukkit.createInventory(this, 9, jsonString);
        // --- ONLY PAPER IMPL ---
        var titleComponent = this.hopperParent.getPlugin().translateComponent("advanced_hopper.inventory.title");
        return Bukkit.createInventory(this, 9, titleComponent);
    }

    // --- SPIGOT/PAPER IMPL ---
    /*private ItemStack createI18NItem(Material material, String key, ChatColor chatColor, BaseComponent... args) {
        var itemStack = new ItemStack(material);
        var itemMeta = itemStack.getItemMeta();
mponent = new TranslatableComponent(key);
        component.setWith(Arrays.asList(args));
        component.setColor(chatColor);
        var co

        itemMeta.setDisplayName(ComponentSerializer.toString(component));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }*/

    // --- ONLY PAPER IMPL ---
    private ItemStack createI18NItem(Material material, String key, TextColor color, Component... args) {
        var itemStack = new ItemStack(material);
        var itemMeta = itemStack.getItemMeta();
        var displayComponent = this.hopperParent.getPlugin().translateComponent(key, args).color(color);
        itemMeta.displayName(displayComponent);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public void updateInventory() {
        this.updateInventory(this.inventory);
    }

    private void updateInventory(Inventory inventory) {
        ItemStack filterStateItem;
        if (this.hopperParent.isEnabled()) {
            // --- SPIGOT/PAPER IMPL ---
            // filterStateItem = createI18NItem(Material.GREEN_CONCRETE, "advanced_hopper.inventory.item.enabled", ChatColor.GREEN);
            // --- ONLY PAPER IMPL ---
            filterStateItem = createI18NItem(Material.GREEN_CONCRETE, "advanced_hopper.inventory.item.enabled", NamedTextColor.GREEN);
        } else {
            // --- SPIGOT/PAPER IMPL ---
            // filterStateItem = createI18NItem(Material.RED_CONCRETE, "advanced_hopper.inventory.item.disabled", ChatColor.RED);
            // --- ONLY PAPER IMPL ---
            filterStateItem = createI18NItem(Material.RED_CONCRETE, "advanced_hopper.inventory.item.disabled", NamedTextColor.RED);
        }

        inventory.setItem(8, filterStateItem);

        var items = this.hopperParent.getFilterItems();
        for (int i = 0; i < 8; i++) {
            ItemStack item;
            if (items.containsKey(i)) {
                var mat = items.get(i);
                // --- SPIGOT/PAPER IMPL ---
                // item = createI18NItem(mat, "advanced_hopper.inventory.filter.used", ChatColor.WHITE,
                //        new TextComponent("#" + (i+1)), new TranslatableComponent(getTranslationKey(mat)));
                // --- ONLY PAPER IMPL ---
                item = createI18NItem(mat, "advanced_hopper.inventory.filter.used",
                       NamedTextColor.WHITE, Component.text("#" + (i+1)), Component.translatable(mat.translationKey()));
            } else {
                // --- SPIGOT/PAPER IMPL ---
                // item = createI18NItem(Material.BARRIER, "advanced_hopper.inventory.filter.empty", ChatColor.RED,
                //        new TextComponent("#" + (i+1)));
                // --- ONLY PAPER IMPL ---
                item = createI18NItem(Material.BARRIER, "advanced_hopper.inventory.filter.empty",
                       NamedTextColor.RED, Component.text("#" + (i+1)));
            }
            inventory.setItem(i, item);
        }
    }
    public String getTranslationKey(Material material) {
        if (material.isBlock()) {
            return String.format("block.minecraft.%s", material.getKey().getKey());
        }
        return String.format("item.minecraft.%s", material.getKey().getKey());
    }

    public final AdvancedHopper getHopperParent() {
        return hopperParent;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void handleInventoryClick(InventoryClickEvent event, Player player) {
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            return;
        }

        if (this.inventory.equals(event.getClickedInventory())) {
            int slot = event.getSlot();
            if (slot >= 0 && slot <= 7) {
                if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR
                        && event.getCurrentItem() != null && event.getCursor() != null) {
                    ItemStack cursor = event.getCursor();
                    this.hopperParent.getFilterItems().put(slot, cursor.getType());
                    this.updateInventory(this.inventory);
                } else if (event.getAction() == InventoryAction.PICKUP_ONE
                        || event.getAction() == InventoryAction.PICKUP_ALL) {
                    this.hopperParent.getFilterItems().remove(slot);
                    this.updateInventory(this.inventory);
                }
            } else if (event.getSlot() == 8) {
                this.hopperParent.setEnabled(!this.hopperParent.isEnabled());
                this.updateInventory(this.inventory);
            }
            event.setCancelled(true);
        }
    }

}
