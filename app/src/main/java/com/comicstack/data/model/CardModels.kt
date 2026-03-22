package com.comicstack.data.model

import kotlin.math.abs

enum class Rarity(
    val label: String,
    val colorHex: Long,
    val hpRange: IntRange,
    val atkRange: IntRange,
    val defRange: IntRange,
    val spdRange: IntRange
) {
    COMMON("Common", 0xFF6B7280, 100..150, 10..20, 5..15, 5..8),
    UNCOMMON("Uncommon", 0xFF4CAF50, 150..220, 20..35, 15..25, 8..11),
    RARE("Rare", 0xFF2196F3, 220..310, 35..55, 25..40, 11..14),
    SUPER_RARE("Super Rare", 0xFF9C27B0, 310..420, 55..75, 40..60, 14..17),
    LEGENDARY("Legendary", 0xFFFFB300, 420..500, 75..100, 60..80, 17..20);
}

enum class Finish { STATIC, FOIL, CHROME, PARALLAX, HOLOGRAPHIC }

data class CardStats(val hp: Int, val atk: Int, val def: Int, val spd: Int) {
    companion object {
        fun derive(seed: String, rarity: Rarity): CardStats {
            val h = abs((seed + rarity.name).hashCode())
            fun pick(r: IntRange, off: Int) = r.first + (abs(h + off) % (r.last - r.first + 1))
            return CardStats(pick(rarity.hpRange, 1), pick(rarity.atkRange, 2), pick(rarity.defRange, 3), pick(rarity.spdRange, 4))
        }
    }
}
