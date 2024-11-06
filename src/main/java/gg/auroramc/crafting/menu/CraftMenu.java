package gg.auroramc.crafting.menu;

import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.AuroraRecipe;
import gg.auroramc.crafting.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CraftMenu implements InventoryHolder {
    private final AuroraCrafting plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<Integer> matrixSlots;
    private final Set<Integer> matrixLookup;
    private final int resultSlot;
    private final ItemStack invalidResultItem;
    private final ItemStack fillerItem;

    public static CraftMenu craftMenu(AuroraCrafting plugin, Player player) {
        return new CraftMenu(plugin, player);
    }

    public CraftMenu(AuroraCrafting plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        var config = plugin.getConfigManager().getWorkbenchConfig();
        this.matrixSlots = config.getMatrixSlots();
        this.matrixLookup = Set.copyOf(matrixSlots);
        this.resultSlot = config.getResultSlot();

        this.inventory = Bukkit.createInventory(this, config.getRows() * 9, Text.component(config.getTitle()));
        this.invalidResultItem = ItemBuilder.of(config.getInvalidResultItem()).toItemStack(player);
        this.fillerItem = ItemBuilder.of(config.getFiller()).toItemStack(player);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (!matrixLookup.contains(i)) {
                inventory.setItem(i, fillerItem);
            }
        }
        inventory.setItem(resultSlot, invalidResultItem);
    }

    public void open() {
        player.openInventory(inventory);
    }

    private boolean isCustomSlotClick(InventoryClickEvent event) {
        return event.getClickedInventory() == inventory
                && !matrixLookup.contains(event.getSlot()) && event.getSlot() != resultSlot;
    }

    private boolean isUpdateRequired(InventoryClickEvent event) {
        return matrixLookup.contains(event.getSlot()) || (event.getClickedInventory() != inventory && event.isShiftClick());
    }

    private boolean isDumbAssClick(InventoryClickEvent event) {
        return event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY
                && event.getAction() != InventoryAction.PICKUP_ALL
                && event.getAction() != InventoryAction.PLACE_SOME
                && event.getAction() != InventoryAction.PLACE_ALL;
    }

    private boolean isIllegalShiftClick(InventoryClickEvent event) {
        return event.getClickedInventory() != inventory
                && event.isShiftClick()
                && event.getCurrentItem() != null
                && (event.getCurrentItem().isSimilar(inventory.getItem(resultSlot)) || event.getCurrentItem().isSimilar(fillerItem));
    }

    public void onClick(InventoryClickEvent event) {
        // Stop dumb shift clicks
        if (isIllegalShiftClick(event)) {
            event.setCancelled(true);
            return;
        }

        // Otherwise we don't care what the players do in their inventory
        if (event.getClickedInventory() != inventory) {
            return;
        }

        // Prevent interacting with the other parts of the custom gui
        if (isCustomSlotClick(event)) {
            event.setCancelled(true);
            return;
        }

        // Update the result if the matrix slots has changed or valid shift click happened
        // We don't need to cancel this
        if (isUpdateRequired(event)) {
            player.getScheduler().run(plugin, (t) -> updateResult(), null);
            return;
        }

        // At this point, we checked everything. If the player didn't click on the result we don't care.
        if (event.getSlot() != resultSlot) return;

        // If the player clicked on the result slot but used some weird ass click action, cancel the event
        if (isDumbAssClick(event)) {
            event.setCancelled(true);
            return;
        }

        // Get the crafting matrix
        var matrix = getMatrix(event.getInventory(), matrixSlots);

        // If we don't have a recipe cancel the event
        var recipe = plugin.getRecipeManager().getRecipe(matrix);
        if (recipe == null || !recipe.hasPermission(player)) {
            event.setCancelled(true);
            return;
        }

        if (event.getInventory().getItem(resultSlot) == invalidResultItem) {
            event.setCancelled(true);
        }

        // Based on the crafting matrix, let's see how many times can we craft the recipe
        var timesCraftable = recipe.getTimesCraftable(matrix);
        if (timesCraftable == 0) {
            event.setCancelled(true);
            return;
        }

        // Handle crafting when shift clicking
        if (event.isShiftClick()) {
            // Check the player inventory for space. If one crafting result fits, allow the shift click
            int currentSpace = InventoryUtils.calculateSpaceForItem(player.getInventory(), event.getCurrentItem());
            if (currentSpace < recipe.getResult().amount()) {
                event.setCancelled(true);
                return;
            }
            // Add the remaining items to the player inventory, but only the amount that fits, deduct the matrix
            final int availableSpace = currentSpace - recipe.getResult().amount();
            final int timesCrafted = Math.min((availableSpace / recipe.getResult().amount()) + 1, timesCraftable);

            // If only the shift click is craftable, just update the matrix and return
            if (timesCrafted == 1) {
                player.getScheduler().run(plugin, (t) -> updateMatrix(recipe, timesCraftable, 1, matrix), null);
                return;
            }

            // If there is more space, add the remaining items to the player inventory and update the matrix
            player.getScheduler().run(plugin, (t) -> {
                player.getInventory().addItem(
                        ItemUtils.createStacksFromAmount(
                                recipe.getResultItem(), (timesCrafted - 1) * recipe.getResult().amount()));
                updateMatrix(recipe, timesCraftable, timesCrafted, matrix);
            }, null);

            // Handle crafting for regular clicks
        } else {
            if (event.getCursor().isEmpty()) {
                // Allow taking the result and deduct the matrix
                player.getScheduler().run(plugin,
                        (t) -> updateMatrix(recipe, timesCraftable, 1, matrix), null);
            } else {
                var cursor = event.getCursor();
                var result = event.getCurrentItem();

                // Allow stacking the result and deduct the matrix
                if (cursor.isSimilar(result)) {
                    var maxAmount = cursor.getMaxStackSize() - cursor.getAmount();
                    if (recipe.getResult().amount() <= maxAmount) {
                        player.getScheduler().run(plugin, (t) -> {
                            if (player.getItemOnCursor().isSimilar(result)) {
                                player.getItemOnCursor().setAmount(cursor.getAmount() + recipe.getResult().amount());
                            }
                            updateMatrix(recipe, timesCraftable, 1, matrix);
                        }, null);
                    }
                }

                event.setCancelled(true);
            }
        }
    }

    public void onDrag(InventoryDragEvent event) {
        if (matrixLookup.stream().anyMatch(s -> event.getInventorySlots().contains(s))) {
            player.getScheduler().run(plugin, (t) -> updateResult(), null);
        }
    }

    public void onClose(InventoryCloseEvent event) {
        var items = new ArrayList<ItemStack>();

        for (var slot : matrixSlots) {
            var item = event.getInventory().getItem(slot);
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
    }

    private void updateMatrix(AuroraRecipe recipe, int timesCraftable, int timesCrafted, List<ItemStack> matrix) {
        var newMatrix = recipe.calcRemainingIngredientMatrix(timesCrafted, matrix);

        for (int i = 0; i < matrixSlots.size(); i++) {
            inventory.setItem(matrixSlots.get(i), newMatrix[i]);
        }
        if (timesCraftable > timesCrafted) {
            inventory.setItem(resultSlot, recipe.getResultItem());
        } else {
            inventory.setItem(resultSlot, invalidResultItem);
        }
        player.updateInventory();
    }

    private void updateResult() {
        inventory.setItem(resultSlot, invalidResultItem);
        // Calc new potential result
        var matrix = getMatrix(inventory, matrixSlots);

        var recipe = plugin.getRecipeManager().getRecipe(matrix);

        if (recipe != null && recipe.hasPermission(player)) {
            var timesCraftable = recipe.getTimesCraftable(matrix);
            if (timesCraftable > 0) {
                inventory.setItem(resultSlot, recipe.getResultItem());
            }
        } else {
            inventory.setItem(resultSlot, invalidResultItem);
        }
        player.updateInventory();
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

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
