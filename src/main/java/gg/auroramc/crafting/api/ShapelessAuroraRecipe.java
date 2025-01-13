package gg.auroramc.crafting.api;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ShapelessAuroraRecipe extends AuroraRecipe {

    public ShapelessAuroraRecipe(String id, ItemPair result, String permission, List<String> lockedLore) {
        super(id, result, permission, lockedLore);
    }

    public ShapelessAuroraRecipe(String id, ItemPair result, String workbench, String permission, List<String> lockedLore) {
        super(id, result, workbench, permission, lockedLore);
    }

    public boolean registerIngredient(ItemPair itemPair) {
        if (itemPair.id().id().equals("air")) return false;
        ingredients.add(itemPair);
        ingredients.sort(Comparator.comparing(a -> a.id().toString()));
        return true;
    }

    public int getTimesCraftable(List<ItemStack> currentMatrix) {
        var items = new ArrayList<ItemPair>();

        for (var item : currentMatrix) {
            var id = AuroraAPI.getItemManager().resolveId(item);
            if (id.id().equals("air")) continue;
            items.add(new ItemPair(id, item.getAmount()));
        }

        items.sort(Comparator.comparing(a -> a.id().toString()));

        int maxCraftable = Integer.MAX_VALUE;
        var matches = true;

        for (int i = 0; i < currentMatrix.size(); i++) {
            var ingredient = ingredients.size() > i ? ingredients.get(i) : new ItemPair(TypeId.from(Material.AIR), 0);
            var item = items.size() > i && items.get(i) != null ? items.get(i) : new ItemPair(TypeId.from(Material.AIR), 0);
            var itemTypeId = item.id();
            if (!itemTypeId.equals(ingredient.id())) {
                matches = false;
                break;
            } else if (item.amount() < ingredient.amount()) {
                matches = false;
                break;
            } else if (!ingredient.id().id().equals("air")) {
                maxCraftable = Math.min(maxCraftable, Math.max(1, item.amount()) / Math.max(1, ingredient.amount()));
            }
        }

        if (!matches) return 0;

        return maxCraftable;
    }

    public ItemStack[] calcRemainingIngredientMatrix(int timesCrafted, List<ItemStack> currentMatrix) {
        var remainingItems = new ItemStack[currentMatrix.size()];
        var ingredientsCopy = new ArrayList<>(ingredients); // Create a copy of ingredients

        // Copy the matrix to maintain the order, then process each item to deduct ingredients
        for (int i = 0; i < currentMatrix.size(); i++) {
            var item = currentMatrix.get(i);
            if (item == null || item.getAmount() == 0) {
                remainingItems[i] = null;
                continue;
            }

            var itemId = AuroraAPI.getItemManager().resolveId(item);

            // Find matching ingredient for the current item
            for (var ingredient : ingredientsCopy) {
                if (ingredient.id().equals(itemId)) {
                    int requiredAmount = ingredient.amount() * timesCrafted;
                    int currentAmount = item.getAmount();

                    if (currentAmount <= requiredAmount) {
                        remainingItems[i] = null; // All used up
                    } else {
                        var newItem = item.clone();
                        newItem.setAmount(currentAmount - requiredAmount);
                        remainingItems[i] = newItem;
                    }

                    // Remove the ingredient from the copy list after deducting
                    ingredientsCopy.remove(ingredient);
                    break;
                }
            }
        }

        return remainingItems;
    }
}
