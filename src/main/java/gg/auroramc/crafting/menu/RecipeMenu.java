package gg.auroramc.crafting.menu;

import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.AuroraRecipe;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class RecipeMenu {
    private final AuroraCrafting plugin;
    private final Player player;
    private final AuroraRecipe recipe;
    private final boolean hasBackButton;

    public static RecipeMenu recipeMenu(AuroraCrafting plugin, Player player, AuroraRecipe recipe, boolean hasBackButton) {
        return new RecipeMenu(plugin, player, recipe, hasBackButton);
    }

    public void open() {
        var workbenchConfig = plugin.getConfigManager().getWorkbenchConfig();
        var mc = plugin.getConfigManager().getRecipeViewConfig();

        var menu = new AuroraMenu(player, mc.getTitle(), workbenchConfig.getRows() * 9, false);
        menu.addFiller(ItemBuilder.of(workbenchConfig.getFiller()).toItemStack(player));

        var ingredientItems = recipe.getIngredientItems();

        for (int i = 0; i < workbenchConfig.getMatrixSlots().size(); i++) {
            var slot = workbenchConfig.getMatrixSlots().get(i);
            var item = i < ingredientItems.size() ? ingredientItems.get(i) : ItemStack.empty();
            menu.addItem(ItemBuilder.item(item).slot(slot).build(player));
        }

        menu.addItem(ItemBuilder.item(recipe.getResultItem()).slot(mc.getResultSlot()).build(player));

        if (hasBackButton && recipe.getCategory() != null) {
            menu.addItem(ItemBuilder.of(mc.getItems().get("back")).build(player), (e) -> {
                RecipeCategoryMenu.recipeCategoryMenu(plugin, player, recipe.getCategory()).open();
            });
        }

        for (var item : mc.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(item).build(player));
        }

        menu.open();
    }
}
