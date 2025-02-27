package com.runemate.woodcutter;

import com.runemate.ui.setting.annotation.open.*;
import com.runemate.ui.setting.open.*;

/*
 * This is a very simple implementation of a settings descriptor. The descriptor needs to be annotated with '@SettingsGroup' and extend
 * 'Settings' in order for RuneMate to identify it as such.
 *
 * This is an interface, so you do not need to provide implementations for the methods. However, you can mark the methods as 'default'
 * if you want to provide default values for your settings.
 */
@SettingsGroup
public interface WoodcuttingSettings extends Settings {

    /*
     * The @Setting annotation provides RuneMate with the information it needs to display this setting on the user interface.
     * The 'key' attribute tells RuneMate what name to store the setting under.
     * The 'title' attribute tells RuneMate what name to display next to the setting in the user interface.
     * The 'order' attribute tells RuneMate how to order the settings.
     */

    /*
     * Tree Type Selection:
     * - Allows the user to specify the type of tree the bot should prioritize.
     * - Defaults to "NORMAL" (regular trees).
     */
    @Setting(key = "treeType", title = "Tree type", order = 1)
    default TreeType getTreeType() {
        return TreeType.NORMAL;
    }

    /*
     * Drop Logs Instead of Banking:
     * - If enabled (true), logs will be dropped instead of being taken to the bank.
     * - If disabled (false), logs will be banked.
     * - Default setting: Banking logs.
     */
    @Setting(key = "dropLogs", title = "Drop logs instead of banking?", order = 2)
    default boolean shouldDropLogs() {
        return false; // Default is now banking logs instead of dropping
    }

    /*
     * Max Players Per Tree:
     * - Specifies how many players can be cutting the same tree before the bot looks for another.
     * - Helps the bot avoid trees that are too crowded.
     * - Default value: 2 (avoids trees with more than 2 players cutting them).
     */
    @Setting(key = "maxPlayersPerTree", title = "Max players per tree", description = "The bot will exclude trees being chopped above the set maximum players per tree", order = 3)
    default int getMaxPlayersPerTree() {
        return 2; // Avoid trees with more than 2 players
    }

    /*
     * Minimum Tree Timeout:
     * - The shortest time (in milliseconds) the bot will wait before considering moving on.
     * - Ensures the bot doesn't leave too quickly before giving the tree a chance to be chopped.
     * - Default value: 3000ms (3 seconds).
     */
    @Setting(key = "minTreeTimeout", title = "Minimum wait time for a tree (ms)", description = "The shortest time (in milliseconds) the bot will wait before deciding to switch trees." , order = 5)
    default int getMinTreeTimeout() {
        return 3000; // Minimum 3 seconds
    }

    /*
     * Maximum Tree Timeout:
     * - The longest time (in milliseconds) the bot is allowed to wait before deciding to switch trees.
     * - Helps balance patience vs efficiency in woodcutting.
     * - Default value: 7000ms (7 seconds).
     */
    @Setting(key = "maxTreeTimeout", title = "Maximum wait time for a tree (ms)", description = "The longest time (in milliseconds) the bot will wait before deciding to switch trees.", order = 6)
    default int getMaxTreeTimeout() {
        return 7000; // Maximum 7 seconds
    }
}
