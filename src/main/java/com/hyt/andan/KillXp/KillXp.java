package com.hyt.andan.KillXp;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
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

public class KillXp extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage("§b§l[KillXp] §a§l插件加载成功!");
        ConfigLoad();
        Bukkit.getPluginManager().registerEvents(this, this);
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
    public void onGameStart(BedwarsGameStartEvent event) {
        Game game = event.getGame();
        for (Player player : game.getPlayers()) {
            player.sendMessage(BedwarsRel.getInstance().getConfig().getString("chat-prefix") + "");
        }

    }

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Game bw = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (bw == null) {
            return;
        }
        if (e.getEntity().getKiller() != null) {
            Player killer = e.getEntity().getKiller();
            String playerName = player.getName();
            String killerName = killer.getName();
            int bili = getConfig().getInt("bili");
            int golbal = getConfig().getInt("golbal");
            XPManager xpman = XPManager.getXPManager(bw.getName());
            int count = (xpman.getXP(player) * bili / 100);
            xpman.setXP(killer, xpman.getXP(killer) + count);
            if (count >= golbal) {
                for (Player gamePlayer : bw.getPlayers()) {
                    gamePlayer.sendMessage("§b起床战争§7>> §e" + killerName + "§f击杀§e" + playerName + "§f并无情的掠夺了§b" + count + "§f经验!!");
                }
            }
            killer.playSound(killer.getLocation(), SoundMachine.get("ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP"), 10.0F, 1.0F);
            TitleAPI.sendTitle(killer, 1, 2, 1, " ", "§b+" + count + "§a经验");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equals("killxpreload") && (sender.hasPermission("killxp.reload") || sender.isOp())) {
            this.reloadConfig();
            sender.sendMessage("§b§lKillXp重载成功!");
            return true;
        }
        if (label.equals("kp")) {
            this.reloadConfig();
            sender.sendMessage("§b§lKillXp重载成功!");
            return true;
        }
        return false;
    }
}

