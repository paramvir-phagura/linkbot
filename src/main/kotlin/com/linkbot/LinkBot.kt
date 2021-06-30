package com.linkbot

import com.linkbot.model.Category
import com.linkbot.model.Media
import com.linkbot.util.Backup
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.entities.TextChannel
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

object LinkBot {

    private val log = LoggerFactory.getLogger(LinkBot::class.java)
    lateinit var jda: JDA
    private val mediaList = ArrayDeque<Media>()

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val startTime = System.currentTimeMillis()

            log.warn("Initializing JDA...")
            jda = JDABuilder.createDefault(Constants.TOKEN)
                .addEventListeners(CommandHandler)
                .build().awaitReady()

            log.warn("Initializing categories...")
            initCategories()
            log.info("Done. Initialized ${Category.categories.size} categories.")

            log.warn("Deleting old messages...")
            purgeAll()
            log.info("Done.")

            log.warn("Parsing messages...")
            parseMessages()
            log.info("Done. Parsed ${mediaList.size} media.")

            log.warn("Performing backup...")
            backup()
            log.info("Done.")

            log.warn("Sending messages...")
            sendMessages(mediaList)
            log.info("Done.")

            log.info("Finished in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)} seconds.")
        } catch (e: Exception) {
            log.error("Error starting!", e)
        }
    }

    private fun initCategories() {
        Category.categories = jda.textChannels
            .filter { it.name != "general" && it.name != "paste-here" }
            .map { Category(Category.toKeywords(it.name)) }.toMutableList()
    }

    fun purge(ch: TextChannel) {
        if (ch.name == "general" || ch.name == "paste-here")
            return

        val messages = ch.getHistoryFromBeginning(100)
        messages.queue { history -> ch.deleteMessages(history.retrievedHistory).queue() }
    }

    private fun purgeAll() {
        jda.textChannels.forEach { purge(it) }
    }

    private fun parseMessages(after: Message? = null) {
        val messages =
            if (after != null)
                jda.getTextChannelsByName(Constants.PASTE_CHANNEL, true).first().getHistoryAfter(after, 100)
            else
                jda.getTextChannelsByName(Constants.PASTE_CHANNEL, true).first().getHistoryFromBeginning(100)

        var lastMessage: Message? = null

        messages.queue { history ->
            history.retrievedHistory.forEach { message ->
                if (message.type != MessageType.DEFAULT)
                    return@forEach

                val text = message.contentDisplay
                val words = text.split(" ", "\n").toMutableList()
                val urlIndex = words.indexOfFirst { it.contains("http") }
                val url =
                    if (urlIndex != -1)
                        words.removeAt(urlIndex)
                    else
                        words.firstOrNull { it.contains("http") }
                val title = words.stream().collect(Collectors.joining(" "))
                val media = Media(title, url, Category.categoriesFor(title))

                mediaList.addFirst(media)
            }
            lastMessage = history.retrievedHistory.firstOrNull()
        }
        messages.complete()

        if (lastMessage != null)
            parseMessages(lastMessage)
    }

    private fun sendMessages(mediaList: Queue<Media>) {
        while (mediaList.peek() != null) {
            val media = mediaList.poll()

            if (media.url == null)
                continue

            val channels = channelsFor(media.categories.map { Category.toChannelName(it.keywords) })

            channels.forEach { ch -> ch.sendMessage("${media.title}\n${media.url}").complete() }
        }
    }

    private fun backup() {
        Backup.backupMedia(mediaList.toList())
    }

    fun shutdown() {
        jda.shutdown()
    }

    private fun channelsFor(keywords: List<String>) =
        jda.textChannels.filter { keywords.contains(it.name) }
}