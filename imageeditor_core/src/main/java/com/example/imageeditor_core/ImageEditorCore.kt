package com.example.imageeditor_core

internal class ImageEditorCore {

    /**
     * A native method that is implemented by the 'imageeditor_core' native library,
     * which is packaged with this application.
     */
    external fun helloWorld()

    companion object {
        // Used to load the 'imageeditor_core' library on application startup.
        init {
            System.loadLibrary("imageeditor_core")
        }
    }

}
