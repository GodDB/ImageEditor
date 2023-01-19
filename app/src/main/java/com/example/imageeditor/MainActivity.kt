package com.example.imageeditor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageEditorView = ImageEditorView(this)
        setContentView(imageEditorView)
    }
}
