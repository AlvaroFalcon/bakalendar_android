package com.frostfel.animelist.views.anime_detail
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
        binding = ActivityAnimeDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        hideStatusBar()
        initSwipeToDismiss()
        val bundle = intent.extras
        val item = bundle?.getParcelable<AnimeWithPreferences?>(ANIME_EXTRA)
        startDetail(item)
    }


    /**
     * Drag down to close. While the content is scrolled or the cover is collapsed, a downward
     * drag scrolls back first, as usual.
     */
    private fun initSwipeToDismiss() {
        binding.dismissLayout.onDismiss = {
            finish()
            disableCloseAnimation()
        }
        binding.dismissLayout.canScrollUp = {
            val scroll = findViewById<View>(R.id.scroll)
            val appBar = findViewById<View>(R.id.appBar)
            (scroll?.canScrollVertically(-1) == true) || (appBar != null && appBar.top < 0)
        }
    }

    /** The content already slid out; skip the default close animation. */
    @Suppress("DEPRECATION")
    private fun disableCloseAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            overridePendingTransition(0, 0)
        }
    }

    // The theme has no action bar; requestWindowFeature() after super.onCreate() crashes.
    private fun hideStatusBar() {
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
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