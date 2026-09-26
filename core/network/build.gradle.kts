plugins {
    id("devfest.kmp.library")
    alias(libs.plugins.appollo)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.bundles.appollo)
        }
    }
}

apollo {
    service("service") {
        packageName.set("com.gdgnantes.devfest.core.network.graphql")
        plugin("com.apollographql.cache:normalized-cache-apollo-compiler-plugin:${libs.versions.appolloCache.get()}")
        pluginArgument("com.apollographql.cache.packageName", packageName.get())
    }
}
