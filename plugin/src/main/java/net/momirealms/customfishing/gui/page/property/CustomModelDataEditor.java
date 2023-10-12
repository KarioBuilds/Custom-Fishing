package net.momirealms.customfishing.gui.page.property;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.gui.YamlPage;
import net.momirealms.customfishing.gui.icon.BackGroundItem;
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

public class CustomModelDataEditor {

    private final Player player;
    private final YamlPage parentPage;
    private String cmd;
    private final ConfigurationSection section;
    private final String material;

    public CustomModelDataEditor(Player player, YamlPage parentPage, ConfigurationSection section) {
        this.player = player;
        this.parentPage = parentPage;
        this.section = section;
        this.material = section.getString("material");

        Item border = new SimpleItem(new ItemBuilder(Material.AIR));
        var confirm = new ConfirmIcon();
        Gui upperGui = Gui.normal()
                .setStructure(
                        "a # b"
                )
                .addIngredient('a', new ItemBuilder(CustomFishingPlugin.get()
                        .getItemManager()
                        .getItemStackAppearance(player, material)
                )
                .setCustomModelData(section.getInt("custom-model-data", 0))
                .setDisplayName(String.valueOf(section.getInt("custom-model-data", 0))))
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
                        AdventureManagerImpl.getInstance().getComponentFromMiniMessage("Edit CustomModelData")
                ))
                .addRenameHandler(s -> {
                    cmd = s;
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
            if (cmd == null || cmd.isEmpty()) {
                return new ItemBuilder(Material.STRUCTURE_VOID).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        "<#00CED1>● Delete property"
                )));
            } else {
                try {
                    int value = Integer.parseInt(cmd);
                    if (value >= 0) {
                        return new ItemBuilder(
                                CustomFishingPlugin.get()
                                        .getItemManager()
                                        .getItemStackAppearance(player, material)
                        )
                                .setCustomModelData(value)
                                .setDisplayName("New value: " + value)
                                .addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                        "<#00FF7F> -> Click to confirm"
                                )));
                    } else {
                        return new ItemBuilder(Material.BARRIER).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                "<red>● Invalid number"
                        )));
                    }
                } catch (NumberFormatException e) {
                    return new ItemBuilder(Material.BARRIER).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            "<red>● Invalid number"
                    )));
                }
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (cmd == null || cmd.isEmpty()) {
                section.set("custom-model-data", null);
            } else {
                try {
                    int value = Integer.parseInt(cmd);
                    if (value >= 0) {
                        section.set("custom-model-data", value);
                    } else {
                        return;
                    }
                } catch (NumberFormatException e) {
                    return;
                }
            }
            parentPage.reOpen();
            parentPage.save();
        }
    }
}
