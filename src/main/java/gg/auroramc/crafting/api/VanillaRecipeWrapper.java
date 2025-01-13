package gg.auroramc.crafting.api;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.crafting.AuroraCrafting;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class VanillaRecipeWrapper extends AuroraRecipe {
    private static final Set<Material> BUCKET = Set.of(Material.MILK_BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET);
    private static final Set<Material> LEATHER_ARMOR = Set.of(
            Material.LEATHER_HORSE_ARMOR, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET,
            Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS
    );

    private ItemStack[] matrix;
    private final CraftingRecipe recipe;

    public VanillaRecipeWrapper(CraftingRecipe recipe, ItemStack[] matrix) {
        super(recipe.getKey().toString(), new ItemPair(AuroraAPI.getItemManager().resolveId(recipe.getResult()), Math.max(recipe.getResult().getAmount(), 1)), null, new ArrayList<>());
        this.matrix = matrix;
        this.recipe = recipe;
    }

    @Override
    protected boolean registerIngredient(ItemPair itemPair) {
        return false;
    }

    @Override
    public int getTimesCraftable(List<ItemStack> items) {
        return items.stream().filter(i -> i != null && i.getType() != Material.AIR).min(Comparator.comparingInt(ItemStack::getAmount)).map(ItemStack::getAmount).orElse(0);
    }

    @Override
    public ItemStack[] calcRemainingIngredientMatrix(int timesCrafted, List<ItemStack> currentMatrix) {
        return currentMatrix.stream().map(item -> {
            var clone = item.clone();
            if (BUCKET.contains(clone.getType())) {
                return clone.withType(Material.BUCKET);
            }
            clone.setAmount(Math.max(clone.getAmount() - timesCrafted, 0));
            return clone;
        }).toArray(ItemStack[]::new);
    }

    @Override
    public ItemStack getResultItem() {
        if (recipe.getKey().getNamespace().equals("minecraft") && recipe.getKey().getKey().equals("armor_dye")) {
            return getDyeResult();
        }

        return super.getResultItem();
    }

    @Override
    public ItemStack[] getTotalResult(int timesCrafted) {
        if (recipe.getKey().getNamespace().equals("minecraft") && recipe.getKey().getKey().equals("armor_dye")) {
            ItemUtils.createStacksFromAmount(getDyeResult(), timesCrafted);
        }
        return super.getTotalResult(timesCrafted);
    }

    private ItemStack getDyeResult() {
        ItemStack armor = null;
        ItemStack dye = null;

        for (var item : matrix) {
            if (LEATHER_ARMOR.contains(item.getType())) {
                armor = item.clone();
                armor.setAmount(1);
            } else if (item.getType().name().endsWith("_DYE")) {
                dye = item;
            }
        }

        if (armor == null || dye == null) {
            AuroraCrafting.logger().warning("Failed to find armor or dye in matrix");
            return null;
        }

        var armorMeta = armor.getItemMeta();
        var dyeMeta = dye.getItemMeta();

        AuroraCrafting.logger().info(dyeMeta.getClass().getSimpleName());

        if (armorMeta instanceof LeatherArmorMeta leatherArmorMeta) {
            try {
                var color = DyeColor.valueOf(dye.getType().name().replace("_DYE", "")).getColor();
                leatherArmorMeta.setColor(color);
                armor.setItemMeta(leatherArmorMeta);
                return armor;
            } catch (Exception e) {
                AuroraCrafting.logger().warning("Failed to parse dye color for " + dye.getType().name());
                return null;
            }
        } else {
            return null;
        }
    }
}
