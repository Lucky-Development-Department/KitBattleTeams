package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands

import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class HelpCMD(name: String, vararg aliases: String) : SubCommand(name, *aliases) {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        sendHelpMessage(sender)
    }

    companion object {
        fun sendHelpMessage(sender: CommandSender) {
            sender.sendMessage("§6Team Commands:")
            sender.sendMessage(" ")
            sender.sendMessage("§6* §e/team create §7- Create a team.")
            sender.sendMessage("§6* §e/team join [leader] §7- Join a team.")
            sender.sendMessage("§6* §e/team leave §7- Leave your current team.")
            sender.sendMessage("§6* §e/team info §7- Get details about your team.")
            sender.sendMessage("§6* §e/team info [playerName] §7- Get details about a team.")
            sender.sendMessage("§6* §e/team chat §7- Toggles team chatting.")
            sender.sendMessage("§6* §e/team chat [message] §7- Send only teammates a message.")

            if (sender !is Player) {
                sendHelpMessage(sender)
            } else {
                val teamData = Database.getTeamData(sender)
                if (teamData.teamID != 0 && teamData.leader == sender.uniqueId)
                    sendLeaderHelpMessage(sender)
            }
        }

        private fun sendLeaderHelpMessage(sender: CommandSender) {
            sender.sendMessage(" ")
            sender.sendMessage("§6Team Leader Commands:")
            sender.sendMessage(" ")
            sender.sendMessage("§6* §e/team invite [playerName] §7- Create a team.")
            sender.sendMessage("§6* §e/team kick [playerName] §7- Kick a player from the team.")
            sender.sendMessage("§6* §e/team promote [playerName] §7- Appoint another player as the leader.")
            sender.sendMessage("§6* §e/team privacy §7- Toggles the team privacy setting.")
            sender.sendMessage("§6* §e/team privacy [public/private] §7- Sets the team privacy setting.")
            sender.sendMessage("§6* §e/team disband §7- Disband the team.")
            sender.sendMessage("§6* §e/team ff §7- Toggles friendly fire.")
        }
    }

}