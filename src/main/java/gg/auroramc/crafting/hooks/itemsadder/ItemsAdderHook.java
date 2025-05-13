package gg.auroramc.crafting.hooks.itemsadder;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.hooks.Hook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderHook implements Hook, Listener {
    private AuroraCrafting plugin;

    @Override
    public void hookAtStartUp(AuroraCrafting plugin) {
        plugin.getItemLoader().addToWaitFor("ItemsAdder", 400);
    }

    @Override
    public void hook(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDataLoad(ItemsAdderLoadDataEvent event) {
        if (event.getCause() == ItemsAdderLoadDataEvent.Cause.FIRST_LOAD) {
            plugin.getItemLoader().setLoaded("ItemsAdder");
        }
    }
}
