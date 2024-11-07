package gg.auroramc.crafting.api;

import java.util.List;

public class RecipeFactory {
    public static AuroraRecipe createRecipe(String id, ItemPair result, boolean shapeless, String permission, List<String> lockedLore) {
        if (shapeless) {
            return new ShapelessAuroraRecipe(id, result, permission, lockedLore);
        } else {
            return new ShapedAuroraRecipe(id, result, permission, lockedLore);
        }
    }
}
