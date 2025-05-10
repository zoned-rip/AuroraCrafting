package gg.auroramc.crafting.api;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public class ItemStackPair {
    private static final ItemStackPair EMPTY = new ItemStackPair(null, 0);

    private final ItemStack item;
    private int amount;

    public ItemStackPair add(int amount) {
        this.amount += amount;
        return this;
    }

    public static ItemStackPair empty() {
        return EMPTY;
    }
}
