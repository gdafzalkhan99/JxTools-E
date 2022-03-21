package com.picfix.tools.model.db

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.picfix.tools.bean.*
import com.picfix.tools.config.Constant
import com.picfix.tools.model.dao.*


@Database(
    entities = [FileWithType::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    //获取数据表操作实例
    abstract fun fileDao(): PicDao

    //单例模式
    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, Constant.ROOM_DB_NAME
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }

        //数据库升级用的
        var migration: Migration = object : Migration(2, 3) {
            override fun migrate(@NonNull database: SupportSQLiteDatabase) {
            }
        }
    }
}