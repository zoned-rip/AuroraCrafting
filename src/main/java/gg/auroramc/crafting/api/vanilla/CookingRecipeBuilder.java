package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.recipe.CookingBookCategory;

public abstract class CookingRecipeBuilder<T extends CookingRecipe<T>> extends RecipeBuilder<CookingRecipeBuilder<T>> {
    protected RecipeChoice input = EmptyRecipeChoice.get();
    protected float experience = 0;
    protected int cookingTime = 200;
    protected CookingBookCategory category = CookingBookCategory.MISC;
    private String group;

    public CookingRecipeBuilder(String id) {
        super(id);
    }

    public CookingRecipeBuilder<T> category(CookingBookCategory category) {
        this.category = category;
        return this;
    }

    public CookingRecipeBuilder<T> group(String group) {
        this.group = group;
        return this;
    }

    public CookingRecipeBuilder<T> input(TypeId input) {
        this.input = new RecipeChoice.ExactChoice(AuroraAPI.getItemManager().resolveItem(input));
        return this;
    }

    public CookingRecipeBuilder<T> experience(float experience) {
        this.experience = experience;
        return this;
    }

    public CookingRecipeBuilder<T> cookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
        return this;
    }

    protected abstract CookingRecipe<T> buildInternal();

    public CookingRecipe<T> build() {
        var recipe = buildInternal();
        recipe.setCategory(category);
        if (group != null) {
            recipe.setGroup(group);
        }
        return recipe;
    }
}
