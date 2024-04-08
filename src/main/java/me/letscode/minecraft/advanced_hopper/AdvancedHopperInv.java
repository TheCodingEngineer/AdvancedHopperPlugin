package me.letscode.minecraft.advanced_hopper;

import com.destroystokyo.paper.MaterialSetTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdvancedHopperInv implements InventoryHolder {

    private static final Component CHECK_MARK = Component.text( '✔', NamedTextColor.GREEN);

    private static final Component CROSS_MARK = Component.text('✘', NamedTextColor.RED);

    private final AdvancedHopper hopperParent;

    private final Inventory inventory;

    private int[] indicies;

    public AdvancedHopperInv(AdvancedHopper hopperParent) {
        this.hopperParent = hopperParent;
        this.inventory = this.createInventory();
        this.indicies = new int[AdvancedHopper.MAX_FILTER_ITEMS];
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

    private ItemStack createI18NItemDesc(Material material, String key, TextColor color, List<Component> desc,
                                         Component... args) {
        var itemStack = new ItemStack(material);
        var itemMeta = itemStack.getItemMeta();
        var displayComponent = this.hopperParent.getPlugin().translateComponent(key, args).color(color);
        itemMeta.displayName(displayComponent);
        itemMeta.lore(desc);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public void updateInventory() {
        this.updateInventory(this.inventory);
    }

    public void tickInventory() {
        if (!this.inventory.getViewers().isEmpty()) {
            this.updateInventory(this.inventory);
        }
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
                var filter = items.get(i);
                // --- SPIGOT/PAPER IMPL ---
                // item = createI18NItem(mat, "advanced_hopper.inventory.filter.used", ChatColor.WHITE,
                //        new TextComponent("#" + (i+1)), new TranslatableComponent(getTranslationKey(mat)));
                // --- ONLY PAPER IMPL ---
                MaterialSetTag tagSet = TagFilters.getTag(filter.material());
                Material material = filter.material();
                Component materialName = Component.translatable(filter.material().translationKey());
                if (tagSet != null) {
                    if (filter.wildcard()) {
                        int index = (this.indicies[i] + 1) % tagSet.getValues().size();
                        this.indicies[i] = index;

                        material = tagSet.getValues().stream().toList().get(index);
                        materialName = Component.text(tagSet.key().toString());
                    }

                    item = createI18NItemDesc(material, "advanced_hopper.inventory.filter.used",
                            NamedTextColor.WHITE,
                            Arrays.asList(
                                    text("Wildcard: ", NamedTextColor.GRAY).append(filter.wildcard() ? CHECK_MARK : CROSS_MARK),
                                    text("Tag: ", NamedTextColor.GRAY).append(text(tagSet.getKey().toString(), NamedTextColor.DARK_GRAY))
                            ),
                            Component.text("#" + (i+1)),
                            materialName);
                } else {
                    item = createI18NItem(material, "advanced_hopper.inventory.filter.used",
                            NamedTextColor.WHITE, Component.text("#" + (i+1)),
                            materialName);
                }
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
    private String getTranslationKey(Material material) {
        if (material.isBlock()) {
            return String.format("block.minecraft.%s", material.getKey().getKey());
        }
        return String.format("item.minecraft.%s", material.getKey().getKey());
    }

    private Component text(String text, TextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }

    public final AdvancedHopper getHopperParent() {
        return hopperParent;
    }

    @Override
    public @NotNull Inventory getInventory() {
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
                    this.indicies[slot] = 0;
                    this.hopperParent.getFilterItems().put(slot, new FilterItem(cursor.getType(), false));
                    this.updateInventory(this.inventory);
                } else if (event.isRightClick()) {
                    var item = this.hopperParent.getFilterItems().get(slot);
                    if (item != null) {
                        var tagRegistry = TagFilters.getTag(item.material());
                        if (item.wildcard()) {
                            this.indicies[slot] = 0;
                            this.hopperParent.getFilterItems().put(slot, new FilterItem(item.material(), false));
                            this.updateInventory(this.inventory);
                        } else  if (tagRegistry != null) {
                            this.indicies[slot] = 0;
                            this.hopperParent.getFilterItems().put(slot, new FilterItem(item.material(), true));
                            this.updateInventory(this.inventory);
                        }
                    }
                } else if (event.isLeftClick()) {
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
