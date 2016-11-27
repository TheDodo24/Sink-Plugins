package de.static_interface.sinkirc.irc_command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.sun.org.glassfish.gmbal.Description;

import de.static_interface.sinkirc.SinkIRC;
import de.static_interface.sinklibrary.api.command.SinkCommand;
import de.static_interface.sinklibrary.api.configuration.Configuration;

@Description("Ein Reload Command fuer den IRC")
public class ReloadCommand extends SinkCommand {

	public ReloadCommand(Plugin plugin, Configuration config) {
		super(plugin, config);
		getCommandOptions().setIrcOpOnly(true);
		
	}

	@Override
	protected boolean onExecute(CommandSender sender, String label, String[] args){
		if(args.length > 1) {
			return false;
		}
		
		sender.sendMessage(sender.getName()+": Der Server wird gleich neugestartet");
		Bukkit.getScheduler().runTaskLater(SinkIRC.getPlugin(SinkIRC.class), new Runnable() {
			
			@Override
			public void run() {
				Bukkit.reload();
			}
		}, 5);
		return false;
	}
	

}
