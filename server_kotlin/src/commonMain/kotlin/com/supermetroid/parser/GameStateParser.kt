package com.supermetroid.parser

import com.supermetroid.model.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Super Metroid Game State Parser
 * Converts raw memory data into structured game state
 * Equivalent to Python's SuperMetroidGameStateParser
 */
class GameStateParser {
    private val mutex = Mutex()

    // Persistent state for Mother Brain phases - once detected, stays detected
    private var motherBrainPhaseState = mutableMapOf(
        "mb1_detected" to false,
        "mb2_detected" to false
    )

    // Super Metroid memory layout
    private val memoryMap = mapOf(
        "health" to 0x7E09C2,
        "max_health" to 0x7E09C4,
        "missiles" to 0x7E09C6,
        "max_missiles" to 0x7E09C8,
        "supers" to 0x7E09CA,
        "max_supers" to 0x7E09CC,
        "power_bombs" to 0x7E09CE,
        "max_power_bombs" to 0x7E09D0,
        "reserve_energy" to 0x7E09D6,
        "max_reserve_energy" to 0x7E09D4,
        "room_id" to 0x7E079B,
        "area_id" to 0x7E079F,
        "game_state" to 0x7E0998,
        "player_x" to 0x7E0AF6,
        "player_y" to 0x7E0AFA,
        "items" to 0x7E09A4,
        "beams" to 0x7E09A8,
        "bosses" to 0x7ED828,
    )

    // Area names mapping
    private val areas = mapOf(
        0 to "Crateria",
        1 to "Brinstar",
        2 to "Norfair",
        3 to "Wrecked Ship",
        4 to "Maridia",
        5 to "Tourian"
    )

    suspend fun parseCompleteGameState(memoryData: Map<String, ByteArray>): GameState = mutex.withLock {
        try {
            // Parse basic stats from bulk read
            val basicStats = memoryData["basic_stats"]
            val stats = if (basicStats != null && basicStats.size >= 22) {
                parseBasicStats(basicStats)
            } else {
                mapOf<String, Int>()
            }

            // Parse location data
            val locationData = parseLocationData(
                memoryData["room_id"],
                memoryData["area_id"],
                memoryData["game_state"],
                memoryData["player_x"],
                memoryData["player_y"]
            )

            // Parse items with reset detection
            val items = parseItems(
                memoryData["items"],
                locationData,
                stats["health"] ?: 0,
                stats["missiles"] ?: 0,
                stats["max_missiles"] ?: 0
            )

            // Parse beams with reset detection
            val beams = parseBeams(
                memoryData["beams"],
                locationData,
                stats["health"] ?: 0,
                stats["missiles"] ?: 0,
                stats["max_missiles"] ?: 0
            )

            // Parse bosses
            val bosses = parseBosses(
                memoryData["main_bosses"],
                memoryData["crocomire"],
                memoryData["boss_plus_1"],
                memoryData["boss_plus_2"],
                memoryData["boss_plus_3"],
                memoryData["boss_plus_4"],
                memoryData["boss_plus_5"],
                memoryData["escape_timer_1"],
                memoryData["escape_timer_2"],
                memoryData["escape_timer_3"],
                memoryData["escape_timer_4"],
                locationData
            )

            return GameState(
                health = stats["health"] ?: 0,
                maxHealth = stats["max_health"] ?: 99,
                missiles = stats["missiles"] ?: 0,
                maxMissiles = stats["max_missiles"] ?: 0,
                supers = stats["supers"] ?: 0,
                maxSupers = stats["max_supers"] ?: 0,
                powerBombs = stats["power_bombs"] ?: 0,
                maxPowerBombs = stats["max_power_bombs"] ?: 0,
                reserveEnergy = stats["reserve_energy"] ?: 0,
                maxReserveEnergy = stats["max_reserve_energy"] ?: 0,
                roomId = locationData["room_id"] as? Int ?: 0,
                areaId = locationData["area_id"] as? Int ?: 0,
                areaName = locationData["area_name"] as? String ?: "Unknown",
                gameState = locationData["game_state"] as? Int ?: 0,
                playerX = locationData["player_x"] as? Int ?: 0,
                playerY = locationData["player_y"] as? Int ?: 0,
                items = items,
                beams = beams,
                bosses = bosses
            )

        } catch (e: Exception) {
            println("Error parsing game state: ${e.message}")
            GameState()
        }
    }

    private fun parseBasicStats(data: ByteArray): Map<String, Int> {
        if (data.size < 22) return emptyMap()

        return try {
            mapOf(
                "health" to data.readInt16LE(0),
                "max_health" to data.readInt16LE(2),
                "missiles" to data.readInt16LE(4),
                "max_missiles" to data.readInt16LE(6),
                "supers" to data.readInt16LE(8),
                "max_supers" to data.readInt16LE(10),
                "power_bombs" to data.readInt16LE(12),
                "max_power_bombs" to data.readInt16LE(14),
                "max_reserve_energy" to data.readInt16LE(18),
                "reserve_energy" to data.readInt16LE(20)
            )
        } catch (e: Exception) {
            println("Error parsing basic stats: ${e.message}")
            emptyMap()
        }
    }

    private fun parseLocationData(
        roomIdData: ByteArray?,
        areaIdData: ByteArray?,
        gameStateData: ByteArray?,
        playerXData: ByteArray?,
        playerYData: ByteArray?
    ): Map<String, Any> {
        return try {
            val roomId = roomIdData?.readInt16LE(0) ?: 0
            val areaId = areaIdData?.let { if (it.isNotEmpty()) it[0].toInt() and 0xFF else 0 } ?: 0
            val gameState = gameStateData?.readInt16LE(0) ?: 0
            val playerX = playerXData?.readInt16LE(0) ?: 0
            val playerY = playerYData?.readInt16LE(0) ?: 0

            mapOf(
                "room_id" to roomId,
                "area_id" to areaId,
                "area_name" to (areas[areaId] ?: "Unknown"),
                "game_state" to gameState,
                "player_x" to playerX,
                "player_y" to playerY
            )
        } catch (e: Exception) {
            println("Error parsing location data: ${e.message}")
            emptyMap()
        }
    }

    private fun shouldResetItemState(
        locationData: Map<String, Any>,
        health: Int,
        missiles: Int,
        maxMissiles: Int
    ): Boolean {
        val areaId = locationData["area_id"] as? Int ?: 0
        val roomId = locationData["room_id"] as? Int ?: 0

        // Reset scenarios - CONSERVATIVE:

        // 1. Intro scene (very specific early game indicators)
        val inStartingArea = areaId == 0 // Crateria
        val hasStartingHealth = health <= 99
        val inStartingRooms = roomId < 1000
        if (inStartingArea && hasStartingHealth && inStartingRooms) {
            println("🔄 ITEM STATE RESET: intro scene detected - Area:$areaId, Room:$roomId, Health:$health")
            return true
        }

        // 2. Very specific new game indicators only
        val definiteNewGame = (health == 99 && missiles == 0 && maxMissiles == 0 && roomId < 1000)
        if (definiteNewGame) {
            println("🔄 ITEM STATE RESET: new game detected - Area:$areaId, Room:$roomId, Health:$health, Missiles:$missiles/$maxMissiles")
            return true
        }

        return false
    }

    private fun parseItems(
        itemsData: ByteArray?,
        locationData: Map<String, Any>,
        health: Int,
        missiles: Int,
        maxMissiles: Int
    ): Map<String, Boolean> {
        if (itemsData == null || itemsData.size < 2) return emptyMap()

        // Check if we should reset item state
        if (shouldResetItemState(locationData, health, missiles, maxMissiles)) {
            return mapOf(
                "morph" to false,
                "bombs" to false,
                "varia" to false,
                "gravity" to false,
                "hijump" to false,
                "speed" to false,
                "space" to false,
                "screw" to false,
                "spring" to false,
                "grapple" to false,
                "xray" to false
            )
        }

        val itemValue = itemsData.readInt16LE(0)

        return mapOf(
            "morph" to ((itemValue and 0x0004) != 0),
            "bombs" to ((itemValue and 0x1000) != 0),
            "varia" to ((itemValue and 0x0001) != 0),
            "gravity" to ((itemValue and 0x0020) != 0),
            "hijump" to ((itemValue and 0x0100) != 0),
            "speed" to ((itemValue and 0x2000) != 0),
            "space" to ((itemValue and 0x0200) != 0),
            "screw" to ((itemValue and 0x0008) != 0),
            "spring" to ((itemValue and 0x0002) != 0),
            "grapple" to ((itemValue and 0x4000) != 0),
            "xray" to ((itemValue and 0x8000) != 0)
        )
    }

    private fun parseBeams(
        beamsData: ByteArray?,
        locationData: Map<String, Any>,
        health: Int,
        missiles: Int,
        maxMissiles: Int
    ): Map<String, Boolean> {
        if (beamsData == null || beamsData.size < 2) return emptyMap()

        // Check if we should reset beam state (same logic as items)
        if (shouldResetItemState(locationData, health, missiles, maxMissiles)) {
            println("🔄 BEAM STATE RESET: Resetting to starting state (no beams)")
            return mapOf(
                "charge" to false,  // Charge beam must be collected
                "ice" to false,
                "wave" to false,
                "spazer" to false,
                "plasma" to false,
                "hyper" to false
            )
        }

        val beamValue = beamsData.readInt16LE(0)

        return mapOf(
            "charge" to ((beamValue and 0x1000) != 0),
            "ice" to ((beamValue and 0x0002) != 0),
            "wave" to ((beamValue and 0x0001) != 0),
            "spazer" to ((beamValue and 0x0004) != 0),
            "plasma" to ((beamValue and 0x0008) != 0),
            "hyper" to ((beamValue and 0x0010) != 0)
        )
    }

    internal fun parseBosses(
        mainBossesData: ByteArray?,
        crocomireData: ByteArray?,
        bossPlus1Data: ByteArray?,
        bossPlus2Data: ByteArray?,
        bossPlus3Data: ByteArray?,
        bossPlus4Data: ByteArray?,
        @Suppress("UNUSED_PARAMETER") bossPlus5Data: ByteArray?,
        escapeTimer1Data: ByteArray?,
        escapeTimer2Data: ByteArray?,
        escapeTimer3Data: ByteArray?,
        escapeTimer4Data: ByteArray?,
        locationData: Map<String, Any>
    ): Map<String, Boolean> {
        // Basic boss flags - EXACT Python bit patterns
        val mainBosses = mainBossesData?.readInt16LE(0) ?: 0

        // Parse crocomire separately (matches Python logic)
        val crocomireValue = crocomireData?.readInt16LE(0) ?: 0
        val crocomireDefeated = ((crocomireValue and 0x0002) != 0) && (crocomireValue >= 0x0202)

        // Advanced boss detection using boss_plus patterns (matches Python logic)
        // bossPlus1 is not used in the function logic, so we can suppress the warning
        @Suppress("UNUSED_VARIABLE") val bossPlus1 = bossPlus1Data?.readInt16LE(0) ?: 0
        val bossPlus2 = bossPlus2Data?.readInt16LE(0) ?: 0
        val bossPlus3 = bossPlus3Data?.readInt16LE(0) ?: 0
        val bossPlus4 = bossPlus4Data?.readInt16LE(0) ?: 0

        // Phantoon detection - Fixed to match Python logic
        val phantoonDetected = (bossPlus3 != 0) && ((bossPlus3 and 0x01) != 0)

        // Botwoon detection - Fixed to match Python logic
        val botwoonDetected = (((bossPlus2 and 0x04) != 0) && (bossPlus2 > 0x0100)) ||
                             (((bossPlus4 and 0x02) != 0) && (bossPlus4 > 0x0001))

        // Draygon detection - FIXED! Only use boss_plus_3 == 0x0301 pattern
        val draygonDetected = (bossPlus3 == 0x0301)

        // Ridley detection - Fixed to match Python logic and avoid false positives
        var ridleyDetected = false
        // Check for specific Ridley patterns while excluding known false positives
        if ((bossPlus2 and 0x0001) != 0) {  // Check boss_plus_2 first
            // Current Ridley pattern: 0x0107, Draygon false positive: 0x0203
            if (bossPlus2 >= 0x0100 && bossPlus2 != 0x0203) {
                ridleyDetected = true
            }
        } else if ((bossPlus4 and 0x0001) != 0) {  // Check boss_plus_4 only as fallback
            // Exclude known Botwoon patterns (0x0003, 0x0007, etc.) and require higher values
            if (bossPlus4 >= 0x0011 && bossPlus4 != 0x0003 && bossPlus4 != 0x0007) {
                ridleyDetected = true
            }
        }

        // Golden Torizo detection - More liberal detection patterns to match Python logic
        // Multiple detection patterns for Golden Torizo
        val condition1 = ((bossPlus1 and 0x0700) != 0) && ((bossPlus1 and 0x0003) != 0)  // Basic pattern matching
        val condition2 = ((bossPlus2 and 0x0100) != 0) && (bossPlus2 >= 0x0400)  // Lowered threshold
        val condition3 = (bossPlus1 >= 0x0603)  // Direct value check
        val condition4 = ((bossPlus3 and 0x0100) != 0)  // Alternative address pattern

        val goldenTorizoDetected = (condition1 || condition2 || condition3 || condition4)

        // Mother Brain phase detection
        val motherBrainDefeated = (mainBosses and 0x0001) != 0

        // Check if we're in the Mother Brain room
        val areaId = locationData["area_id"] as? Int ?: 0
        val roomId = locationData["room_id"] as? Int ?: 0
        val inMotherBrainRoom = (areaId == 5 && roomId == 56664)

        // Check escape timers for MB2 detection
        val escapeTimer1 = escapeTimer1Data?.readInt16LE(0) ?: 0
        val escapeTimer2 = escapeTimer2Data?.readInt16LE(0) ?: 0
        val escapeTimer3 = escapeTimer3Data?.readInt16LE(0) ?: 0
        val escapeTimer4 = escapeTimer4Data?.readInt16LE(0) ?: 0
        val escapeTimerActive = escapeTimer1 > 0 || escapeTimer2 > 0 || escapeTimer3 > 0 || escapeTimer4 > 0

        // Nuclear reset scenario - reset MB2 if we're back in MB room with reasonable missile count
        if (inMotherBrainRoom && motherBrainPhaseState["mb2_detected"] == true) {
            // Check if we have reasonable missile count (indicating a new fight)
            val missiles = locationData["missiles"] as? Int ?: 0
            val maxMissiles = locationData["max_missiles"] as? Int ?: 1
            val hasReasonableMissiles = missiles > (maxMissiles * 0.7)

            if (hasReasonableMissiles) {
                println("🚨 NUCLEAR RESET: In MB room with $missiles/$maxMissiles missiles - force clearing MB2")
                motherBrainPhaseState["mb2_detected"] = false
            }
        }

        // MB1 detection - if in MB room and MB bit set
        if (motherBrainDefeated && inMotherBrainRoom) {
            motherBrainPhaseState["mb1_detected"] = true
            println("🧠 MB1 DETECTED: In MB room with MB bit set")
        }

        // MB2 detection - if MB1 detected and escape timer active
        if (motherBrainPhaseState["mb1_detected"] == true && escapeTimerActive) {
            motherBrainPhaseState["mb2_detected"] = true
            println("🧠 MB2 DETECTED: MB1 already detected and escape timer active")
        }

        return mapOf(
            // Basic bosses - Python bit patterns (REMOVED incorrect Draygon detection)
            "bomb_torizo" to ((mainBosses and 0x0004) != 0),      // bit 2 ✅
            "kraid" to ((mainBosses and 0x0100) != 0),            // bit 8 ✅
            "spore_spawn" to ((mainBosses and 0x0200) != 0),      // bit 9 ✅
            "mother_brain" to motherBrainDefeated,                // bit 0 ✅

            // Advanced bosses using correct patterns
            "crocomire" to crocomireDefeated,                     // Special crocomire logic ✅
            "phantoon" to phantoonDetected,                       // Advanced detection ✅
            "botwoon" to botwoonDetected,                        // Advanced detection ✅
            "draygon" to draygonDetected,                        // FIXED: Only 0x0301 pattern ✅
            "ridley" to ridleyDetected,                          // Advanced detection with multiple patterns ✅
            "golden_torizo" to goldenTorizoDetected,             // Advanced detection with multiple conditions ✅
            "mother_brain_1" to motherBrainPhaseState["mb1_detected"]!!,
            "mother_brain_2" to motherBrainPhaseState["mb2_detected"]!!,
            "samus_ship" to false  // End-game detection - keep simple for now
        )
    }

    suspend fun resetMotherBrainCache() = mutex.withLock {
        motherBrainPhaseState["mb1_detected"] = false
        motherBrainPhaseState["mb2_detected"] = false
        println("🔄 MB cache reset to default (not detected)")
    }

    // Extension function to read little-endian 16-bit integers from ByteArray
    private fun ByteArray.readInt16LE(offset: Int): Int {
        if (offset + 1 >= size) return 0
        return (this[offset].toInt() and 0xFF) or ((this[offset + 1].toInt() and 0xFF) shl 8)
    }
}
