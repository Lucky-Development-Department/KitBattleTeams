package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands

import dev.luckynetwork.id.lyrams.kitbattleteams.managers.AntiSpamManager
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class LeaveCMD(name: String) : SubCommand(name) {

    private var antiSpamMap = AntiSpamManager.antiSpamMap["LEAVECMD"]

    init {
        val emptyMap = HashMap<Player, Long>()
        antiSpamMap = emptyMap
    }

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player)
            return

        val team = Database.getTeamData(sender)

        if (team.teamID == 0) {
            sender.sendMessage("§cYou are not in a team!")
            return
        }

        if (sender.uniqueId == team.leader) {
            sender.sendMessage(
                "§cYou are the leader of this team! Please transfer the leadership to another player " +
                        "if you wish to leave the team or use /team disband to disband the team."
            )
            return
        }

        val currentTime = System.currentTimeMillis()

        if (!sender.hasPermission("kbteams.bypassantispam"))
            if (antiSpamMap!!.containsKey(sender)) {
                if (currentTime - antiSpamMap!![sender]!! < 10000) {
                    val seconds = (10000 - (currentTime - antiSpamMap!![sender]!!)) / 1000 % 60
                    val milliSeconds = (10000 - (currentTime - antiSpamMap!![sender]!!)) % 1000 / 100
                    sender.sendMessage("§cPlease wait ${seconds}.${milliSeconds}s before doing that!")
                    return
                } else {
                    antiSpamMap!![sender] = currentTime
                }
            } else if (!antiSpamMap!!.containsKey(sender) || currentTime - antiSpamMap!![sender]!! < 10000) {
                antiSpamMap!![sender] = currentTime
            }

        if (team.members == null)
            return

        team.members!!.remove(sender.uniqueId)

        Database.saveTeamData(team)

        for (targetTeamMemberUUIDs in team.members!!) {
            if (Bukkit.getPlayer(targetTeamMemberUUIDs) == null)
                continue

            val targetTeamMembers = Bukkit.getPlayer(targetTeamMemberUUIDs)
            Database.getTeamData(targetTeamMemberUUIDs).whenComplete { targetTeamMemberTeamData, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@whenComplete
                }

                if (targetTeamMemberTeamData != null)
                    targetTeamMemberTeamData.members!!.remove(sender.uniqueId)
            }
            targetTeamMembers.sendMessage("§c§l- §e${sender.name} §6left the team!")
        }

        sender.sendMessage("§6You joined §l${Bukkit.getOfflinePlayer(team.leader).name}'s §6team!")

        team.teamID = 0
        team.members = null
        team.leader = null
        team.friendlyFire = false

    }

}