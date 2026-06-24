package com.example.studylensmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.studylensmobile.data.local.entity.BoardScanEntity

@Dao
interface BoardScanDao {
    @Query("SELECT * FROM board_scans ORDER BY createdAt DESC")
    suspend fun getAll(): List<BoardScanEntity>

    @Query("SELECT * FROM board_scans WHERE id = :id")
    suspend fun getById(id: String): BoardScanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(scans: List<BoardScanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(scan: BoardScanEntity)

    @Query("DELETE FROM board_scans WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM board_scans")
    suspend fun deleteAll()
}
