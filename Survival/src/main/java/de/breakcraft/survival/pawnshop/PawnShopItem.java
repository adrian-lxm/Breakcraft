package de.breakcraft.survival.pawnshop;

import org.bukkit.Material;

public enum PawnShopItem {
    DIRT(Material.DIRT, 1, "Erde"),
    STONE(Material.STONE, 2, "Stein"),
    COBBLESTONE(Material.COBBLESTONE, 2, "Bruchstein"),
    GRAVEL(Material.GRAVEL, 1, "Kies"),
    SAND(Material.SAND, 1, "Sand"),
    COAL(Material.COAL, 2, "Kohle"),
    IRON_INGOT(Material.IRON_INGOT, 90, "Eisenbaren"),
    OAK_LOG(Material.OAK_LOG, 5, "Eichenstamm"),
    BIRCH_LOG(Material.BIRCH_LOG, 5, "Birkenstamm"),
    SPRUCE_LOG(Material.SPRUCE_LOG, 5, "Fichtenstamm"),
    NETHERRACK(Material.NETHERRACK, 1, "Netherrack"),
    QUARZ(Material.QUARTZ, 1, "Quarz");

    private final Material material;
    private final int worth;
    private final String name;

    PawnShopItem(Material material, int worth, String name) {
        this.material = material;
        this.worth = worth;
        this.name = name;
    }

    public static PawnShopItem getByMaterial(Material type) {
        for(var item : values()) {
            if(item.material.equals(type)) return item;
        }
        return null;
    }

    public Material getMaterial() {
        return material;
    }

    public int getWorth() {
        return worth;
    }

    public String getName() {
        return name;
    }
}
