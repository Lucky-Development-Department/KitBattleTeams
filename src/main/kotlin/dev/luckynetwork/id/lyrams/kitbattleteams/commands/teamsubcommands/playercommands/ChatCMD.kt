package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands

import dev.luckynetwork.id.lyrams.kitbattleteams.managers.isSpyChat
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.toggleTeamChatting
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ChatCMD(name: String, vararg aliases: String) : SubCommand(name, *aliases) {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player)
            return

        val team = Database.getTeamData(sender)

        if (team.teamID == 0) {
            sender.sendMessage("§cYou are not in a team!")
            return
        }

        if (args.isNotEmpty())
            sendTeamMessage(
                sender,
                args.joinToString(" ")
            )
        else {

            val state = team.toggleTeamChatting()
            sender.sendMessage("§aTeam chat: ${state}!")

        }


    }


    companion object {

        fun sendTeamMessage(player: Player, message: String) {
            val teamData = Database.getTeamData(player)
            val teamMembers = teamData.members ?: return
            val isLeader = teamData.leader == player.uniqueId

            for (memberUUIDs in teamMembers) {
                if (Bukkit.getPlayer(memberUUIDs) == null)
                    continue

                val teamMember = Bukkit.getPlayer(memberUUIDs)

                if (isLeader)
                    teamMember.sendMessage("§6§l${player.name}§e: $message")
                else
                    teamMember.sendMessage("§6${player.name}§e: $message")

            }

            sendSpyChatMessage(player, message)

        }

        private fun sendSpyChatMessage(sender: Player, message: String) {

            Bukkit.getOnlinePlayers().forEach {
                if (it.isSpyChat())
                    it.sendMessage("§7§l[SPY] §7${sender.name}: $message")
            }

        }

    }

}