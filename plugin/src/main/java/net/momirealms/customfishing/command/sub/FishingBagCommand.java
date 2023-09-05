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

package net.momirealms.customfishing.command.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.user.OfflineUser;
import net.momirealms.customfishing.setting.Locale;
import net.momirealms.customfishing.storage.user.OfflineUserImpl;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class FishingBagCommand {

    public static FishingBagCommand INSTANCE = new FishingBagCommand();

    public CommandAPICommand getBagCommand() {
         return new CommandAPICommand("fishingbag")
                    .withPermission("fishingbag.user")
                    .withSubcommand(getAdminCommand())
                    .executesPlayer(((player, args) -> {
                        var inv = CustomFishingPlugin.get().getBagManager().getOnlineBagInventory(player.getUniqueId());
                        if (inv != null) player.openInventory(inv);
                    }));
    }

    private CommandAPICommand getAdminCommand() {
        return new CommandAPICommand("edit")
                .withPermission("fishingbag.admin")
                .withArguments(new OfflinePlayerArgument("player"))
                .executesPlayer(((player, args) -> {
                    OfflinePlayer offlinePlayer = (OfflinePlayer) args.get("player");
                    UUID uuid = offlinePlayer.getUniqueId();
                    Inventory onlineInv = CustomFishingPlugin.get().getBagManager().getOnlineBagInventory(uuid);
                    if (onlineInv != null) {
                        player.openInventory(onlineInv);
                    } else {
                        CustomFishingPlugin.get().getStorageManager().getOfflineUser(uuid, false).thenAccept(optional -> {
                           if (optional.isEmpty()) {
                               AdventureManagerImpl.getInstance().sendMessageWithPrefix(player, Locale.MSG_Unsafe_Modification);
                           } else {
                               OfflineUser offlineUser = optional.get();
                               if (offlineUser == OfflineUserImpl.NEVER_PLAYED_USER) {
                                   AdventureManagerImpl.getInstance().sendMessageWithPrefix(player, Locale.MSG_Never_Played);
                               } else {

                               }
                           }
                        });
                    }
                }));
    }
}
