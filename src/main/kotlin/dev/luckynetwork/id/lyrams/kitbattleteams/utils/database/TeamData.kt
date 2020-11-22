package dev.luckynetwork.id.lyrams.kitbattleteams.utils.database

import dev.luckynetwork.id.lyrams.kitbattleteams.KitBattleTeams
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.enums.TeamPrivacy
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.util.*

data class TeamData(
    var player: Player? = null,
    var teamID: Int = 0,
    var leader: UUID? = null,
    var members: MutableSet<UUID>? = null,
    var friendlyFire: Boolean = false,
    var privacy: TeamPrivacy = TeamPrivacy.PRIVATE
)

fun TeamData.isTeamChatting() =
    this.player!!.hasMetadata("TEAMCHAT")

fun TeamData.toggleTeamChatting(boolean: Boolean? = null): Boolean {
    if (boolean == null)
        if (!this.isTeamChatting())
            this.player!!.setMetadata("TEAMCHAT", FixedMetadataValue(KitBattleTeams.instance, true))
        else
            this.player!!.removeMetadata("TEAMCHAT", KitBattleTeams.instance)
    else
        if (boolean == true)
            this.player!!.setMetadata("TEAMCHAT", FixedMetadataValue(KitBattleTeams.instance, true))
        else
            this.player!!.removeMetadata("TEAMCHAT", KitBattleTeams.instance)

    return this.isTeamChatting()
}
