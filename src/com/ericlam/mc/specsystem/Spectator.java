package com.ericlam.mc.specsystem;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

class Spectator {
    private GameMode oldGameMode;
    private ItemStack[] storgeContent;
    private ItemStack[] armorContent;
    private ItemStack offhand;
    private ItemStack[] extra;
    private Player player;

    Spectator(Player player) {
        this.player = player;
        this.oldGameMode = player.getGameMode();
        PlayerInventory playerInventory = player.getInventory();
        this.storgeContent = playerInventory.getStorageContents().clone();
        this.armorContent = playerInventory.getArmorContents();
        this.offhand = playerInventory.getItemInOffHand();
        this.extra = playerInventory.getExtraContents();
        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().clear();
        player.getInventory().addItem(SpectatingSystem.getInv());
    }

    ItemStack[] getArmorContent() {
        return armorContent;
    }

    ItemStack getOffhand() {
        return offhand;
    }

    ItemStack[] getExtra() {
        return extra;
    }

    GameMode getOldGameMode() {
        return oldGameMode;
    }

    ItemStack[] getStorgeContent() {
        return storgeContent;
    }

    Player getPlayer() {
        return player;
    }
}
