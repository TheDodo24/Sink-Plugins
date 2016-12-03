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

package de.static_interface.sinkirc.irc_command;

import de.static_interface.sinklibrary.api.command.annotation.Description;
import de.static_interface.sinklibrary.api.command.annotation.Usage;
import de.static_interface.sinklibrary.api.configuration.Configuration;
import de.static_interface.sinklibrary.util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@Description("Kick a player ingame")
@Usage("<player> [reason]")
public class KickCommand extends IrcCommand {

    public KickCommand(Plugin plugin, Configuration config) {
        super(plugin, config);
        getCommandOptions().setIrcOpOnly(true);
    }

    @Override
    public boolean onExecute(CommandSender sender, String label, String[] args) {
        if(args.length < 1) {
        	return false;
        }
        Player toKick = Bukkit.getPlayer(args[0]);
        if(toKick != null){
        	String msgWithArgs = "";
        	int i = 0;
    		for(String arg : args) {
    			if(i == args.length) {
    				break;
    			}
    			i++;
    			if(msgWithArgs.isEmpty()) {
    				msgWithArgs = arg;
    				continue;
    			}
    			msgWithArgs = msgWithArgs + ' ' + arg;
    		}
    		final String finishMsgWithArgs = msgWithArgs;
    		toKick.kickPlayer("§cDu wurdest von §4" + sender.getName() + " §caus dem §eIRC §cgekickt!\n"
    				+ "§3Grund: §c" + finishMsgWithArgs);
        	return true;
        } else {
        	sender.sendMessage("Fehler: Der Spieler " + args[0] + " ist nicht online!");
        	return false;
        }
    }
}
