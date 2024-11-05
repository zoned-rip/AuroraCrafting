package gg.auroramc.crafting.menu;

import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.crafting.AuroraCrafting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CraftingMenu {
    private final Player player;
    private final AuroraMenu menu;
    private final ItemStack invalidResultItem;
    private final int resultSlot;

    public CraftingMenu(AuroraCrafting plugin, Player player) {
        this.player = player;
        var config = plugin.getConfigManager().getWorkbenchConfig();
        this.menu = new AuroraMenu(player, config.getTitle(), config.getRows() * 9, false);

        this.invalidResultItem = ItemBuilder.of(plugin.getConfigManager().getWorkbenchConfig().getInvalidResultItem()).toItemStack(player);

        var matrixSlots = plugin.getConfigManager().getWorkbenchConfig().getMatrixSlots();
        this.resultSlot = plugin.getConfigManager().getWorkbenchConfig().getResultSlot();

        menu.addFiller(ItemBuilder.of(config.getFiller()).toItemStack(player));
        menu.managedSlots(List.of(resultSlot));
        menu.freeSlots(matrixSlots);

        menu.onClose((m, e) -> {
            player.getScheduler().run(plugin, (t) -> {
                var items = new ArrayList<ItemStack>();

                for (var slot : matrixSlots) {
                    var item = e.getInventory().getItem(slot);
                    if (item != null) {
                        items.add(item);
                    }
                }

                if (items.isEmpty()) {
                    return;
                }

                var failed = player.getInventory().addItem(items.toArray(new ItemStack[0]));

                if (!failed.isEmpty()) {
                    failed.forEach((s, i) -> player.getWorld().dropItem(player.getLocation(), i));
                }
            }, null);

        });

        // When the free slot crafting matrix updates we need to recalc the results
        menu.onBeforeFreeSlotsUpdate((inventory) -> {
            // Instant reset
            player.getScheduler().runDelayed(plugin, (t) -> {
                inventory.setItem(resultSlot, invalidResultItem);
                // Calc new potential result
                var matrix = getMatrix(menu.getInventory(), matrixSlots);

                var recipe = plugin.getRecipeManager().getRecipe(matrix);

                if (recipe != null) {
                    var timesCraftable = recipe.getTimesCraftable(matrix);
                    if (timesCraftable > 0) {
                        menu.getInventory().setItem(resultSlot, recipe.getResultItem());
                    }
                }

                player.updateInventory();
            }, null, 2);
        });

        menu.onManagedSlotClick((event, slot) -> {
            if (slot != resultSlot) return;
            AuroraCrafting.logger().debug("Result slot clicked");

            // We don't care about other events that nobody uses
            if (event.getAction() == InventoryAction.PICKUP_ALL) {
                // Take the ingredients from the crafting slots
                var matrix = getMatrix(event.getInventory(), matrixSlots);

                var recipe = plugin.getRecipeManager().getRecipe(matrix);
                if (recipe == null) return;

                var timesCrafted = recipe.getTimesCraftable(matrix);
                var canCraft =  timesCrafted > 0;
                if (!canCraft) return;

                var newMatrix = recipe.calcRemainingIngredientMatrix(1, matrix);

                player.getScheduler().run(plugin, (t) -> {
                    for (int i = 0; i < matrixSlots.size(); i++) {
                        menu.getInventory().setItem(matrixSlots.get(i), newMatrix[i]);
                    }
                    if(timesCrafted > 1) {
                        menu.getInventory().setItem(resultSlot, recipe.getResultItem());
                    } else {
                        menu.getInventory().setItem(resultSlot, invalidResultItem);
                    }
                    player.updateInventory();
                }, null);

                event.setCancelled(false);
            } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                // Calculate how many times the item can be crafted
                var matrix = getMatrix(event.getInventory(), matrixSlots);
                var recipe = plugin.getRecipeManager().getRecipe(matrix);
                var timesCrafted = recipe.getTimesCraftable(matrix);
                var newMatrix = recipe.calcRemainingIngredientMatrix(timesCrafted, matrix);

                // Remove the ingredients from the crafting slots
                player.getScheduler().run(plugin, (t) -> {
                    for (int i = 0; i < matrixSlots.size(); i++) {
                        menu.getInventory().setItem(matrixSlots.get(i), newMatrix[i]);
                    }
                    menu.getInventory().setItem(resultSlot, invalidResultItem);

                    var failed = player.getInventory().addItem(recipe.getTotalResult(timesCrafted));
                    player.getScheduler().run(plugin, (task) -> {
                        failed.forEach((s, i) -> player.getWorld().dropItem(player.getLocation(), i));
                    }, null);

                    player.updateInventory();
                }, null);
            }
        });
    }

    public static CraftingMenu craftingMenu(AuroraCrafting plugin, Player player) {
        return new CraftingMenu(plugin, player);
    }

    public void open() {
        menu.open(player, false, (m) -> {
            menu.getInventory().setItem(resultSlot, invalidResultItem);
            player.updateInventory();
        });
    }

    private List<ItemStack> getMatrix(Inventory inventory, List<Integer> matrixSlots) {
        var matrix = new ArrayList<ItemStack>();
        for (var slot : matrixSlots) {
            var item = inventory.getItem(slot);
            if (item != null) {
                matrix.add(item);
            } else {
                matrix.add(ItemStack.empty());
            }
        }
        return matrix;
    }
}
