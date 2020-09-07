package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.staffcommands

import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.CompletableFuture

@Suppress("DEPRECATION")
class ForceDisbandCMD(name: String, vararg aliases: String) : SubCommand(name, *aliases) {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("kbteams.admin")) {
            return
        }

        // casts target
        val target =
            when {
                args.isEmpty() -> {
                    sender.sendMessage("§cInvalid usage!")
                    return
                }

                else -> Bukkit.getOfflinePlayer(args[0])
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

            removeTeamFromDB(team.teamID)

            if (team.members == null)
                return@whenComplete

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

                Bukkit.getPlayer(members).sendMessage("§aYour team has been force-disbanded!")
            }

            sender.sendMessage("§aTeam disbanded!")
        }
    }


    private fun removeTeamFromDB(teamID: Int): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {
            try {
                Database.helper.query("DELETE FROM teamdata WHERE team_id = ?;")
                    .execute(teamID)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, Database.threadPool)

    }

    private fun removePlayerFromTeam(uuid: UUID): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {
            try {
                Database.helper.query("UPDATE playerdata SET team_id = ? WHERE uuid = ?")
                    .execute("0", uuid)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, Database.threadPool)

    }

}