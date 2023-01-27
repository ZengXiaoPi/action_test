package com.valorin.arenas;

import com.valorin.Main;
import com.valorin.configuration.ConfigManager;
import com.valorin.dan.CustomDan;
import com.valorin.data.encapsulation.ArenaInfo;
import com.valorin.util.ItemTaker;
import com.valorin.util.ViaVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.valorin.configuration.languagefile.MessageSender.gm;
import static com.valorin.configuration.languagefile.MessageSender.sm;

public class Arena {
    /*
     * 竞技场属性：
     *
     * 竞技场名称（编辑名） 是否启用（有玩家在PVP，否为false，是为true） 阶段（0为前三秒的准备，1为对打中） 玩家1（String形式）
     * 玩家2（String形式） 观战者（List<String>形式） Timer（独立的定时器，计时） 时刻（进行的秒数，从-2开始） 赛前倒计时
     * 玩家1的总伤害/最大伤害 玩家2的总伤害/最大伤害 玩家1积累的经验值 玩家2积累的经验值 玩家1原来的位置 玩家2原来的位置 玩家1原来的段位
     * 玩家2原来的段位 比赛开始的方式（1为匹配赛，2为邀请赛，3为强制赛）
     */
    private final String name;
    private String p1;
    private String p2;
    private List<String> watchers;

    private boolean enable = false;

    private BukkitTask timer;
    private int taskId;
    private int time;
    private int countDown;
    private int timeout;

    private int stage = -1;

    private double player1Damage;
    private double player1MaxDamage;
    private double player2Damage;
    private double player2MaxDamage;

    private int player1Exp;
    private int player2Exp;

    private int player1MultiHit;
    private int player2MultiHit;

    private Location player1Location;
    private Location player2Location;

    private CustomDan player1Dan;
    private CustomDan player2Dan;

    private List<ItemStack> player1InventoryItems;
    private List<ItemStack> player2InventoryItems;
    private ItemStack player1Helmet;
    private ItemStack player2Helmet;
    private ItemStack player1ChestPlate;
    private ItemStack player2ChestPlate;
    private ItemStack player1Leggings;
    private ItemStack player2Leggings;
    private ItemStack player1Boots;
    private ItemStack player2Boots;
    private ItemStack player1OffHandItem;
    private ItemStack player2OffHandItem;

    private boolean canTeleport;
    private boolean watchersTeleport;

    private int startWay;

    public Arena(String name) { // 加载
        this.name = name;
    }

    // 加载初始信息
    public void start(String p1, String p2, int startWay) {
        this.p1 = p1;
        this.p2 = p2;
        this.startWay = startWay;
        watchers = new ArrayList<>();
        canTeleport = false;
        watchersTeleport = false;
        enable = true;
        time = getCountDown();
        ConfigManager configManager = Main.getInstance().getConfigManager();
        timeout = configManager.getGameTimeOut();
        stage = 0;
        Player player1 = Bukkit.getPlayerExact(p1);
        Player player2 = Bukkit.getPlayerExact(p2);
        sm("&7比赛即将开始..", player1, player2);

        ArenaInfo arenaInfo = Main.getInstance().getCacheHandler()
                .getArenaInfo().get(name);
        if (arenaInfo.isKitMode()) {
            Inventory player1Inventory = player1.getInventory();
            Inventory player2Inventory = player2.getInventory();
            ItemStack air = new ItemStack(Material.AIR, 1);
            player1InventoryItems = new ArrayList<>();
            player2InventoryItems = new ArrayList<>();
            for (int i = 0; i < 36; i++) {
                if (player1Inventory.getItem(i) != null) {
                    player1InventoryItems.add(player1Inventory.getItem(i));
                } else {
                    player1InventoryItems.add(air);
                }
                if (player2Inventory.getItem(i) != null) {
                    player2InventoryItems.add(player2Inventory.getItem(i));
                } else {
                    player2InventoryItems.add(air);
                }
            }
            if (player1Inventory.getItem(36) != null) {
                player1Helmet = player1Inventory.getItem(36);
            } else {
                player1Helmet = air;
            }
            if (player2Inventory.getItem(36) != null) {
                player2Helmet = player2Inventory.getItem(36);
            } else {
                player2Helmet = air;
            }
            if (player1Inventory.getItem(37) != null) {
                player1ChestPlate = player1Inventory.getItem(37);
            } else {
                player1ChestPlate = air;
            }
            if (player2Inventory.getItem(37) != null) {
                player2ChestPlate = player2Inventory.getItem(37);
            } else {
                player2ChestPlate = air;
            }
            if (player1Inventory.getItem(38) != null) {
                player1Leggings = player1Inventory.getItem(38);
            } else {
                player1Leggings = air;
            }
            if (player2Inventory.getItem(38) != null) {
                player2Leggings = player2Inventory.getItem(38);
            } else {
                player2Leggings = air;
            }
            if (player1Inventory.getItem(39) != null) {
                player1Boots = player1Inventory.getItem(39);
            } else {
                player1Boots = air;
            }
            if (player2Inventory.getItem(39) != null) {
                player2Boots = player2Inventory.getItem(39);
            } else {
                player2Boots = air;
            }
            if (ViaVersion.isHasOffHandMethod()) {
                player1OffHandItem = ViaVersion.getItemInOffHand(player1);
                player2OffHandItem = ViaVersion.getItemInOffHand(player2);
            }

            ItemTaker.cleanInventory(player1);
            ItemTaker.cleanInventory(player2);

            if (arenaInfo.getKit() != null) { //检测kit中是否有物品
                for (ItemStack kitItem : arenaInfo.getKit()) {
                    Material material = kitItem.getType();
                    if (ViaVersion.isHelmet(material)) {
                        if (player1.getInventory().getItem(39) == null) {
                            player1.getInventory().setItem(39, kitItem);
                            player2.getInventory().setItem(39, kitItem);
                            continue;
                        }
                    }
                    if (ViaVersion.isChestPlate(material)) {
                        if (player1.getInventory().getItem(38) == null) {
                            player1.getInventory().setItem(38, kitItem);
                            player2.getInventory().setItem(38, kitItem);
                            continue;
                        }
                    }
                    if (ViaVersion.isLeggings(material)) {
                        if (player1.getInventory().getItem(37) == null) {
                            player1.getInventory().setItem(37, kitItem);
                            player2.getInventory().setItem(37, kitItem);
                            continue;
                        }
                    }
                    if (ViaVersion.isBoots(material)) {
                        if (player1.getInventory().getItem(36) == null) {
                            player1.getInventory().setItem(36, kitItem);
                            player2.getInventory().setItem(36, kitItem);
                            continue;
                        }
                    }
                    if (Material.getMaterial("SHIELD") != null) {
                        if (material.toString().equals("SHIELD")) {
                            ViaVersion.setItemInOffHand(player1, new ItemStack(Material.getMaterial("SHIELD")));
                            ViaVersion.setItemInOffHand(player2, new ItemStack(Material.getMaterial("SHIELD")));
                            continue;
                        }
                    }
                    player1.getInventory().addItem(kitItem);
                    player2.getInventory().addItem(kitItem);
                }
            }
            //清除药水效果
            for (PotionEffect potionEffect : player1.getActivePotionEffects()) {
                player1.removePotionEffect(potionEffect.getType());
            }
            for (PotionEffect potionEffect : player2.getActivePotionEffects()) {
                player2.removePotionEffect(potionEffect.getType());
            }
        }

        timer = new BukkitRunnable() {
            @Override
            public void run() {
                Player player1 = Bukkit.getPlayerExact(p1);
                Player player2 = Bukkit.getPlayerExact(p2);
                time = time + 1;

                if (time >= 0) {
                    if (time == 0) {
                        stage = 1; // 阶段变更：战前准备->战斗状态
                        player1.playSound(player1.getLocation(), ViaVersion
                                        .getSound("ENTITY_PLAYER_LEVELUP", "LEVELUP"),
                                1, 0);
                        player2.playSound(player2.getLocation(), ViaVersion
                                        .getSound("ENTITY_PLAYER_LEVELUP", "LEVELUP"),
                                1, 0);
                        sm("&a比赛开始！！亮剑吧！", player1, player2);
                        ViaVersion.sendTitle(player1, gm("&a&l战斗开始", player1),
                                gm("&2比赛将在 &a&l{time} &2秒后结束，请尽快击败对手！", player1, "time", new String[]{"" + timeout}), 20, 40,
                                10);
                        ViaVersion.sendTitle(player2, gm("&a&l战斗开始", player2),
                                gm("&2比赛将在 &a&l{time} &2秒后结束，请尽快击败对手！", player2, "time", new String[]{"" + timeout}), 20, 40,
                                10);
                        ArenaCommands.executeArenaCommands(name, player1,
                                player2);
                    }
                    ViaVersion.sendActionBar(player1, gm("&b比赛时间剩余 &6{second} &b秒", player1, "second", new String[]{"" + (timeout - time)}));
                    ViaVersion.sendActionBar(player2, gm("&b比赛时间剩余 &6{second} &b秒", player1, "second", new String[]{"" + (timeout - time)}));
                    if (time >= timeout) {
                        if (configManager.isTimeOutHandlingSchemeEnabled()) {
                            ConfigManager.TimeOutHandlingScheme scheme = configManager.getTimeOutHandlingScheme();
                            if (scheme.equals(ConfigManager.TimeOutHandlingScheme.COMPARE_HEALTH_VALUE)) {
                                double player1HealthValue = BigDecimal.valueOf(player1.getHealth()).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                                double player2HealthValue = BigDecimal.valueOf(player2.getHealth()).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                                if (player1HealthValue != player2HealthValue) {
                                    if (player1HealthValue > player2HealthValue) {
                                        sm("&3比赛超时，玩家 &b{winner} &3的血量为 &b{winner_health_value} &3点，多于玩家 &b{loser} &3的 &b{loser_health_value} &3点。血量多的一方判为胜者", player1, "winner winner_health_value loser loser_health_value", new String[]{p1, "" + player1HealthValue, p2, "" + player2HealthValue});
                                        sm("&3比赛超时，玩家 &b{winner} &3的血量为 &b{winner_health_value} &3点，多于玩家 &b{loser} &3的 &b{loser_health_value} &3点。血量多的一方判为胜者", player2, "winner winner_health_value loser loser_health_value", new String[]{p1, "" + player1HealthValue, p2, "" + player2HealthValue});
                                        Bukkit.getScheduler().runTask(Main.getInstance(), () -> FinishGame.normalEnd(name, p1, p2, false));
                                    } else {
                                        sm("&3比赛超时，玩家 &b{winner} &3的血量为 &b{winner_health_value} &3点，多于玩家 &b{loser} &3的 &b{loser_health_value} &3点。血量多的一方判为胜者", player1, "winner winner_health_value loser loser_health_value", new String[]{p2, "" + player2HealthValue, p1, "" + player1HealthValue});
                                        sm("&3比赛超时，玩家 &b{winner} &3的血量为 &b{winner_health_value} &3点，多于玩家 &b{loser} &3的 &b{loser_health_value} &3点。血量多的一方判为胜者", player2, "winner winner_health_value loser loser_health_value", new String[]{p2, "" + player2HealthValue, p1, "" + player1HealthValue});
                                        Bukkit.getScheduler().runTask(Main.getInstance(), () -> FinishGame.normalEnd(name, p2, p1, false));
                                    }
                                } else {
                                    sm("&3比赛超时，双方血量值相当，判为平局！", player1, player2);
                                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> FinishGame.normalEnd(name, p1, p2, true));
                                }
                            }
                            if (scheme.equals(ConfigManager.TimeOutHandlingScheme.COMPARE_HEALTH_PERCENTAGE)) {
                                double player1HealthPercentage = player1.getHealth() / player1.getMaxHealth();
                                double player2HealthPercentage = player2.getHealth() / player2.getMaxHealth();
                                if (player1HealthPercentage != player2HealthPercentage) {
                                    double player1HealthPercentageShowed = BigDecimal.valueOf(player1HealthPercentage * 100).setScale(2, BigDecimal.ROUND_UP).doubleValue();
                                    double player2HealthPercentageShowed = BigDecimal.valueOf(player2HealthPercentage * 100).setScale(2, BigDecimal.ROUND_UP).doubleValue();
                                    if (player1HealthPercentage > player2HealthPercentage) {
                                        sm("&3比赛超时，玩家 &b{winner} &3的血量百分比为 &b{winner_health_percentage}% &3，高于玩家 &b{loser} &3的 &b{loser_health_percentage}% &3。血量百分比高的一方判为胜者", player1, "winner winner_health_percentage loser loser_health_percentage", new String[]{p1, "" + player1HealthPercentageShowed, p2, "" + player2HealthPercentageShowed});
                                        sm("&3比赛超时，玩家 &b{winner} &3的血量百分比为 &b{winner_health_percentage}% &3，高于玩家 &b{loser} &3的 &b{loser_health_percentage}% &3。血量百分比高的一方判为胜者", player2, "winner winner_health_percentage loser loser_health_percentage", new String[]{p1, "" + player1HealthPercentageShowed, p2, "" + player2HealthPercentageShowed});
                                        Bukkit.getScheduler().runTask(Main.getInstance(), () -> FinishGame.normalEnd(name, p1, p2, false));
                                    } else {
                                        sm("&3比赛超时，玩家 &b{winner} &3的血量百分比为 &b{winner_health_percentage}% &3，高于玩家 &b{loser} &3的 &b{loser_health_percentage}% &3。血量百分比高的一方判为胜者", player1, "winner winner_health_percentage loser loser_health_percentage", new String[]{p2, "" + player2HealthPercentageShowed, p1, "" + player1HealthPercentageShowed});
                                        sm("&3比赛超时，玩家 &b{winner} &3的血量百分比为 &b{winner_health_percentage}% &3，高于玩家 &b{loser} &3的 &b{loser_health_percentage}% &3。血量百分比高的一方判为胜者", player2, "winner winner_health_percentage loser loser_health_percentage", new String[]{p2, "" + player2HealthPercentageShowed, p1, "" + player1HealthPercentageShowed});
                                        Bukkit.getScheduler().runTask(Main.getInstance(), () -> FinishGame.normalEnd(name, p2, p1, false));
                                    }
                                } else {
                                    sm("&3比赛超时，双方血量百分比相当，判为平局！", player1, player2);
                                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> FinishGame.normalEnd(name, p1, p2, true));
                                }
                            }
                        } else {
                            Bukkit.getScheduler().runTask(Main.getInstance(), () -> FinishGame.normalEnd(name, p1, p2, true));
                        }
                        cancel();
                    }
                } else {
                    sm("&7比赛开始倒计时 &b{time}s", player1, "time",
                            new String[]{"" + (-time)});
                    sm("&7比赛开始倒计时 &b{time}s", player2, "time",
                            new String[]{"" + (-time)});
                    player1.playSound(
                            player1.getLocation(),
                            ViaVersion.getSound("BLOCK_NOTE_BASS", "NOTE_BASS"),
                            1, 0);
                    player2.playSound(
                            player2.getLocation(),
                            ViaVersion.getSound("BLOCK_NOTE_BASS", "NOTE_BASS"),
                            1, 0);

                    String p1SubTitle, p2SubTitle;
                    if (time < -3) {
                        p1SubTitle = gm("&f你将与玩家 &7&l{opponent} &f进行对战！",
                                player1, "opponent",
                                new String[]{player2.getName()});
                        p2SubTitle = gm("&f你将与玩家 &7&l{opponent} &f进行对战！",
                                player2, "opponent",
                                new String[]{player1.getName()});
                    } else {
                        p1SubTitle = gm("&3战斗即将开始...", player1);
                        p2SubTitle = gm("&3战斗即将开始...", player2);
                    }

                    ViaVersion.sendTitle(player1, "§b§l" + (-time),
                            p1SubTitle, 0, 20, 20);
                    ViaVersion.sendTitle(player2, "§b§l" + (-time),
                            p2SubTitle, 0, 20, 20);
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 20, 20);
        this.taskId = timer.getTaskId();
    }

    public void finish() {// 结束这个竞技场的比赛
        try {
            if (!timer.isCancelled()) {
                timer.cancel();
            }
        } catch (NoSuchMethodError error) {
            if (Bukkit.getScheduler().isCurrentlyRunning(taskId)) {
                timer.cancel();
            }
        }
        enable = false;
        p1 = null;
        p2 = null;
        watchers = null;
        player1Damage = 0;
        player1MaxDamage = 0;
        player2Damage = 0;
        player2MaxDamage = 0;
        player1Exp = 0;
        player2Exp = 0;
        player1MultiHit = 0;
        player2MultiHit = 0;
        player1Location = null;
        player2Location = null;
        stage = -1;
        watchersTeleport = false;
    }

    public String getName() {
        return name;
    }

    public String getp1() {
        return p1;
    }

    public String getp2() {
        return p2;
    }

    public int getStartWay() {
        return startWay;
    }

    public List<String> getWatchers() {
        return watchers;
    }

    public void removeWatcher(String watcher) {
        if (!watchers.contains(watcher)) {
            return;
        }
        watchers.remove(watcher);
    }

    public void addWatcher(String watcher) {
        if (watchers.contains(watcher)) {
            return;
        }
        watchers.add(watcher);
    }

    public String getTheOtherPlayer(String pn) {
        String theother = null;
        if (pn.equals(p1)) {
            theother = p2;
        }
        if (pn.equals(p2)) {
            theother = p1;
        }
        return theother;
    }

    public boolean isp1(String pn) {
        return p1.equals(pn);
    }

    public int getStage() {
        return stage;
    }

    public int getTime() {
        return time;
    }

    public int getCountDown() {
        if (countDown == 0) {
            return -6;
        } else {
            return -countDown;
        }
    }

    public void setCountDown(int countDown) {
        this.countDown = countDown;
    }

    public boolean getEnable() {
        return enable;
    }

    public double getDamage(boolean isp1) {
        if (isp1) {
            return player1Damage;
        } else {
            return player2Damage;
        }
    }

    public void addDamage(boolean isp1, double d) {
        if (isp1) {
            player1Damage = player1Damage + d;
        } else {
            player2Damage = player2Damage + d;
        }
    }

    public double getMaxDamage(boolean isp1) {
        if (isp1) {
            return player1MaxDamage;
        } else {
            return player2MaxDamage;
        }
    }

    public void setMaxDamage(boolean isp1, double d) {
        if (isp1) {
            player1MaxDamage = d;
        } else {
            player2MaxDamage = d;
        }
    }

    public int getExp(boolean isp1) {
        if (isp1) {
            return player1Exp;
        } else {
            return player2Exp;
        }
    }

    public void addExp(boolean isp1, int i) {
        if (isp1) {
            player1Exp = player1Exp + i;
        } else {
            player2Exp = player2Exp + i;
        }
    }

    public int getHit(boolean isp1) {
        if (isp1) {
            return player1MultiHit;
        } else {
            return player2MultiHit;
        }
    }

    public void addHit(boolean isp1) {
        if (isp1) {
            player1MultiHit = player1MultiHit + 1;
            player2MultiHit = 0;
        } else {
            player2MultiHit = player2MultiHit + 1;
            player1MultiHit = 0;
        }
    }

    public void setLocation(Player p1, Player p2) {
        if (p1 == null || p2 == null) {
            return;
        }
        player1Location = p1.getLocation();
        player2Location = p2.getLocation();
    }

    public Location getLocation(boolean isp1) {
        if (isp1) {
            return player1Location;
        } else {
            return player2Location;
        }
    }

    public CustomDan getDan(boolean isp1) {
        if (isp1) {
            return player1Dan;
        } else {
            return player2Dan;
        }
    }

    protected void setDan(boolean isp1, CustomDan dan) {
        if (isp1) {
            player1Dan = dan;
        } else {
            player2Dan = dan;
        }
    }

    public List<ItemStack> getPlayerInventoryItems(boolean isp1) {
        if (isp1) {
            return player1InventoryItems;
        } else {
            return player2InventoryItems;
        }
    }

    public ItemStack getPlayerHelmet(boolean isp1) {
        if (isp1) {
            return player1Helmet;
        } else {
            return player2Helmet;
        }
    }

    public ItemStack getPlayerChestPlate(boolean isp1) {
        if (isp1) {
            return player1ChestPlate;
        } else {
            return player2ChestPlate;
        }
    }

    public ItemStack getPlayerLeggings(boolean isp1) {
        if (isp1) {
            return player1Leggings;
        } else {
            return player2Leggings;
        }
    }

    public ItemStack getPlayerBoots(boolean isp1) {
        if (isp1) {
            return player1Boots;
        } else {
            return player2Boots;
        }
    }

    public ItemStack getPlayerOffHandItem(boolean isp1) {
        if (isp1) {
            return player1OffHandItem;
        } else {
            return player2OffHandItem;
        }
    }

    public boolean canTeleport() {
        return canTeleport;
    }

    protected void setCanTeleport(boolean canTeleport) {
        this.canTeleport = canTeleport;
    }

    public boolean getWatchersTeleport() {
        return watchersTeleport;
    }

    protected void setWatchersTeleport(boolean watchersTeleport) {
        this.watchersTeleport = watchersTeleport;
    }

    public int getTimeout() {
        return timeout;
    }
}
