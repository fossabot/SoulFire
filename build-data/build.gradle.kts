plugins {
    id("sw.license-conventions")
    id("net.kyori.blossom")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

blossom {
    replaceToken("{version}", rootProject.version)
    replaceToken("{description}", rootProject.description)
    replaceToken("{url}", "https://github.com/AlexProgrammerDE/ServerWrecker")
    replaceToken("{commit}", rootProject.latestCommitHash())
}
