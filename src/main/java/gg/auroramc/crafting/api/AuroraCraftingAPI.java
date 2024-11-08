package gg.auroramc.crafting.api;

import gg.auroramc.crafting.AuroraCrafting;

public class AuroraCraftingAPI {
    public static RecipeManager getRecipeManager() {
        return AuroraCrafting.getInstance().getRecipeManager();
    }
}
