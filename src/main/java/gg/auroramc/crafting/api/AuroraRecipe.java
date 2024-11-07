package gg.auroramc.crafting.api;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.aurora.api.util.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public abstract class AuroraRecipe {
    protected final String id;
    protected final ItemPair result;
    protected final List<ItemPair> ingredients = new ArrayList<>();
    protected final String permission;
    @Setter
    protected List<String> lockedLore;
    protected final Map<TypeId, Integer> ingredientCount = new HashMap<>();
    @Setter
    private String category;

    public AuroraRecipe(String id, ItemPair result, String permission, List<String> lockedLore) {
        this.id = id;
        this.result = result;
        this.permission = permission;
        this.lockedLore = lockedLore;

    }

    public void addIngredient(ItemPair itemPair) {
        if (registerIngredient(itemPair)) {
            ingredientCount.merge(itemPair.id(), itemPair.amount(), Integer::sum);
        }
    }

    protected abstract boolean registerIngredient(ItemPair itemPair);

    public String asLookupKey() {
        var key = new StringBuilder();

        for (var ingredient : ingredients) {
            key.append(ingredient.id().toString());
            key.append(";");
        }

        return key.toString();
    }

    public abstract int getTimesCraftable(List<ItemStack> items);

    public abstract ItemStack[] calcRemainingIngredientMatrix(int timesCrafted, List<ItemStack> currentMatrix);

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

    public int getQuickCraftTimes(Player player) {
        Map<TypeId, Integer> itemCount = new HashMap<>(player.getInventory().getSize());

        for (var item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR || item.getAmount() == 0) continue;
            var id = AuroraAPI.getItemManager().resolveId(item);
            itemCount.merge(id, item.getAmount(), Integer::sum);
        }

        int maxCraftable = Integer.MAX_VALUE;
        var matches = true;

        for (var entry : ingredientCount.entrySet()) {
            var ingredient = entry.getKey();
            var ingredientAmount = entry.getValue();
            var itemAmount = itemCount.getOrDefault(ingredient, 0);

            if (itemAmount < ingredientAmount) {
                matches = false;
                break;
            } else if (ingredientAmount != 0) {
                maxCraftable = Math.min(maxCraftable, itemAmount / ingredientAmount);
            }
        }

        return matches ? maxCraftable : 0;
    }

    /**
     * Quick craft the recipe for the player
     * You should check with getQuickCraftTimes before calling this method
     */
    public void quickCraft(Player player, int times, boolean addMinusOneResult) {
        var itemsToRemove = ingredientCount.entrySet().stream().flatMap((entry) -> {
            var item = AuroraAPI.getItemManager().resolveItem(entry.getKey());
            return Arrays.stream(ItemUtils.createStacksFromAmount(item, entry.getValue() * times));
        }).toArray(ItemStack[]::new);

        player.getInventory().removeItemAnySlot(itemsToRemove);
        player.getInventory().addItem(this.getTotalResult(addMinusOneResult ? times - 1 : times));
    }
}
