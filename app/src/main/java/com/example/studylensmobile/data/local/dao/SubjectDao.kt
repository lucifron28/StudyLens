package com.example.studylensmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.studylensmobile.data.local.entity.SubjectEntity

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY title ASC")
    suspend fun getAll(): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getById(id: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(subjects: List<SubjectEntity>)

    @Query("DELETE FROM subjects")
    suspend fun deleteAll()
}
