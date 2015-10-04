/*
 * Copyright (c) 2013 - 2014 http://static-interface.de and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.static_interface.sinklibrary.util;

import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.api.annotation.Unstable;
import de.static_interface.sinklibrary.api.sender.IrcCommandSender;
import de.static_interface.sinklibrary.api.user.SinkUser;
import de.static_interface.sinklibrary.stream.BukkitBroadcastMessageStream;
import de.static_interface.sinklibrary.stream.BukkitBroadcastStream;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

public class BukkitUtil {

    /** Since Bukkit 1.7.9 R0.3, the return type of {@link org.bukkit.Bukkit#getOnlinePlayers()} changed, so
     *  plugins will possibly break when using this method on older bukkit versions
     *
     * @return The current online players
     * @deprecated Older Bukkit versions are not supported anymore
     */
    @Unstable
    @Deprecated
    public static List<Player> getOnlinePlayers() {
        return new CopyOnWriteArrayList<>(Bukkit.getOnlinePlayers());

        //List<Player> tmp = new CopyOnWriteArrayList<>();
        //try {
        //    if (Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).getReturnType() == Collection.class) // 1.7.9 R0.3 and newer
        //    {
        //        tmp = (List<Player>) Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null);
        //    } else {
        //        Collections.addAll(tmp, ((Player[]) Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null)));
        //    }
        //} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
        //}
        //return tmp;
    }

    public static List<Player> getOnlinePlayersForPlayer(Player p) {
        List<Player> players = new CopyOnWriteArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!p.canSee(player)) {
                players.remove(player);
            }
        }

        if(players.size() == 0 ){
            return new CopyOnWriteArrayList<>(Bukkit.getOnlinePlayers());
        }

        return players;
    }


    /**
     * Get online Player by name.
     *
     * @param name Name of the
     * @return If more than one matches, it will return the player with exact name.
     */
    @Nullable
    public static Player getPlayer(String name) {
        List<String> matchedPlayers = new ArrayList<>();
        for (Player player : BukkitUtil.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(name.toLowerCase())) {
                matchedPlayers.add(player.getName());
            }
        }

        Player exactPlayer = Bukkit.getPlayerExact(name);
        Collections.sort(matchedPlayers);

        if (exactPlayer != null) {
            return exactPlayer;
        } else {
            try {
                return Bukkit.getPlayer(matchedPlayers.get(0));
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    public static UUID getUniqueIdByName(String name) {
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }

    public static String getNameByUniqueId(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    /**
     * @param sender Command Sender
     * @return If {@link org.bukkit.command.CommandSender CommandSnder} is instance of {@link org.bukkit.command.ConsoleCommandSender ConsoleCommandSender},
     * it will return "Console" in {@link org.bukkit.ChatColor#RED RED}, if sender is instance of
     * {@link org.bukkit.entity.Player Player}, it will return player's {@link org.bukkit.entity.Player#getDisplayName() DisplayName}
     */
    public static String getSenderName(CommandSender sender) {
        if (sender instanceof IrcCommandSender) {
            ChatColor prefix = sender.isOp() ? ChatColor.DARK_RED : ChatColor.DARK_AQUA;
            return prefix + sender.getName() + ChatColor.RESET;
        }
        if (sender instanceof ConsoleCommandSender) {
            return ChatColor.DARK_RED + "Console" + ChatColor.RESET;
        }
        SinkUser user = SinkLibrary.getInstance().getUser(sender);
        return user.getDisplayName();
    }

    /**
     * @param message Message to send
     */
    public static void broadcastMessage(String message) {
        SinkLibrary.getInstance().getMessageStream("bukkit_broadcastmessage", BukkitBroadcastMessageStream.class).sendMessage(message);
    }

    /**
     * Use this instead of {@link org.bukkit.Bukkit#broadcast(String message, String permission).
     * Send message to all players with specified permission.
     *
     * @param message    Message to send
     * @param permission Permission needed to receive the message
     */
    public static void broadcast(String message, String permission) {
        SinkLibrary.getInstance().getMessageStream("bukkit_broadcast", BukkitBroadcastStream.class).sendMessage(null, message, permission);
    }

    public static World getMainWorld() {
        return Bukkit.getWorlds().get(0);
    }
}
