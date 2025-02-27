package com.runemate.woodcutter;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.input.Keyboard;
import com.runemate.game.api.hybrid.local.Camera;
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.location.navigation.Landmark;
import com.runemate.game.api.hybrid.location.navigation.cognizant.ScenePath;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.hybrid.util.calculations.Distance;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.LoopingBot;
import com.runemate.game.api.script.framework.listeners.SettingsListener;
import com.runemate.game.api.script.framework.listeners.events.SettingChangedEvent;
import com.runemate.ui.setting.annotation.open.SettingsProvider;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem;
import com.sun.glass.events.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.Override;
import java.lang.String;
import com.runemate.pathfinder.Pathfinder;
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank;
/*
 * LoopingBot is the framework I recommend for most people. The other options are TaskBot and TreeBot which each have their own
 * use-cases, but realistically LoopingBot is the simplest in almost all cases.
 *
 * We implement the 'SettingsListener' interface here, because we want to be able to react when the user presses the "Start" button.
 *
 * Note: Please pay attention to the Java Naming Conventions! This is an important habit to get into, and it will make your code
 * easier to read and maintain.
 * https://www.geeksforgeeks.org/java-naming-conventions/
 */
public class SimpleWoodcutter extends LoopingBot implements SettingsListener {

    /*
     * This creates a logger instance for this class, which is useful for reporting status and for debugging issues.
     * With good logging, you should be able to see exactly what the bot is up to at any given time.
     */
    private static final Logger logger = LogManager.getLogger(SimpleWoodcutter.class);

    /*
     * RuneMate provides a default user interface, which includes "smart settings". The @SettingsProvider annotation tells RuneMate
     * that you want it to create the user interface using the settings descriptor, in this case using 'WoodcuttingSettings'.
     *
     * Once RuneMate has created these settings, it will set the value of this field so that we can access them, as you will see below.
     */
    @SettingsProvider(updatable = true)
    private WoodcuttingSettings settings;

    /*
     * Here I have created an enum that defines the valid states that the bot can be in. In this case, that is either 'CHOP' or 'DROP'.
     * I store the current state in a field, and update it as necessary.
     */
    private WoodcuttingState state = WoodcuttingState.CHOP;

    /*
     * This is just a simple flag that we use to wait until the user has pressed the start button in the user interface. If they haven't
     * pressed it yet, we don't want the bot to do anything.
     */
    private boolean settingsConfirmed = false;

    /*
    Declare Pathfinder instance
     */
    private Pathfinder pathfinder = Pathfinder.create();

    /*
    Globally declare instance variables
     */
    private static Area woodcuttingArea;
    private boolean bankModeEnabled = false;
    // ✅ Place getSafePlayer() here, above other bot logic methods!
    private Player getSafePlayer() {
        Player player = Players.getLocal();
        if (player == null) {
            logger.warn("Local player is null.");
        }
        return player;
    }
    /*
     * #onStart() is a method inherited from AbstractBot that we can override to perform actions when the bot is started.
     * In this implementation we are registering this class (which implements SettingsListener) with the EventDispatcher. This means
     * that the EventDispatcher will know to send any 'settings' events to this class.
     */
    @Override
    public void onStart(final String... arguments) {
        getEventDispatcher().addListener(this);
        // Immediately set the initial state based on user settings
        if (settings.shouldDropLogs()) {
            state = WoodcuttingState.DROP;
            logger.info("User has selected to drop logs. Bot will drop logs.");
        } else {
            state = WoodcuttingState.BANK;
            logger.info("User has selected to bank logs. Bot will bank logs.");
        }
        if (woodcuttingArea == null) {
            setWoodcuttingArea();  // 🔹 Always ensure an area is set before chopping
        }
        logger.info("Woodcutting bot initialized. Current area: {}", woodcuttingArea);
    }

    /*
     * This is where your bot logic goes! You can control how fast the bot loops by using 'setLoopDelay()' and providing your desired
     * loop delay (in milliseconds).
     *
     * It's good practice in bots to limit in-game interactions to 1 per loop.
     */
    @Override
    public void onLoop() {
        // Ensure bot doesn't start before user confirms settings
        if (!settingsConfirmed) return;

        // Check if the user presses F1 to set a new woodcutting area
        if (Keyboard.isPressed(KeyEvent.VK_F1)) {
            setWoodcuttingArea(); // Dynamically sets the woodcutting area
            logger.info("Woodcutting area set at: {}", woodcuttingArea);
        }
        if (Keyboard.isPressed(KeyEvent.VK_F2)) {
            bankModeEnabled = !bankModeEnabled;
            logger.info("Bank mode enabled: {}", bankModeEnabled);
            Execution.delay(500); // Prevents spam toggles
        }
        // ✅ Automatically determine whether to BANK or DROP logs when inventory is full
        if (Inventory.isFull()) {
            if (settings.shouldDropLogs()) {
                state = WoodcuttingState.DROP; // Drop logs instead of banking
            } else if (bankModeEnabled) {
                state = WoodcuttingState.BANK; // Bank logs if banking is enabled
            }
        }
        switch (state) {
            case CHOP:
                chopTrees();
                break;
            case DROP:
                dropLogs();
                break;
            case BANK:
                bankLogs();  // ✅ Now calls the new bankLogs() function!
                break;
        }
    }
    // This method will dynamically set the woodcutting area
    public void setWoodcuttingArea() {
        Player player = getSafePlayer(); // ✅ Get safe player reference
        if (player == null) {
            logger.warn("Cannot set woodcutting area. Player is null.");
            return; // ✅ Exit safely
        }
        Coordinate playerPos = player.getPosition();
        if (playerPos == null) {
            logger.warn("Cannot set woodcutting area. Player position is null.");
            return; // ✅ Prevents NullPointerException
        }

        woodcuttingArea = Area.rectangular(playerPos.derive(-3, -3), playerPos.derive(3, 3)); // ✅ Define a 7x7 tile area
        logger.info("Woodcutting area set to: {}", woodcuttingArea);
    }

    public void walkToBank() {
        Player Player = Players.getLocal(); // Use safe method to get player
        if (Player == null || Player.getPosition() == null) { // This if statement will dodge 'Null Pointer Exception' and avoid crashing the bot
            logger.warn("Cannot walk to the bank: Player or position is null.");
            return;
        }
        if (pathfinder.pathBuilder() // Otherwise begin path building, if it can't find a path, log a warning and stop
                .start(Players.getLocal().getPosition()) // Start from player's current position
                .destination(Landmark.BANK) // Target the nearest bank
                .preferSpeed() // Faster navigation
                .enableTeleports(false) // No teleports
                .avoidWilderness(true) // Avoid wilderness
                .findPath() == null) { // ✅ Ensure a path was actually found
            logger.warn("No valid path found. Cannot walk to the bank.");
            return;
        }
        // Walking to the bank if all goes well
        while (pathfinder.getLastPath() != null && pathfinder.getLastPath().isValid()) {
            pathfinder.getLastPath().step(); // ✅ Move towards the bank naturally
        }
    }
    public void walkBackToWoodcuttingArea() {
        // Ensure the player exists before trying to move
        Player player = Players.getLocal();
        if (player == null) {
            logger.warn("Cannot walk back to woodcutting area. Local player is null.");
            return;
        }

        // Ensure the player has a valid position
        Coordinate playerPos = player.getPosition();
        if (playerPos == null) {
            logger.warn("Cannot walk back to woodcutting area. Player position is null.");
            return;
        }

        // Ensure woodcutting area is set
        if (woodcuttingArea == null) {
            logger.warn("Woodcutting area is not set. Unable to return.");
            return;
        }
        // Build a path using Pathfinder
        if (pathfinder.pathBuilder()
                .start(Players.getLocal().getPosition()) // Start from player's current position
                .destination(woodcuttingArea) // Target the nearest bank
                .preferSpeed() // Faster navigation
                .enableTeleports(false) // No teleports
                .avoidWilderness(true) // Avoid wilderness
                .findPath() == null) { // ✅ Ensure a path was actually found
            logger.warn("❌ No valid path found. Cannot walk to the bank.");
            return;
        }
        // Walking to the set woodcuttingArea
        while (pathfinder.getLastPath() != null && pathfinder.getLastPath().isValid()) {
            pathfinder.getLastPath().step(); // ✅ Move towards the bank naturally
        }
        logger.info("Returned to woodcutting area.");
    }



    public void bankLogs() {  // bankLogs() remains focused only on banking actions
        walkToBank();
        if (Bank.isOpen()) {
            logger.info("✅ Successfully opened bank.");
            Bank.depositInventory();
            Execution.delay(1000, 2000);
            logger.info("📦 Logs successfully deposited.");
        } else {
            logger.warn("⚠️ Failed to open the bank.");
        }
        walkBackToWoodcuttingArea();
        state = WoodcuttingState.CHOP; // Return to chopping after banking
    }
    private void dropLogs() {
        // Get the log name from settings
        String logName = settings.getTreeType().getLogName();

        // Find the first log in the inventory
        SpriteItem logs = Inventory.newQuery().names(logName).results().first();

        // If no logs are found, switch back to CHOP mode
        if (logs == null) {
            state = WoodcuttingState.CHOP;
            logger.info("No more logs left, returning to chopping.");
            return;
        }

        // Attempt to drop logs and wait until they are removed from inventory
        if (logs.interact("Drop") && Execution.delayWhile(logs::isValid, 600)) {
            logger.info("Dropped a log.");
        }
    }
    private void chopTrees() {
        // First thing we want to do when we're meant to be chopping is checking that we can actually chop!
        // If our inventory is full, we want to update the state to 'DROP' or 'BANK' so that the bot handles logs accordingly.
        if (Inventory.isFull()) {
            state = settings.shouldDropLogs() ? WoodcuttingState.DROP : WoodcuttingState.BANK;
            logger.info("Inventory is full, switching state to: {}", state);
            return;
        }
        // Safely get the local player
        Player player = Players.getLocal();
        if (player == null || player.getPosition() == null) {
            logger.warn("Cannot chop trees: Player or position is null.");
            return;
        }
        // This gets a reference to the current player. We're going to use this to check if we're already animating
        // and avoid spam clicking, as well as walking towards the next tree.
        if (player == null) {
            logger.warn("Unable to find local player.");
            return;
        }

        // When our player is idle our animation ID will be -1. If our animation isn't -1, we can safely assume
        // that we're already chopping and don't need to do anything else!
        if (player.getAnimationId() != -1) {
            logger.info("Already chopping...");
            return;
        }
        // Ensure the bot is not in an empty location before looking for trees
        if (woodcuttingArea == null || GameObjects.newQuery().names(settings.getTreeType().getTreeName()).within(woodcuttingArea).results().isEmpty()) {
            logger.warn("No valid trees found in the area. Resetting woodcutting area...");
            setWoodcuttingArea();  // 🔹 Try resetting the area to avoid getting stuck
            return;
        }
        // RuneMate's QueryBuilders are a powerful way to locate virtually anything in the game.
        // Here we are using our 'WoodcuttingSettings' to work out which type of tree we want to chop,
        // and then looking for an object in-game that has that name.
        String treeName = settings.getTreeType().getTreeName();
        GameObject tree = GameObjects.newQuery().names(treeName).results().nearest();

        // Ensure that the tree exists and is still valid
        if (tree == null || !tree.isValid()) {
            logger.warn("No valid tree found: {}", treeName);
            return;
        }

        // Just because we managed to find a nearby tree doesn't mean that we can immediately interact with it!
        // This block of code will do a few things:
        //  1. Check if the tree is too far away and move towards it.
        //  2. Build a path to the tree, and walk it using 'step()'.
        //  3. Ensure the tree is visible for interaction.

        if (Distance.between(player, tree) > 6) {
            logger.info("We're far away from {}, walking towards it", tree);

            // Using ScenePath to walk efficiently towards the tree.
            ScenePath path = ScenePath.buildTo(tree);
            if (path != null) {
                path.step();
                // Wait until we are close enough to the tree before continuing
                Execution.delayUntil(() -> Distance.between(player, tree) <= 4, 1500);
            }
            return;
        }

        // If the tree isn't visible, turn the camera towards it.
        if (!tree.isVisible()) {
            Camera.concurrentlyTurnTo(tree);
        }

        // There's quite a lot to break down in this line, so let's take it step-by-step.
        //
        // Most entities in the game are 'Interactable', which means we can use the 'interact' method on them.
        // This method returns a boolean which will be 'true' when the interaction succeeded, and 'false' when the interaction fails.
        //
        // Likewise, the "delay" methods in the 'Execution' class also return a boolean.
        // Using 'delayUntil' will wait until either:
        //   1. The condition in the first parameter is met, in this case if the player is animating.
        //   2. The timeout in the last parameter is met, in this case 1200ms (or 2 game ticks).
        //
        // The second parameter is a "reset" condition, which resets the timeout while true.
        // In this example, it means that the 1200ms timeout will not start counting down until the player has stopped moving.
        //
        // The delay is necessary in order to stop the bot from spam-clicking the tree.
        // If both of these methods succeed, we know that we have successfully started chopping the tree.

        if (tree.interact("Chop down")) {
            boolean startedChopping = Execution.delayUntil(
                    () -> player.getAnimationId() != -1,  // Wait until chopping starts
                    1200
            );
            if (startedChopping) {
                logger.info("Chopping tree.");
            } else {
                logger.warn("Failed to start chopping.");
            }
        } else {
            logger.warn("Tree interaction failed.");
        }
    }



    /*
     * This method is called when the user presses the 'Start' button in the user interface.
     */
    @Override
    public void onSettingsConfirmed() {
        settingsConfirmed = true;
    }

    /*
     * Detects changes in user settings.
     */
    @Override
    public void onSettingChanged(SettingChangedEvent event) {
        // Reserved for future updates if needed
    }
}
