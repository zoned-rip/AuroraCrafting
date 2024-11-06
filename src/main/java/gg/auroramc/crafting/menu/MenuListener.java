package gg.auroramc.crafting.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class MenuListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        var holder = event.getInventory().getHolder();
        if (holder instanceof CraftMenu menu) {
            menu.onClick(event);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        var holder = event.getInventory().getHolder();
        if (holder instanceof CraftMenu menu) {
            menu.onDrag(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        var holder = event.getInventory().getHolder();
        if (holder instanceof CraftMenu menu) {
            menu.onClose(event);
        }
    }
}
