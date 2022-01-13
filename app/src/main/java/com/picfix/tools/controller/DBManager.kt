package com.picfix.tools.controller

import android.content.Context
import com.picfix.tools.bean.*
import com.picfix.tools.model.db.AppDatabase
import com.picfix.tools.utils.JLog

object DBManager {


    fun insert(context: Context, file: FileWithType) {
        val dao = AppDatabase.getDatabase(context).fileDao()
        val acc = dao.find(file.path)
        if (acc != null) {
            dao.update(file)
        } else {
            dao.insert(file)
        }
    }


    fun getAllFiles(context: Context): ArrayList<FileWithType> {
        val dao = AppDatabase.getDatabase(context).fileDao()
        val list = dao.getAll()
        if (list.isNullOrEmpty()) {
            return arrayListOf()
        }

        return list as ArrayList<FileWithType>
    }

    fun getPicByKey(context: Context, type: String, minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): ArrayList<FileWithType> {
        val dao = AppDatabase.getDatabase(context).fileDao()
        val result = arrayListOf<FileWithType>()
        when (type) {
            "default" -> {
                JLog.i("$type+$minSize+$maxSize+$minDate+$maxDate")
                val list = dao.findPics(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                for (child in list) {
                    result.add(child)
                }
            }

            else -> {
                JLog.i("$type+$minSize+$maxSize+$minDate+$maxDate")
                return dao.findPics(type, minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
            }
        }

        if (result.isNullOrEmpty()) {
            return arrayListOf()
        }

        return result
    }

    fun getVoiceByKey(context: Context, sort: String, minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): ArrayList<FileWithType> {
        val dao = AppDatabase.getDatabase(context).fileDao()
        val result = arrayListOf<FileWithType>()
        JLog.i("$sort+$minSize+$maxSize+$minDate+$maxDate")
        when (sort) {
            "default" -> {
                val list = dao.findVoices(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "date_desc" -> {
                val list = dao.findVoicesByDateDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "date_asc" -> {
                val list = dao.findVoicesByDateAsc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "size_desc" -> {
                val list = dao.findVoicesBySizeDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "size_asc" -> {
                val list = dao.findVoicesBySizeAsc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }
        }

        if (result.isNullOrEmpty()) {
            return arrayListOf()
        }

        return result
    }


    fun getVideoByKey(context: Context, sort: String, minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): ArrayList<FileWithType> {
        val dao = AppDatabase.getDatabase(context).fileDao()
        val result = arrayListOf<FileWithType>()
        JLog.i("$sort+$minSize+$maxSize+$minDate+$maxDate")
        when (sort) {
            "default" -> {
                val list = dao.findVideos(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "date_desc" -> {
                val list = dao.findVideosByDateDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "date_asc" -> {
                val list = dao.findVideosByDateAsc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "size_desc" -> {
                val list = dao.findVideosBySizeDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "size_asc" -> {
                val list = dao.findVideosBySizeAsc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }
        }

        if (result.isNullOrEmpty()) {
            return arrayListOf()
        }

        return result
    }

    fun getDocByKey(context: Context, sort: String, minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): ArrayList<FileWithType> {
        val dao = AppDatabase.getDatabase(context).fileDao()
        val result = arrayListOf<FileWithType>()
        JLog.i("$sort+$minSize+$maxSize+$minDate+$maxDate")
        when (sort) {
            "default" -> {
                val list = dao.findDocs(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "date_desc" -> {
                val list = dao.findDocsByDateDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "date_asc" -> {
                val list = dao.findDocsByDateAsc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "size_desc" -> {
                val list = dao.findDocsBySizeDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "size_asc" -> {
                val list = dao.findDocsBySizeAsc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }
        }

        if (result.isNullOrEmpty()) {
            return arrayListOf()
        }

        return result
    }


    /**
     * 删除数据
     */
    fun delete(context: Context, file: FileWithType) {
        val dao = AppDatabase.getDatabase(context).fileDao()
        dao.delete(file)
    }

    /**
     * 删除数据
     */
    fun deleteFiles(context: Context) {
        val dao = AppDatabase.getDatabase(context).fileDao()
        dao.delete()
    }


}