import com.runemate.game.api.bot.data.Category

plugins {
    id("java")
    id("com.runemate") version "1.5.1"
}

group = "com.runemate.party"
version = "1.0.0"

runemate {
    devMode = true
    autoLogin = true

    /*
     * The submission token is used by Gradle to authenticate with RuneMate servers when publishing bots to the store.
     * You can get a token from the RuneMate developer panel, and store it in your root 'gradle.properties' file.
     * - On Windows, that will be in %userprofile%\.gradle (make one if it doesn't exist)
     * - On Mac/Linux, that will be in ~/.gradle
     *
     * Do not specify it in this file, it will be detected automatically if declared in your gradle.properties under the key
     * 'runemateSubmissionToken', I have only included it here so that you are aware of it
     */
    submissionToken = ""

    manifests {
        create("Basic Woodcutter") {
            mainClass = "com.runemate.woodcutter.SimpleWoodcutter"
            tagline = "Max's Woodcutter"
            description = "This woodcutter will do short-range woodcutting, with the ability to Chop, Drop, and Bank"
            version = "1.0.0"
            internalId = "maximo-woodcutter"

            categories(Category.WOODCUTTING)
        }

    }
}

tasks.runClient {
    dependsOn(tasks.build)
}