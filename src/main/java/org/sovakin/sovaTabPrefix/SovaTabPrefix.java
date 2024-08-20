package org.sovakin.sovaTabPrefix;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import me.clip.placeholderapi.PlaceholderAPI;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SovaTabPrefix extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getMessenger().registerIncomingPluginChannel(this, "sovatab:main", (channel, player, message) -> {
            handleClientMessage(player, message);
        });

        getServer().getMessenger().registerOutgoingPluginChannel(this, "sovatab:main");

        new BukkitRunnable() {
            @Override
            public void run() {
                updatePrefixes();
            }
        }.runTaskTimer(this, 0L, 20L * 5);
    }

    private void handleClientMessage(Player player, byte[] message) {
        getLogger().info("Received message from client: " + player.getName());
        updatePrefixes();
    }

    private void updatePrefixes() {
        List<String> playerNames = new ArrayList<>();
        List<String> prefixes = new ArrayList<>();

        for (Player player : getServer().getOnlinePlayers()) {
            String prefix = PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix%");
            playerNames.add(player.getName());
            prefixes.add(prefix);
            getLogger().info("Player: " + player.getName() + ", Prefix: " + prefix);
        }

        byte[] packet = constructPrefixPacket(playerNames, prefixes);

        for (Player player : getServer().getOnlinePlayers()) {
            player.sendPluginMessage(this, "sovatab:main", packet);
            getLogger().info("Sent prefix data to " + player.getName());
        }
    }

    private byte[] constructPrefixPacket(List<String> playerNames, List<String> prefixes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            dos.writeInt(playerNames.size());
            for (int i = 0; i < playerNames.size(); i++) {
                dos.writeUTF(playerNames.get(i));
                dos.writeUTF(prefixes.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}