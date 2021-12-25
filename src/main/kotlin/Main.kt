// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.update4j.Configuration
import org.update4j.FileMetadata
import org.update4j.UpdateOptions
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.writer
import kotlin.streams.asSequence

val baseUrl = "https://raw.githubusercontent.com/davidwhitman/SelfUpdateTesting/master"

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(text)
        }
    }
}

fun main() = application {
    writeLocalUpdateConfig()
    updateApp()

    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

private fun updateApp() {
    val remoteConfigUrl = URI.create("$baseUrl/update-config.xml").toURL()

    val remoteConfig = runCatching {
        remoteConfigUrl.openStream().use { stream ->
            Configuration.read(stream.bufferedReader())
        }
    }
        .onFailure { println(it) }
        .getOrNull()

    remoteConfig?.update(UpdateOptions.archive(Path.of("update.zip")))
}

private fun writeLocalUpdateConfig(): Configuration? {
    val config = Configuration.builder()
        .baseUri(baseUrl)
        .basePath(Paths.get("").toAbsolutePath().toString())
        .files(
            FileMetadata.streamDirectory("build\\compose\\binaries\\main\\app\\SelfUpdateTesting")
                .asSequence()
                .onEach { r -> r.classpath(r.source.toString().endsWith(".jar")) }
                .toList())
        .build()


    Path.of("update-config.xml").writer().use {
        config.write(it)
        println("Wrote config to $it")
    }
    return config
}
