package com.hyt.andan.KillXp;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameOverEvent;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.events.BedwarsPlayerJoinedEvent;
import io.github.bedwarsrel.game.Game;
import ldcr.BedwarsXP.api.XPManager;
import ldcr.BedwarsXP.utils.SoundMachine;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;

public class KillXp extends JavaPlugin implements Listener {

    public ArrayList<Player> playerEnabledList = new ArrayList<>();
    public String bwPrefix;

    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage("§b§l[KillXp] §a§l插件加载成功! 作者: 暗淡, 二改: https://github.com/LinMoyuu/KillXp");
        ConfigLoad();
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginCommand("killxpreload").setExecutor(this);
        Bukkit.getPluginCommand("kp").setTabCompleter(this);

        bwPrefix = BedwarsRel.getInstance().getConfig().getString("chat-prefix").replaceAll("&", "§");
    }

    public void ConfigLoad() {
        File f = new File(getDataFolder() + "/config.yml");
        if (f.exists()) {
            getServer().getConsoleSender().sendMessage("§b§l[KillXp] §a§l检测到config.yml,开始加载配置!");
        } else {
            getServer().getConsoleSender().sendMessage("§b§l[KillXp] §c§l未检测到config.yml,正在创建默认配置文件!");
            saveDefaultConfig();
        }
        reloadConfig();
    }

    @EventHandler
    public void onPlayerJoin(BedwarsPlayerJoinedEvent event) {
        playerEnabledList.add(event.getPlayer());
    }

    @EventHandler
    public void onGameStart(BedwarsGameStartEvent event) {
        Game game = event.getGame();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Player player : game.getPlayers()) {
                player.sendMessage(bwPrefix + " §a§l掠夺经验提示已自动开启，§e/kp§a可以关闭或者开启此功能!");
            }
        }, 2 * 20L);
    }

    @EventHandler
    public void onGameOver(BedwarsGameOverEvent event) {
        Game game = event.getGame();
        for (Player player : game.getPlayers()) {
            playerEnabledList.remove(player);
        }
    }

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Game bw = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (bw == null) {
            return;
        }
        if (e.getEntity().getKiller() == null) return;
        Player killer = e.getEntity().getKiller();
        String playerName = player.getName();
        String killerName = killer.getName();
        int bili = getConfig().getInt("bili");
        int golbal = getConfig().getInt("golbal");
        XPManager xpman = XPManager.getXPManager(bw.getName());
        int count = (xpman.getXP(player) * bili / 100);
        boolean giveKillerXP = getConfig().getBoolean("giveKillerXP");
        if (giveKillerXP) {
            xpman.setXP(killer, xpman.getXP(killer) + count);
        }
        if (count >= golbal) {
            String message = bwPrefix + " §e" + killerName + "§f击杀§e" + playerName + "§f并无情的掠夺了§b" + count + "§f经验!!";
            for (Player gamePlayer : bw.getPlayers()) {
                gamePlayer.sendMessage(message);
            }
        }
        killer.playSound(killer.getLocation(), SoundMachine.get("ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP"), 10.0F, 1.0F);
        if (!playerEnabledList.contains(killer) || count == 0) return;
        TitleAPI.sendTitle(killer, 0, 20, 10, "", "经验§f+§a" + count);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equals("killxpreload") && (sender.hasPermission("killxp.reload") || sender.isOp())) {
            this.reloadConfig();
            sender.sendMessage("§b§lKillXp重载成功!");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能使用此命令!");
            return true;
        }
        Player player = (Player) sender;
        if (label.equals("kp")) {
            boolean isEnabled = playerEnabledList.contains(player);
            if (isEnabled) {
                playerEnabledList.remove(player);
            } else {
                playerEnabledList.add(player);
            }
            sender.sendMessage(bwPrefix + " §a成功" + (isEnabled ? "关闭" : "开启") + "击杀Title，再次输入§e/kp§a可以" + (isEnabled ? "开启" : "关闭") + "。");
            return true;
        }
        return false;
    }
}

