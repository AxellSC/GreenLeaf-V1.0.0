package com.example.greenleaf_v100.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityLoginBinding
import com.example.greenleaf_v100.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    //Referencia a activity_login
    private lateinit var binding: ActivityStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}