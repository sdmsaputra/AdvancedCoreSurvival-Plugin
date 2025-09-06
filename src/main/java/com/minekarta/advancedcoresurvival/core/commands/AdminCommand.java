package com.minekarta.advancedcoresurvival.core.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.modules.ModuleManager;
import com.minekarta.advancedcoresurvival.modules.claims.ClaimsModule;
import com.minekarta.advancedcoresurvival.modules.claims.tax.ClaimTaxManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class AdminCommand implements CommandExecutor {

    private final AdvancedCoreSurvival plugin;

    public AdminCommand(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("AdvancedCoreSurvival Admin").color(NamedTextColor.GOLD));
            sender.sendMessage(Component.text("/acs runtax - Manually run the claim tax collection.").color(NamedTextColor.GRAY));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        if (subCommand.equals("runtax")) {
            handleRunTax(sender);
            return true;
        }

        return true;
    }

    private void handleRunTax(CommandSender sender) {
        if (!sender.hasPermission("advancedcoresurvival.admin")) {
            sender.sendMessage(Component.text("You don't have permission to do that.").color(NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("Manually starting claim tax collection...").color(NamedTextColor.YELLOW));

        // This is tricky because the tax manager is private inside ClaimsModule.
        // A proper solution would use a getter or a central registry.
        // For now, we will use reflection as a workaround.
        try {
            ModuleManager moduleManager = plugin.getModuleManager();
            ClaimsModule claimsModule = (ClaimsModule) moduleManager.getModule("claims").orElse(null);
            if (claimsModule == null) {
                sender.sendMessage(Component.text("Claims module is not enabled.").color(NamedTextColor.RED));
                return;
            }

            Field taxManagerField = ClaimsModule.class.getDeclaredField("taxManager");
            taxManagerField.setAccessible(true);
            ClaimTaxManager taxManager = (ClaimTaxManager) taxManagerField.get(claimsModule);

            // We need to call the private collectTaxes method, also with reflection.
            java.lang.reflect.Method collectTaxesMethod = ClaimTaxManager.class.getDeclaredMethod("collectTaxes");
            collectTaxesMethod.setAccessible(true);
            collectTaxesMethod.invoke(taxManager);

            sender.sendMessage(Component.text("Tax collection process started asynchronously. Check console for details.").color(NamedTextColor.GREEN));

        } catch (Exception e) {
            sender.sendMessage(Component.text("An error occurred while running the tax collector. See console.").color(NamedTextColor.RED));
            e.printStackTrace();
        }
    }
}
