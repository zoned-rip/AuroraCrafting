package gg.auroramc.crafting.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.auroramc.crafting.AuroraCrafting;
import org.bukkit.entity.Player;

@CommandAlias("%recipesAlias")
public class RecipesCommand extends BaseCommand {
    private final AuroraCrafting plugin;

    public RecipesCommand(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @Default
    @Description("Opens the recipes menu")
    @CommandPermission("aurora.crafting.recipes")
    @CommandCompletion("@recipes @nothing")
    public void onMenu(Player player, @Optional String recipe) {
        // TODO: open the main menu
    }
}
