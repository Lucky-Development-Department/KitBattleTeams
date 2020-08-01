package dev.luckynetwork.id.lyrams.kitbattleteams.managers

import org.bukkit.entity.Player

object AntiSpamManager {

    val antiSpamMap: HashMap<String, HashMap<Player, Long>> = HashMap()

}