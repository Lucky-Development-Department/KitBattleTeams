package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands

import com.github.alviannn.sqlhelper.utils.Closer
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.AntiSpamManager
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.enums.AntiSpamType
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.enums.TeamPrivacy
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.TeamData
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class CreateCMD(name: String) : SubCommand(name) {

    private var antiSpamMap = AntiSpamManager.antiSpamMap[AntiSpamType.CREATE]

    init {
        val emptyMap = HashMap<Player, Long>()
        antiSpamMap = emptyMap
    }

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player)
            return

        val team = Database.getTeamData(sender)
        if (team.teamID != 0) {
            sender.sendMessage("§cYou are already in a team!")
            return
        }

        val currentTime = System.currentTimeMillis()
        if (!sender.hasPermission("kbteams.bypassantispam"))
            if (antiSpamMap!!.containsKey(sender)) {
                if (currentTime - antiSpamMap!![sender]!! < 60000) {
                    val seconds = (60000 - (currentTime - antiSpamMap!![sender]!!)) / 1000 % 60
                    val milliSeconds = (60000 - (currentTime - antiSpamMap!![sender]!!)) % 1000 / 100
                    sender.sendMessage("§cPlease wait ${seconds}.${milliSeconds}s before doing that!")
                    return
                } else {
                    antiSpamMap!![sender] = currentTime
                }
            } else if (!antiSpamMap!!.containsKey(sender) || currentTime - antiSpamMap!![sender]!! < 60000) {
                antiSpamMap!![sender] = currentTime
            }

        val members = ConcurrentHashMap.newKeySet<UUID>()
        members.add(sender.uniqueId)

        team.player = sender
        team.teamID = -1
        team.leader = sender.uniqueId
        team.members = members
        team.friendlyFire = false
        team.privacy = TeamPrivacy.PRIVATE

        createTeam(team)
        sender.sendMessage("§aTeam created!")
    }

    private fun createTeam(teamData: TeamData): CompletableFuture<Void> {
        return CompletableFuture.runAsync({

            try {
                Closer().use { closer ->

                    Database.helper.query("INSERT INTO teamdata (leader, members, friendly_fire, privacy) VALUES (?, ?, ?, ?);")
                        .execute(
                            teamData.leader,
                            teamData.members.toString(),
                            teamData.friendlyFire,
                            teamData.privacy.toString()
                        )

                    val results = closer.add(
                        Database.helper.query("SELECT * FROM teamdata WHERE leader = ?;")
                            .getResults(teamData.leader)
                    )

                    val set = results.resultSet
                    if (set.next()) {
                        teamData.teamID = set.getInt("team_id")

                        Database.helper.query("UPDATE playerdata SET team_id = ? WHERE uuid = ?")
                            .execute(teamData.teamID, teamData.leader)
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, Database.threadPool)

    }

}