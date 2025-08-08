/*
 * JUnique - Helps in preventing multiple instances of the same application
 *
 * Copyright (C) 2025 Moriafly
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version
 * 2.1, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License 2.1 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License version 2.1 along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

@file:Suppress("unused")

package com.moriafly.junique

import kotlinx.serialization.json.Json
import kotlin.system.exitProcess

/**
 * Ensures that only one instance of the application is running.
 *
 * If no other instance is running, it acquires a global lock and executes the [onCreate] callback.
 * If an instance is already running, it sends the command-line arguments [args] to the first instance,
 * and then the current instance exits.
 *
 * @param args The command-line arguments passed to the main function.
 * @param appId A unique application ID within the operating system, used to create and identify the lock.
 * @param onNewArgsReceive This callback is invoked when an already running instance receives the startup arguments from a new instance.
 * @param onCreate This is called only when this is the first instance of the application. The main startup logic of the application should be placed here.
 */
@UnstableJUniqueApi
fun singleInstanceApplication(
    args: Array<String>,
    appId: String,
    onNewArgsReceive: (args: Array<String>) -> Unit,
    onCreate: (args: Array<String>) -> Unit
) {
    try {
        // Try to acquire the lock and become the main instance
        JUnique.acquireLock(
            appId,
            MessageHandler { message ->
                val receivedArgs = Json.decodeFromString<Array<String>>(message)
                onNewArgsReceive(receivedArgs)
                return@MessageHandler null
            }
        )

        // Release the lock safely on shutdown
        Runtime.getRuntime().addShutdownHook(
            Thread {
                JUnique.releaseLock(appId)
            }
        )

        // If successful, run the main application logic
        onCreate(args)
    } catch (_: AlreadyLockedException) {
        // Failed to acquire lock: an instance is already running
        // Send args to the single instance
        val json = Json.encodeToString(args)
        JUnique.sendMessage(appId, json)
        // Exit current instance
        exitProcess(0)
    }
}
