package gg.auroramc.crafting.api;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ShapedAuroraRecipe extends AuroraRecipe {
    public ShapedAuroraRecipe(String id, ItemPair result, String permission, List<String> lockedLore) {
        super(id, result, permission, lockedLore);
    }

    public boolean registerIngredient(ItemPair itemPair) {
        ingredients.add(itemPair);
        return true;
    }

    public int getTimesCraftable(List<ItemStack> items) {
        int maxCraftable = Integer.MAX_VALUE;

        var matches = true;

        for (int i = 0; i < items.size(); i++) {
            var ingredient = ingredients.size() > i ? ingredients.get(i) : new ItemPair(TypeId.from(Material.AIR), 0);
            var item = items.get(i);
            var itemTypeId = item.isEmpty() ? TypeId.from(Material.AIR) : AuroraAPI.getItemManager().resolveId(item);
            if (!itemTypeId.equals(ingredient.id())) {
                matches = false;
                break;
            } else if (item.getAmount() < ingredient.amount()) {
                matches = false;
                break;
            } else if (!ingredient.id().id().equals("air")) {
                maxCraftable = Math.min(maxCraftable, Math.max(1, item.getAmount()) / Math.max(1, ingredient.amount()));
            }
        }

        if (!matches) return 0;

        return maxCraftable;
    }

    public ItemStack[] calcRemainingIngredientMatrix(int timesCrafted, List<ItemStack> currentMatrix) {
        var items = new ItemStack[currentMatrix.size()];

        for (int i = 0; i < currentMatrix.size(); i++) {
            var ingredient = ingredients.size() > i ? ingredients.get(i) : new ItemPair(TypeId.from(Material.AIR), 0);
            var item = currentMatrix.get(i);
            if (item.getAmount() <= ingredient.amount() * timesCrafted) {
                items[i] = null;
            } else {
                var newItem = item.clone();
                newItem.setAmount(item.getAmount() - ingredient.amount() * timesCrafted);
                items[i] = newItem;
            }
        }

        return items;
    }
}
