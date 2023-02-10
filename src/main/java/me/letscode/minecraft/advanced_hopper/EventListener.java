package me.letscode.minecraft.advanced_hopper;

import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class EventListener implements Listener {

    private final AdvancedHopperPlugin plugin;

    public EventListener(AdvancedHopperPlugin plugin) {
        this.plugin = plugin;
    }

    /*@EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (this.plugin.isFilterHopper(block)) {
                var hopperState = (Hopper) block.getState();
                var filterHopper = this.plugin.tryLoadFilterHopper(hopperState);

                // only if player clicks with item and is sneaking do nothing
                if (event.hasItem() && player.isSneaking()) {
                    return;
                }

                if (filterHopper != null) {
                    filterHopper.openInventory(player);
                }
                event.setUseInteractedBlock(Event.Result.DENY);
            }
        }
    }*/

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.HOPPER) {
            var location = event.getInventory().getLocation();
            if (location != null) {
                var block = location.getBlock();
                if (this.plugin.isFilterHopper(block)) {
                    var hopperState = (Hopper) block.getState();
                    var filterHopper = this.plugin.tryLoadFilterHopper(hopperState);

                    if (filterHopper != null) {
                        filterHopper.openInventory((Player) event.getPlayer());
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (event.getInventory().getHolder() instanceof AdvancedHopperInv filterInv) {
                filterInv.handleInventoryClick(event, player);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof AdvancedHopperInv filterInv) {
            this.plugin.updateFilterHopper(filterInv.getHopperParent());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFilterItem(InventoryMoveItemEvent event) {
        if (event.getDestination().getType() == InventoryType.HOPPER && event.getDestination().getLocation() != null) {
            var position = new BlockPos(event.getDestination().getLocation());
            var cache = this.plugin.getHopperCache();
            if (cache.containsKey(position)) {
                boolean passed = cache.get(position).willItemPassFilter(event.getItem());
                event.setCancelled(!passed);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFilterItem(InventoryPickupItemEvent event) {
        if (event.getInventory().getType() == InventoryType.HOPPER && event.getInventory().getLocation() != null) {
            var position = new BlockPos(event.getInventory().getLocation());
            var cache = this.plugin.getHopperCache();
            if (cache.containsKey(position)) {
                boolean passed = cache.get(position).willItemPassFilter(event.getItem().getItemStack());
                event.setCancelled(!passed);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHopperPlace(BlockPlaceEvent event) {
        var block = event.getBlock();
        if (block.getType() == Material.HOPPER && this.plugin.isFilterHopperItem(event.getItemInHand())) {
            this.plugin.placeFilterHopper(block);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHopperDestroy(BlockBreakEvent event) {
        var block = event.getBlock();
        if (this.plugin.isFilterHopper(block)) {
            this.plugin.removeFilterHopper((Hopper) block.getState());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockItemDrop(BlockDropItemEvent event) {
        if (this.plugin.isFilterHopper(event.getBlockState())) {
            for (Item item : event.getItems()) {
                var itemStack = item.getItemStack();
                if (itemStack.getType() == Material.HOPPER && itemStack.getItemMeta().hasDisplayName()) {
                    /* drop special item */
                    item.setItemStack(this.plugin.createFilterHopperItem());
                    break;
                }
            }
        }
    }

    /* World events: clean up cache */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        this.plugin.loadFilterHoppers(event.getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        this.plugin.unloadFilterHoppers(event.getChunk());
    }


}
