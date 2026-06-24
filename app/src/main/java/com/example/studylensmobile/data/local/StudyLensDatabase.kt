package com.example.studylensmobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.studylensmobile.data.local.dao.BoardScanDao
import com.example.studylensmobile.data.local.dao.ModuleDao
import com.example.studylensmobile.data.local.dao.SubjectDao
import com.example.studylensmobile.data.local.entity.BoardScanEntity
import com.example.studylensmobile.data.local.entity.ModuleEntity
import com.example.studylensmobile.data.local.entity.SubjectEntity

@Database(
    entities = [
        SubjectEntity::class,
        ModuleEntity::class,
        BoardScanEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StudyLensDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun moduleDao(): ModuleDao
    abstract fun boardScanDao(): BoardScanDao

    companion object {
        @Volatile private var INSTANCE: StudyLensDatabase? = null

        fun getInstance(context: Context): StudyLensDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudyLensDatabase::class.java,
                    "studylens.db"
                ).build().also { INSTANCE = it }
            }
    }
}
