/*
 * Copyright (c) 2014 http://adventuria.eu, http://static-interface.de and contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.static_interface.sinklibrary;

import de.static_interface.sinklibrary.configuration.PlayerConfiguration;
import de.static_interface.sinklibrary.exceptions.ChatNotAvailableException;
import de.static_interface.sinklibrary.exceptions.EconomyNotAvailableException;
import de.static_interface.sinklibrary.exceptions.PermissionsNotAvailableException;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@SuppressWarnings("NewExceptionWithoutArguments")
public class User
{
    private static Player base = null;
    private static Economy econ = null;
    private String playerName = null;
    private CommandSender sender = null;
    private PlayerConfiguration config = null;
    private UUID uuid = null;

    /**
     * Get User instance by player's name
     * <p>
     * <b>Use {@link #User(java.util.UUID)} for offline players</b>
     *
     * @param sender Sender
     */
    User(CommandSender sender)
    {
        initUser(sender.getName());
    }

    public void initUser(String player)
    {
        if ( player.equalsIgnoreCase("CONSOLE") )
        {
            sender = Bukkit.getConsoleSender();
            base = null;
            econ = SinkLibrary.getEconomy();
            playerName = "Console";
            return;
        }
        base = BukkitUtil.getPlayer(player);
        econ = SinkLibrary.getEconomy();
        playerName = player;
        if ( base == null ) return;
        sender = base;
        uuid = base.getUniqueId();
    }

    /**
     * Get an user by UUID
     *
     * @param uuid UUID of user
     */
    User(UUID uuid)
    {
        this.uuid = uuid;
        initUser(Bukkit.getOfflinePlayer(uuid).getName());
    }

    /**
     * Get current money of player
     *
     * @return Money of player
     * @throws de.static_interface.sinklibrary.exceptions.EconomyNotAvailableException if economy is not available.
     */
    public double getMoney()
    {
        validateEconomy();

        OfflinePlayer player = base;

        String target = playerName;
        if ( target == null || target.isEmpty() )
        {
            if ( player == null )
            {
                player = Bukkit.getOfflinePlayer(uuid);
            }
            target = player.getName();
        }

        EconomyResponse response = econ.bankBalance(target); // Does this work when the player is offline?...
        return (int) response.balance;
    }

    /**
     * Allows to add or substract money from user.
     *
     * @param amount Amount to be added / substracted. May be negative for substracting
     * @return true if successful
     */
    public boolean addBalance(double amount)
    {
        if ( getName().isEmpty() ) return false;

        double roundedAmount = (int) Math.round(amount * 100) / (double) 100;
        validateEconomy();
        EconomyResponse response;
        if ( roundedAmount > 0 )
        {
            SinkLibrary.getCustomLogger().debug("econ.withDrawPlayer(" + getName() + ", " + -roundedAmount + ");");
            response = econ.withdrawPlayer(getName(), -roundedAmount);
        }
        else if ( roundedAmount < 0 )
        {
            SinkLibrary.getCustomLogger().debug("econ.depositPlayer(" + getName() + ", " + roundedAmount + ");");
            response = econ.depositPlayer(getName(), roundedAmount);
        }
        else
        {
            return true;
        }
        boolean result = response.transactionSuccess();
        SinkLibrary.getCustomLogger().debug("result = " + result);
        return response.transactionSuccess();
    }

    private void validateEconomy()
    {
        if ( isConsole() )
        {
            throw new NullPointerException("User is console, cannot get Player instance!");
        }

        if ( !SinkLibrary.isEconomyAvailable() )
        {
            throw new EconomyNotAvailableException();
        }

        assert econ != null;
        assert base != null;
    }

    /**
     * @return The PlayerConfiguration of the Player
     */
    public PlayerConfiguration getPlayerConfiguration()
    {
        if ( config == null )
        {
            config = new PlayerConfiguration(this);
            config.load();
        }
        return config;
    }

    /**
     * Get Player instance
     *
     * @return Player
     */
    public Player getPlayer()
    {
        if ( isConsole() )
        {
            throw new NullPointerException("User is console!");
        }
        return base;
    }

    /**
     * @param permission Permission required
     * @return True if the player has the permission specified by parameter.
     */
    public boolean hasPermission(String permission)
    {
        //Todo: fix this for offline usage
        if ( isConsole() )
        {
            return true;
        }

        if ( !isOnline() )
        {
            throw new RuntimeException("This may be only used for online players!");
        }
        //if (SinkLibrary.permissionsAvailable())
        //{
        //    return SinkLibrary.getPermissions().has(base, permission);
        //}
        //else
        //{
        return base.hasPermission(permission);
        //}
    }

    /**
     * Get user's primary group.
     *
     * @return Primary Group
     * @throws de.static_interface.sinklibrary.exceptions.PermissionsNotAvailableException if permissions are not available
     */
    public String getPrimaryGroup()
    {
        if ( isConsole() )
        {
            throw new NullPointerException("User is console!");
        }

        if ( !SinkLibrary.isPermissionsAvailable() )
        {
            throw new PermissionsNotAvailableException();
        }
        return SinkLibrary.getPermissions().getPrimaryGroup(base);
    }


    /**
     * @return Display Name with Permission Prefix or Op/non-Op Prefix
     */
    public String getDefaultDisplayName()
    {
        if ( isConsole() || !isOnline() )
        {
            return playerName;
        }
        try
        {
            if ( SinkLibrary.isChatAvailable() )
            {
                String playerPrefix = getPrefix();
                return playerPrefix + playerName + ChatColor.RESET;
            }
        }
        catch ( Exception ignored ) {}

        String prefix = base.isOp() ? ChatColor.RED.toString() : ChatColor.WHITE.toString();
        return prefix + playerName + ChatColor.RESET;
    }

    /**
     * Get Prefix
     *
     * @return Player prefix
     * @throws de.static_interface.sinklibrary.exceptions.ChatNotAvailableException if chat is not available
     */
    public String getPrefix()
    {
        if ( !SinkLibrary.isChatAvailable() )
        {
            throw new ChatNotAvailableException();
        }
        return ChatColor.translateAlternateColorCodes('&', SinkLibrary.getChat().getPlayerPrefix(base));
    }

    /**
     * Get players name (useful for offline player usage)
     *
     * @return Players name
     */
    public String getName()
    {
        return playerName;
    }

    /**
     * @return True if player is online and does not equals null
     */
    public boolean isOnline()
    {
        if ( isConsole() )
        {
            return true;
        }
        base = Bukkit.getPlayerExact(playerName);
        if ( base != null )
        {
            if ( base.isOnline() )
            {
                return true;
            }
        }
        return false;

    }

    /**
     * @return True if User is Console
     */
    public boolean isConsole()
    {
        return sender != null && (sender.equals(Bukkit.getConsoleSender()));
    }

    /**
     * @return If {@link org.bukkit.command.CommandSender CommandSnder} is instance of {@link org.bukkit.command.ConsoleCommandSender ConsoleCommandSender},
     * it will return "Console" in {@link org.bukkit.ChatColor#RED RED}, if sender is instance of
     * {@link org.bukkit.entity.Player Player}, it will return player's {@link org.bukkit.entity.Player#getDisplayName() DisplayName}
     */
    public String getDisplayName()
    {
        if ( isConsole() )
        {
            return ChatColor.RED + "Console" + ChatColor.RESET;
        }
        if ( !isOnline() )
        {
            return playerName;
        }
        if ( !SinkLibrary.getSettings().isDisplayNamesEnabled() || !getPlayerConfiguration().getHasDisplayName() )
        {
            String prefix = "";
            if ( SinkLibrary.isChatAvailable() )
            {
                prefix = ChatColor.translateAlternateColorCodes('&', SinkLibrary.getChat().getPlayerPrefix(base));
            }
            return prefix + base.getDisplayName();
        }
        else
        {
            return getPlayerConfiguration().getDisplayName();
        }
    }

    /**
     * Sends message to user if online
     *
     * @param message Message to be displayed
     */
    public void sendMessage(String message)
    {
        if ( isOnline() )
        {
            if ( isConsole() ) sender.sendMessage(message);
            else base.sendMessage(message);
        }
    }

    /**
     * @return The unique ID of the user
     */
    public UUID getUniqueId()
    {
        return uuid;
    }

    /**
     * Sends the message if debugging is enabled in the config
     *
     * @param message Message to be displayed
     */
    public void sendDebugMessage(String message)
    {
        if ( SinkLibrary.getSettings().isDebugEnabled() )
            sendMessage(ChatColor.GRAY + "[" + ChatColor.BLUE + "Debug" + ChatColor.GRAY + "] " + ChatColor.RESET + message);
    }
}
