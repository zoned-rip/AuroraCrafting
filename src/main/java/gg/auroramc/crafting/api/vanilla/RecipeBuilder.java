package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public abstract class RecipeBuilder<T extends RecipeBuilder<T>> {
    protected final NamespacedKey key;
    protected ItemStack result;

    public RecipeBuilder(String id) {
        this.key = new NamespacedKey("aurora", id);
    }

    public RecipeBuilder<T> result(TypeId result) {
        this.result = AuroraAPI.getItemManager().resolveItem(result);
        return this;
    }

    public abstract Recipe build();
}
