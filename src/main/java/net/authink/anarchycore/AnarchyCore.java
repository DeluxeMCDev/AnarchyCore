package net.authink.anarchycore;

import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class AnarchyCore extends JavaPlugin implements Listener {




    // MODIFICATIONS
    String tab_header = "&6DeluxeMC";
    String tab_email = "admin@deluxemc.net";











    // DON'T TOUCH IF YOU DO NOT UNDERSTAND !!!
    private final HashMap<UUID, UUID> lastMessaged = new HashMap<>();
    private final HashMap<UUID, UUID> lastMessager = new HashMap<>();
    private Instant startTime;
    @Override
    public void onEnable() {
        startTime = Instant.now();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().warning("This plugin is unreliable and should only be used on an anarchy server. Proceed with caution...");

        String[] commands = {"msg", "whisper", "pm", "w", "r", "reply", "l", "last", "kill", "help"};
        for (String command : commands) {
            this.getCommand(command).setExecutor(this);
        }


        Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> {
            for (Player p : getServer().getOnlinePlayers()) {
                updateTabForPlayer(p, getServer().getTPS()[0], p.getPing());
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void AttributeChangeJoin(PlayerJoinEvent event) {
        event.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1024);
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateTabForPlayer(event.getPlayer(), getServer().getTPS()[0], event.getPlayer().getPing());
    }

    private void updateTabForPlayer(org.bukkit.entity.Player player, double tps, int ping) {
        String header = ChatColor.translateAlternateColorCodes('&', tab_header+"\n");
        String tpsColor;
        String pingColor;

        if (tps < 15.0) {
            tpsColor = ChatColor.RED + String.format("%.2f", tps);
        } else if (tps < 18.0) {
            tpsColor = ChatColor.YELLOW + String.format("%.2f", tps);
        } else {
            tpsColor = ChatColor.GREEN + String.format("%.2f", tps);
        }

        if (ping < 100) {
            pingColor = ChatColor.GREEN + String.valueOf(ping);
        } else if (ping < 200) {
            pingColor = ChatColor.YELLOW + String.valueOf(ping);
        } else {
            pingColor = ChatColor.RED + String.valueOf(ping);
        }

        String uptime = parseUnix(Instant.now().getEpochSecond() - startTime.getEpochSecond());
        String footer = ChatColor.translateAlternateColorCodes('&', "&cTPS&f: " + tpsColor + " &8| &cOnline Players&f: &6" + getServer().getOnlinePlayers().size() + " &8| &cUptime&f: &a" + uptime + " &8| &cPing&f: " + pingColor + "ms\n&7Check out our github: https://github.com/DeluxeMCDev\n&7Contact us at " + tab_email);
        player.setPlayerListHeaderFooter(header, footer);
    }

    private String parseUnix(long seconds) {
        long day = TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
        long second = seconds - (TimeUnit.SECONDS.toMinutes(seconds) * 60);
        return day + "d " + hours + "h " + minute + "m " + second + "s";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();
        switch (command.getName().toLowerCase()) {
            case "msg":
            case "whisper":
            case "pm":
            case "w":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /" + label + " <player> <message>");
                    return true;
                }
                return handleMessaging(player, command, args);

            case "r":
            case "reply":
                if (args.length < 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /" + label + " <message>");
                    return true;
                }
                return handleDirectMessage(player, "reply", args);

            case "l":
            case "last":
                if (args.length < 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /" + label + " <message>");
                    return true;
                }
                return handleDirectMessage(player, "last", args);

            case "kill":
                player.setHealth(0);
                player.sendMessage(ChatColor.RED + "You have been killed.");
                return true;

            case "help":
                player.sendMessage(ChatColor.GRAY + "---------------------------------------------");
                player.sendMessage(ChatColor.DARK_AQUA + "/msg <player> <message> - send a private message to a player");
                player.sendMessage(ChatColor.DARK_AQUA + "/whisper <player> <message> - alias of /msg");
                player.sendMessage(ChatColor.DARK_AQUA + "/pm <player> <message> - alias of /msg");
                player.sendMessage(ChatColor.DARK_AQUA + "/w <player> <message> - alias of /msg");
                player.sendMessage(ChatColor.DARK_AQUA + "/reply <message> - reply to the last private message received");
                player.sendMessage(ChatColor.DARK_AQUA + "/r <message> - alias of /reply");
                player.sendMessage(ChatColor.DARK_AQUA + "/last - alias of /reply");
                player.sendMessage(ChatColor.DARK_AQUA + "/kill - die");
                player.sendMessage(ChatColor.DARK_AQUA + "/help - display available commands and their usage");
                player.sendMessage(ChatColor.GRAY + "---------------------------------------------");

                return true;

            default:
                return false;
        }
    }

    @EventHandler
    public void chatColorEvent(AsyncPlayerChatEvent e) {
        if (e.getMessage().startsWith(">")) {
            e.setMessage(ChatColor.GREEN + e.getMessage());
        }

    }

    private boolean handleDirectMessage(Player player, String type, String[] args) {
        UUID targetId = null;
        if (type.equals("reply")) {
            targetId = lastMessager.get(player.getUniqueId());
        } else if (type.equals("last")) {
            targetId = lastMessaged.get(player.getUniqueId());
        }

        if (targetId == null) {
            player.sendMessage(ChatColor.RED + "No recent contacts found.");
            return true;
        }

        Player target = Bukkit.getPlayer(targetId);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found or is offline.");
            return true;
        }

        String message = String.join(" ", args);
        target.sendMessage(ChatColor.GRAY + "From " + player.getName() + ": " + message);
        player.sendMessage(ChatColor.GRAY + "To " + target.getName() + ": " + message);

        lastMessaged.put(player.getUniqueId(), target.getUniqueId());
        lastMessager.put(target.getUniqueId(), player.getUniqueId());
        return true;
    }

    private boolean handleMessaging(Player player, Command command, String[] args) {
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        String message = String.join(" ", args).substring(args[0].length()).trim();
        target.sendMessage(ChatColor.GRAY + "From " + player.getName() + ": " + message);
        player.sendMessage(ChatColor.GRAY + "To " + target.getName() + ": " + message);
        lastMessaged.put(player.getUniqueId(), target.getUniqueId());
        lastMessager.put(target.getUniqueId(), player.getUniqueId());
        return true;
    }
}