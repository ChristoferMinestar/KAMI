package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.Friends.friends
import me.zeroeightsix.kami.util.Friends.getFriendByName
import me.zeroeightsix.kami.util.MessageSendHelper
import me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage

/**
 * Created by 086 on 14/12/2017.
 * Updated by Xiaro on 14/08/20
 */
class FriendCommand : Command("friend", ChunkBuilder()
        .append("mode", true, EnumParser(arrayOf("add", "del", "list", "\$toggle"))) /* toggle has a $ in it in order to allow friending on the user named 'toggle'. stupid edge case */
        .append("name")
        .build(), "f") {

    override fun call(args: Array<String?>) {
        val subCommand = getSubCommand(args)
        if (!Friends.enabled && subCommand != SubCommands.NULL && subCommand != SubCommands.TOGGLE) {
            MessageSendHelper.sendWarningMessage("&6Warning: Friends is disabled!")
            MessageSendHelper.sendWarningMessage("These commands will still have effect, but will not visibly do anything.")
        }
        when (subCommand) {
            SubCommands.ADD -> {
                if (Friends.isFriend(args[1])) {
                    sendChatMessage("That player is already your friend.")
                } else {
                    // New thread because of potential internet connection made
                    Thread(Runnable {
                        val f = getFriendByName(args[1])
                        if (f == null) {
                            sendChatMessage("Failed to find UUID of " + args[1])
                        } else {
                            friends.value.add(f)
                            sendChatMessage("&7${f.username}&r has been friended.")
                        }
                    }).start()
                }
            }

            SubCommands.DEL -> {
                var name = ""
                val removed = friends.value.removeIf { friend ->
                    name = friend.username
                    name.equals(args[1], ignoreCase = true)
                }
                if (removed) sendChatMessage("&7$name&r has been unfriended.")
                else sendChatMessage("That player isn't your friend.")
            }

            SubCommands.LIST -> {
                if (friends.value.isEmpty()) {
                    sendChatMessage("You currently don't have any friends added. &7${commandPrefix}friend add <name>&r to add one.")
                } else {
                    var f = ""
                    friends.value.forEach { _f -> f += "    ${_f.username}\n" } // nicely format the chat output
                    sendChatMessage("Your friends: \n$f")
                }
            }

            SubCommands.IS_FRIEND -> {
                sendChatMessage(String.format(
                        if (Friends.isFriend(args[0])) "Yes, %s is your friend."
                        else "No, %s isn't a friend of yours.",
                        args[0]))
            }

            SubCommands.TOGGLE -> {
                Friends.enabled = !Friends.enabled
                if (Friends.enabled) {
                    sendChatMessage("Friends has been enabled")
                } else {
                    sendChatMessage("Friends has been disabled")
                }
            }

            SubCommands.NULL -> {
                val commands = args.joinToString(separator = " ")
                sendChatMessage("Invalid sub command $commands!")
            }
        }
    }

    private fun getSubCommand(args: Array<String?>): SubCommands {
        return when {
            args[0] == null || args[0]?.equals("list", ignoreCase = true) == true -> SubCommands.LIST

            args[0].equals("add", ignoreCase = true)
                    || args[0].equals("new", ignoreCase = true) -> SubCommands.ADD

            args[0].equals("del", ignoreCase = true)
                    || args[0].equals("remove", ignoreCase = true)
                    || args[0].equals("delete", ignoreCase = true) -> SubCommands.DEL

            args[0].equals("\$toggle", ignoreCase = true) -> SubCommands.TOGGLE

            args[1] == null -> SubCommands.IS_FRIEND

            else -> SubCommands.NULL
        }
    }

    private enum class SubCommands {
        ADD, DEL, LIST, IS_FRIEND, TOGGLE, NULL
    }

    init {
        setDescription("Add someone as your friend!")
    }
}