package com.frostfel.animelist.views.anime_detail
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.commit
import com.frostfel.animelist.R
import com.frostfel.animelist.databinding.ActivityAnimeDetailBinding
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
        startDetail(intent.getIntExtra(ANIME_ID_EXTRA, NO_ID))
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
            val episodes = findViewById<View>(R.id.episodes)
            val appBar = findViewById<View>(R.id.appBar)
            (scroll?.isShown == true && scroll.canScrollVertically(-1)) ||
                (episodes?.isShown == true && episodes.canScrollVertically(-1)) ||
                (appBar != null && appBar.top < 0)
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

    private fun startDetail(malId: Int) {
        if (malId == NO_ID) {
            finish()
            return
        }
        // Recreated activities keep their fragment.
        if (supportFragmentManager.findFragmentById(R.id.container) != null) return
        supportFragmentManager.commit {
            replace(R.id.container, AnimeDetailFragment.newInstance(malId))
        }
    }

    companion object {
        const val ANIME_ID_EXTRA = "ANIME_ID"
        private const val NO_ID = -1

        fun intent(context: Context, malId: Int): Intent =
            Intent(context, AnimeDetailActivity::class.java).putExtra(ANIME_ID_EXTRA, malId)
    }
}