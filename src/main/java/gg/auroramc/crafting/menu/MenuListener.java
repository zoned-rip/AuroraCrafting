package gg.auroramc.crafting.menu;

import gg.auroramc.crafting.AuroraCrafting;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class MenuListener implements Listener {
    private final AuroraCrafting plugin;
    private final Map<UUID, Long> cache = new HashMap<>();
    private final Map<UUID, Long> shiftCache = new HashMap<>();

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        var holder = event.getInventory().getHolder();
        if (holder instanceof CraftMenu menu) {
            if (isOnCooldown(event.getWhoClicked().getUniqueId())) {
                return;
            }

            if (isOnShiftCooldown(event.getWhoClicked().getUniqueId())) {
                return;
            }

            menu.onClick(event);

            if (event.getClickedInventory() != menu.getInventory()) return;
            if (menu.getMatrixLookup().contains(event.getSlot())) {
                return;
            }

            if (event.isShiftClick()) {
                shiftCache.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
            } else {
                cache.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    private boolean isOnCooldown(UUID uuid) {
        var time = cache.get(uuid);
        if (time == null) return false;
        return System.currentTimeMillis() - time < plugin.getConfigManager().getConfig().getClickCooldown();
    }

    public boolean isOnShiftCooldown(UUID uuid) {
        var time = shiftCache.get(uuid);
        if (time == null) return false;
        return System.currentTimeMillis() - time < plugin.getConfigManager().getConfig().getShiftClickCooldown();
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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cache.remove(event.getPlayer().getUniqueId());
        shiftCache.remove(event.getPlayer().getUniqueId());
    }
}
