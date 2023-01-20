package com.example.imageeditor

import com.example.imageeditor.R
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var imageEditorView: ImageEditorView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageEditorView = ImageEditorView(this)
        setContentView(imageEditorView)
        imageEditorView.init(R.drawable.test_image)
    }

    override fun onResume() {
        super.onResume()
        imageEditorView.queueEvent {
            imageEditorView.onResume()
        }
    }

    override fun onPause() {
        imageEditorView.queueEvent {
            imageEditorView.onPause()
        }
        super.onPause()
    }
}
