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

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.FishMeta;
import net.momirealms.customfishing.integration.SkillInterface;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class SkillXPImpl extends AbstractAction implements Action {

    private final double amount;

    public SkillXPImpl(double amount, double chance) {
        super(chance);
        this.amount = amount;
    }

    @Override
    public void doOn(Player player, Player another, @Nullable FishMeta fishMeta) {
        if (!canExecute()) return;
        SkillInterface skillInterface = CustomFishing.getInstance().getIntegrationManager().getSkillInterface();
        if (skillInterface == null) return;
        skillInterface.addXp(player, amount);
    }
}