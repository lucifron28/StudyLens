package com.example.studylensmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.studylensmobile.data.local.entity.ModuleEntity

@Dao
interface ModuleDao {
    @Query("SELECT * FROM modules WHERE subjectId = :subjectId ORDER BY updatedAt DESC")
    suspend fun getBySubject(subjectId: String): List<ModuleEntity>

    @Query("SELECT * FROM modules WHERE id = :id")
    suspend fun getById(id: String): ModuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(module: ModuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(modules: List<ModuleEntity>)

    @Query("DELETE FROM modules WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM modules WHERE subjectId = :subjectId")
    suspend fun deleteBySubject(subjectId: String)
}
