package dev.luckynetwork.id.lyrams.kitbattleteams.listeners

import dev.luckynetwork.id.lyrams.kitbattleteams.utils.database.Database
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class DamageListener : Listener {

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player || event.damager == null)
            return

        val victim = event.entity as Player
        val damager = event.damager
        val teamData = Database.getTeamData(victim)

        if (teamData.friendlyFire || teamData.members == null)
            return


        if (damager is Player && teamData.members!!.contains(damager.uniqueId) && damager != victim) {

            event.isCancelled = true

        } else if (damager is Arrow) {

            val shooter = damager.shooter
            if (shooter is Player && teamData.members!!.contains(shooter.uniqueId))
                event.isCancelled = true

        }

    }

}