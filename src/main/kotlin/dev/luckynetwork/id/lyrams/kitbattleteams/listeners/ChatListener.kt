package dev.luckynetwork.id.lyrams.kitbattleteams.listeners

import dev.luckynetwork.id.lyrams.kitbattleteams.commands.teamsubcommands.playercommands.ChatCMD
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.isTeamChatting
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener : Listener {

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val teamData = Database.getTeamData(player)

        if (teamData.teamID == 0 || !teamData.isTeamChatting())
            return

        event.isCancelled = true
        ChatCMD.sendTeamMessage(player, event.message)

    }

}