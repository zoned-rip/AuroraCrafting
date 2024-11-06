package gg.auroramc.crafting.api;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.aurora.api.util.ItemUtils;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public class AuroraRecipe {
    private final String id;
    private final ItemPair result;
    private final List<ItemPair> ingredients = new ArrayList<>();
    private final String permission;

    public AuroraRecipe(String id, ItemPair result) {
        this(id, result, null);
    }

    public AuroraRecipe(String id, ItemPair result, String permission) {
        this.id = id;
        this.result = result;
        this.permission = permission;
    }

    public void addIngredient(ItemPair itemPair) {
        ingredients.add(itemPair);
    }

    public String asLookupKey() {
        var key = new StringBuilder();
        for (var ingredient : ingredients) {
            key.append(ingredient.id().toString());
            key.append(";");
        }
        return key.toString();
    }

    public int getTimesCraftable(List<ItemStack> items) {
        int maxCraftable = Integer.MAX_VALUE;

        var matches = true;

        for (int i = 0; i < 9; i++) {
            var item = items.get(i);
            var itemTypeId = item.isEmpty() ? TypeId.from(Material.AIR) : AuroraAPI.getItemManager().resolveId(item);
            if (!itemTypeId.equals(ingredients.get(i).id())) {
                matches = false;
                break;
            } else if (item.getAmount() < ingredients.get(i).amount()) {
                matches = false;
                break;
            } else if(!ingredients.get(i).id().id().equals("air")) {
                maxCraftable = Math.min(maxCraftable, Math.max(1, item.getAmount()) / Math.max(1, ingredients.get(i).amount()));
            }
        }

        if (!matches) return 0;

        return maxCraftable;
    }

    public ItemStack[] calcRemainingIngredientMatrix(int timesCrafted, List<ItemStack> currentMatrix) {
        var items = new ItemStack[9];

        for (int i = 0; i < 9; i++) {
            var ingredient = ingredients.get(i);
            var item = currentMatrix.get(i);
            if (item.getAmount() <= ingredient.amount() * timesCrafted) {
                items[i] = null;
            } else {
                item.setAmount(item.getAmount() - ingredient.amount() * timesCrafted);
                items[i] = item;
            }
        }

        return items;
    }

    public ItemStack[] getTotalResult(int timesCrafted) {
        var item = AuroraAPI.getItemManager().resolveItem(result.id());
        return ItemUtils.createStacksFromAmount(item, result.amount() * timesCrafted);
    }

    public ItemStack getResultItem() {
        var item = AuroraAPI.getItemManager().resolveItem(result.id());
        item.setAmount(result.amount());
        return item;
    }

    public List<ItemStack> getIngredientItems() {
        var items = new ArrayList<ItemStack>();

        for (var entry : ingredients) {
            var item = AuroraAPI.getItemManager().resolveItem(entry.id());
            item.setAmount(entry.amount());
            items.add(item);
        }

        return items;
    }

    public boolean hasPermission(Player player) {
        return permission == null || player.hasPermission(permission);
    }
}
