package dev.luckynetwork.id.lyrams.kitbattleteams.managers

import dev.luckynetwork.id.lyrams.kitbattleteams.KitBattleTeams
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

fun Player.isSpyChat(): Boolean =
    this.hasMetadata("SPYCHAT")

fun Player.toggleTeamChatting(boolean: Boolean? = null): Boolean {
    if (boolean == null)
        if (!this.isSpyChat())
            this.player!!.setMetadata("SPYCHAT", FixedMetadataValue(KitBattleTeams.instance, true))
        else
            this.player!!.removeMetadata("SPYCHAT", KitBattleTeams.instance)
    else
        if (boolean == true)
            this.player!!.setMetadata("SPYCHAT", FixedMetadataValue(KitBattleTeams.instance, true))
        else
            this.player!!.removeMetadata("SPYCHAT", KitBattleTeams.instance)

    return this.isSpyChat()
}