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

import de.static_interface.sinkirc.IrcUtil;
import de.static_interface.sinkirc.SinkIRC;
import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.api.command.SinkCommand;
import de.static_interface.sinklibrary.api.command.annotation.Aliases;
import de.static_interface.sinklibrary.api.command.annotation.DefaultPermission;
import de.static_interface.sinklibrary.api.command.annotation.Description;
import de.static_interface.sinklibrary.api.command.annotation.Usage;
import de.static_interface.sinklibrary.api.configuration.Configuration;
import de.static_interface.sinklibrary.api.user.SinkUser;
import de.static_interface.sinklibrary.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.pircbotx.Channel;

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
        getCommandOptions().setMinRequiredArgs(1);
        getCommandOptions().setIrcOpOnly(true);
    }

    @Override
    public boolean onExecute(CommandSender sender, String label, String[] args) {
    	if(args.length < 1) {
    		return false;
    	}
    	
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
		
        SinkUser user = SinkLibrary.getInstance().getUser((Object) sender);

        String target = args[0];

        for (Channel channel : SinkIRC.getInstance().getJoinedChannels()) {
            channel.send().kick(IrcUtil.getUser(target), finishMsgWithArgs);
        }
        return true;
    }
}
