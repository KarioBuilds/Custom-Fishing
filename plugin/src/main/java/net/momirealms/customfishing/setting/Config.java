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

package net.momirealms.customfishing.setting;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.api.util.OffsetUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Config {

    // config version
    public static String configVersion = "26";

    // language
    public static String language;

    // update checker
    public static boolean updateChecker;

    // BStats
    public static boolean metrics;

    // fishing event priority
    public static EventPriority eventPriority;

    // thread pool settings
    public static int corePoolSize;
    public static int maximumPoolSize;
    public static int keepAliveTime;

    // detection order for item id
    public static List<String> itemDetectOrder;

    // fishing bag
    public static boolean enableFishingBag;
    public static boolean bagStoreLoots;
    public static String bagTitle;
    public static List<Material> bagWhiteListItems;

    // Animation
    public static boolean enableBaitAnimation;
    public static boolean enableSplashAnimation;
    public static int splashAnimationTime;
    public static String lavaSplashItem;
    public static String waterSplashItem;

    // Lava fishing
    public static int lavaMinTime;
    public static int lavaMaxTime;

    // Exception
    public static boolean vanillaMechanicIfNoLoot;
    public static Action[] noLootActions;
    public static boolean debug;

    // Competition
    public static boolean redisRanking;
    public static int placeholderLimit;

    //
    public static int dataSaveInterval;

    public static boolean legacyColorSupport;

    public static void load() {
        try {
            YamlDocument.create(
                    new File(CustomFishingPlugin.getInstance().getDataFolder(), "config.yml"),
                    Objects.requireNonNull(CustomFishingPlugin.getInstance().getResource("config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings
                            .builder()
                            .setAutoUpdate(true)
                            .build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings
                            .builder()
                            .setVersioning(new BasicVersioning("config-version"))
                            .addIgnoredRoute(configVersion, "mechanics.mechanic-requirements", '.')
                            .addIgnoredRoute(configVersion, "mechanics.global-loot-properties", '.')
                            .build()
            );
            loadSettings(CustomFishingPlugin.getInstance().getConfig("config.yml"));
        } catch (IOException e) {
            LogUtils.warn(e.getMessage());
        }
    }

    private static void loadSettings(YamlConfiguration config) {
        debug = config.getBoolean("debug", false);

        language = config.getString("lang", "english");
        updateChecker = config.getBoolean("update-checker");
        metrics = config.getBoolean("metrics");
        eventPriority = EventPriority.valueOf(config.getString("other-settings.event-priority", "NORMAL").toUpperCase(Locale.ENGLISH));

        corePoolSize = config.getInt("other-settings.thread-pool-settings.corePoolSize", 4);
        maximumPoolSize = config.getInt("other-settings.thread-pool-settings.maximumPoolSize", 8);
        keepAliveTime = config.getInt("other-settings.thread-pool-settings.keepAliveTime", 10);

        itemDetectOrder = config.getStringList("other-settings.item-detection-order");

        enableFishingBag = config.getBoolean("mechanics.fishing-bag.enable", true);
        bagTitle = config.getString("mechanics.fishing-bag.bag-title");
        bagStoreLoots = config.getBoolean("mechanics.fishing-bag.can-store-loot", false);
        bagWhiteListItems = config.getStringList("mechanics.fishing-bag.whitelist-items").stream().map(it -> Material.valueOf(it.toUpperCase(Locale.ENGLISH))).toList();

        lavaMinTime = config.getInt("mechanics.lava-fishing.min-wait-time", 100);
        lavaMaxTime = config.getInt("mechanics.lava-fishing.max-wait-time", 600);

        enableSplashAnimation = config.getBoolean("mechanics.animation.splash.enable", true);
        enableBaitAnimation = config.getBoolean("mechanics.animation.bait.enable", true);
        waterSplashItem = config.getString("mechanics.animation.splash.water");
        lavaSplashItem = config.getString("mechanics.animation.splash.lava");
        splashAnimationTime = config.getInt("mechanics.animation.splash.duration");

        vanillaMechanicIfNoLoot = config.getBoolean("mechanics.vanilla-mechanic-if-no-loot.enable", false);
        noLootActions = CustomFishingPlugin.get().getActionManager().getActions(config.getConfigurationSection("mechanics.vanilla-mechanic-if-no-loot.actions"));

        redisRanking = config.getBoolean("mechanics.competition.redis-ranking", false);
        placeholderLimit = config.getInt("mechanics.competition.placeholder-limit", 3);

        dataSaveInterval = config.getInt("other-settings.data-saving-interval", 600);
        legacyColorSupport = config.getBoolean("other-settings.legacy-color-code-support", false);

        OffsetUtils.loadConfig(config.getConfigurationSection("other-settings.offset-characters"));
    }
}
