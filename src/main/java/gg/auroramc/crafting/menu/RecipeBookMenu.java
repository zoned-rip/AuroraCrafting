package gg.auroramc.crafting.menu;

import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class RecipeBookMenu {
    private final AuroraCrafting plugin;
    private final Player player;

    public static RecipeBookMenu recipeBookMenu(AuroraCrafting plugin, Player player) {
        return new RecipeBookMenu(plugin, player);
    }

    public void open() {
        var mc = plugin.getConfigManager().getRecipeBookMenuConfig();
        var categories = plugin.getConfigManager().getRecipeBookConfig().getCategories();

        var menu = new AuroraMenu(player, mc.getTitle(), mc.getRows() * 9, false);

        menu.addFiller(ItemBuilder.of(mc.getFiller()).toItemStack(player));

        for (var category : categories) {
            menu.addItem(ItemBuilder.of(category.getMenu().getItem())
                            .loreCompute(() -> {
                                var lore = category.getMenu().getItem().getLore();
                                lore.addAll(mc.getAppendLore());
                                return lore.stream().map(l -> Text.component(player, l)).toList();
                            })
                            .build(player),
                    (e) -> {
                        RecipeCategoryMenu.recipeCategoryMenu(plugin, player, category).open();
                    });
        }

        for (var item : mc.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(item).build(player));
        }

        menu.open();
    }
}
