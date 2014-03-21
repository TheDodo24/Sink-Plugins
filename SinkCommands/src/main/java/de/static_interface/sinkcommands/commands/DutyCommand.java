/*
 * Copyright (c) 2014 http://adventuria.eu, http://static-interface.de and contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.static_interface.sinkcommands.commands;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import de.static_interface.shadow.tameru.SQLiteDatabase;
import de.static_interface.sinkcommands.commands.DutyCommand.dutySumType;
import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.User;
import de.static_interface.sinklibrary.configuration.LanguageConfiguration;

public class DutyCommand implements CommandExecutor
{
	public static final String PREFIX = ChatColor.DARK_PURPLE+"[SinkDuty] "+ChatColor.RESET;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments)
	{
		if ( !sender.hasPermission("sinkcommands.duty") )
		{
			sender.sendMessage(LanguageConfiguration._("Permissions.General"));
		}
		
		if ( arguments.length == 0 )
		{
			sender.sendMessage(String.format(LanguageConfiguration._("SinkDuty.Time"), getDutySumTime(SinkLibrary.getUser(sender), dutySumType.ALL).toString()));
			return true;
		}
		
		if ( arguments.length == 1 )
		{
			if ( arguments[0].equalsIgnoreCase("on") )
			{
				if ( sender.equals(Bukkit.getConsoleSender()) )
				{
					sender.sendMessage(LanguageConfiguration._("General.ConsoleNotAvailable"));
				}
				startDuty(SinkLibrary.getUser(sender));
				sender.sendMessage(LanguageConfiguration._("SinkDuty.Time.Start"));
			}
			if ( arguments[0].equalsIgnoreCase("off") )
			{
				if ( sender.equals(Bukkit.getConsoleSender()) )
				{
					sender.sendMessage(LanguageConfiguration._("General.ConsoleNotAvailable"));
					return true;
				}
				if ( getPlayersInDuty().contains(SinkLibrary.getUser(sender)) ) 
				{
					endDuty(SinkLibrary.getUser(sender));
					sender.sendMessage(LanguageConfiguration._("SinkDuty.Time.Finish"));
					return true;
				}
				else
				{
					sender.sendMessage(LanguageConfiguration._("SinkDuty.Time.NotInDuty"));
					return true;
				}
			}
			User otherUser = SinkLibrary.getUser(arguments[0]);
			if ( !otherUser.isOnline() )
			{
				sender.sendMessage(String.format(LanguageConfiguration._("General.NotOnline"), arguments[0]));
				return true;
			}
			
			sender.sendMessage(String.format(LanguageConfiguration._("SinkDuty.Time.Others"), arguments[0], getDutySumTime(otherUser, dutySumType.ALL).toString()));
			return true;
		}
		
		if ( arguments.length == 2 )
		{
			User otherUser = SinkLibrary.getUser(arguments[0]);
			if ( !otherUser.isOnline() )
			{
				sender.sendMessage(String.format(LanguageConfiguration._("General.NotOnline"), arguments[0]));
				return true;
			}
			
			switch ( arguments[1].toLowerCase() )
			{
			case "all":
				sender.sendMessage(String.format(LanguageConfiguration._("SinkDuty.Time.Others"), arguments[0], getDutySumTime(otherUser, dutySumType.ALL).toString()));
				return true;
			case "today":
				sender.sendMessage(String.format(LanguageConfiguration._("SinkDuty.Time.Others"), arguments[0], getDutySumTime(otherUser, dutySumType.TODAY).toString()));
				return true;
			case "yesterday":
				sender.sendMessage(String.format(LanguageConfiguration._("SinkDuty.Time.Others"), arguments[0], getDutySumTime(otherUser, dutySumType.YESTERDAY).toString()));
				return true;
			case "last_week":
				sender.sendMessage(String.format(LanguageConfiguration._("SinkDuty.Time.Others"), arguments[0], getDutySumTime(otherUser, dutySumType.LAST_WEEK).toString()));
				return true;
			case "last_two_weeks":
				sender.sendMessage(String.format(LanguageConfiguration._("SinkDuty.Time.Others"), arguments[0], getDutySumTime(otherUser, dutySumType.LAST_TWO_WEEKS).toString()));
				return true;
			case "last_month":
				sender.sendMessage(String.format(LanguageConfiguration._("SinkDuty.Time.Others"), arguments[0], getDutySumTime(otherUser, dutySumType.LAST_MONTH).toString()));
				return true;
			default:
				sender.sendMessage(String.format(LanguageConfiguration._("SinkDuty.Time.Others"), arguments[0], getDutySumTime(otherUser, dutySumType.LAST_DUTY_ONLY).toString()));
				return true;
			}
		}
		return true;
	}
	
	public enum dutySumType
	{
		LAST_DUTY_ONLY,
		TODAY,
		YESTERDAY,
		LAST_WEEK,
		LAST_TWO_WEEKS,
		LAST_MONTH,
		ALL,
		LAST_X_DAYS // not implemented yet
	}
	
	public static void startDuty(User user)
	{
		endDuty(user);
		
		DatabaseHandler.updateUser(user, true);
	}
	
	public static Time endDuty(User user)
	{
		DatabaseHandler.updateUser(user, false);
		
		return getLastDutyTime(user);
	}
	
	public static Time getDutySumTime(User user, dutySumType sumType)
	{
		return DatabaseHandler.getSumTime(user, sumType);
	}
	
	public static Time getLastDutyTime(User user)
	{
		return getDutySumTime(user, dutySumType.LAST_DUTY_ONLY);
	}
	
	public static List<User> getPlayersInDuty()
	{
		return DatabaseHandler.getPlayersInDuty();
	}
}

class DatabaseHandler
{
	private static final String TABLE_DUTY ="duties";
	private static SQLiteDatabase database = new SQLiteDatabase(SinkLibrary.getCustomDataFolder().getAbsolutePath()+File.separator+"duties.db");
	
	private static void initDatabase()
	{
		if ( !database.containsTable(TABLE_DUTY) )
		{
			database.createTable(TABLE_DUTY, 
										"`ID` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
										+ "`UUID` TEXT, "
										+ "`TIMESTAMP_START` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
										+ "`TIMESTAMP_END` TIMESTAMP");
		}
	}
	
	public static List<User> getPlayersInDuty()
	{
		List<User> userList = new ArrayList<User>();
		
		initDatabase();
		
		ResultSet set = database.selectRaw("SELECT "
									+ "UUID"
								+ " FROM " 
									+ TABLE_DUTY 
								+ " WHERE "
									+ "`TIMESTAMP_COMPLETE` IS NULL");
									
		if (set != null) 
		{
			try 
			{
				while (set.next())
				{
				
					String uuidString =  set.getString("UUID");
					UUID uuid = UUID.fromString(uuidString);
					User user = SinkLibrary.getUserByUniqueId(uuid);
					
					if (user != null) 
					{
						userList.add(user);
					}
				}
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		
		return userList;
	}
	
	public static Time getSumTime(User user, dutySumType sumType)
	{
		initDatabase();
		
		if ((user != null) && (!user.isConsole()))
		{
			String additionalClauses = "";
			
			switch (sumType) {
			case LAST_DUTY_ONLY:
				additionalClauses = " ORDER BY `ID` DESC LIMIT 1";
				break;
			case TODAY:
				additionalClauses = " AND DATE(`TIMESTAMP_START`) = CURDATE()";
				break;
			case YESTERDAY:
				additionalClauses = " AND DATE(`TIMESTAMP_START`) > (NOW() - INTERVAL 1 DAY)";
				break;
			case LAST_WEEK:
				additionalClauses = " AND DATE(`TIMESTAMP_START`) > (NOW() - INTERVAL 7 DAY)";
				break;
			case LAST_TWO_WEEKS:
				additionalClauses = " AND DATE(`TIMESTAMP_START`) > (NOW() - INTERVAL 14 DAY)";
				break;
			case LAST_MONTH:
				additionalClauses = " AND DATE(`TIMESTAMP_START`) > (NOW() - INTERVAL 30 DAY)";
				break;
			default:
				break;
			}
			
			ResultSet set = database.selectRaw("SELECT "
									+ "SUM(TIME_TO_SEC(`TIMESTAMP_COMPLETE`))"
										+ " AS totaltime"
								+ " FROM " 
									+ TABLE_DUTY 
								+ " WHERE "
									+ "UUID='" + user.getUniqueId().toString() + "'"
								+ additionalClauses);
			
			if (set != null) 
			{
				try 
				{
					set.next();
					
					Time sumTime =  set.getTime("totaltime");
					if (sumTime != null)
					{
						return sumTime;
					}
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			SinkLibrary.getCustomLogger().log(Level.SEVERE, "[DutyCommand.updateUser] Kein gültiger User.");
		}
		
		return new Time(-(new Time(0)).getTime());
	}
	
	public static void updateUser(User user, boolean start)
	{
		initDatabase();
		
		if ((user != null) && (!user.isConsole()))
		{
			if (start)
			{
				database.insertIntoDatabase(TABLE_DUTY, "UUID", "'" + user.getUniqueId().toString() + "'");
			}
			else
			{
				database.updateRaw("UPDATE " 
										+ TABLE_DUTY 
									+ " SET "
										+ "TIMESTAMP_END = NOW(), "
										+ "TIMESTAMP_COMPLETE = SEC_TO_TIME(TIMESTAMP_END - TIMESTAMP_START) "
									+ "WHERE "
										+ "UUID = '" + user.getUniqueId().toString() + "'"
										+ " AND "
										+ "TIMESTAMP_COMPLETE IS NULL;");
			}
		}
		else {
			SinkLibrary.getCustomLogger().log(Level.SEVERE, "[DutyCommand.updateUser] Kein gültiger User.");
		}
	}
}