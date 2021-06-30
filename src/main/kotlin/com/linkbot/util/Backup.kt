package com.linkbot.util

import com.linkbot.model.Media
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

object Backup {

    private val fileName: String

    private val dateFormatter = SimpleDateFormat("HH-mm-ss-dd-MM-yyyy");

    init {
        fileName = "${dateFormatter.format(Date()).toString()}-BACKUP.txt"
    }

    fun backupMedia(media: List<Media>) {
        Files.newBufferedWriter(Paths.get("./backup/$fileName")).use { out ->
            media.forEach { media ->
                out.write("${media.title}\n${media.url ?: ""}\n\n")
            }
        }
    }
}