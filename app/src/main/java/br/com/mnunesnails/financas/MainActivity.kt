package br.com.mnunesnails.financas

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val padding = (24 * resources.displayMetrics.density).toInt()
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(padding, padding, padding, padding)
            setBackgroundColor(Color.rgb(255, 247, 250))
        }

        page.addView(TextView(this).apply {
            text = "MNunes Nails Finanças"
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(110, 38, 70))
        })

        page.addView(TextView(this).apply {
            text = "Página de teste\nInício do projeto em Kotlin."
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, padding, 0, 0)
            setTextColor(Color.rgb(70, 60, 65))
        })

        setContentView(page)
    }
}
