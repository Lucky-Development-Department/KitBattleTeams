package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands

import dev.luckynetwork.id.lyrams.kitbattleteams.KitBattleTeams
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("DEPRECATION")
class InfoCMD(name: String, vararg aliases: String) : SubCommand(name, *aliases) {

    override fun execute(sender: CommandSender, args: Array<out String>) {

        val target =
            when {
                sender !is Player -> {
                    when {
                        args.isEmpty() -> {
                            HelpCMD.sendHelpMessage(sender)
                            return
                        }
                        else -> Bukkit.getOfflinePlayer(args[0])
                    }
                }
                args.isNotEmpty() ->
                    Bukkit.getOfflinePlayer(args[0])

                else -> sender

            }

        Database.getTeamData(target.uniqueId).whenComplete { team, error ->
            if (error != null) {
                error.printStackTrace()
                return@whenComplete
            }

            if (team == null || team.teamID == 0) {
                if (target != sender)
                    sender.sendMessage("§c§l${target.name} §cis not in a team!")
                else
                    sender.sendMessage("§cYou are not in a team!")

                return@whenComplete
            }

            sender.sendMessage("§8§m§l--------§8§l (§e§lTeam Info§8§l) §8§m§l--------")
            sender.sendMessage("§eTeam ID: §6${team.teamID}")
            sender.sendMessage("§eTeam Leader: §6${Bukkit.getOfflinePlayer(team.leader).name}")
            sender.sendMessage("§eTeam Status: §6${team.privacy}")
            sender.sendMessage("§eFriendly Fire: §6${team.friendlyFire}")

            if (team.members == null)
                return@whenComplete

            sender.sendMessage("§eMembers§7(${team.members!!.size}/${KitBattleTeams.max_team_size})§e: ")
            for (memberUUIDs in team.members!!) {
                if (memberUUIDs == team.leader)
                    sender.sendMessage("§e- §6§l${Bukkit.getOfflinePlayer(memberUUIDs).name}")
                else
                    sender.sendMessage("§e- §6${Bukkit.getOfflinePlayer(memberUUIDs).name}")
            }
        }
    }
}