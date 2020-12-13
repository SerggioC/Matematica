package com.sergiocruz.MatematicaPro.database

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface HistoryDAO {

    @Query("SELECT * FROM HistoryDataClass WHERE operation == :operation AND persist == 1")
    suspend fun getFavoritesForOperation(operation: String): HistoryDataClass?

    @Query("SELECT * FROM HistoryDataClass WHERE operation == :operation AND primary_key == :key AND persist == 1 LIMIT 1")
    suspend fun getFavoriteForKeyAndOp(key: String, operation: String): HistoryDataClass?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveCard(card: HistoryDataClass?)

    @Query("UPDATE HistoryDataClass SET persist = :persist WHERE operation == :operation AND primary_key == :key")
    suspend fun makeHistoryItemPersistent(operation: String, key: String, persist: Boolean)

    @Query("DELETE FROM HistoryDataClass WHERE operation == :operation AND primary_key == :key")
    suspend fun deleteHistoryItem(operation: String, key: String)

    @Query("DELETE FROM HistoryDataClass WHERE operation == :op AND persist == 0")
    suspend fun deleteNonPersistentFromOperation(op: String)

}
