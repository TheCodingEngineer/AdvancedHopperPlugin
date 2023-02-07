package me.letscode.minecraft.advanced_hopper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;
import java.util.UUID;

public class BlockPos {

    private final UUID worldUID;

    private final int x;

    private final int y;

    private final int z;

    public BlockPos(Location location) {
        this(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public BlockPos(UUID worldUID, int x, int y, int z) {
        this.worldUID = worldUID;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(this.worldUID);
        return new Location(world, this.x, this.y, this.z);
    }

    public UUID getWorldUID() {
        return worldUID;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPos blockPos = (BlockPos) o;
        return x == blockPos.x && y == blockPos.y && z == blockPos.z && Objects.equals(worldUID, blockPos.worldUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldUID, x, y, z);
    }
}
