package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands

import dev.luckynetwork.id.lyrams.kitbattleteams.KitBattleTeams
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.InviteManager
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class InviteCMD(name: String, vararg aliases: String) : SubCommand(name, *aliases) {

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

        if (team.leader != sender.uniqueId) {
            sender.sendMessage("§cYou are not the leader of this team!")
            return
        }

        if (team.members!!.size >= KitBattleTeams.max_team_size && !sender.hasPermission("kbteams.bypasslimit")) {
            sender.sendMessage("§cSorry! But you can only have ${KitBattleTeams.max_team_size} members in your team!")
            return
        }

        val target = Bukkit.getPlayer(args[0])
        if (target == null) {
            sender.sendMessage("§cPlayer not found!")
            return
        }
        if (target == sender) {
            sender.sendMessage("§cYou can't invite yourself!")
            return
        }

        val inviteManager = InviteManager.INVITE_MAP

        if (!inviteManager.containsKey(sender))
            inviteManager[sender] = ArrayList()

        if (inviteManager[sender]!!.contains(target)) {
            sender.sendMessage("§c§l${target.name} §cis already invited!")
            return
        }

        inviteManager[sender]!!.add(target)

        notifyInvitationToTeam(sender, target)
        sender.sendMessage("§aInvited §l${target.name} §ato your team!")
        target.sendMessage("§a§l${sender.name} §ainvited you to join their team!")
        target.sendMessage("§7This invitation will expire in 1 minute! To accept, please do " +
                "/team join ${sender.name}")

        Bukkit.getScheduler().runTaskLater(KitBattleTeams.instance, {

            if (inviteManager[sender] == null)
                return@runTaskLater

            if (inviteManager[sender]!!.contains(target))
                inviteManager[sender]!!.remove(target)

            if (Bukkit.getPlayer(target.name) != null)
                target.sendMessage("§7Team invitation from §l${sender.name} §7has expired.")

        }, 6000L)

    }

    private fun notifyInvitationToTeam(player: Player, invited: Player) {
        val teamData = Database.getTeamData(player)

        for (teamMembers in teamData.members!!) {
            if (Bukkit.getPlayer(teamMembers) == null || player == invited)
                continue

            val targetTeamMembers = Bukkit.getPlayer(teamMembers)

            targetTeamMembers.sendMessage("§e§l${player.name} §6invited §e${invited.name} §6to your team!")
        }

    }

}

