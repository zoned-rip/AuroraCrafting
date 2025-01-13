package gg.auroramc.crafting.api;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.config.RecipeBookConfig;
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
    private RecipeBookConfig.RecipeCategory category;
    private final String workbench;

    public AuroraRecipe(String id, ItemPair result, String permission, List<String> lockedLore) {
        this(id, result, "default", permission, lockedLore);
    }

    public AuroraRecipe(String id, ItemPair result, String workbench, String permission, List<String> lockedLore) {
        this.id = id;
        this.result = result;
        this.permission = permission;
        this.lockedLore = lockedLore;
        this.workbench = workbench;
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

    public boolean hasPermission(Player player, String workbenchId) {
        var canCraft = permission == null || player.hasPermission(permission);
        var canUseWorkbench = workbench.equals(workbenchId) && player.hasPermission("aurora.crafting.use." + workbenchId);
        return canCraft && canUseWorkbench;
    }

    public boolean hasPermission(Player player) {
        return permission == null || player.hasPermission(permission);
    }

    public int getQuickCraftTimes(Player player) {
        return getQuickCraftTimes(buildItemCounts(player));
    }

    static Map<TypeId, Integer> buildItemCounts(Player player) {
        Map<TypeId, Integer> itemCount = new HashMap<>(player.getInventory().getSize());

        for (var item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR || item.getAmount() == 0) continue;
            var id = AuroraAPI.getItemManager().resolveId(item);
            itemCount.merge(id, item.getAmount(), Integer::sum);
        }

        return itemCount;
    }

    public int getQuickCraftTimes(Map<TypeId, Integer> itemCount) {
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
        // Calculate the total ingredients required
        var totalIngredients = ingredientCount.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue() * times)) // Multiply by the number of times
                .toList();

        // Remove items based on their IDs
        var failedToRemove = false;
        for (var entry : totalIngredients) {
            TypeId itemId = entry.getKey();
            int requiredAmount = entry.getValue();

            // Iterate through the player's inventory to remove items
            var inventory = player.getInventory();
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                ItemStack itemStack = inventory.getItem(slot);
                if (itemStack == null) continue;

                // Resolve the item ID for the current stack
                TypeId currentItemId = AuroraAPI.getItemManager().resolveId(itemStack);
                if (currentItemId != null && currentItemId.equals(itemId)) {
                    int stackAmount = itemStack.getAmount();

                    if (stackAmount >= requiredAmount) {
                        // Reduce the stack size or remove the item
                        itemStack.setAmount(stackAmount - requiredAmount);
                        if (itemStack.getAmount() <= 0) {
                            inventory.setItem(slot, null); // Remove the item if the stack is empty
                        }
                        requiredAmount = 0; // All required items have been removed
                        break;
                    } else {
                        // Remove the entire stack and reduce the required amount
                        inventory.setItem(slot, null);
                        requiredAmount -= stackAmount;
                    }
                }
            }

            // If we couldn't remove all required items, mark it as failed
            if (requiredAmount > 0) {
                failedToRemove = true;
                break;
            }
        }

        if (!failedToRemove) {
            // Add the crafted result to the inventory
            player.getInventory().addItem(this.getTotalResult(addMinusOneResult ? times - 1 : times));
        } else {
            // Log a warning if the recipe couldn't be completed
            AuroraCrafting.logger().severe("Failed to quick craft recipe " + id + " for player " + player.getName() +
                    ", because ingredients couldn't be fully taken. THIS IS A DUPE!");
        }
    }
}
