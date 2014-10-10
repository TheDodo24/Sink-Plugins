/*
 * Copyright (c) 2013 - 2014 http://adventuria.eu, http://static-interface.de and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.static_interface.sinklibrary.listener;

import de.static_interface.sinklibrary.SinkIrcUser;
import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.api.command.SinkCommand;
import de.static_interface.sinklibrary.api.event.IrcCommandEvent;
import de.static_interface.sinklibrary.api.sender.IrcCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.pircbotx.User;

import java.util.Arrays;

public class IrcCommandListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(IrcCommandEvent event) {
        User user = event.getUser();
        String command = event.getCommand();
        String source = event.getSource();
        String label = event.getLabel();
        String[] args = event.getArgs();

        SinkLibrary.getInstance().getCustomLogger().debug("Executing IRC command: " + command + ", source: " + source
                                                          + ", label: " + label + ", args:" + Arrays.toString(args) + ", user: " + user);
        if (!executeCommand(user, command, source, label, args)) {
            SinkLibrary.getInstance().sendIrcMessage(user.getNick() + ": Unknown command: " + command, source);
        }
    }

    private boolean executeCommand(User user, String command, String source, String label, String[] args) {
        //Todo add events
        SinkIrcUser ircUser = SinkLibrary.getInstance().getIrcUser(user);
        IrcCommandSender sender = new IrcCommandSender(ircUser, source);
        SinkCommand cmd = SinkLibrary.getInstance().getCustomCommand(command);
        return !(cmd == null || cmd.isPlayerOnly()) && cmd.onCommand(sender, null, label, args);
    }
}
