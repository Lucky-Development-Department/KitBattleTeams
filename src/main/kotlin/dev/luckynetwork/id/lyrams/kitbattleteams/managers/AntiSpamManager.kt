package dev.luckynetwork.id.lyrams.kitbattleteams.managers

import dev.luckynetwork.id.lyrams.kitbattleteams.managers.enums.AntiSpamType
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

object AntiSpamManager {
    val antiSpamMap: EnumMap<AntiSpamType, HashMap<Player, Long>> = EnumMap(AntiSpamType::class.java)
}