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

package net.momirealms.customfishing.mechanic.fishing;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.event.LavaFishingEvent;
import net.momirealms.customfishing.api.mechanic.TempFishingState;
import net.momirealms.customfishing.api.mechanic.condition.FishingPreparation;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.setting.Config;
import net.momirealms.customfishing.util.ArmorStandUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class HookCheckTimerTask implements Runnable {

    private final FishingManagerImpl manager;
    private final CancellableTask hookMovementTask;
    private LavaEffectTask lavaFishingTask;
    private final FishHook fishHook;
    private final FishingPreparation fishingPreparation;
    private final Effect initialEffect;
    private final int lureLevel;
    private boolean firstTime;
    private boolean fishHooked;
    private boolean reserve;
    private int jumpTimer;
    private Entity hookedEntity;

    public HookCheckTimerTask(
            FishingManagerImpl manager,
            FishHook fishHook,
            FishingPreparation fishingPreparation,
            Effect initialEffect
    ) {
        this.manager = manager;
        this.fishHook = fishHook;
        this.initialEffect = initialEffect;
        this.fishingPreparation = fishingPreparation;
        this.hookMovementTask = CustomFishingPlugin.get().getScheduler().runTaskSyncTimer(this, fishHook.getLocation(), 1, 1);
        this.lureLevel = fishingPreparation.getRodItemStack().getEnchantmentLevel(Enchantment.LURE);
        this.firstTime = true;
    }

    @Override
    public void run() {
        if (
            !fishHook.isValid()
            || fishHook.isOnGround()
            || (fishHook.getHookedEntity() != null && fishHook.getHookedEntity().getType() != EntityType.ARMOR_STAND)
        ) {
            // This task would be cancelled when hook is not at a proper place
            // or player reels in before it goes into water or lava
            this.destroy();
            return;
        }
        if (fishHook.getLocation().getBlock().getType() == Material.LAVA) {
            // if player can fish in lava
            if (!initialEffect.canLavaFishing()) {
                this.destroy();
                return;
            }
            if (firstTime) {
                this.fishingPreparation.insertArg("in-lava", "true");
                if (Config.enableSplashAnimation)
                    ArmorStandUtils.sendAnimationToPlayer(
                            fishingPreparation.getPlayer(),
                            fishHook.getLocation(),
                            CustomFishingPlugin.get().getItemManager().build(null, "util", Config.lavaSplashItem),
                            Config.splashAnimationTime
                    );
                firstTime = false;
                this.setTempState();
            }
            // simulate fishing mechanic
            if (fishHooked) {
                jumpTimer++;
                if (jumpTimer < 4)
                    return;
                jumpTimer = 0;
                fishHook.setVelocity(new Vector(0,0.24,0));
                return;
            }
            if (!reserve) {
                if (jumpTimer < 5) {
                    jumpTimer++;
                    fishHook.setVelocity(new Vector(0,0.2 - jumpTimer * 0.02,0));
                    return;
                }
                reserve = true;
                this.startLavaFishingMechanic();
                this.makeHookStatic(fishHook.getLocation());
            }
            return;
        }
        if (fishHook.isInWater()) {
            // if the hook is in water
            // then cancel the task
            this.fishingPreparation.insertArg("in-lava", "false");
            if (Config.enableSplashAnimation)
                ArmorStandUtils.sendAnimationToPlayer(
                        fishingPreparation.getPlayer(),
                        fishHook.getLocation(),
                        CustomFishingPlugin.get().getItemManager().build(null, "util", Config.waterSplashItem),
                        Config.splashAnimationTime
                );
            this.destroy();
            this.setTempState();
            return;
        }
    }

    public void destroy() {
        this.cancelSubTask();
        this.removeTempEntity();
        this.hookMovementTask.cancel();
        this.manager.removeHookCheckTask(fishingPreparation.getPlayer());
    }

    public void cancelSubTask() {
        if (lavaFishingTask != null && !lavaFishingTask.isCancelled()) {
            lavaFishingTask.cancel();
            lavaFishingTask = null;
        }
    }

    private void setTempState() {
        Loot nextLoot = manager.getNextLoot(initialEffect, fishingPreparation);
        if (nextLoot == null)
            return;
        fishingPreparation.insertArg("loot", nextLoot.getNick());
        fishingPreparation.insertArg("id", nextLoot.getID());
        CustomFishingPlugin.get().getScheduler().runTaskAsync(() -> manager.setTempFishingState(fishingPreparation.getPlayer(), new TempFishingState(
                initialEffect,
                fishingPreparation,
                nextLoot
        )));
    }

    public void removeTempEntity() {
        if (hookedEntity != null && !hookedEntity.isDead())
            hookedEntity.remove();
    }

    private void startLavaFishingMechanic() {
        // get random time
        int random = ThreadLocalRandom.current().nextInt(Config.lavaMinTime, Config.lavaMaxTime);
        random -= lureLevel * 100;
        random *= initialEffect.getTimeModifier();
        random = Math.max(Config.lavaMinTime, random);

        // lava effect task (Three seconds in advance)
        this.lavaFishingTask = new LavaEffectTask(
                this,
                fishHook.getLocation(),
                random - 3 * 20
        );
    }

    public void getHooked() {
        LavaFishingEvent lavaFishingEvent = new LavaFishingEvent(fishingPreparation.getPlayer(), LavaFishingEvent.State.BITE, fishHook);
        Bukkit.getPluginManager().callEvent(lavaFishingEvent);
        if (lavaFishingEvent.isCancelled()) {
            this.startLavaFishingMechanic();
            return;
        }

        this.fishHooked = true;
        this.removeTempEntity();

        AdventureManagerImpl.getInstance().sendSound(
                fishingPreparation.getPlayer(),
                Sound.Source.NEUTRAL,
                Key.key("minecraft:block.pointed_dripstone.drip_lava_into_cauldron"),
                1,
                1
        );

        CustomFishingPlugin.get().getScheduler().runTaskAsyncLater(() -> {
            fishHooked = false;
            reserve = false;
        }, (2 * 20) * 50L, TimeUnit.MILLISECONDS);
    }

    private void makeHookStatic(Location armorLoc) {
        armorLoc.setY(armorLoc.getBlockY() + 0.2);
        if (hookedEntity != null && !hookedEntity.isDead())
            hookedEntity.remove();
        hookedEntity = armorLoc.getWorld().spawn(armorLoc, ArmorStand.class, a -> {
            a.setInvisible(true);
            a.setCollidable(false);
            a.setInvulnerable(true);
            a.setVisible(false);
            a.setCustomNameVisible(false);
            a.setSmall(true);
            a.setGravity(false);
            a.getPersistentDataContainer().set(
                    Objects.requireNonNull(NamespacedKey.fromString("lavafishing", CustomFishingPlugin.get())),
                    PersistentDataType.BOOLEAN,
                    true
            );
        });
        fishHook.setHookedEntity(hookedEntity);
    }

    public boolean isFishHooked() {
        return fishHooked;
    }
}
