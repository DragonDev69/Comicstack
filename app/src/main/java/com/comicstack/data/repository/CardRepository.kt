package com.comicstack.data.repository

import com.comicstack.data.db.CollectedCardDao
import com.comicstack.data.db.CollectedCardEntity
import com.comicstack.data.model.CardStats
import com.comicstack.data.model.Finish
import com.comicstack.data.model.Rarity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CollectedCardDao
) {
    fun getAllCards(): Flow<List<CollectedCardEntity>> = cardDao.getAll()

    suspend fun getCardForIssue(issueId: String): CollectedCardEntity? = cardDao.getByIssue(issueId)

    suspend fun mintCard(
        issueId: String,
        title: String,
        series: String,
        issueNum: Int,
        artist: String,
        coverType: String,
        coverAsset: String,
        rarity: Rarity
    ): CollectedCardEntity {
        val stats = CardStats.derive(issueId, rarity)
        val serial = (cardDao.maxSerial() ?: 0) + 1
        val card = CollectedCardEntity(
            uid = UUID.randomUUID().toString(),
            issueId = issueId,
            title = title,
            series = series,
            issueNum = issueNum,
            artist = artist,
            coverType = coverType,
            coverAsset = coverAsset,
            rarity = rarity.name,
            finish = Finish.STATIC.name, // Common = static
            serialNumber = serial,
            hp = stats.hp, atk = stats.atk, def = stats.def, spd = stats.spd,
            isRead = true
        )
        cardDao.insert(card)
        return card
    }
}
