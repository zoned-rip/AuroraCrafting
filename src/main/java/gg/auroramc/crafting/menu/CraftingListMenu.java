package gg.auroramc.crafting.menu;

import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BlueprintContext;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.api.workbench.custom.CustomWorkbench;
import gg.auroramc.crafting.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CraftingListMenu implements InventoryHolder {
    private final AuroraCrafting plugin;
    private final Player player;
    private final Inventory inventory;
    private final CustomWorkbench workbench;
    private final Map<Integer, Blueprint> slotBlueprints = new HashMap<>();

    private static final int[] BORDER_SLOTS = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
    private static final int[] INNER_SLOTS = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
    private static final int RETURN_SLOT = 49;

    public static CraftingListMenu open(AuroraCrafting plugin, Player player, CustomWorkbench workbench) {
        return new CraftingListMenu(plugin, player, workbench);
    }

    public CraftingListMenu(AuroraCrafting plugin, Player player, CustomWorkbench workbench) {
        this.plugin = plugin;
        this.player = player;
        this.workbench = workbench;

        this.inventory = Bukkit.createInventory(this, 54, Text.component("<dark_gray>Quick Crafting"));

        fillBorder();
        setUpRecipeList();
        setReturnButton();

        open();
    }

    private void fillBorder() {
        var pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        var meta = pane.getItemMeta();
        meta.displayName(Text.component(" "));
        pane.setItemMeta(meta);

        for (int slot : BORDER_SLOTS) inventory.setItem(slot, pane);
    }

    public void setUpRecipeList() {
        slotBlueprints.clear();

        // Clear all inner slots first
        for (int slot : INNER_SLOTS) inventory.setItem(slot, null);

        var recipes = workbench.getCraftableBlueprints(player, INNER_SLOTS.length, false, BlueprintType.SHAPED, BlueprintType.SHAPELESS);

        for (int i = 0; i < INNER_SLOTS.length; i++) {
            if (i < recipes.size()) {
                var recipe = recipes.get(i);
                inventory.setItem(INNER_SLOTS[i], recipe.getResultItem());
                slotBlueprints.put(INNER_SLOTS[i], recipe);
            }
        }
    }

    private void setReturnButton() {
        var arrow = new ItemStack(Material.ARROW);
        var meta = arrow.getItemMeta();
        meta.displayName(Text.component("<yellow>Return To Crafting Menu"));
        arrow.setItemMeta(meta);
        inventory.setItem(RETURN_SLOT, arrow);
    }

    private boolean isDumbAssClick(InventoryClickEvent event) {
        return event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY
                && event.getAction() != InventoryAction.PICKUP_ALL
                && event.getAction() != InventoryAction.PLACE_SOME
                && event.getAction() != InventoryAction.PLACE_ALL
                && event.getAction() != InventoryAction.PLACE_ONE;
    }

    private BlueprintContext context() {
        return new BlueprintContext(player, new ItemStack[0]);
    }

    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == inventory) {
            event.setCancelled(true);
        }

        // Otherwise we don't care what the players do in their inventory
        if (event.getClickedInventory() != inventory) {
            // Should cancel DROP actions though to make quick crafting safe
            if (event.getAction().name().startsWith("DROP")) {
                event.setCancelled(true);
            }
        }

        if (event.getSlot() == RETURN_SLOT) {
            player.getScheduler().run(plugin, (t) -> CraftMenu.craftMenu(plugin, player, workbench).open(), null);
            return;
        }

        var blueprint = slotBlueprints.get(event.getSlot());
        if (blueprint == null) return;

        handleQuickCraftSlot(event, blueprint);


    }

    private void handleQuickCraftSlot(InventoryClickEvent event, Blueprint blueprint) {
        if (isDumbAssClick(event)) return;
        if (event.getClick() == ClickType.RIGHT) return;

        var timesCraftable = blueprint.getQuickCraftTimes(InventoryUtils.buildItemCounts(player));
        if (timesCraftable == 0) return;

        final var currentItem = event.getCurrentItem() != null ? event.getCurrentItem().clone() : ItemStack.empty();

        if (event.isShiftClick()) {
            int currentSpace = InventoryUtils.calculateSpaceForItem(player.getInventory(), event.getCurrentItem());
            if (currentSpace < blueprint.getResult().amount()) return;

            final int availableSpace = currentSpace - blueprint.getResult().amount();
            final int timesCrafted = Math.min((availableSpace / blueprint.getResult().amount()) + 1, timesCraftable);

            if (timesCrafted == 1) {
                player.getScheduler().run(plugin, (t) -> {
                    blueprint.quickCraft(context(), 1, true);
                    setUpRecipeList();
                    player.updateInventory();
                    plugin.callCraftEvent(player, currentItem, blueprint.getResult().amount(), blueprint);
                }, null);
                return;
            }

            player.getScheduler().run(plugin, (t) -> {
                blueprint.quickCraft(context(), timesCrafted, true);
                setUpRecipeList();
                player.updateInventory();
                plugin.callCraftEvent(player, currentItem, timesCrafted * blueprint.getResult().amount(), blueprint);
            }, null);
        } else {
            if (event.getCursor().isEmpty()) {
                player.getScheduler().run(plugin, (t) -> {
                    blueprint.quickCraft(context(), 1, true);
                    player.setItemOnCursor(currentItem);
                    setUpRecipeList();
                    player.updateInventory();
                    plugin.callCraftEvent(player, currentItem, blueprint.getResult().amount(), blueprint);
                }, null);
            } else {
                var cursor = event.getCursor();
                if (cursor.isSimilar(currentItem)) {
                    var maxAmount = cursor.getMaxStackSize() - cursor.getAmount();
                    if (blueprint.getResult().amount() <= maxAmount) {
                        player.getScheduler().run(plugin, (t) -> {
                            if (player.getItemOnCursor().isSimilar(currentItem)) {
                                player.getItemOnCursor().setAmount(cursor.getAmount() + blueprint.getResult().amount());
                            }
                            blueprint.quickCraft(context(), 1, true);
                            setUpRecipeList();
                            player.updateInventory();
                            plugin.callCraftEvent(player, currentItem, blueprint.getResult().amount(), blueprint);
                        }, null);
                    }
                }
            }
        }
    }

    public void open() {
        player.openInventory(inventory);
    }


    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}