package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands

import dev.luckynetwork.id.lyrams.kitbattleteams.KitBattleTeams
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.AntiSpamManager
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.InviteManager
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.enums.AntiSpamType
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.enums.TeamPrivacy
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class JoinCMD(name: String, vararg aliases: String) : SubCommand(name, *aliases) {

    private var antiSpamMap = AntiSpamManager.antiSpamMap[AntiSpamType.JOIN]

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

        val target = Bukkit.getPlayer(args[0])
        if (target == null) {
            sender.sendMessage("§cPlayer not found!")
            return
        }

        Database.getTeamData(target.uniqueId).whenComplete { targetTeam, error ->
            if (error != null) {
                error.printStackTrace()
                return@whenComplete
            }

            if (targetTeam != null) {
                if (targetTeam.teamID == 0) {
                    sender.sendMessage("§c§l${target.name} §cis not in a team!")
                    return@whenComplete
                }

                val inviteManager = InviteManager.INVITE_MAP
                if (targetTeam.privacy == TeamPrivacy.PRIVATE &&
                    (inviteManager[target] == null || !inviteManager[target]!!.contains(sender))
                ) {
                    sender.sendMessage("§c§l${target.name} §cneeds to invite you before you can join their team!")
                    return@whenComplete
                }

                if (targetTeam.members!!.size >= KitBattleTeams.max_team_size) {
                    sender.sendMessage("§cSorry! But that team is full!")
                    return@whenComplete
                }

                val currentTime = System.currentTimeMillis()
                if (!sender.hasPermission("kbteams.bypassantispam"))
                    if (antiSpamMap!!.containsKey(sender)) {
                        if (currentTime - antiSpamMap!![sender]!! < 10000) {
                            val seconds = (10000 - (currentTime - antiSpamMap!![sender]!!)) / 1000 % 60
                            val milliSeconds = (10000 - (currentTime - antiSpamMap!![sender]!!)) % 1000 / 100
                            sender.sendMessage("§cPlease wait ${seconds}.${milliSeconds}s before doing that!")
                            return@whenComplete
                        } else {
                            antiSpamMap!![sender] = currentTime
                        }
                    } else if (!antiSpamMap!!.containsKey(sender) || currentTime - antiSpamMap!![sender]!! < 10000) {
                        antiSpamMap!![sender] = currentTime
                    }

                if (targetTeam.privacy == TeamPrivacy.PRIVATE)
                    inviteManager[target]!!.remove(sender)

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

                    targetTeamMembers.sendMessage("§a§l+ §e${sender.name} §6joined the team!")
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
}