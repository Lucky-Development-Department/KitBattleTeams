package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands

import dev.luckynetwork.id.lyrams.kitbattleteams.managers.AntiSpamManager
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class FriendlyFireCMD(name: String, vararg aliases: String) : SubCommand(name, *aliases) {

    private var antiSpamMap = AntiSpamManager.antiSpamMap["FFCMD"]

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

        if (team.leader != sender.uniqueId) {
            sender.sendMessage("§cYou are not the leader of this team!")
            return
        }

        val currentTime = System.currentTimeMillis()

        if (!sender.hasPermission("kbteams.bypassantispam"))
            if (antiSpamMap!!.containsKey(sender)) {
                if (currentTime - antiSpamMap!![sender]!! < 2000) {
                    val seconds = (2000 - (currentTime - antiSpamMap!![sender]!!)) / 1000 % 60
                    val milliSeconds = (2000 - (currentTime - antiSpamMap!![sender]!!)) % 1000 / 100
                    sender.sendMessage("§cPlease wait ${seconds}.${milliSeconds}s before doing that!")
                    return
                } else {
                    antiSpamMap!![sender] = currentTime
                }
            } else if (!antiSpamMap!!.containsKey(sender) || currentTime - antiSpamMap!![sender]!! < 2000) {
                antiSpamMap!![sender] = currentTime
            }

        team.friendlyFire = !team.friendlyFire

        var duplicated = false

        for (teamMembers in team.members!!) {
            if (Bukkit.getPlayer(teamMembers) == null || sender == Bukkit.getPlayer(teamMembers))
                continue

            val targetTeamMembers = Bukkit.getPlayer(teamMembers)
            val targetTeamData = Database.getTeamData(targetTeamMembers)

            if (targetTeamData.friendlyFire == team.friendlyFire) {
                duplicated = true
                continue
            }

            targetTeamData.friendlyFire = team.friendlyFire

            targetTeamMembers.sendMessage("§e§l${sender.name} §6toggled friendlyfire §e${team.friendlyFire}!")
        }

        if (!duplicated)
            Database.saveTeamData(team)

        sender.sendMessage("§aFriendly Fire: ${team.friendlyFire}")

    }

}