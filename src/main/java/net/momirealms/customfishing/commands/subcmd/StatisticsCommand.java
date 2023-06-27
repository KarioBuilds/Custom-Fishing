/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.fishing.loot.LootImpl;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class StatisticsCommand extends AbstractSubCommand {

    public static final StatisticsCommand INSTANCE = new StatisticsCommand();

    public StatisticsCommand() {
        super("statistics");
        regSubCommand(SetCommand.INSTANCE);
        regSubCommand(ResetCommand.INSTANCE);
    }

    public static class SetCommand extends AbstractSubCommand {

        public static final SetCommand INSTANCE = new SetCommand();

        public SetCommand() {
            super("set");
        }

        @Override
        public boolean onCommand(CommandSender sender, List<String> args) {
            if (!ConfigManager.enableStatistics
                    || super.lackArgs(sender, 3, args.size())
                    || super.playerNotOnline(sender, args.get(0))
            ) return true;
            int amount = Integer.parseInt(args.get(2));
            if (amount < 0) {
                AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.negativeStatistics);
                return true;
            }
            LootImpl loot = CustomFishing.getInstance().getLootManager().getLoot(args.get(1));
            if (loot == null || loot.isDisableStats()) {
                AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.statisticsNotExists);
                return true;
            }
            Player player = Bukkit.getPlayer(args.get(0));
            assert player != null;
            CustomFishing.getInstance().getStatisticsManager().setData(player.getUniqueId(), args.get(1), amount);
            AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.setStatistics.replace("{Player}", args.get(0)).replace("{Amount}", args.get(2)).replace("{Loot}", args.get(1)));
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, List<String> args) {
            if (args.size() == 1) {
                return filterStartingWith(online_players(), args.get(0));
            }
            if (args.size() == 2) {
                return CustomFishing.getInstance().getLootManager().getAllLoots().stream()
                        .filter(loot -> loot.getKey().startsWith(args.get(1)) && !loot.isDisableStats())
                        .map(LootImpl::getKey)
                        .collect(Collectors.toList());
            }
            if (args.size() == 3) {
                return filterStartingWith(List.of("0","1","2","4","8","16","32","64"), args.get(2));
            }
            return null;
        }
    }

    public static class ResetCommand extends AbstractSubCommand {

        public static final ResetCommand INSTANCE = new ResetCommand();

        public ResetCommand() {
            super("reset");
        }

        @Override
        public boolean onCommand(CommandSender sender, List<String> args) {
            if (!ConfigManager.enableStatistics
                    || super.lackArgs(sender, 1, args.size())
                    || super.playerNotOnline(sender, args.get(0))
            ) return true;
            Player player = Bukkit.getPlayer(args.get(0));
            assert player != null;
            if (CustomFishing.getInstance().getStatisticsManager().reset(player.getUniqueId())) AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.resetStatistics.replace("{Player}", args.get(0)));
            else AdventureUtils.sendMessage(sender, MessageManager.prefix + "Internal Error, player's data is not loaded");
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, List<String> args) {
            if (args.size() == 1) {
                return filterStartingWith(online_players(), args.get(0));
            }
            return null;
        }
    }
}
