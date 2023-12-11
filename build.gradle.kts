plugins {
    kotlin("jvm") version "1.9.20"
}

dependencies {
}

sourceSets {
    main {
        kotlin.srcDir("src")
    }
}

tasks {
    wrapper {
        gradleVersion = "8.5"
    }
}
