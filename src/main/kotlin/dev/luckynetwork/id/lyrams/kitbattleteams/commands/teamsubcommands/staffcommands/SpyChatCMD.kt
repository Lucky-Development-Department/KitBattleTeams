package dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.staffcommands

import dev.luckynetwork.id.lyrams.kitbattleteams.managers.toggleTeamChatting
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpyChatCMD(name: String, vararg aliases: String) : SubCommand(name, *aliases) {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player)
            return

        if (!sender.hasPermission("kbteams.admin")) {
            return
        }

        val state = sender.toggleTeamChatting()
        sender.sendMessage("§aChat Spy: $state")

    }

}