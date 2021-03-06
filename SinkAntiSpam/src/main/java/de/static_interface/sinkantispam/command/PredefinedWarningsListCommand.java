/*
 * Copyright (c) 2013 - 2015 http://static-interface.de and contributors
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

package de.static_interface.sinkantispam.command;

import static de.static_interface.sinksql.query.Query.*;

import de.static_interface.sinkantispam.SinkAntiSpam;
import de.static_interface.sinkantispam.database.row.PredefinedWarning;
import de.static_interface.sinklibrary.api.command.SinkCommand;
import de.static_interface.sinklibrary.api.command.annotation.Aliases;
import de.static_interface.sinklibrary.api.command.annotation.DefaultPermission;
import de.static_interface.sinklibrary.api.configuration.Configuration;
import org.apache.commons.cli.ParseException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

@DefaultPermission
@Aliases({"pwarnlist", "pwarninglist"})
public class PredefinedWarningsListCommand extends SinkCommand {

    public PredefinedWarningsListCommand(@Nonnull Plugin plugin, Configuration config) {
        super(plugin, config);
    }

    @Override
    protected boolean onExecute(CommandSender sender, String label, String[] args) throws ParseException {
        PredefinedWarning[] pWarnings = from(SinkAntiSpam.getInstance().getPredefinedWarningsTable()).select().getResults();
        if (pWarnings.length < 1) {
            sender.sendMessage(ChatColor.RED + "No predefined warnings found");
            return true;
        }

        for (PredefinedWarning warning : pWarnings) {
            sender.sendMessage(
                    "• " + ChatColor.GOLD + warning.nameId + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + warning.points + ChatColor.GRAY + "): "
                    + warning.reason);
        }
        return true;
    }
}
