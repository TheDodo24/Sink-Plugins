package de.static_interface.sinkirc.irc_command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import de.static_interface.sinklibrary.api.command.SinkCommand;
import de.static_interface.sinklibrary.api.configuration.Configuration;
import de.static_interface.sinklibrary.api.sender.IrcCommandSender;

public class BroadCastCommand extends SinkCommand {

	public BroadCastCommand(Plugin plugin, Configuration config) {
		super(plugin, config);
		getCommandOptions().setIrcOpOnly(true);
	}

	@Override
	protected boolean onExecute(final CommandSender cs, String label, String[] args){
		if(args.length < 1) {
			return false;
		}
		String ircPrefix = ChatColor.GRAY+"["+ChatColor.GREEN+"Irc-Broadcast"+ChatColor.GRAY+"]"+ChatColor.RESET;
		String msgWithPrefix;
		msgWithPrefix = ircPrefix + " » " + ChatColor.WHITE + label.replace("(i?)bcmsg", "");
		
		Bukkit.broadcastMessage(msgWithPrefix);
		return true;
	}
	
	

}
