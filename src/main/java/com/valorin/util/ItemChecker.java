package com.valorin.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemChecker {
    public static boolean check(Player player, List<String> materialNameLimitedList, List<String> displayNameLimitedList, List<String> loreLimitedList) {
        Inventory inv = player.getInventory();
        for (int slot = 0; slot < 40; slot++) {
            ItemStack itemStack = inv.getItem(slot);
            if (itemStack == null)
                continue;
            String materialName = itemStack.getType().name();
            for (String MaterialNameLimited : materialNameLimitedList) {
                if (materialName.equals(MaterialNameLimited))
                    return true;
            }
            if (!itemStack.hasItemMeta())
                continue;
            List<String> loreList = itemStack.getItemMeta().getLore();
            if (loreList != null) {
                for (String loreLimited : loreLimitedList) {
                    for (String lore : loreList) {
                        if (lore.contains(loreLimited))
                            return true;
                    }
                }
            }
            String displayName = itemStack.getItemMeta().getDisplayName();
            for (String displayNameLimited : displayNameLimitedList) {
                if (displayName.contains(displayNameLimited))
                    return true;
            }
        }
        return false;
    }
}
