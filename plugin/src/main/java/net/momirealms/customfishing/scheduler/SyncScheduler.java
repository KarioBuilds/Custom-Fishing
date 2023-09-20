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

package net.momirealms.customfishing.scheduler;

import net.momirealms.customfishing.api.scheduler.CancellableTask;
import org.bukkit.Location;

public interface SyncScheduler {

    /**
     * Runs a task synchronously on the main server thread or region thread.
     *
     * @param runnable The task to run.
     * @param location The location associated with the task.
     */
    void runSyncTask(Runnable runnable, Location location);

    /**
     * Runs a task synchronously with a specified delay and period.
     *
     * @param runnable    The task to run.
     * @param location    The location associated with the task.
     * @param delayTicks  The delay in ticks before the first execution.
     * @param periodTicks The period between subsequent executions in ticks.
     * @return A CancellableTask for managing the scheduled task.
     */
    CancellableTask runTaskSyncTimer(Runnable runnable, Location location, long delayTicks, long periodTicks);

    /**
     * Runs a task synchronously with a specified delay in ticks.
     *
     * @param runnable    The task to run.
     * @param location    The location associated with the task.
     * @param delayTicks  The delay in ticks before the task execution.
     * @return A CancellableTask for managing the scheduled task.
     */
    CancellableTask runTaskSyncLater(Runnable runnable, Location location, long delayTicks);
}
