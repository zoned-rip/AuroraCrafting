package gg.auroramc.crafting.menu;

import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.AuroraRecipe;
import gg.auroramc.crafting.config.RecipeBookConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RecipeCategoryMenu {
    private final AuroraCrafting plugin;
    private final Player player;
    private final RecipeBookConfig.RecipeCategory category;
    private int page = 0;

    public RecipeCategoryMenu(AuroraCrafting plugin, Player player, RecipeBookConfig.RecipeCategory category) {
        this.plugin = plugin;
        this.player = player;
        this.category = category;
    }

    public static RecipeCategoryMenu recipeCategoryMenu(AuroraCrafting plugin, Player player, RecipeBookConfig.RecipeCategory category) {
        return new RecipeCategoryMenu(plugin, player, category);
    }

    public void open() {
        var mc = plugin.getConfigManager().getRecipeBookCategoryConfig();

        var menu = new AuroraMenu(player, category.getMenu().getTitle(), mc.getRows() * 9, false);
        menu.addFiller(ItemBuilder.of(mc.getFiller()).toItemStack(player));

        for (var item : mc.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(item).build(player));
        }

        if (mc.getCategoryIcon().getEnabled()) {
            var itemConfig = category.getMenu().getItem().merge(mc.getCategoryIcon().getItem());
            menu.addItem(ItemBuilder.of(itemConfig).defaultSlot(4).build(player));
        }

        menu.addItem(ItemBuilder.of(mc.getItems().get("back")).build(player), (e) -> {
            RecipeBookMenu.recipeBookMenu(plugin, player).open();
        });

        var recipes = getPageRecipes(mc.getDisplayArea().size());

        for (int i = 0; i < mc.getDisplayArea().size(); i++) {
            var slot = mc.getDisplayArea().get(i);
            if (i < recipes.size()) {
                var recipe = recipes.get(i);
                var item = recipe.getResultItem();
                if (recipe.hasPermission(player) || !mc.getSecretRecipeDisplay().getEnabled()) {
                    menu.addItem(ItemBuilder.item(item).slot(slot).loreCompute(() -> {
                        var lore = new ArrayList<Component>();
                        if (item.hasItemMeta()) {
                            var meta = item.getItemMeta();
                            if (meta.hasLore()) {
                                lore.addAll(meta.lore());
                            }
                        }
                        lore.addAll(mc.getAppendLore().stream().map(l -> Text.component(player, l)).toList());
                        return lore;
                    }).build(player), (e) -> {
                        RecipeMenu.recipeMenu(plugin, player, recipe, true).open();
                    });
                } else {
                    menu.addItem(ItemBuilder.of(mc.getSecretRecipeDisplay().getItem()).slot(slot).loreCompute(() -> {
                        var lore = new ArrayList<>(mc.getSecretRecipeDisplay().getItem().getLore());
                        lore.addAll(recipe.getLockedLore());
                        return lore.stream().map(l -> Text.component(player, l)).toList();
                    }).build(player));
                }
            } else {
                menu.addItem(ItemBuilder.item(ItemBuilder.filler(Material.AIR)).slot(slot).build(player));
            }
        }

        var recipesInCategory = plugin.getRecipeManager().getRecipesByCategory(category.getId());

        if (recipesInCategory.size() > mc.getDisplayArea().size()) {
            var maxPage = (int) Math.ceil(recipesInCategory.size() / (double) mc.getDisplayArea().size());

            List<Placeholder<?>> placeholders = List.of(
                    Placeholder.of("{current}", page + 1),
                    Placeholder.of("{max}", maxPage)
            );

            menu.addItem(ItemBuilder.of(mc.getItems().get("previous_page")).build(player), (e) -> {
                if (page > 0) {
                    page--;
                    open();
                }
            });

            menu.addItem(ItemBuilder.of(mc.getItems().get("current_page"))
                    .placeholder(placeholders).build(player));

            menu.addItem(ItemBuilder.of(mc.getItems().get("next_page")).build(player), (e) -> {
                if (page < maxPage) {
                    page++;
                    open();
                }
            });
        }

        menu.open();
    }

    public List<AuroraRecipe> getPageRecipes(int pageSize) {
        var recipes = plugin.getRecipeManager().getRecipesByCategory(category.getId());
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, recipes.size());
        return recipes.subList(startIndex, endIndex);
    }
}
