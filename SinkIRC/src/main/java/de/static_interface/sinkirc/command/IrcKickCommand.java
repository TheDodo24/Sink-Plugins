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

package de.static_interface.sinkirc.command;


import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.pircbotx.Channel;

import de.static_interface.sinkirc.IrcUtil;
import de.static_interface.sinkirc.SinkIRC;
import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.api.command.SinkCommand;
import de.static_interface.sinklibrary.api.command.annotation.Aliases;
import de.static_interface.sinklibrary.api.command.annotation.DefaultPermission;
import de.static_interface.sinklibrary.api.command.annotation.Description;
import de.static_interface.sinklibrary.api.command.annotation.Usage;
import de.static_interface.sinklibrary.api.configuration.Configuration;
import de.static_interface.sinklibrary.api.sender.IrcCommandSender;
import de.static_interface.sinklibrary.api.user.SinkUser;

@DefaultPermission
@Description("Kicks an user from IRC")
@Aliases("ikick")
@Usage("<player> <reason>")
public class IrcKickCommand extends SinkCommand {

    public IrcKickCommand(Plugin plugin, Configuration config) {
        super(plugin, config);
        getTabCompleterOptions().setIncludeIngameUsers(false);
        getTabCompleterOptions().setIncludeIrcUsers(true);
        getTabCompleterOptions().setIncludeSuffix(false);
    }

    @Override
    public boolean onExecute(CommandSender sender, String label, String[] args) {
    	if(args.length < 1) {
    		return false;
    	}
    	String text = "";
    	for(int i = 1; i < args.length; i++)  {
    		text = text + " " + args[i];
    	}
    	text = text.replaceFirst(" ", "");
		
        SinkUser user = SinkLibrary.getInstance().getUser((Object) sender);
        
        if(sender instanceof Player) {
        	Player p = (Player) sender;
        	if(p.hasPermission("sinkirc.kick")) {
        		String target = args[0];

                for (Channel channel : SinkIRC.getInstance().getJoinedChannels()) {
                    channel.send().kick(IrcUtil.getUser(target), text);
                }
                return true;
        	} else {
        		p.sendMessage("§4Fehler: §cDu hast keine Rechte!");
        		return false;
        	}
        } else if(sender instanceof IrcCommandSender) {
        	getCommandOptions().setIrcOpOnly(true);
        	String target = args[0];

            for (Channel channel : SinkIRC.getInstance().getJoinedChannels()) {
                channel.send().kick(IrcUtil.getUser(target), text);
            }
            return true;
        } else {
        	sender.sendMessage("§4Fehler: §cDu musst ein IRC-Mitglied oder ein Spieler sein!");
        	return false;
        }
    }
}
