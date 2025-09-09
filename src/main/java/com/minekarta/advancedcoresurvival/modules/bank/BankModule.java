package com.minekarta.advancedcoresurvival.modules.bank;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.modules.Module;
import com.minekarta.advancedcoresurvival.modules.bank.commands.BankCommand;

public class BankModule implements Module {

    private AdvancedCoreSurvival plugin;

    @Override
    public String getName() {
        return "bank";
    }

    @Override
    public void onEnable(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("Bank module has been enabled.");

        BankCommand bankCommand = new BankCommand(plugin);
        this.plugin.getCommand("bank").setExecutor(bankCommand);
        this.plugin.getCommand("bank").setTabCompleter(bankCommand);
    }

    @Override
    public void onDisable() {
        plugin.getLogger().info("Bank module has been disabled.");
    }
}
