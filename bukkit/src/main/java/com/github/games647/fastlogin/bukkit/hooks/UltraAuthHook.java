package com.github.games647.fastlogin.bukkit.hooks;

import com.github.games647.fastlogin.core.hooks.AuthPlugin;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import ultraauth.api.UltraAuthAPI;
import ultraauth.main.Main;
import ultraauth.managers.PlayerManager;

/**
 * Project page:
 *
 * Bukkit: http://dev.bukkit.org/bukkit-plugins/ultraauth-aa/
 *
 * Spigot: https://www.spigotmc.org/resources/ultraauth.17044/
 */
public class UltraAuthHook implements AuthPlugin<Player> {

    private final Plugin ultraAuthPlugin = Main.main;

    @Override
    public boolean forceLogin(Player player) {
        //not thread-safe
        Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(ultraAuthPlugin, () -> {
            if (UltraAuthAPI.isAuthenticated(player)) {
                return true;
            }

            UltraAuthAPI.authenticatedPlayer(player);
            return UltraAuthAPI.isAuthenticated(player);
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException ex) {
            ultraAuthPlugin.getLogger().log(Level.SEVERE, "Failed to forceLogin", ex);
            return false;
        }
    }

    @Override
    public boolean isRegistered(String playerName) throws Exception {
        return UltraAuthAPI.isRegisterd(playerName);
    }

    @Override
    public boolean forceRegister(Player player, String password) {
        UltraAuthAPI.setPlayerPasswordOnline(player, password);
        if (PlayerManager.getInstance().checkPlayerPassword(player, password)) {
            //the register method silents any excpetion so check if our entry was saved
            return forceLogin(player);
        }

        return false;
    }
}
