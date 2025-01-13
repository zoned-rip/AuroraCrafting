package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class RecipesConfig extends AuroraConfig {
    @IgnoreField
    private String fileName;

    private List<RecipeConfig> recipes = new ArrayList<>();

    @Getter
    public static final class RecipeConfig {
        private String id;
        private String permission;
        private String workbench = "default";
        private Boolean shapeless = false;
        private String result;
        private List<String> ingredients;
        private List<String> lockedLore = new ArrayList<>();

        @Setter
        @IgnoreField
        private String sourceFile;
    }

    public RecipesConfig(File file) {
        super(file);
        this.fileName = file.getName().replace(".yml", "");
    }

    @Override
    public void load() {
        super.load();
        recipes.forEach(recipe -> {
            recipe.setSourceFile(fileName);
            var matrixSize = AuroraCrafting.getInstance().getConfigManager().getWorkbenchConfig().get(recipe.getWorkbench()).getMatrixSlots().size();
            if (!recipe.getShapeless() && recipe.getIngredients().size() < matrixSize) {
                for (int i = recipe.getIngredients().size(); i < matrixSize; i++) {
                    recipe.getIngredients().add("");
                }
            }
        });
    }
}
