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
import net.momirealms.customfishing.api.util.LogUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class CFLocale {
    public static String MSG_Total_Size;
    public static String MSG_Catch_Amount;
    public static String MSG_Total_Score;
    public static String MSG_Max_Size;
    public static String MSG_No_Player;
    public static String MSG_No_Score;
    public static String MSG_Prefix;
    public static String MSG_Reload;
    public static String MSG_Competition_Not_Exist;
    public static String MSG_No_Competition_Ongoing;
    public static String MSG_End_Competition;
    public static String MSG_Stop_Competition;
    public static String MSG_No_Rank;
    public static String MSG_Item_Not_Exists;
    public static String MSG_Get_Item;
    public static String MSG_Give_Item;
    public static String MSG_Never_Played;
    public static String MSG_Unsafe_Modification;
    public static String MSG_Data_Not_Loaded;
    public static String MSG_Market_GUI_Open;
    public static String MSG_Fishing_Bag_Open;
    public static String MSG_Split_Char;
    public static String MSG_Possible_Loots;
    public static String FORMAT_Day;
    public static String FORMAT_Hour;
    public static String FORMAT_Minute;
    public static String FORMAT_Second;

    public static void load() {
        try {
            YamlDocument.create(
                    new File(CustomFishingPlugin.getInstance().getDataFolder(), "messages/" + CFConfig.language + ".yml"),
                    Objects.requireNonNull(CustomFishingPlugin.getInstance().getResource("messages/" + CFConfig.language + ".yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings
                            .builder()
                            .setAutoUpdate(true)
                            .build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings
                            .builder()
                            .setVersioning(new BasicVersioning("config-version"))
                            .build()
            );
        } catch (IOException e) {
            LogUtils.warn(e.getMessage());
        }
        loadSettings(CustomFishingPlugin.get().getConfig("messages/" + CFConfig.language + ".yml"));
    }

    private static void loadSettings(YamlConfiguration locale) {
        ConfigurationSection msgSection = locale.getConfigurationSection("messages");
        if (msgSection != null) {
            MSG_Prefix = msgSection.getString("prefix");
            MSG_Reload = msgSection.getString("reload");
            MSG_Competition_Not_Exist = msgSection.getString("competition-not-exist");
            MSG_No_Competition_Ongoing = msgSection.getString("no-competition-ongoing");
            MSG_Stop_Competition = msgSection.getString("stop-competition");
            MSG_End_Competition = msgSection.getString("end-competition");
            MSG_No_Player = msgSection.getString("no-player");
            MSG_No_Score = msgSection.getString("no-score");
            MSG_No_Rank = msgSection.getString("no-rank");
            MSG_Catch_Amount = msgSection.getString("goal-catch-amount");
            MSG_Max_Size = msgSection.getString("goal-max-size");
            MSG_Total_Score = msgSection.getString("goal-total-score");
            MSG_Total_Size = msgSection.getString("goal-total-size");
            MSG_Item_Not_Exists = msgSection.getString("item-not-exist");
            MSG_Get_Item = msgSection.getString("get-item");
            MSG_Give_Item = msgSection.getString("give-item");
            MSG_Never_Played = msgSection.getString("never-played");
            MSG_Unsafe_Modification = msgSection.getString("unsafe-modification");
            MSG_Data_Not_Loaded = msgSection.getString("data-not-loaded");
            MSG_Market_GUI_Open = msgSection.getString("open-market-gui");
            MSG_Fishing_Bag_Open = msgSection.getString("open-fishing-bag");
            FORMAT_Day = msgSection.getString("format-day");
            FORMAT_Hour = msgSection.getString("format-hour");
            FORMAT_Minute = msgSection.getString("format-minute");
            FORMAT_Second = msgSection.getString("format-second");
            MSG_Split_Char = msgSection.getString("split-char");
            MSG_Possible_Loots = msgSection.getString("possible-loots");
        }
    }
}
