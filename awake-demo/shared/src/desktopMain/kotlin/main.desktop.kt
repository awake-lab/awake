// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import java.awt.Container
import java.awt.GraphicsEnvironment

val awtContainer = object : Container() {
    override fun getGraphicsConfiguration() =
        GraphicsEnvironment.getLocalGraphicsEnvironment()
            .defaultScreenDevice
            .defaultConfiguration
}

// call it when your custom compose app changes window position
fun onWindowUpdate(x: Int, y: Int, width: Int, height: Int) {
    awtContainer.setBounds(x, y, width, height)
}

val LocalLayerContainer: androidx.compose.runtime.ProvidableCompositionLocal<Container> =
    staticCompositionLocalOf { awtContainer }

@Composable
fun MainView() {
    CompositionLocalProvider(
        LocalLayerContainer provides awtContainer,
        LocalWindowInfo provides object : WindowInfo {
            override val isWindowFocused: Boolean = true
        }
    ) {
        App()
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}