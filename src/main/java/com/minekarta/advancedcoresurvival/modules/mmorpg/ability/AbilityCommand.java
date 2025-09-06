package com.minekarta.advancedcoresurvival.modules.mmorpg.ability;

import com.minekarta.advancedcoresurvival.modules.mmorpg.ClassManager;
import com.minekarta.advancedcoresurvival.modules.mmorpg.PlayerClass;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AbilityCommand implements CommandExecutor {

    private final PlayerStatsManager statsManager;
    private final ClassManager classManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;

    public AbilityCommand(PlayerStatsManager statsManager, ClassManager classManager, AbilityManager abilityManager, CooldownManager cooldownManager) {
        this.statsManager = statsManager;
        this.classManager = classManager;
        this.abilityManager = abilityManager;
        this.cooldownManager = cooldownManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /ability <ability_name>").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        String abilityId = args[0].toLowerCase();

        PlayerStats stats = statsManager.getPlayerStats(player);
        String playerClassId = stats.getPlayerClass();
        if (playerClassId == null) {
            player.sendMessage(Component.text("You must choose a class first!").color(NamedTextColor.RED));
            return true;
        }

        PlayerClass pc = classManager.getClass(playerClassId);
        if (pc == null || !pc.getAbilities().contains(abilityId)) {
            player.sendMessage(Component.text("Your class cannot use this ability.").color(NamedTextColor.RED));
            return true;
        }

        Ability ability = abilityManager.getAbility(abilityId);
        if (ability == null) {
            player.sendMessage(Component.text("Unknown ability.").color(NamedTextColor.RED));
            return true;
        }

        if (cooldownManager.isOnCooldown(player.getUniqueId(), abilityId)) {
            player.sendMessage(Component.text("This ability is on cooldown for " + cooldownManager.getRemainingCooldown(player.getUniqueId(), abilityId) + " more seconds.").color(NamedTextColor.YELLOW));
            return true;
        }

        if (stats.getMana() < ability.getManaCost()) {
            player.sendMessage(Component.text("You don't have enough mana.").color(NamedTextColor.RED));
            return true;
        }

        // All checks passed, execute ability
        stats.setMana(stats.getMana() - ability.getManaCost());
        cooldownManager.setCooldown(player.getUniqueId(), abilityId, ability.getCooldown());

        // Placeholder for ability effect
        player.sendMessage(Component.text("You used " + ability.getName() + "!").color(NamedTextColor.GREEN));

        return true;
    }
}
