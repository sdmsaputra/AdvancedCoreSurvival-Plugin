package com.minekarta.advancedcoresurvival.modules.mmorpg;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.modules.Module;
import com.minekarta.advancedcoresurvival.modules.mmorpg.ability.AbilityCommand;
import com.minekarta.advancedcoresurvival.modules.mmorpg.ability.AbilityManager;
import com.minekarta.advancedcoresurvival.modules.mmorpg.ability.CooldownManager;
import com.minekarta.advancedcoresurvival.modules.mmorpg.commands.ClassCommand;
import com.minekarta.advancedcoresurvival.modules.rpg.RPGModule;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import com.minekarta.advancedcoresurvival.modules.mmorpg.mana.ManaManager;

public class MMORPGModule implements Module {

    private ClassManager classManager;
    private ManaManager manaManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;

    @Override
    public String getName() {
        return "mmorpg";
    }

    @Override
    public void onEnable(AdvancedCoreSurvival plugin) {
        plugin.getLogger().info("Initializing MMORPG Module...");

        // Initialize Managers
        this.classManager = new ClassManager(plugin);
        this.abilityManager = new AbilityManager(plugin);
        this.cooldownManager = new CooldownManager();
        classManager.loadClasses();
        abilityManager.loadAbilities();

        // Get the PlayerStatsManager from the RPGModule
        RPGModule rpgModule = (RPGModule) plugin.getModuleManager().getModule("rpg")
                .orElseThrow(() -> new IllegalStateException("RPGModule must be enabled for MMORPGModule to work."));
        PlayerStatsManager statsManager = rpgModule.getStatsManager();

        // Initialize and start the Mana Manager
        this.manaManager = new ManaManager(plugin, statsManager);
        manaManager.start();

        // Register commands
        plugin.getCommand("class").setExecutor(new ClassCommand(plugin, statsManager, classManager));
        plugin.getCommand("ability").setExecutor(new AbilityCommand(statsManager, classManager, abilityManager, cooldownManager));
    }

    @Override
    public void onDisable() {
        if (manaManager != null) {
            manaManager.stop();
        }
    }
}
