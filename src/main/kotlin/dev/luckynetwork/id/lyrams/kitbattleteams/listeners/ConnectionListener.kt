package dev.luckynetwork.id.lyrams.kitbattleteams.listeners

import dev.luckynetwork.id.lyrams.kitbattleteams.KitBattleTeams
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.AntiSpamManager
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.InviteManager
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class ConnectionListener(private val plugin: KitBattleTeams) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        Database.loadPlayerData(event.player)
    }

    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        val player = event.player

        Database.savePlayerData(player)
        Database.saveTeamData(Database.getTeamData(player))

        if (InviteManager.INVITE_MAP[player] != null)
            InviteManager.INVITE_MAP.remove(player)

        Bukkit.getScheduler().runTaskLater(plugin, { Database.unloadFromCache(player) }, 20L)
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, {

            // if player decided to rejoin... aka player is now online again
            if (Bukkit.getPlayer(player.name) != null)
                return@runTaskLaterAsynchronously

            AntiSpamManager.antiSpamMap.values.forEach {
                if (it.containsKey(player))
                    it.remove(player)
            }
        }, 60L * 20L)
    }

}