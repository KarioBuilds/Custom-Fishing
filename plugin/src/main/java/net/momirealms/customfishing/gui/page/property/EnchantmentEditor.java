package net.momirealms.customfishing.gui.page.property;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.gui.YamlPage;
import net.momirealms.customfishing.gui.icon.BackGroundItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.AnvilWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchantmentEditor {

    private final Player player;
    private final YamlPage parentPage;
    private final ArrayList<String> enchantments;
    private final ConfigurationSection section;
    private int index;
    private boolean store;

    public EnchantmentEditor(Player player, YamlPage parentPage, ConfigurationSection section, boolean store) {
        this.player = player;
        this.parentPage = parentPage;
        this.section = section;
        this.store = store;
        this.index = 0;
        this.enchantments = new ArrayList<>();
        this.enchantments.add("Select one enchantment");
        ConfigurationSection eSection = section.getConfigurationSection(store ? "stored-enchantments" : "enchantments");
        if (eSection != null)
            for (Map.Entry<String, Object> entry : eSection.getValues(false).entrySet()) {
                this.enchantments.add(entry.getKey() + ":" + entry.getValue());
            }
        reOpen(0);
    }

    public void reOpen(int idx) {
        Item border = new SimpleItem(new ItemBuilder(Material.AIR));
        var confirm  = new ConfirmIcon();
        Gui upperGui = Gui.normal()
                .setStructure(
                        "a # b"
                )
                .addIngredient('a', new ItemBuilder(Material.NAME_TAG).setDisplayName(enchantments.get(idx)))
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
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('c', parentPage.getBackItem())
                .addIngredient('#', new BackGroundItem())
                .setContent(getContents())
                .build();

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(
                        AdventureManagerImpl.getInstance().getComponentFromMiniMessage("Edit" + (store ? " Stored ": " ") + "Enchantment")
                ))
                .addRenameHandler(s -> {
                    if (index == 0) return;
                    enchantments.set(index, s);
                    confirm.notifyWindows();
                })
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public List<Item> getContents() {
        ArrayList<Item> items = new ArrayList<>();
        int i = 1;
        List<String> subList = enchantments.subList(1, enchantments.size());
        for (String lore : subList) {
            items.add(new EnchantmentElement(lore, i++));
        }
        items.add(new AddEnchantment());
        return items;
    }

    public class AddEnchantment extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.ANVIL).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    "<green>[+] <gray>Add a new enchantment"
            )));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            enchantments.add("namespace:enchantment:level");
            index = enchantments.size() - 1;
            reOpen(index);
        }
    }

    public class EnchantmentElement extends AbstractItem {

        private final String line;
        private final int idx;

        public EnchantmentElement(String line, int idx) {
            this.line = line;
            this.idx = idx;
        }

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.ENCHANTED_BOOK).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    line
            ))).addLoreLines("")
                    .addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            "<#00FF7F> -> Left click to edit"
                    ))).addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            "<#FF6347> -> Right click to delete"
                    )));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType == ClickType.LEFT) {
                index = idx;
                reOpen(idx);
            } else if (clickType == ClickType.RIGHT) {
                enchantments.remove(idx);
                index = Math.min(index, enchantments.size() - 1);
                reOpen(index);
            }
        }
    }

    public class ConfirmIcon extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            List<String> subList = enchantments.subList(1, enchantments.size());
            if (subList.isEmpty()) {
                return new ItemBuilder(Material.STRUCTURE_VOID).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        "<#00CED1>● Delete property"
                )));
            } else {
                var builder = new ItemBuilder(Material.NAME_TAG)
                        .setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                "<#00FF7F> -> Click to confirm"
                        )));
                for (String enchantment : subList) {
                    String[] split = enchantment.split(":");
                    if (split.length != 3) {
                        return new ItemBuilder(Material.BARRIER).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                "<red>● Illegal format"
                        )));
                    }
                    try {
                        Integer.parseInt(split[2]);
                        builder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                " <gray>-</gray> " + enchantment
                        )));
                    } catch (NumberFormatException e) {
                        return new ItemBuilder(Material.BARRIER).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                "<red>● Illegal format"
                        )));
                    }
                }
                return builder;
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            List<String> subList = enchantments.subList(1, enchantments.size());
            for (String line : subList) {
                String[] split = line.split(":");
                if (split.length != 3) {
                    return;
                }
                try {
                    Integer.parseInt(split[2]);
                } catch (NumberFormatException e) {
                    return;
                }
            }
            section.set(store ? "stored-enchantments" : "enchantments", null);
            for (String line : subList) {
                String[] split = line.split(":");
                section.set((store ? "stored-enchantments" : "enchantments") + "." + split[0] + ":" + split[1], Integer.parseInt(split[2]));
            }
            parentPage.reOpen();
            parentPage.save();
        }
    }
}
