package de.static_interface.sinkirc.irc_command;

import org.bukkit.Bukkit;
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
		IrcCommandSender sender = (IrcCommandSender) cs;
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
		
		Bukkit.broadcastMessage(finishMsgWithArgs);
		
		return true;
	}
	
	

}
