package com.sergiocruz.MatematicaPro.database

import androidx.room.*

@Dao
interface HistoryDAO {

    @Query("SELECT * FROM HistoryDataClass WHERE operation == :operation AND favorite == 1 ORDER BY primary_key DESC")
    suspend fun getAllFavoritesForOperation(operation: String): List<HistoryDataClass>?

    @Query("SELECT * FROM HistoryDataClass WHERE operation == :operation AND primary_key == :key AND favorite == 1 LIMIT 1")
    suspend fun getFavoriteForKeyAndOp(key: String, operation: String): HistoryDataClass?

    @Query("SELECT * FROM HistoryDataClass WHERE operation == :operation AND primary_key == :key LIMIT 1")
    suspend fun getResultForKeyAndOp(key: String, operation: String): HistoryDataClass?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveCard(card: HistoryDataClass?)

    @Query("UPDATE HistoryDataClass SET favorite = 1 WHERE favorite == 0 AND operation == :operation")
    suspend fun makeNonFavoriteFavorite(operation: String): Int

    @Query("UPDATE HistoryDataClass SET favorite = :favorite WHERE operation == :operation AND primary_key == :key")
    suspend fun makeHistoryItemFavorite(operation: String, key: String, favorite: Boolean)

    @Query("DELETE FROM HistoryDataClass WHERE operation == :operation AND primary_key == :key")
    suspend fun deleteHistoryItem(operation: String, key: String)

    @Query("DELETE FROM HistoryDataClass WHERE operation == :op AND favorite == 0")
    suspend fun deleteNonFavoritesFromOperation(op: String)

}
