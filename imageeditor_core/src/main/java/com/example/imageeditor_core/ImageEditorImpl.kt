package com.example.imageeditor_core

class ImageEditorImpl : ImageEditor {

    private val core = ImageEditorCore()

    override fun helloWorld() {
        core.helloWorld()
    }
}
