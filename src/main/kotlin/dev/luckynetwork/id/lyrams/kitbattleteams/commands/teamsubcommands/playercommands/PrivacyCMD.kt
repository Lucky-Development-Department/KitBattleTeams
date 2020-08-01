package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands

import dev.luckynetwork.id.lyrams.kitbattleteams.managers.AntiSpamManager
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.TeamPrivacy
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class PrivacyCMD(name: String, vararg aliases: String) : SubCommand(name, *aliases) {

    private var antiSpamMap = AntiSpamManager.antiSpamMap["PRIVACYCMD"]

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
                    val milliSeconds = (20000 - (currentTime - antiSpamMap!![sender]!!)) % 1000 / 100
                    sender.sendMessage("§cPlease wait ${seconds}.${milliSeconds}s before doing that!")
                    return
                } else {
                    antiSpamMap!![sender] = currentTime
                }
            } else if (!antiSpamMap!!.containsKey(sender) || currentTime - antiSpamMap!![sender]!! < 2000) {
                antiSpamMap!![sender] = currentTime
            }

        val toggle: Boolean? =
            if (args.isNotEmpty())
                when {
                    args[0].equals("public", true) -> true
                    args[0].equals("private", true) -> false
                    else -> null
                }
            else
                null

        val state = team.privacy

        if (toggle == null)
            if (state == TeamPrivacy.PRIVATE)
                team.privacy = TeamPrivacy.PUBLIC
            else
                team.privacy = TeamPrivacy.PRIVATE
        else if (toggle)
            team.privacy = TeamPrivacy.PUBLIC
        else
            team.privacy = TeamPrivacy.PRIVATE


        var duplicated = false

        for (teamMembers in team.members!!) {
            if (Bukkit.getPlayer(teamMembers) == null || sender == Bukkit.getPlayer(teamMembers))
                continue

            val targetTeamMembers = Bukkit.getPlayer(teamMembers)
            val targetTeamData = Database.getTeamData(targetTeamMembers)

            if (targetTeamData.privacy == team.privacy) {
                duplicated = true
                continue
            }

            targetTeamData.privacy = team.privacy

            targetTeamMembers.sendMessage("§e§l${sender.name} §6set the team privacy to §e${team.privacy}!")
        }

        if (!duplicated)
            Database.saveTeamData(team)

        sender.sendMessage("§aPrivacy: ${team.privacy}")

    }

}