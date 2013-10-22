package de.static_interface.sinkchat.command;

import de.static_interface.sinkchat.channel.IPrivateChannel;
import de.static_interface.sinkchat.channel.PrivateChannelHandler;
import de.static_interface.sinkchat.channel.channels.PrivateChannel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static de.static_interface.sinklibrary.configuration.LanguageConfiguration._;

public class PrivateChannelCommand implements CommandExecutor
{


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args)
    {
        //No args / only 1 argument / 2 arguments but args[1] doesnt equal "leave"

        if ( (args.length == 0) || (args.length == 1) || (args.length == 2 && !(args[0].equalsIgnoreCase("leave"))) )
        {
            sender.sendMessage("/" + cmd + " <channel identifier> <player> [players...]");
            sender.sendMessage("/" + cmd + " leave <channel identifier>");
            sender.sendMessage("/" + cmd + " kick <channel identifier> <player> [reason]");
            return true;
        }

        IPrivateChannel ch = PrivateChannelHandler.getChannel(args[1]);
        switch ( args[0].toLowerCase() )
        {
            case "invite":
                if ( ch == null )
                {
                    if ( Bukkit.getPlayer(args[2]) == null )
                    {
                        sender.sendMessage(String.format(_("SinkChat.Channels.Private.HasInvitedToChat.ErrorNotOnline"), args[2]));
                        return true;
                    }
                    PrivateChannelHandler.registerChannel(new PrivateChannel(args[1], (Player) (sender), Bukkit.getPlayer(args[2])));
                    ch = (PrivateChannelHandler.getChannel(args[1]));
                }
                for ( String s : args )
                {
                    if ( s.equals("invite") || s.equals(args[1]) || (ch.contains(Bukkit.getPlayer(s))) ) continue;
                    if ( Bukkit.getPlayer(s) == null )
                    {
                        sender.sendMessage(String.format(_("SinkChat.Channels.Private.HasInvitedToChat.ErrorNotOnline"), s));
                    }
                    ch.addPlayer((Player) (sender), Bukkit.getPlayer(s));
                }

                return true;

            case "leave":
                if ( !(ch.contains((Player) (sender))) || (ch == null) )
                {
                    sender.sendMessage(String.format(_("SinkChat.Commands.Channel.ChannelUnknown"), args[1]));
                    return true;
                }
                ch.kickPlayer((Player) sender, (Player) sender, "");
                return true;

            case "kick":
                if ( !(ch.contains((Player) sender)) || (ch == null) )
                {
                    sender.sendMessage(String.format(_("SinkChat.Commands.Channel.ChannelUnknown"), args[1]));
                    return true;
                }
                if ( !(ch.getStarter().equals(sender)) )
                {
                    sender.sendMessage(_("Permissions.General"));
                    return true;
                }
                if ( Bukkit.getPlayer(args[2]) == null )
                {
                    sender.sendMessage(String.format(_("SinkChat.Channels.Private.HasInvitedToChat.ErrorNotOnline"), args[2]));
                }
                if ( !(ch.contains(Bukkit.getPlayer(args[2]))) )
                {
                    sender.sendMessage(String.format(_("SinkChat.Channels.Private.PlayerKicked"), args[2]));
                    return true;
                }

                String reason = args[3];
                if ( reason == null ) reason = "kicked !";

                ch.kickPlayer(Bukkit.getPlayer(args[2]), (Player) sender, reason);
                return true;
        }


        return true;
    }
}