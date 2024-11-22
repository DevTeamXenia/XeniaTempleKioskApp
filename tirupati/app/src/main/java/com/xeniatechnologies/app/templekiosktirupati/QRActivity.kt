package com.xeniatechnologies.app.templekiosktirupati

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.xeniatechnologies.app.templekiosktirupati.databinding.ActivityHomeBinding
import com.xeniatechnologies.app.templekiosktirupati.databinding.ActivityQractivityBinding

class QRActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQractivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQractivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this)
            .asGif()
            .load(R.drawable.time)
            .apply(
                RequestOptions()
                .override(Target.SIZE_ORIGINAL)
                .fitCenter()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .priority(Priority.HIGH))
            .into(binding.imgBackground)

        }
    }
