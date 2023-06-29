package com.valorin.ranking.hologram;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class HologramInstance {
    private Object hologram;
    private final HologramPlugin hologramPlugin;
    private final RankingType rankingType;
    private List<String> content;
    private final ItemStack itemStack;

    public HologramInstance(Plugin plugin, HologramPlugin hologramPlugin, Location location, RankingType rankingType, List<String> content, ItemStack itemStack) {
        this.hologramPlugin = hologramPlugin;
        this.rankingType = rankingType;
        this.content = content;
        this.itemStack = itemStack;
        switch (hologramPlugin) {
            case HOLOGRAPHIC_DISPLAY:
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            this.hologram = com.gmail.filoghost.holographicdisplays.api.HologramsAPI.createHologram(plugin, location);
                            ((com.gmail.filoghost.holographicdisplays.api.Hologram) this.hologram).appendItemLine(itemStack);
                            content.forEach(((com.gmail.filoghost.holographicdisplays.api.Hologram) this.hologram)::appendTextLine);
                        });
                break;
            case TRHOLOGRAM:
                me.arasple.mc.trhologram.api.hologram.HologramBuilder builder = me.arasple.mc.trhologram.api.TrHologramAPI
                        .builder(location);
                builder.append(viewer -> itemStack);
                content.forEach(builder::append);
                this.hologram = builder.build();
                break;
            case CMI:
                this.hologram = new com.Zrips.CMI.Modules.Holograms.CMIHologram(rankingType.name(), location);
                ((com.Zrips.CMI.Modules.Holograms.CMIHologram) this.hologram).setLoc(location);
                ((com.Zrips.CMI.Modules.Holograms.CMIHologram) this.hologram).setLines(content);
                ((com.Zrips.CMI.Modules.Holograms.CMIHologram) this.hologram).refresh();
                break;
            case DECENT_HOLOGRAM:
                this.hologram = eu.decentsoftware.holograms.api.DHAPI.createHologram(rankingType.name(), location, content);
        }
    }

    public void destroy() {
        switch (hologramPlugin) {
            case HOLOGRAPHIC_DISPLAY:
                ((com.gmail.filoghost.holographicdisplays.api.Hologram) hologram).delete();
                break;
            case TRHOLOGRAM:
                ((me.arasple.mc.trhologram.module.display.Hologram) hologram).destroy();
                break;
            case CMI:
                ((com.Zrips.CMI.Modules.Holograms.CMIHologram) hologram).remove();
                break;
            case DECENT_HOLOGRAM:
                ((eu.decentsoftware.holograms.api.holograms.Hologram) hologram).delete();
        }
    }

    public void refresh(List<String> content) {
        switch (hologramPlugin) {
            case HOLOGRAPHIC_DISPLAY:
                ((com.gmail.filoghost.holographicdisplays.api.Hologram) hologram).clearLines();
                content.forEach(((com.gmail.filoghost.holographicdisplays.api.Hologram) hologram)::appendTextLine);
                break;
            case TRHOLOGRAM:
                me.arasple.mc.trhologram.api.hologram.HologramBuilder builder = ((me.arasple.mc.trhologram.module.display.Hologram) this.hologram).rebuild();
                content.forEach(builder::append);
                hologram = builder.build();
                break;
            case CMI:
                ((com.Zrips.CMI.Modules.Holograms.CMIHologram) hologram).setLines(content);
                ((com.Zrips.CMI.Modules.Holograms.CMIHologram) hologram).refresh();
                break;
            case DECENT_HOLOGRAM:
                eu.decentsoftware.holograms.api.DHAPI.setHologramLines((eu.decentsoftware.holograms.api.holograms.Hologram) hologram, content);
        }
        this.content = content;
    }


    public void refresh(Location location) {
        switch (hologramPlugin) {
            case HOLOGRAPHIC_DISPLAY:
                ((com.gmail.filoghost.holographicdisplays.api.Hologram) hologram).teleport(location);
                break;
            case TRHOLOGRAM:
                //TrHologram的API不支持直接改位置，只能重新创建
                ((me.arasple.mc.trhologram.module.display.Hologram) hologram).destroy();
                me.arasple.mc.trhologram.api.hologram.HologramBuilder builder = me.arasple.mc.trhologram.api.TrHologramAPI
                        .builder(location);
                builder.append(viewer -> itemStack);
                content.forEach(builder::append);
                hologram = builder.build();
                break;
            case CMI:
                ((com.Zrips.CMI.Modules.Holograms.CMIHologram) hologram).setLoc(location);
                ((com.Zrips.CMI.Modules.Holograms.CMIHologram) hologram).refresh();
                break;
            case DECENT_HOLOGRAM:
                eu.decentsoftware.holograms.api.DHAPI.moveHologram((eu.decentsoftware.holograms.api.holograms.Hologram) hologram, location);
        }
    }

    public RankingType getRankingType() {
        return rankingType;
    }

    public enum RankingType {
        WIN, KD
    }
}
