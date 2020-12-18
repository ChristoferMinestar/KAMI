package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.manager.managers.WaypointManager.Waypoint
import me.zeroeightsix.kami.module.modules.movement.AutoWalk
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.math.CoordinateConverter.bothConverted
import me.zeroeightsix.kami.util.onMainThreadSafe
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.math.BlockPos

object WaypointCommand : ClientCommand(
    name = "waypoint",
    alias = arrayOf("wp"),
    description = "Manages waypoint."
) {

    private val stashRegex = "(\\(.* chests, .* shulkers, .* droppers, .* dispensers\\))".toRegex()
    private var confirmTime = 0L

    init {
        literal("add", "new", "create", "+") {
            string("name") { nameArg ->
                int("x") { xArg ->
                    int("y") { yArg ->
                        int("z") { zArg ->
                            execute {
                                add(nameArg.name, BlockPos(xArg.value, yArg.value, zArg.value))
                            }
                        }
                    }
                }

                blockPos("pos") { posArg ->
                    execute {
                        add(nameArg.name, posArg.value)
                    }
                }

                executeSafe {
                    add(nameArg.value, player.position)
                }
            }

            executeSafe {
                add("Unnamed", player.position)
            }
        }

        literal("del", "remove", "delete", "-") {
            int("id") { idArg ->
                executeAsync {
                    delete(idArg.value)
                }
            }
        }

        literal("goto", "path") {
            int("id") { idArg ->
                executeAsync {
                    goto(idArg.value)
                }
            }
        }

        literal("list") {
            execute {
                list()
            }
        }

        literal("stash", "stashes") {
            executeAsync {
                stash()
            }
        }

        literal("search") {
            string("name") { nameArg ->
                executeAsync {
                    search(nameArg.value)
                }
            }
        }

        literal("clear") {
            execute {
                clear()
            }
        }
    }

    private fun add(name: String, pos: BlockPos) {
        WaypointManager.add(pos, name)
        MessageSendHelper.sendChatMessage("Added waypoint at ${pos.asString()} in the ${InfoCalculator.dimension()} with name '&7$name&f'.")
    }

    private fun delete(id: Int) {
        if (WaypointManager.remove(id)) {
            MessageSendHelper.sendChatMessage("Removed waypoint with ID $id")
        } else {
            MessageSendHelper.sendChatMessage("No waypoint with ID $id")
        }
    }

    private fun goto(id: Int) {
        val waypoint = WaypointManager.get(id)
        onMainThreadSafe {
            if (waypoint != null) {
                if (AutoWalk.isEnabled) AutoWalk.disable()
                val pos = waypoint.currentPos()
                MessageSendHelper.sendBaritoneCommand("goto", pos.x.toString(), pos.y.toString(), pos.z.toString())
            } else {
                MessageSendHelper.sendChatMessage("Couldn't find a waypoint with the ID $id")
            }
        }
    }

    private fun list() {
        if (WaypointManager.waypoints.isEmpty()) {
            MessageSendHelper.sendChatMessage("No waypoints have been saved.")
        } else {
            MessageSendHelper.sendChatMessage("List of waypoints:")
            WaypointManager.waypoints.forEach {
                MessageSendHelper.sendRawChatMessage(format(it))
            }
        }
    }

    private fun stash() {
        val filtered = WaypointManager.waypoints.filter { it.name.matches(stashRegex) }
        if (filtered.isEmpty()) {
            MessageSendHelper.sendChatMessage("No stashes have been logged.")
        } else {
            MessageSendHelper.sendChatMessage("List of logged stashes:")
            filtered.forEach {
                MessageSendHelper.sendRawChatMessage(format(it))
            }
        }
    }

    private fun search(name: String) {
        val filtered = WaypointManager.waypoints.filter { it.name.equals(name, true) }
        if (filtered.isEmpty()) {
            MessageSendHelper.sendChatMessage("No results for &7$name&f")
        } else {
            MessageSendHelper.sendChatMessage("Result of search for &7$name&f:")
            filtered.forEach {
                MessageSendHelper.sendRawChatMessage(format(it))
            }
        }
    }

    private fun clear() {
        if (System.currentTimeMillis() - confirmTime > 15000L) {
            confirmTime = System.currentTimeMillis()
            MessageSendHelper.sendChatMessage("This will delete ALL your waypoints, run '&7${prefix}wp clear&f' again to confirm")
        } else {
            confirmTime = 0L
            WaypointManager.clear()
            MessageSendHelper.sendChatMessage("Waypoints have been &ccleared")
        }
    }

    private fun format(waypoint: Waypoint): String {
        return "${waypoint.id} [${waypoint.server}] ${waypoint.name} (${bothConverted(waypoint.dimension, waypoint.pos)})"
    }

}