package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.aurora.api.util.Version;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.inventory.RecipeChoice;

public class EmptyRecipeChoice {
    private static RecipeChoice empty = null;

    @SneakyThrows
    public static RecipeChoice get() {
        if (empty != null) {
            return empty;
        }

        if (Version.isAtLeastVersion(20, 6)) {
            var emptyMethod = RecipeChoice.class.getDeclaredMethod("empty");
            empty = (RecipeChoice) emptyMethod.invoke(null);
            return empty;
        } else {
            empty = new RecipeChoice.MaterialChoice(Material.AIR);
            return empty;
        }
    }
}
