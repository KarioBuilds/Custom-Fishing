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

package net.momirealms.customfishing.gui.page.property;

import net.momirealms.customfishing.adventure.AdventureHelper;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.gui.SectionPage;
import net.momirealms.customfishing.gui.icon.BackGroundItem;
import net.momirealms.customfishing.setting.CFLocale;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.AnvilWindow;

public class DisplayNameEditor {

    private final SectionPage parentPage;
    private String name;
    private final ConfigurationSection section;

    public DisplayNameEditor(Player player, SectionPage parentPage) {
        this.parentPage = parentPage;
        this.section = parentPage.getSection();

        Item border = new SimpleItem(new ItemBuilder(Material.AIR));
        var confirm  = new ConfirmIcon();
        Gui upperGui = Gui.normal()
                .setStructure("a # b")
                .addIngredient('a', new ItemBuilder(Material.NAME_TAG).setDisplayName(section.getString("display.name", CFLocale.GUI_NEW_DISPLAY_NAME)))
                .addIngredient('#', border)
                .addIngredient('b', confirm)
                .build();

        var gui = PagedGui.items()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # # # c # # # #"
                )
                .addIngredient('x', new ItemStack(Material.AIR))
                .addIngredient('c', parentPage.getBackItem())
                .addIngredient('#', new BackGroundItem())
                .build();

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(
                        AdventureHelper.getInstance().getComponentFromMiniMessage(CFLocale.GUI_TITLE_DISPLAY_NAME)
                ))
                .addRenameHandler(s -> {
                    name = s;
                    confirm.notifyWindows();
                })
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public class ConfirmIcon extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            if (name == null || name.isEmpty()) {
                return new ItemBuilder(Material.STRUCTURE_VOID).setDisplayName(new ShadedAdventureComponentWrapper(AdventureHelper.getInstance().getComponentFromMiniMessage(
                        CFLocale.GUI_DELETE_PROPERTY
                )));
            } else {
                return new ItemBuilder(Material.NAME_TAG)
                        .setDisplayName(new ShadedAdventureComponentWrapper(AdventureHelper.getInstance().getComponentFromMiniMessage(
                              "<!i>" + name
                        )))
                        .addLoreLines(new ShadedAdventureComponentWrapper(AdventureHelper.getInstance().getComponentFromMiniMessage(
                                CFLocale.GUI_CLICK_CONFIRM
                        )));
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (name == null || name.isEmpty()) {
                section.set("display.name", null);
            } else {
                section.set("display.name", name);
            }
            parentPage.reOpen();
            parentPage.save();
        }
    }
}
