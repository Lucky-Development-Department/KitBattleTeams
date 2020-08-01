package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands

import dev.luckynetwork.id.lyrams.kitbattleteams.managers.AntiSpamManager
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.HashMap

@Suppress("DEPRECATION")
class PromoteCMD(name: String) : SubCommand(name) {

    private var antiSpamMap = AntiSpamManager.antiSpamMap["TRASNFERCMD"]

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
            sender.sendMessage("§cYou are not the leader of this team!")
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

        val target =
            when {
                args.isEmpty() -> {
                    HelpCMD.sendHelpMessage(sender)
                    return
                }

                else -> Bukkit.getOfflinePlayer(args[0])
            }

        if (target.uniqueId == sender.uniqueId) {
            sender.sendMessage("§cYou are already the leader of this team!")
            return
        }

        if (team.members == null || !team.members!!.contains(target.uniqueId)) {
            sender.sendMessage("§c§l${target.name} §cis not in your team!")
            return
        }

        team.leader = target.uniqueId

        for (targetTeamMemberUUIDs in team.members!!) {
            if (Bukkit.getPlayer(targetTeamMemberUUIDs) == null || Bukkit.getPlayer(targetTeamMemberUUIDs) == sender)
                continue

            val targetTeamMembers = Bukkit.getPlayer(targetTeamMemberUUIDs)
            val targetTeamMemberTeamData = Database.getTeamData(targetTeamMemberUUIDs) ?: return

            targetTeamMemberTeamData.leader = target.uniqueId
            targetTeamMembers.sendMessage("§e§l${sender.name} §6transferred the leadership of this team to " +
                    "§e${target.name}§6!")
        }

        if (Bukkit.getPlayer(target.name) != null) {
            target as Player
            target.sendMessage("§6§lYou are now the leader of this team!")
        }

        Database.saveTeamData(team)

        sender.sendMessage("§aYou transferred your leadership to §a${target.name}§6!")

    }

}