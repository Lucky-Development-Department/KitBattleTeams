package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands

import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("DEPRECATION")
class KickCMD(name: String, vararg aliases: String) : SubCommand(name, *aliases) {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player)
            return

        if (args.isEmpty()) {
            HelpCMD.sendHelpMessage(sender)
            return
        }

        val team = Database.getTeamData(sender)
        if (team.teamID == 0) {
            sender.sendMessage("§cYou are not in a team!")
            return
        }

        if (sender.uniqueId != team.leader) {
            sender.sendMessage("§cYou are not the leader of this team!")
            return
        }

        val target =
            when {
                args.isEmpty() -> {
                    HelpCMD.sendHelpMessage(sender)
                    return
                }

                else -> Bukkit.getOfflinePlayer(args[0])
            }

        if (team.members == null || !team.members!!.contains(target.uniqueId)) {
            sender.sendMessage("§c§l${target.name} §cis not in your team!")
            return
        }

        team.members!!.remove(target.uniqueId)

        for (targetTeamMemberUUIDs in team.members!!) {
            if (Bukkit.getPlayer(targetTeamMemberUUIDs) == null || Bukkit.getPlayer(targetTeamMemberUUIDs) == sender)
                continue

            val targetTeamMembers = Bukkit.getPlayer(targetTeamMemberUUIDs)
            Database.getTeamData(targetTeamMemberUUIDs).whenComplete { targetTeamMemberTeamData, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@whenComplete
                }

                if (targetTeamMemberTeamData != null) {
                    targetTeamMemberTeamData.members!!.remove(sender.uniqueId)
                    targetTeamMembers.sendMessage("§c§l- §e§l${sender.name} §6kicked §e${target.name} §6from your team!")
                }
            }

        }

        if (Bukkit.getPlayer(target.name) != null) {
            target as Player
            Database.getTeamData(target.uniqueId).whenComplete { targetTeamData, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@whenComplete
                }

                if (targetTeamData != null) {
                    targetTeamData.teamID = 0
                    targetTeamData.leader = null
                    targetTeamData.members = null
                    targetTeamData.friendlyFire = false
                }
            }
            target.sendMessage("§6§l${sender.name} §ekicked you from their team!")
        }

        Database.saveTeamData(team)
        sender.sendMessage("§aYou kicked §l${target.name} §afrom your team!")

    }

}