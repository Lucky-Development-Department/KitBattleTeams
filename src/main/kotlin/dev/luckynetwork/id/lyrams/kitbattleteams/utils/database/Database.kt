package dev.luckynetwork.id.lyrams.kitbattleteams.utils.database

import com.github.alviannn.sqlhelper.SQLHelper
import com.github.alviannn.sqlhelper.utils.Closer
import dev.luckynetwork.id.lyrams.kitbattleteams.KitBattleTeams
import dev.luckynetwork.id.lyrams.kitbattleteams.managers.TeamPrivacy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.cache2k.Cache
import org.cache2k.Cache2kBuilder
import org.cache2k.event.CacheEntryExpiredListener
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Supplier


object Database {

    private lateinit var plugin: KitBattleTeams
    lateinit var helper: SQLHelper
    private lateinit var playerCache: Cache<UUID, TeamData>
    val threadPool: ExecutorService = Executors.newCachedThreadPool()

    fun init(plugin: KitBattleTeams) {
        Database.plugin = plugin

        playerCache = Cache2kBuilder.of(UUID::class.java, TeamData::class.java)
            .name("kitbattle_team_cache")
            .entryCapacity(Long.MAX_VALUE)
            .permitNullValues(false)
            .eternal(true)
            .addListener(CacheEntryExpiredListener<UUID, TeamData> { _, entry ->
                val teamData: TeamData = entry.value
                saveTeamData(teamData)
            })
            .addListener(CacheEntryExpiredListener<UUID, TeamData> { _, _ ->
                Bukkit.getOnlinePlayers().forEach { savePlayerData(it) }
            })
            .build()

        try {
            val dbFile = File(plugin.dataFolder, "database.db")

            helper = SQLHelper.newBuilder(SQLHelper.Type.H2)
                .setDatabase(dbFile.path)
                .toSQL()

            helper.connect()

            helper.executeQuery(
                "CREATE TABLE IF NOT EXISTS teamdata " +
                        "(team_id BIGINT AUTO_INCREMENT, leader VARCHAR(36), members VARCHAR(512), " +
                        "friendly_fire TINYINT(1), privacy VARCHAR(7));"
            )
            helper.executeQuery(
                "CREATE TABLE IF NOT EXISTS playerdata " +
                        "(uuid VARCHAR(36), team_id BIGINT NOT NULL);"
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun loadTeamData(player: Player): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {

            try {
                Closer().use { closer ->
                    val results =
                        closer.add(
                            helper.query("SELECT * FROM teamdata WHERE team_id = ?;")
                                .getResults(getTeamData(player).teamID)
                        )

                    val set = results.resultSet

                    if (set.next()) {

                        val members = set.getString("members")
                            .replace("[", "")
                            .replace("]", "")
                            .split(", ")

                        val membersArray = ConcurrentHashMap.newKeySet<UUID>()

                        for (member in members)
                            membersArray.add(UUID.fromString(member))

                        playerCache.remove(player.uniqueId)
                        playerCache.put(
                            player.uniqueId,
                            TeamData(
                                player,
                                set.getInt("team_id"),
                                UUID.fromString(set.getString("leader")),
                                membersArray,
                                set.getInt("friendly_fire") > 0,
                                TeamPrivacy.valueOf(set.getString("privacy"))
                            )
                        )

                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, threadPool)

    }

    private fun loadTeamData(uuid: UUID): CompletableFuture<TeamData?> {
        return CompletableFuture.supplyAsync(Supplier {
            var teamData: TeamData? = null

            try {
                Closer().use { closer ->

                    val results2 =
                        closer.add(helper.query("SELECT * FROM playerdata WHERE uuid = ?;").getResults(uuid))
                    val set2 = results2.resultSet

                    val teamID =
                        if (set2.next())
                            set2.getInt("team_id")
                        else
                            0

                    val results =
                        closer.add(
                            helper.query("SELECT * FROM teamdata WHERE team_id = ?;")
                                .getResults(teamID)
                        )

                    val set = results.resultSet

                    if (set.next()) {

                        val members = set.getString("members")
                            .replace("[", "")
                            .replace("]", "")
                            .split(", ")

                        val membersArray = ConcurrentHashMap.newKeySet<UUID>()

                        for (member in members)
                            membersArray.add(UUID.fromString(member))

                        teamData =
                            TeamData(
                                if (Bukkit.getPlayer(uuid) != null)
                                    Bukkit.getPlayer(uuid)
                                else
                                    null,
                                set.getInt("team_id"),
                                UUID.fromString(set.getString("leader")),
                                membersArray,
                                set.getInt("friendly_fire") > 0,
                                TeamPrivacy.valueOf(set.getString("privacy"))
                            )

                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            teamData

        }, threadPool)

    }

    fun loadPlayerData(player: Player): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {
            var exists = false

            try {
                Closer().use { closer ->
                    val results =
                        closer.add(helper.query("SELECT * FROM playerdata WHERE uuid = ?;").getResults(player.uniqueId))
                    val set = results.resultSet

                    if (set.next()) {

                        playerCache.put(
                            player.uniqueId,
                            TeamData(player, set.getInt("team_id"))
                        )

                        exists = true

                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (exists)
                loadTeamData(player)
            else
                createEmptyPlayerData(player)

        }, threadPool)

    }

    fun savePlayerData(player: Player): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {

            try {

                val teamData = getTeamData(player)

                helper.query("UPDATE playerdata SET team_id = ? WHERE uuid = ?")
                    .execute(teamData.teamID, player.uniqueId)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, threadPool)

    }

    fun saveTeamData(teamData: TeamData): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {

            if (teamData.teamID == 0)
                return@Runnable

            try {

                helper.query("UPDATE teamdata SET leader = ?, members = ?, friendly_fire = ?, privacy = ? WHERE team_id = ?;")
                    .execute(
                        teamData.leader,
                        teamData.members.toString(),
                        teamData.friendlyFire,
                        teamData.privacy.toString(),
                        teamData.teamID
                    )

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, threadPool)

    }

    private fun createEmptyPlayerData(player: Player): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {

            try {
                Closer().use {

                    helper.query("INSERT INTO playerdata (uuid, team_id) VALUES (?, ?);")
                        .execute(player.uniqueId, 0)

                    playerCache.put(
                        player.uniqueId,
                        TeamData(player)
                    )

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, threadPool)

    }

    fun unloadFromCache(player: Player) =
        playerCache.containsAndRemove(player.uniqueId)

    fun getTeamData(uuid: UUID): TeamData? {
        var teamData: TeamData? = playerCache.get(uuid)

        if (teamData == null)
            teamData = loadTeamData(uuid).join()

        return teamData
    }

    fun getTeamData(player: Player): TeamData =
        playerCache.get(player.uniqueId)


    fun shutdown() {
        playerCache.clearAndClose()

        try {
            helper.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        threadPool.shutdown()

        System.gc()
    }

}