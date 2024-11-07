package gg.auroramc.crafting.menu;

import gg.auroramc.crafting.AuroraCrafting;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class RecipeCategoryMenu {
    private final AuroraCrafting plugin;
    private final Player player;
    private final String categoryId;

    public static RecipeCategoryMenu recipeCategoryMenu(AuroraCrafting plugin, Player player, String categoryId) {
        return new RecipeCategoryMenu(plugin, player, categoryId);
    }

    public void open() {

    }
}
