package gg.auroramc.crafting.hooks;

import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Set;

@Getter
public enum Hooks {
    AURORA_QUESTS("gg.auroramc.crafting.hooks.auroraquests.AuroraQuestsHook", "AuroraQuests"),
    QUESTS("gg.auroramc.crafting.hooks.quests.QuestsHook", "Quests"),
    QUESTS_LMBISHOP("gg.auroramc.crafting.hooks.quests2.QuestsLmBishopHook", "Quests"),
    BETON_QUEST("gg.auroramc.crafting.hooks.betonquests.BetonQuestHook", "BetonQuest"),
    JOBS_REBORN("gg.auroramc.crafting.hooks.jobsreborn.JobsRebornHook", "Jobs"),
    ITEMS_ADDER("gg.auroramc.crafting.hooks.itemsadder.ItemsAdderHook", "ItemsAdder"),
    MYTHIC_MOBS("gg.auroramc.crafting.hooks.mythicmobs.MythicHook", "MythicMobs"),
    HEAD_DATABASE("gg.auroramc.crafting.hooks.hdb.HdbHook", "HeadDatabase"),
    ADVANCED_ENCHANTMENTS("gg.auroramc.crafting.hooks.advancedenchantments.AEHook", "AdvancedEnchantments"),
    ;

    private final String className;
    private final Set<String> plugins;

    Hooks(String className, String plugin) {
        this.className = className;
        this.plugins = Set.of(plugin);
    }

    Hooks(String className, Set<String> plugins) {
        this.className = className;
        this.plugins = plugins;
    }

    public Class<? extends Hook> resolveHookClass() {
        try {
            var clazz = Class.forName(className);
            if (!Hook.class.isAssignableFrom(clazz)) {
                return null;
            }
            @SuppressWarnings("unchecked")
            var hookClass = (Class<? extends Hook>) clazz;
            return hookClass;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public boolean canHook() {
        for (String plugin : plugins) {
            if (Bukkit.getPluginManager().getPlugin(plugin) != null) {
                if (AuroraCrafting.getInstance().getConfigManager().getConfig().getHooks().getOrDefault(plugin, true)) {
                    return true;
                }
            }
        }
        return false;
    }
}