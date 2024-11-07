package gg.auroramc.crafting.api;

import gg.auroramc.aurora.api.AuroraAPI;
import org.bukkit.Material;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VanillaRecipeWrapper extends AuroraRecipe {

    public VanillaRecipeWrapper(CraftingRecipe recipe) {
        super(recipe.getKey().toString(), new ItemPair(AuroraAPI.getItemManager().resolveId(recipe.getResult()), recipe.getResult().getAmount()), null, new ArrayList<>());
    }

    public VanillaRecipeWrapper(String id, ItemPair result, String permission, List<String> lockedLore) {
        super(id, result, permission, lockedLore);
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
            clone.setAmount(Math.max(clone.getAmount() - timesCrafted, 0));
            return clone;
        }).toArray(ItemStack[]::new);
    }
}
