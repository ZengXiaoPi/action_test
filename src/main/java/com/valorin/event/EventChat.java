package com.valorin.event;

import com.valorin.Main;
import com.valorin.dan.DanHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static com.valorin.Main.getInstance;

public class EventChat implements Listener {
    @EventHandler
    public void showDan(AsyncPlayerChatEvent e) {
        if (!Main.getInstance().getConfigManager().isDanShowed()) {
            return;
        }
        Player p = e.getPlayer();
        DanHandler danHandler = Main.getInstance().getDanHandler();
        String danDisplayName;
        if (danHandler.getPlayerDan(p.getName()) == null) {
            danDisplayName = getInstance().getConfigManager()
                    .getInitialDanName();
        } else {
            danDisplayName = danHandler.getPlayerDan(p.getName()).getDisplayName();
        }
        e.setFormat(danDisplayName.replace("&", "§") + e.getFormat());
    }
}
