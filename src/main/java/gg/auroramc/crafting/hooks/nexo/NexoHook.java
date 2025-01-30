package gg.auroramc.crafting.hooks.nexo;

import com.nexomc.nexo.api.events.resourcepack.NexoPostPackGenerateEvent;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.hooks.Hook;
import gg.auroramc.crafting.util.RecipeRegistrar;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class NexoHook implements Hook, Listener {
    private AuroraCrafting plugin;

    @Override
    public void hook(AuroraCrafting plugin) {
        this.plugin = plugin;
        AuroraCrafting.logger().info("Hooked into Nexo to re-register vanilla recipes after resource pack generation");
    }

    @EventHandler
    public void onPostResourcePackGeneration(NexoPostPackGenerateEvent event) {
        AuroraCrafting.logger().info("Reregistering vanilla recipes after Nexo resource pack generation");
        var count = RecipeRegistrar.reRegisterCurrenRecipes();
        AuroraCrafting.logger().info("Registered " + count + " vanilla recipes");

        if (count > 0 && plugin.getConfigManager().getConfig().getAutoDiscoverVanillaRecipes()) {
            for (var player : Bukkit.getOnlinePlayers()) {
                RecipeRegistrar.discoverRecipes(player);
            }
            AuroraCrafting.logger().info("Discovered vanilla recipes for all online players");
        }
    }
}
