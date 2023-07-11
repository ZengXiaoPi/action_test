package com.valorin.commands.sub;

import static com.valorin.Main.getInstance;
import static com.valorin.configuration.languagefile.MessageSender.sm;

import com.valorin.ranking.hologram.HologramInstance;
import com.valorin.ranking.hologram.HologramManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valorin.Main;
import com.valorin.caches.AreaCache;
import com.valorin.commands.SubCommand;
import com.valorin.commands.way.AdminCommand;
import com.valorin.data.Data;

public class CMDHologramRanking extends SubCommand implements AdminCommand {

    public CMDHologramRanking() {
        super("hd");
    }

    public void sendHelp(Player player) {
        sm("", player);
        sm("&3&lDan&b&l&oTiao &f&l>> &a管理员帮助：排行榜全息图操作", player, false);
        sm("&b/dt hd win &f- &a创建/移动：全息图-胜场排行榜", player, false);
        sm("&b/dt hd winremove &f- &a删除：全息图-胜场排行榜", player, false);
        sm("&b/dt hd kd &f- &a创建/移动：全息图-KD值排行榜", player, false);
        sm("&b/dt hd kdremove &f- &a删除：全息图-KD值排行榜", player, false);
        sm("&b/dt hd refresh &f- &a强制刷新：所有全息图", player, false);
        sm("", player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
                             String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        if (args.length == 1) {
            sendHelp(player);
            return true;
        }
        HologramManager hologramManager = getInstance().getHologramManager();
        if (!hologramManager.isEnabled()) {
            sm("&c[x]未发现HD全息插件！无法使用此功能！", player);
            return true;
        }
        AreaCache cache = Main.getInstance().getCacheHandler().getArea();
        if (args[1].equalsIgnoreCase("win") || args[1].equalsIgnoreCase("kd")) {
            if (player == null) {
                sm("&c[x]这条指令只能由服务器内的玩家执行！后台无法使用！", null);
                return true;
            }
            Location location = player.getLocation();
            if (args[1].equalsIgnoreCase("win")) {
                cache.setWinRanking(location);
            } else {
                cache.setKDRanking(location);
            }
            HologramInstance.RankingType rankingType = args[1].equalsIgnoreCase("win") ? HologramInstance.RankingType.WIN : HologramInstance.RankingType.KD;
            //有则移动，无则创建
            if (hologramManager.getHologramInstanceMap().containsKey(rankingType)) {
                if (rankingType == HologramInstance.RankingType.WIN) {
                    Main.getInstance().getCacheHandler().getArea().setWinRanking(location);
                }
                if (rankingType == HologramInstance.RankingType.KD) {
                    Main.getInstance().getCacheHandler().getArea().setKDRanking(location);
                }
                hologramManager.getHologramInstanceMap().get(rankingType).refresh(location);
                sm("&b移动全息图...", player);
            } else {
                hologramManager.load(rankingType);
                sm("&b创建全息图...", player);
            }
            return true;
        }
        if (args[1].equalsIgnoreCase("winremove") || args[1].equalsIgnoreCase("kdremove")) {
            if (Data.getHologramLocation(0) == null) {
                sm("&c[x]该全息图本来就不存在", player);
                return true;
            }
            if (args[1].equalsIgnoreCase("winremove")) {
                cache.setWinRanking(null);
            } else {
                cache.setKDRanking(null);
            }
            HologramInstance.RankingType rankingType = args[1].equalsIgnoreCase("winremove") ? HologramInstance.RankingType.WIN : HologramInstance.RankingType.KD;
            hologramManager.unload(rankingType);
            sm("&a[v]全息图删除完毕", player);
            return true;
        }
        if (args[1].equalsIgnoreCase("refresh")) {
            if (cache.getWinRankingLocation() == null && cache.getKDRankingLocation() == null) {
                sm("&c[x]无任何全息图！", player);
                return true;
            }
            hologramManager.refresh(false);
            //因为有时候后台会自动调用这个指令，所以不向后台提示这则消息
            if (player != null) {
                sm("&a[v]全息图刷新完毕！", player);
            }
            return true;
        }
        sendHelp(player);
        return true;
    }

}
