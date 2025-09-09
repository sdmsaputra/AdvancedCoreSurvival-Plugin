package com.minekarta.advancedcoresurvival.modules.bank.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.locale.LocaleManager;
import com.minekarta.advancedcoresurvival.core.storage.Storage;
import com.minekarta.advancedcoresurvival.modules.bank.BankModule;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BankCommand implements CommandExecutor, TabCompleter {

    private final AdvancedCoreSurvival plugin;
    private final Storage storage;
    private final BankModule bankModule;
    private final Economy economy;

    public BankCommand(AdvancedCoreSurvival plugin, Storage storage, BankModule bankModule) {
        this.plugin = plugin;
        this.storage = storage;
        this.bankModule = bankModule;
        this.economy = plugin.getEconomy();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.must-be-player"));
            return true;
        }

        if (economy == null) {
            player.sendMessage(LocaleManager.getInstance().getFormattedMessage("bank.no-economy"));
            return true;
        }

        if (args.length == 0) {
            bankModule.getBankGUI().open(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create" -> handleCreate(player, args);
            // All other commands would be implemented here. This is a sample.
            default -> player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.not-implemented"));
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(LocaleManager.getInstance().getFormattedMessage("bank.create-usage"));
            return;
        }
        final String bankName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        storage.hasBank(bankName).thenAcceptAsync(exists -> {
            if (exists) {
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("bank.creation-fail-exists"));
            } else {
                storage.createBank(bankName, player.getUniqueId()).thenAccept(created -> {
                    if (created) {
                        player.sendMessage(LocaleManager.getInstance().getFormattedMessage("bank.bank-created", "%bank_name%", bankName));
                    } else {
                        player.sendMessage(LocaleManager.getInstance().getFormattedMessage("bank.create-error"));
                    }
                });
            }
        }, plugin.getServer().getScheduler().getMainThreadExecutor(plugin));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("gui", "balance", "deposit", "withdraw", "create", "delete", "invite", "join", "kick", "members");
            return subcommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
