package me.letscode.minecraft.advanced_hopper;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

public class TagFilters {
    private static MaterialSetTag WOOL = new MaterialSetTag(keyFor("wool"))
            .add(Tag.WOOL).lock();
    private static MaterialSetTag WOOL_CARPETS = new MaterialSetTag(keyFor("wool_carpets"))
            .add(Tag.WOOL_CARPETS).lock();
    private static MaterialSetTag CANDLES = new MaterialSetTag(keyFor("candles"))
            .add(Tag.CANDLES).lock();
    private static MaterialSetTag PLANKS = new MaterialSetTag(keyFor("planks"))
            .add(Tag.PLANKS).lock();
    private static MaterialSetTag LOGS = new MaterialSetTag(keyFor("logs"))
            .add(Tag.LOGS).lock();
    private static MaterialSetTag FLOWERS = new MaterialSetTag(keyFor("flowers"))
            .add(Tag.FLOWERS).lock();
    private static MaterialSetTag BANNERS = new MaterialSetTag(keyFor("banners"))
            .add(Tag.BANNERS).lock();

    public static final MaterialSetTag[] BLOCK_FILTERS = {
            BANNERS,
            MaterialTags.BEDS,
            CANDLES,
            MaterialTags.CONCRETES,
            MaterialTags.CONCRETE_POWDER,
            MaterialTags.CORAL_BLOCKS,
            MaterialTags.CORAL_FANS,
            LOGS,
            FLOWERS,
            PLANKS,
            MaterialTags.SHULKER_BOXES,
            MaterialTags.STAINED_GLASS,
            MaterialTags.STAINED_GLASS_PANES,
            MaterialTags.TERRACOTTA,
            MaterialTags.GLAZED_TERRACOTTA,
            MaterialTags.MUSIC_DISCS,
            MaterialTags.ARROWS,
            MaterialTags.DYES,
            MaterialTags.SANDSTONES,
            MaterialTags.QUARTZ_BLOCKS,
            MaterialTags.SIGNS,
            MaterialTags.SPAWN_EGGS,
            WOOL,
            WOOL_CARPETS
    };

    public static MaterialSetTag getTag(Material material) {
        for (var registry : BLOCK_FILTERS) {
            if (registry.isTagged(material)) {
                return registry;
            }
        }
        return null;
    }

    private static NamespacedKey keyFor(String key) {
        return new NamespacedKey("paper", key + "_settag");
    }

}
