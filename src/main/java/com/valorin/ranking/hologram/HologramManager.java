package com.valorin.ranking.hologram;

import com.valorin.Main;
import com.valorin.caches.AreaCache;
import com.valorin.caches.RankingCache;
import com.valorin.configuration.languagefile.MessageBuilder;
import com.valorin.util.Debug;
import com.valorin.util.ViaVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.valorin.configuration.languagefile.MessageSender.gm;

public class HologramManager {
    private boolean hologramPluginExist = false;
    private HologramPlugin hologramPluginUsed;
    private boolean isEnabled = false;
    private boolean isNeedRefresh = false;
    private final Map<HologramInstance.RankingType, HologramInstance> hologramInstanceMap = new HashMap<>();

    public HologramManager() {
        enable();
    }

    public void enable() {
        //检查是否有全息图插件
        checkHologramPlugin();
        if (!hologramPluginExist) {
            return;
        }

        //加载所有种类的全息图
        for (HologramInstance.RankingType rankingType : HologramInstance.RankingType.values()) {
            load(rankingType);
        }

        //读取配置文件中的自动保存间隔设置，并将秒化为游戏刻，且要确保至少为10秒
        int interval = Main.getInstance().getConfigManager()
                .getHologramRefreshInterval();
        interval = interval < 10 ? 10 * 20 : interval * 20;
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            if (isNeedRefresh) {
                refresh(true);
                isNeedRefresh = false;
            }
        }, interval, interval);

        //确认初始化完毕
        isEnabled = true;

        if (Main.getInstance().getConfigManager().isDebug()) {
            Debug.send("全息插件启动成功，当前使用的为：" + hologramPluginUsed.getRealName(),
                    "The plugin " + hologramPluginUsed.getRealName() + " for hologram is enabled");
        }
    }

    public void disable() {
        for (HologramInstance.RankingType rankingType : HologramInstance.RankingType.values()) {
            unload(rankingType);
        }
    }

    public void setIsNeedRefresh(boolean isNeedRefresh) {
        this.isNeedRefresh = isNeedRefresh;
    }

    public void refresh(boolean auto) {
        for (HologramInstance hologramInstance : hologramInstanceMap.values()) {
            hologramInstance.refresh(getRefreshContent(hologramInstance.getRankingType()));
        }
        if (auto) {
            Bukkit.getConsoleSender().sendMessage(gm("&7全息图已自动刷新..."));
        }
    }

    public Map<HologramInstance.RankingType, HologramInstance> getHologramInstanceMap() {
        return hologramInstanceMap;
    }

    public void load(HologramInstance.RankingType rankingType) {
        AreaCache areaCache = Main.getInstance().getCacheHandler().getArea();
        Location location;
        List<String> content = new ArrayList<>();
        ItemStack itemStack;
        switch (rankingType) {
            case WIN:
                location = areaCache.getWinRankingLocation();
                content = getRefreshContent(rankingType);
                itemStack = new ItemStack(ViaVersion.getGoldenSwordMaterial());
                break;
            case KD:
                location = areaCache.getKDRankingLocation();
                content = getRefreshContent(rankingType);
                itemStack = new ItemStack(ViaVersion.getGoldenAxeMaterial());
                break;
            default:
                location = null;
                itemStack = null;
        }
        //如果位置为空，说明管理员还没创建这种全息图
        if (location == null) {
            return;
        }
        hologramInstanceMap.put(rankingType, new HologramInstance(Main.getInstance(), hologramPluginUsed, location, rankingType, content, itemStack));
    }

    public void unload(HologramInstance.RankingType rankingType) {
        hologramInstanceMap.get(rankingType).destroy();
        hologramInstanceMap.remove(rankingType);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    //供初始化和刷新时用
    private List<String> getRefreshContent(HologramInstance.RankingType rankingType) {
        List<String> content = new ArrayList<>();
        RankingCache rankingCache = Main.getInstance().getCacheHandler().getRanking();

        switch (rankingType) {
            case WIN:
                content.add(gm("&b[star1]单挑-胜场排行榜[star2]"));
                content.addAll(IntStream.range(0, Math.min(rankingCache.getWin().size(), 10))
                        .mapToObj(i -> MessageBuilder.getRankingString(i, rankingCache.getWin(), true))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
                break;
            case KD:
                content.add(gm("&b[star1]单挑-KD比值排行榜[star2]"));
                content.addAll(IntStream.range(0, Math.min(rankingCache.getKD().size(), 10))
                        .mapToObj(i -> MessageBuilder.getRankingString(i, rankingCache.getKD(), true))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
                break;
        }
        return content;
    }

    //检查全息插件
    private void checkHologramPlugin() {
        String hologramPluginNameInConfig = Main.getInstance().getConfigManager().getHologramPluginUsed();
        List<HologramPlugin> hologramPluginInstalledList = new ArrayList<>();
        for (HologramPlugin hologramPlugin : HologramPlugin.values()) {
            if (Bukkit.getPluginManager().getPlugin(hologramPluginNameInConfig) == null) {
                continue;
            }
            hologramPluginInstalledList.add(hologramPlugin);
            if (hologramPlugin.getRealName().equals(hologramPluginNameInConfig)) {
                hologramPluginUsed = hologramPlugin;
                hologramPluginExist = true;
            }
        }
        //如果遍历一轮过后没有找到配置文件中所填写的全息插件，那就找其他已安装的全息图插件
        if (!hologramPluginExist && hologramPluginInstalledList.size() > 0) {
            hologramPluginUsed = hologramPluginInstalledList.get(0);
            hologramPluginExist = true;
        }
    }
}
