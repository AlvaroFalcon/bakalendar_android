package com.frostfel.animelist.views.anime_detail
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.frostfel.animelist.R
import com.frostfel.animelist.databinding.ActivityAnimeDetailBinding
import com.frostfel.animelist.model.AnimeWithPreferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnimeDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = ActivityAnimeDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val bundle = intent.extras
        val item = bundle?.getParcelable<AnimeWithPreferences?>(ANIME_EXTRA)
        startDetail(item)
    }


    private fun startDetail(anime: AnimeWithPreferences?) {
        if(anime == null) {
            finish()
            return
        }
        val fragment = AnimeDetailFragment.newInstance(anime)
        supportFragmentManager.commit {
            replace(R.id.container, fragment)
        }
    }

    companion object {
        const val ANIME_EXTRA = "ANIME_EXTRA"
    }
}