package com.valorin.commands.sub;

import com.valorin.Main;
import com.valorin.caches.CacheHandler;
import com.valorin.commands.SubCommand;
import com.valorin.commands.way.AdminCommand;
import com.valorin.util.ViaVersion;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.valorin.configuration.languagefile.MessageSender.sm;

public class CMDTest extends SubCommand implements AdminCommand {

    public CMDTest() {
        super("test");
    }

    public void sendHelp(Player p) {
        sm("", p);
        sm("&3&lDan&b&l&oTiao &f&l>> &a管理员帮助：测试操作", p, false);
        sm("&b/dt test itemtype &f- &a查看手上物品种类的英文名", p, false);
        sm("&b/dt test reloadcache <模块名> &f- &a重载某个模块的缓存数据", p, false);
        sm("", p);
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
        if (args[1].equalsIgnoreCase("itemtype")) {
            if (player == null) {
                sm("&c[x]这条指令只能由服务器内的玩家执行！后台无法使用！", null);
                return true;
            }
            ItemStack itemStack = ViaVersion.getItemInMainHand(player);
            String type = itemStack != null ? itemStack.getType().name() : Material.AIR.name();
            sm("&b物品英文名：&f{name}", player, "name", new String[]{type});
            return true;
        }
        if (args[1].equalsIgnoreCase("reloadcache")) {
            if (args.length != 3) {
                sm("&7正确格式：/dt test reloadcache <模块名>", player);
                return true;
            }
            String cacheTypeEnter = args[2].toUpperCase();
            CacheHandler.CacheType cacheType;
            try {
                cacheType = CacheHandler.CacheType.valueOf(cacheTypeEnter);
            } catch (IllegalArgumentException e) {
                sm("&c[x]请输入正确的模块名！", player);
                return true;
            }
            Main.getInstance().getCacheHandler().reloadCache(cacheType);
            return true;
        }
        sendHelp(player);
        return true;
    }
}
