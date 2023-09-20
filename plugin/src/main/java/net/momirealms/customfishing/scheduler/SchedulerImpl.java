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

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.api.scheduler.Scheduler;
import net.momirealms.customfishing.setting.CFConfig;
import org.bukkit.Location;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A scheduler implementation responsible for scheduling and managing tasks in a multi-threaded environment.
 */
public class SchedulerImpl implements Scheduler {

    private final SyncScheduler syncScheduler;
    private final ScheduledThreadPoolExecutor schedule;
    private final CustomFishingPlugin plugin;

    public SchedulerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.syncScheduler = plugin.getVersionManager().isFolia() ?
                new FoliaSchedulerImpl(plugin) : new BukkitSchedulerImpl(plugin);
        this.schedule = new ScheduledThreadPoolExecutor(4);
        this.schedule.setMaximumPoolSize(4);
        this.schedule.setKeepAliveTime(10, TimeUnit.SECONDS);
        this.schedule.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * Reloads the scheduler configuration based on CustomFishingPlugin settings.
     */
    public void reload() {
        this.schedule.setCorePoolSize(CFConfig.corePoolSize);
        this.schedule.setKeepAliveTime(CFConfig.keepAliveTime, TimeUnit.SECONDS);
        this.schedule.setMaximumPoolSize(CFConfig.maximumPoolSize);
    }

    /**
     * Shuts down the scheduler.
     */
    public void shutdown() {
        if (this.schedule != null && !this.schedule.isShutdown())
            this.schedule.shutdown();
    }

    /**
     * Runs a task synchronously on the main server thread or region thread.
     *
     * @param runnable The task to run.
     * @param location The location associated with the task.
     */
    @Override
    public void runTaskSync(Runnable runnable, Location location) {
        this.syncScheduler.runSyncTask(runnable, location);
    }

    /**
     * Runs a task asynchronously.
     *
     * @param runnable The task to run.
     */
    @Override
    public void runTaskAsync(Runnable runnable) {
        this.schedule.execute(runnable);
    }

    /**
     * Runs a task synchronously with a specified delay and period.
     *
     * @param runnable    The task to run.
     * @param location    The location associated with the task.
     * @param delayTicks  The delay in ticks before the first execution.
     * @param periodTicks The period between subsequent executions in ticks.
     * @return A CancellableTask for managing the scheduled task.
     */
    @Override
    public CancellableTask runTaskSyncTimer(Runnable runnable, Location location, long delayTicks, long periodTicks) {
        return this.syncScheduler.runTaskSyncTimer(runnable, location, delayTicks, periodTicks);
    }

    /**
     * Runs a task asynchronously with a specified delay.
     *
     * @param runnable  The task to run.
     * @param delay     The delay before the task execution.
     * @param timeUnit  The time unit for the delay.
     * @return A CancellableTask for managing the scheduled task.
     */
    @Override
    public CancellableTask runTaskAsyncLater(Runnable runnable, long delay, TimeUnit timeUnit) {
        return new ScheduledTask(schedule.schedule(runnable, delay, timeUnit));
    }

    /**
     * Runs a task synchronously with a specified delay.
     *
     * @param runnable  The task to run.
     * @param location  The location associated with the task.
     * @param delay     The delay before the task execution.
     * @param timeUnit  The time unit for the delay.
     * @return A CancellableTask for managing the scheduled task.
     */
    @Override
    public CancellableTask runTaskSyncLater(Runnable runnable, Location location, long delay, TimeUnit timeUnit) {
        return new ScheduledTask(schedule.schedule(() -> {
            runTaskSync(runnable, location);
        }, delay, timeUnit));
    }

    /**
     * Runs a task synchronously with a specified delay in ticks.
     *
     * @param runnable    The task to run.
     * @param location    The location associated with the task.
     * @param delayTicks  The delay in ticks before the task execution.
     * @return A CancellableTask for managing the scheduled task.
     */
    @Override
    public CancellableTask runTaskSyncLater(Runnable runnable, Location location, long delayTicks) {
        return this.syncScheduler.runTaskSyncLater(runnable, location, delayTicks);
    }

    /**
     * Runs a task asynchronously with a specified delay and period.
     *
     * @param runnable    The task to run.
     * @param delay       The delay before the first execution.
     * @param period      The period between subsequent executions.
     * @param timeUnit    The time unit for the delay and period.
     * @return A CancellableTask for managing the scheduled task.
     */
    @Override
    public CancellableTask runTaskAsyncTimer(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return new ScheduledTask(schedule.scheduleAtFixedRate(runnable, delay, period, timeUnit));
    }

    /**
     * Represents a thread-pool task that can be cancelled.
     */
    public static class ScheduledTask implements CancellableTask {

        private final ScheduledFuture<?> scheduledFuture;

        public ScheduledTask(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }

        @Override
        public void cancel() {
            this.scheduledFuture.cancel(false);
        }

        @Override
        public boolean isCancelled() {
            return this.scheduledFuture.isCancelled();
        }
    }
}
