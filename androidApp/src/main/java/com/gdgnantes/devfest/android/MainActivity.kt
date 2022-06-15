package com.gdgnantes.devfest.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gdgnantes.devfest.Greeting
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint

fun greet(): String {
    return Greeting().greeting()
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tv: TextView = findViewById(R.id.text_view)
        tv.text = greet()
    }
}
