package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;

public class SmithingTransformRecipeBuilder extends RecipeBuilder<SmithingTransformRecipeBuilder> {
    private RecipeChoice template = EmptyRecipeChoice.get();
    private RecipeChoice base = EmptyRecipeChoice.get();
    private RecipeChoice addition = EmptyRecipeChoice.get();
    private boolean copyDataComponents = false;

    public SmithingTransformRecipeBuilder(String id) {
        super(id);
    }

    public static SmithingTransformRecipeBuilder smithingTransformRecipe(String id) {
        return new SmithingTransformRecipeBuilder(id);
    }

    public SmithingTransformRecipeBuilder template(TypeId template) {
        this.template = new RecipeChoice.ExactChoice(AuroraAPI.getItemManager().resolveItem(template));
        return this;
    }

    public SmithingTransformRecipeBuilder base(TypeId base) {
        this.base = new RecipeChoice.ExactChoice(AuroraAPI.getItemManager().resolveItem(base));
        return this;
    }

    public SmithingTransformRecipeBuilder addition(TypeId addition) {
        this.addition = new RecipeChoice.ExactChoice(AuroraAPI.getItemManager().resolveItem(addition));
        return this;
    }

    public SmithingTransformRecipeBuilder copyDataComponents(boolean copyDataComponents) {
        this.copyDataComponents = copyDataComponents;
        return this;
    }

    public SmithingTransformRecipe build() {
        return new SmithingTransformRecipe(key, result, template, base, addition, copyDataComponents);
    }
}

