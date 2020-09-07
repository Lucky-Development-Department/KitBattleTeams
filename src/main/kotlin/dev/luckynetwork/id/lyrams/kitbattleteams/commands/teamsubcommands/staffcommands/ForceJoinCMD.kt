package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.staffcommands

import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("DEPRECATION")
class ForceJoinCMD(name: String, vararg aliases: String) : SubCommand(name, *aliases) {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player)
            return

        if (!sender.hasPermission("kbteams.admin")) {
            sender.sendMessage("§cNo Permission!")
            return
        }

        val team = Database.getTeamData(sender)

        if (team.teamID != 0) {
            sender.sendMessage("§cYou are already in a team!")
            return
        }

        val target =
            when {
                args.isEmpty() -> {
                    sender.sendMessage("§cInvalid usage!")
                    return
                }

                else -> Bukkit.getOfflinePlayer(args[0])
            }


        Database.getTeamData(target.uniqueId).whenComplete { targetTeam, error ->
            if (error != null) {
                error.printStackTrace()
                return@whenComplete
            }

            if (targetTeam == null || targetTeam.teamID == 0) {
                sender.sendMessage("§cIs not in a team!")
                return@whenComplete
            }

            val silent =
                args.joinToString(" ").contains("-s")

            for (targetTeamMemberUUIDs in targetTeam.members!!) {
                if (Bukkit.getPlayer(targetTeamMemberUUIDs) == null)
                    continue

                val targetTeamMembers = Bukkit.getPlayer(targetTeamMemberUUIDs)
                Database.getTeamData(targetTeamMemberUUIDs).whenComplete { targetTeamMemberTeamData, error1 ->
                    if (error1 != null) {
                        error1.printStackTrace()
                        return@whenComplete
                    }

                    if (targetTeamMemberTeamData != null)
                        targetTeamMemberTeamData.members!!.add(sender.uniqueId)
                }
                if (!silent)
                    targetTeamMembers.sendMessage("§e${sender.name} §6joined the team!")
            }

            targetTeam.members!!.add(sender.uniqueId)

            team.teamID = targetTeam.teamID
            team.members = targetTeam.members
            team.leader = targetTeam.leader
            team.friendlyFire = targetTeam.friendlyFire

            Database.saveTeamData(team)
            sender.sendMessage("§6You joined §e${Bukkit.getOfflinePlayer(team.leader).name}'s §6team!")
        }

    }

}