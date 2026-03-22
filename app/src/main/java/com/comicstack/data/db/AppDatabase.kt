package com.comicstack.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "collected_cards")
data class CollectedCardEntity(
    @PrimaryKey val uid: String,
    val issueId: String,
    val title: String,
    val series: String,
    val issueNum: Int,
    val artist: String,
    val coverType: String,
    val coverAsset: String,
    val rarity: String,
    val finish: String,
    val serialNumber: Int,
    val hp: Int,
    val atk: Int,
    val def: Int,
    val spd: Int,
    val isRead: Boolean = false,
    val earnedAt: Long = System.currentTimeMillis()
)

@Dao
interface CollectedCardDao {
    @Query("SELECT * FROM collected_cards ORDER BY earnedAt DESC")
    fun getAll(): Flow<List<CollectedCardEntity>>

    @Query("SELECT * FROM collected_cards WHERE issueId = :issueId LIMIT 1")
    suspend fun getByIssue(issueId: String): CollectedCardEntity?

    @Query("SELECT MAX(serialNumber) FROM collected_cards")
    suspend fun maxSerial(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CollectedCardEntity)
}

@Database(entities = [CollectedCardEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CollectedCardDao
}
