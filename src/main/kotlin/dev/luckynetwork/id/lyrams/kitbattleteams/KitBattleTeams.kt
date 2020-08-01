package dev.luckynetwork.id.lyrams.kitbattleteams

import dev.luckynetwork.id.lyrams.kitbattleteams.commands.TeamCMD
import dev.luckynetwork.id.lyrams.kitbattleteams.listeners.ChatListener
import dev.luckynetwork.id.lyrams.kitbattleteams.listeners.ConnectionListener
import dev.luckynetwork.id.lyrams.kitbattleteams.listeners.DamageListener
import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class KitBattleTeams : JavaPlugin() {

    companion object {

        lateinit var instance: KitBattleTeams
        var max_team_size: Int = 4

    }

    override fun onEnable() {
        instance = this
        Database.init(this)

        this.saveDefaultConfig()

        max_team_size = this.config.getInt("max-team-size")

        getCommand("team").executor = TeamCMD()

        val pluginManager = server.pluginManager
        pluginManager.registerEvents(ConnectionListener(this), this)
        pluginManager.registerEvents(ChatListener(), this)
        pluginManager.registerEvents(DamageListener(), this)

        Bukkit.getScheduler().runTaskLaterAsynchronously(this, {

            Bukkit.getOnlinePlayers().forEach { Database.loadPlayerData(it) }

        }, 1L)

    }

    override fun onDisable() {

        Bukkit.getScheduler().cancelTasks(this)
        Database.shutdown()

    }

}