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
public class CookingRecipesConfig extends AuroraConfig {
    @IgnoreField
    private String fileName;

    private List<RecipeConfig> recipes = new ArrayList<>();

    @Getter
    public static final class RecipeConfig {
        private String id;
        private String result;
        private String input;
        private Float experience = 0.0F;
        private Integer cookingTime = 200;
        private String category = "MISC";
        private String group;

        @Setter
        @IgnoreField
        private String sourceFile;
    }

    public CookingRecipesConfig(File file) {
        super(file);
        this.fileName = file.getName().replace(".yml", "");
    }

    @Override
    public void load() {
        super.load();
        recipes.forEach(recipe -> recipe.setSourceFile(fileName));

        var it = recipes.iterator();

        while (it.hasNext()) {
            var recipe = it.next();

            if (recipe.id == null) {
                it.remove();
                AuroraCrafting.logger().severe("Cooking recipe in " + fileName + " has no id, removing...");
            } else if (recipe.result == null) {
                it.remove();
                AuroraCrafting.logger().severe("Cooking recipe in " + fileName + " with id " + recipe.id + " has no result, removing...");
            } else if (recipe.input == null) {
                it.remove();
                AuroraCrafting.logger().severe("Cooking recipe in " + fileName + " with id " + recipe.id + " has no input, removing...");
            }
        }
    }
}
