package de.static_interface.sinkirc.irc_command;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.pircbotx.User;

import de.static_interface.sinklibrary.api.command.SinkCommand;
import de.static_interface.sinklibrary.api.command.annotation.Description;
import de.static_interface.sinklibrary.api.command.annotation.Usage;
import de.static_interface.sinklibrary.api.configuration.Configuration;

@Description("Zeigt eine Hilfe über Befehle im IRC")
@Usage("")
public class HelpCommand extends SinkCommand {

	public HelpCommand(Plugin plugin, Configuration config) {
		super(plugin, config);
	}

	@Override
	protected boolean onExecute(CommandSender sender, String label, String[] args) {
		if(args.length >= 1) {
			return false;
		}
		
		User user = (User) sender;
		boolean isChanOp = de.static_interface.sinkirc.IrcUtil.isOp(user);
		
			sender.sendMessage("     |~| IRC-Hilfe |~|     \n"
					+ "~help » Ruft diese Hilfe auf\n"
					+ "~say [Naricht] » Chatverbindung in den Ingame-Chat\n"
					+ "~list » Listet die Spieler Ingame auf");
			if(isChanOp) {
				sender.sendMessage("~exec [Kommand] » Führt einen Kommando als Konsole aus\n"
						+ "~kick [Spieler] » Kickt einen Ingame-Spieler\n"
						+ "~set [Variable] [Zustand] » Setzt eine Variable auf den Zustand\n"
						+ "~srl » Reloaded den Ingame-Server");
			}
		
		return true;
	}
	
	

}
