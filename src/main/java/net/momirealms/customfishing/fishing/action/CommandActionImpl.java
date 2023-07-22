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

package net.momirealms.customfishing.fishing.action;

import net.momirealms.customfishing.fishing.FishMeta;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class CommandActionImpl extends AbstractAction implements Action {

    private final String[] commands;
    private final String nick;

    public CommandActionImpl(String[] commands, @Nullable String nick, double chance) {
        super(chance);
        this.commands = commands;
        this.nick = nick == null ? "" : nick;
    }

    @Override
    public void doOn(Player player, @Nullable Player anotherPlayer, @Nullable FishMeta fishMeta) {
        if (!canExecute()) return;
        for (String command : commands) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                    command.replace("{player}", player.getName())
                            .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                            .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                            .replace("{z}", String.valueOf(player.getLocation().getBlockZ()))
                            .replace("{loot}", nick)
                            .replace("{world}", player.getWorld().getName())
                            .replace("{activator}", anotherPlayer == null ? "" : anotherPlayer.getName())
                            .replace("{size}", fishMeta == null ? "" : String.format("%.2f", fishMeta.size()))
            );
        }
    }
}
