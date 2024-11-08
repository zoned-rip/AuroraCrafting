package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Getter
public class MessageConfig extends AuroraConfig {

    private String reloaded = "&aReloaded configuration!";
    private String dataNotLoadedYet = "&cData for this player hasn't loaded yet, try again later!";
    private String dataNotLoadedYetSelf = "&cYour data isn't loaded yet, please try again later!";
    private String playerOnlyCommand = "&cThis command can only be executed by a player!";
    private String noPermission = "&cYou don't have permission to execute this command!";
    private String invalidSyntax = "&cInvalid command syntax!";
    private String mustBeNumber = "&cArgument must be a number!";
    private String playerNotFound = "&cPlayer not found!";
    private String commandError = "&cAn error occurred while executing this command!";
    private String merchantOpened = "&aOpened merchant: &2{id} &afor player: &2{player}&a!";
    private String merchantFailedToOpen = "&cFailed to open merchant: &4{id} &cfor player: &4{player}&c!";

    public MessageConfig(AuroraCrafting plugin, String language) {
        super(getFile(plugin, language));
    }

    private static File getFile(AuroraCrafting plugin, String language) {
        return new File(plugin.getDataFolder(), "messages_" + language + ".yml");
    }

    public static void saveDefault(AuroraCrafting plugin, String language) {
        if (!getFile(plugin, language).exists()) {
            try {
                plugin.saveResource("messages_" + language + ".yml", false);
            } catch (Exception e) {
                AuroraCrafting.logger().warning("Internal message file for language: " + language + " not found! Creating a new one from english...");

                var file = getFile(plugin, language);


                try (InputStream in = plugin.getResource("messages_en.yml")) {
                    Files.copy(in, file.toPath());
                } catch (IOException ex) {
                    AuroraCrafting.logger().severe("Failed to create message file for language: " + language);
                    ex.printStackTrace();
                }
            }
        }
    }
}
