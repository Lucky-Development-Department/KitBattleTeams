package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands

import dev.luckynetwork.id.lyrams.kitbattleteams.managers.AntiSpamManager
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.enums.AntiSpamType
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

class DisbandCMD(name: String) : SubCommand(name) {

    private var antiSpamMap = AntiSpamManager.antiSpamMap[AntiSpamType.DISBAND]

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

        if (sender.uniqueId != team.leader) {
            sender.sendMessage("§cOnly team leaders can disband the team!")
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

        removeTeamFromDB(team.teamID)

        if (team.members == null)
            return

        for (members in team.members!!) {
            if (Bukkit.getPlayer(members) == null) {
                removePlayerFromTeam(members)
                continue
            }

            val teamMember = Database.getTeamData(Bukkit.getPlayer(members))

            teamMember.teamID = 0
            teamMember.members = null
            teamMember.leader = null
            teamMember.friendlyFire = false

            Bukkit.getPlayer(members).sendMessage("§aYour team has been disbanded!")
        }

        sender.sendMessage("§aTeam disbanded!")

    }

    private fun removeTeamFromDB(teamID: Int): CompletableFuture<Void> {
        return CompletableFuture.runAsync({
            try {
                Database.helper.query("DELETE FROM teamdata WHERE team_id = ?;").execute(teamID)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, Database.threadPool)
    }

    private fun removePlayerFromTeam(uuid: UUID): CompletableFuture<Void> {
        return CompletableFuture.runAsync({
            try {
                Database.helper.query("UPDATE playerdata SET team_id = ? WHERE uuid = ?").execute("0", uuid)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, Database.threadPool)
    }

}