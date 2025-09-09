package com.minekarta.advancedcoresurvival.modules.bank.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.locale.LocaleManager;
import com.minekarta.advancedcoresurvival.modules.bank.gui.BankGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BankCommand implements CommandExecutor, TabCompleter {

    private final AdvancedCoreSurvival plugin;
    private final BankGUI bankGUI;

    public BankCommand(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
        this.bankGUI = new BankGUI(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.must-be-player"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            bankGUI.open(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "gui":
                bankGUI.open(player);
                break;
            case "balance":
                // TODO: Implement balance check
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.not-implemented"));
                break;
            case "deposit":
                // TODO: Implement deposit logic
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.not-implemented"));
                break;
            case "withdraw":
                // TODO: Implement withdraw logic
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.not-implemented"));
                break;
            case "create":
                // TODO: Implement bank creation logic
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.not-implemented"));
                break;
            case "delete":
                // TODO: Implement bank deletion logic
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.not-implemented"));
                break;
            case "invite":
                // TODO: Implement member invitation logic
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.not-implemented"));
                break;
            case "kick":
                // TODO: Implement member kick logic
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.not-implemented"));
                break;
            case "members":
                // TODO: Implement list members logic
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.not-implemented"));
                break;
            default:
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.invalid-usage", "%usage%", "/bank <subcommand>"));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("gui", "balance", "deposit", "withdraw", "create", "delete", "invite", "kick", "members");
            return subcommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        // TODO: Add more specific tab completion for subcommands (e.g., player names for /bank invite)
        return new ArrayList<>();
    }
}
