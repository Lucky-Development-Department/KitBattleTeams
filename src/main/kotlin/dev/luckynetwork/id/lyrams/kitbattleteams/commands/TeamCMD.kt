package dev.luckynetwork.id.lyrams.kitbattleteams.commands

import dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands.*
import dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.staffcommands.ForceDisbandCMD
import dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.staffcommands.ForceJoinCMD
import dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.staffcommands.SpyChatCMD
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.SubCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture

class TeamCMD : CommandExecutor {

    private var subCommands: List<SubCommand> = listOf(
        ChatCMD("chat", "c", "msg"),
        CreateCMD("create"),
        DisbandCMD("disband"),
        ForceDisbandCMD("forcedisband", "fdisband"),
        ForceJoinCMD("forcejoin", "fjoin"),
        FriendlyFireCMD("friendlyfire", "ff"),
        InfoCMD("info", "i", "check"),
        InviteCMD("invite"),
        HelpCMD("help", "?"),
        JoinCMD("join"),
        KickCMD("kick", "remove"),
        LeaveCMD("leave"),
        PrivacyCMD("privacy"),
        PromoteCMD("promote"),
        SpyChatCMD("spychat", "spy")
    )

    override fun onCommand(sender: CommandSender, command: Command, cmd: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            HelpCMD.sendHelpMessage(sender)
            return false
        }

        val subCmd = args[0].toLowerCase()
        val newArgs = args.copyOfRange(1, args.size)

        CompletableFuture.runAsync {
            try {
                for (subCommand in subCommands) {
                    if (subCommand.name == subCmd || subCommand.aliases.contains(subCmd)) {
                        subCommand.execute(sender, newArgs)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        return false
        
    }

}