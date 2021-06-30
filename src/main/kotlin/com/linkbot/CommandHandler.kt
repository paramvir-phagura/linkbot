package com.linkbot

import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object CommandHandler : ListenerAdapter() {

    private const val COMMAND_NOTATION = "!"

    override fun onMessageReceived(event: MessageReceivedEvent) {
        try {
            val message = event.message.contentDisplay;

            if (!message.startsWith(COMMAND_NOTATION) && message.length <= 1)
                return

            handleCommand(event, message.substring(1))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleCommand(event: MessageReceivedEvent, command: String) {
        if (event.channelType != ChannelType.TEXT)
            return

        val ch = event.channel as TextChannel

        when (command) {
            "purge" -> { LinkBot.purge(ch) }
            "shutdown" -> { LinkBot.shutdown() }
            else -> {}
        }
    }
}