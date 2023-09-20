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

package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.entity.EntityLibrary;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.bukkit.Location;

public interface EntityManager {

    /**
     * Registers an entity library for use in the plugin.
     *
     * @param entityLibrary The entity library to register.
     * @return {@code true} if the entity library was successfully registered, {@code false} if it already exists.
     */
    boolean registerEntityLibrary(EntityLibrary entityLibrary);

    /**
     * Unregisters an entity library by its identification key.
     *
     * @param identification The identification key of the entity library to unregister.
     * @return {@code true} if the entity library was successfully unregistered, {@code false} if it does not exist.
     */
    boolean unregisterEntityLibrary(String identification);

    /**
     * Summons an entity based on the given loot configuration to a specified location.
     *
     * @param hookLocation   The location where the entity will be summoned, typically where the fishing hook is.
     * @param playerLocation The location of the player who triggered the entity summoning.
     * @param loot           The loot configuration that defines the entity to be summoned.
     */
    void summonEntity(Location hookLocation, Location playerLocation, Loot loot);
}
