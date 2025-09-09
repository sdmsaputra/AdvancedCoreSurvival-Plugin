package com.minekarta.advancedcoresurvival.modules.bank;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.modules.Module;
import com.minekarta.advancedcoresurvival.core.storage.Storage;
import com.minekarta.advancedcoresurvival.modules.bank.commands.BankCommand;
import com.minekarta.advancedcoresurvival.modules.bank.gui.BankGUI;
import com.minekarta.advancedcoresurvival.modules.bank.gui.BankGUIListener;
import org.bukkit.Bukkit;

public class BankModule implements Module {

    private BankGUI bankGUI;

    @Override
    public String getName() {
        return "bank";
    }

    @Override
    public void onEnable(AdvancedCoreSurvival plugin) {
        plugin.getLogger().info("Bank module has been enabled.");

        Storage storage = plugin.getStorageManager().getStorage();
        this.bankGUI = new BankGUI(plugin, storage);

        BankCommand bankCommand = new BankCommand(plugin, storage, this);
        plugin.getCommand("bank").setExecutor(bankCommand);
        plugin.getCommand("bank").setTabCompleter(bankCommand);

        Bukkit.getPluginManager().registerEvents(new BankGUIListener(plugin, storage, this), plugin);
    }

    @Override
    public void onDisable() {
        // No-op
    }

    public BankGUI getBankGUI() {
        return bankGUI;
    }
}
