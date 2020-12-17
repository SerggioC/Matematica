package com.sergiocruz.MatematicaPro.database

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface HistoryDAO {

    @Query("SELECT * FROM HistoryDataClass WHERE operation == :operation AND persist == 1 ORDER BY primary_key DESC")
    suspend fun getAllFavoritesForOperation(operation: String): List<HistoryDataClass>?

    @Query("SELECT * FROM HistoryDataClass WHERE operation == :operation AND primary_key == :key AND persist == 1 LIMIT 1")
    suspend fun getFavoriteForKeyAndOp(key: String, operation: String): HistoryDataClass?

    @Query("SELECT * FROM HistoryDataClass WHERE operation == :operation AND primary_key == :key LIMIT 1")
    suspend fun getResultForKeyAndOp(key: String, operation: String): HistoryDataClass?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveCard(card: HistoryDataClass?)

    @Query("UPDATE HistoryDataClass SET persist = 1 WHERE persist == 0 AND operation == :operation")
    suspend fun makeNonFavoriteFavorite(operation: String)

    @Query("UPDATE HistoryDataClass SET persist = :persist WHERE operation == :operation AND primary_key == :key")
    suspend fun makeHistoryItemPersistent(operation: String, key: String, persist: Boolean)

    @Query("DELETE FROM HistoryDataClass WHERE operation == :operation AND primary_key == :key")
    suspend fun deleteHistoryItem(operation: String, key: String)

    @Query("DELETE FROM HistoryDataClass WHERE operation == :op AND persist == 0")
    suspend fun deleteNonPersistentFromOperation(op: String)

}
