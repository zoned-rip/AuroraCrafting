package gg.auroramc.crafting.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.crafting.AuroraCrafting;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("%craftingAlias")
public class CraftingCommand extends BaseCommand {
    private final AuroraCrafting plugin;

    public CraftingCommand(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @Default
    @Description("Opens the crafting menu")
    @CommandPermission("aurora.crafting.use")
    public void onMenu(Player player) {
        // TODO: open the crafting menu
    }

    @Subcommand("reload")
    @Description("Reloads crafting configurations")
    @CommandPermission("aurora.crafting.admin.reload")
    public void onReload(CommandSender sender) {
        plugin.reload();
        Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getReloaded());
    }
}