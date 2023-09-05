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

package net.momirealms.customfishing.api.mechanic.competition;

import java.util.concurrent.ThreadLocalRandom;

public enum CompetitionGoal {

    CATCH_AMOUNT,
    TOTAL_SCORE,
    MAX_SIZE,
    TOTAL_SIZE,
    RANDOM;

    public static CompetitionGoal getRandom() {
        return CompetitionGoal.values()[ThreadLocalRandom.current().nextInt(CompetitionGoal.values().length - 1)];
    }
}
