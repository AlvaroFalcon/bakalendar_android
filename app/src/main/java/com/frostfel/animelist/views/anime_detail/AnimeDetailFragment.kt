package com.frostfel.animelist.views.anime_detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.palette.graphics.Palette
import com.frostfel.animelist.R
import com.frostfel.animelist.databinding.FragmentAnimeDetailBinding
import com.frostfel.animelist.databinding.WatchSheetBinding
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.EpisodeItem
import com.frostfel.animelist.model.RelatedItem
import com.frostfel.animelist.model.StreamingLink
import com.frostfel.animelist.model.TrailerInfo
import com.frostfel.animelist.model.countdownText
import com.frostfel.animelist.model.nextEpisodeAt
import com.frostfel.animelist.model.thumbnailUrl
import com.frostfel.animelist.model.watchUrl
import com.frostfel.animelist.notifications.NotificationPermissionRequest
import com.frostfel.animelist.views.utils.loadCached
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

private const val ANIME_ID_PARAM = "ANIME_ID"
private const val SYNOPSIS_LINES = 5

/**
 * Collapsing cover with the title, at-a-glance stats, next episode, trailer, synopsis and
 * information. The accent colour comes from the cover (Palette), so every anime looks its own.
 */
@AndroidEntryPoint
class AnimeDetailFragment : Fragment() {
    private val viewModel by viewModels<AnimeDetailFragmentViewModel>()
    private val notificationPermission = NotificationPermissionRequest(this)
    private lateinit var binding: FragmentAnimeDetailBinding
    private var boundAnimeId: Int? = null
    private var synopsisExpanded = false
    private val relatedAdapter = RelatedAdapter(::openRelated)
    private val recommendationsAdapter = RelatedAdapter(::openRelated)
    private val episodesAdapter = EpisodesAdapter { showWhereToWatch(it) }
    private var streaming: List<StreamingLink> = emptyList()
    private var animeUrl: String? = null

    @ColorInt
    private var accent: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setAnimeId(requireArguments().getInt(ANIME_ID_PARAM))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnimeDetailBinding.inflate(inflater, container, false)
        accent = ContextCompat.getColor(requireContext(), R.color.primary_color)
        binding.toolbar.setNavigationOnClickListener { requireActivity().finish() }
        binding.readMore.setOnClickListener { toggleSynopsis() }
        binding.loadRetry.setOnClickListener { viewModel.retry() }
        binding.related.adapter = relatedAdapter
        binding.recommendations.adapter = recommendationsAdapter
        binding.episodes.adapter = episodesAdapter
        // The add animation fades rows to full opacity, which would undo the faded upcoming ones.
        binding.episodes.itemAnimator = null
        binding.episodesRetry.setOnClickListener { viewModel.loadEpisodes() }
        binding.detailTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = showTab(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
        binding.episodes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 8) binding.favoriteFab.shrink() else if (dy < -8) binding.favoriteFab.extend()
            }
        })
        // The favourite button folds into just the star while scrolling down.
        binding.scroll.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY + 8) binding.favoriteFab.shrink()
            else if (scrollY < oldScrollY - 8) binding.favoriteFab.extend()
        }
        observeData()
        return binding.root
    }

    private fun observeData() {
        viewModel.anime.observe(viewLifecycleOwner) {
            renderLoadState(it != null)
            it ?: return@observe
            setupView(it)
        }
        viewModel.loadFailed.observe(viewLifecycleOwner) {
            renderLoadState(viewModel.anime.value != null)
        }
        viewModel.related.observe(viewLifecycleOwner) { items ->
            binding.relatedTitle.isVisible = items.isNotEmpty()
            binding.related.isVisible = items.isNotEmpty()
            relatedAdapter.submitList(items)
        }
        viewModel.recommendations.observe(viewLifecycleOwner) { items ->
            binding.recommendationsTitle.isVisible = items.isNotEmpty()
            binding.recommendations.isVisible = items.isNotEmpty()
            recommendationsAdapter.submitList(items)
        }
        viewModel.episodes.observe(viewLifecycleOwner, ::renderEpisodes)
    }

    /** Only for anime that are not cached yet (e.g. opened from "Related"). */
    private fun renderLoadState(hasAnime: Boolean) {
        val failed = viewModel.loadFailed.value == true
        binding.loadState.isVisible = !hasAnime
        binding.loadProgress.isVisible = !hasAnime && !failed
        binding.loadError.isVisible = !hasAnime && failed
        binding.loadRetry.isVisible = !hasAnime && failed
    }

    private fun setupView(item: AnimeWithPreferences) {
        val anime = item.anime
        bindFavourite(item)
        // Everything else only changes when another anime is shown, not on every favourite tap.
        if (boundAnimeId == anime.malId) return
        boundAnimeId = anime.malId

        binding.collapsingToolbar.title = anime.title
        animeUrl = anime.url
        binding.heroImage.loadCached(anime.images.webp.largeImageUrl) { extractAccent() }
        bindStats(anime)
        bindGenres(anime)
        bindNextEpisode(anime)
        bindTrailer(anime.trailer)
        bindSynopsis(anime)
        bindInformation(anime)
        binding.malButton.isVisible = anime.url != null
        binding.malButton.setOnClickListener { anime.url?.let { openUrl(it) } }
        applyAccent()
    }

    private fun bindFavourite(item: AnimeWithPreferences) {
        val starred = item.userPreferences?.starred == true
        binding.favoriteFab.setText(if (starred) R.string.detail_in_favorites else R.string.detail_add_favorite)
        binding.favoriteFab.setIconResource(if (starred) R.drawable.ic_star_rounded else R.drawable.ic_star_outline)
        binding.favoriteFab.setOnClickListener {
            if (viewModel.onFavTap(item)) notificationPermission.requestIfNeeded()
        }
    }

    private fun bindStats(anime: Anime) {
        val group = binding.stats
        group.removeAllViews()
        if (anime.score > 0) {
            group.addView(statChip(getString(R.string.detail_score, anime.score), R.drawable.ic_star_rounded))
        }
        if (anime.rank > 0) group.addView(statChip(getString(R.string.detail_rank, anime.rank)))
        anime.type?.let { group.addView(statChip(it)) }
        if (anime.episodes > 0) {
            group.addView(
                statChip(resources.getQuantityString(R.plurals.detail_episodes, anime.episodes, anime.episodes))
            )
        }
        val season = anime.season?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        if (season != null && anime.year != null) group.addView(statChip("$season ${anime.year}"))
        group.isVisible = group.isNotEmpty()
    }

    private fun bindGenres(anime: Anime) {
        val group = binding.genres
        group.removeAllViews()
        anime.genres.forEach { group.addView(statChip(it.name).apply { tag = GENRE_TAG }) }
        group.isVisible = group.isNotEmpty()
    }

    private fun bindNextEpisode(anime: Anime) {
        val now = ZonedDateTime.now()
        val next = anime.nextEpisodeAt(now)
        binding.nextEpisodeCard.isVisible = next != null
        if (next == null) return
        val local = next.withZoneSameInstant(ZoneId.systemDefault())
        val premiere = anime.status == "Not yet aired"
        binding.nextEpisodeLabel.setText(if (premiere) R.string.detail_starts else R.string.detail_next_episode)
        binding.nextEpisodeCountdown.text = countdownText(requireContext(), now, next)
        binding.nextEpisodeWhen.text = getString(
            R.string.detail_when_local,
            local.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                .replaceFirstChar { it.titlecase(Locale.getDefault()) },
            local.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        )
    }

    private fun bindTrailer(trailer: TrailerInfo?) {
        val watchUrl = trailer?.watchUrl
        binding.trailer.isVisible = watchUrl != null
        if (watchUrl == null) return
        binding.trailerImage.loadCached(trailer.thumbnailUrl)
        binding.trailer.setOnClickListener { openTrailer(trailer.youtubeId!!, watchUrl) }
    }

    private fun bindSynopsis(anime: Anime) {
        synopsisExpanded = false
        binding.description.maxLines = SYNOPSIS_LINES
        binding.description.text = anime.synopsis ?: getString(R.string.detail_no_synopsis)
        binding.readMore.isVisible = false
        // Only offer "Read more" when the text is actually cut.
        binding.description.post {
            val layout = binding.description.layout ?: return@post
            val lines = layout.lineCount
            binding.readMore.isVisible = lines > 0 && layout.getEllipsisCount(lines - 1) > 0
        }
    }

    private fun toggleSynopsis() {
        synopsisExpanded = !synopsisExpanded
        TransitionManager.beginDelayedTransition(binding.scroll)
        binding.description.maxLines = if (synopsisExpanded) Int.MAX_VALUE else SYNOPSIS_LINES
        binding.readMore.setText(if (synopsisExpanded) R.string.detail_read_less else R.string.detail_read_more)
    }

    private fun bindInformation(anime: Anime) {
        val rows = listOfNotNull(
            anime.titleEnglish?.takeIf { it != anime.title }?.let { R.string.info_english_title to it },
            anime.titleJapanese?.let { R.string.info_japanese_title to it },
            anime.status?.let { R.string.info_status to it },
            anime.aired.stringValue?.let { R.string.info_aired to it },
            anime.broadcast.stringValue?.let { R.string.info_broadcast to it },
            anime.studios.joinToString { it.name }.takeIf { it.isNotEmpty() }?.let { R.string.info_studios to it },
            anime.source?.let { R.string.info_source to it },
            anime.duration?.let { R.string.info_duration to it },
            anime.rating?.let { R.string.info_rating to it },
            anime.themes.joinToString { it.name }.takeIf { it.isNotEmpty() }?.let { R.string.info_themes to it },
            anime.demographics.joinToString { it.name }.takeIf { it.isNotEmpty() }
                ?.let { R.string.info_demographic to it },
            anime.members.takeIf { it > 0 }
                ?.let { R.string.info_members to NumberFormat.getIntegerInstance().format(it) },
        )
        val container = binding.infoRows
        container.removeAllViews()
        rows.forEachIndexed { index, (label, value) ->
            if (index > 0) container.addView(divider())
            container.addView(infoRow(getString(label), value))
        }
    }

    /** Picks a colour from the cover and tints the screen with it. */
    private fun extractAccent() {
        val bitmap = (binding.heroImage.drawable as? BitmapDrawable)?.bitmap ?: return
        Palette.from(bitmap).generate { palette ->
            if (palette == null || !isAdded) return@generate
            val picked = palette.getDarkVibrantColor(
                palette.getVibrantColor(palette.getDarkMutedColor(palette.getDominantColor(accent)))
            )
            // Keep white text readable on top of it.
            accent = if (ColorUtils.calculateLuminance(picked) > 0.4) {
                ColorUtils.blendARGB(picked, Color.BLACK, 0.35f)
            } else picked
            applyAccent()
        }
    }

    private fun applyAccent() {
        val tint = ColorUtils.blendARGB(accent, Color.WHITE, 0.86f)
        val accentList = ColorStateList.valueOf(accent)
        binding.collapsingToolbar.setContentScrimColor(accent)
        binding.favoriteFab.backgroundTintList = accentList
        binding.nextEpisodeCard.setCardBackgroundColor(tint)
        binding.nextEpisodeIcon.imageTintList = accentList
        binding.nextEpisodeLabel.setTextColor(accent)
        binding.readMore.setTextColor(accent)
        binding.malButton.setTextColor(accent)
        binding.malButton.iconTint = accentList
        relatedAdapter.accent = accent
        episodesAdapter.accent = accent
        binding.detailTabs.setSelectedTabIndicatorColor(accent)
        binding.detailTabs.setTabTextColors(
            ContextCompat.getColor(requireContext(), R.color.textInputLayoutHint), accent
        )
        binding.malButton.strokeColor = ColorStateList.valueOf(ColorUtils.blendARGB(accent, Color.WHITE, 0.6f))
        // Genres take the accent; the neutral stats stay grey.
        val group = binding.genres
        (0 until group.childCount).map { group.getChildAt(it) as Chip }.forEach { chip ->
            chip.chipBackgroundColor = ColorStateList.valueOf(tint)
            chip.setTextColor(accent)
        }
    }

    private fun openRelated(item: RelatedItem) {
        if (item.isAnime) {
            startActivity(AnimeDetailActivity.intent(requireContext(), item.malId))
        } else {
            item.url?.let(::openUrl)
        }
    }

    /** Overview and Episodes share the collapsing cover; only one is visible. */
    private fun showTab(position: Int) {
        val episodes = position == TAB_EPISODES
        binding.scroll.isVisible = !episodes
        binding.episodesPage.isVisible = episodes
        binding.favoriteFab.extend()
        if (episodes) viewModel.loadEpisodes()
    }

    private fun renderEpisodes(state: EpisodesState) {
        binding.episodesProgress.isVisible = state is EpisodesState.Loading
        val loaded = state as? EpisodesState.Loaded
        streaming = loaded?.streaming.orEmpty()
        episodesAdapter.submitList(loaded?.episodes.orEmpty())
        val message = when {
            state is EpisodesState.Error -> R.string.episodes_error
            loaded != null && loaded.episodes.isEmpty() -> R.string.episodes_empty
            else -> null
        }
        binding.episodesMessage.isVisible = message != null
        message?.let { binding.episodesMessageText.setText(it) }
        binding.episodesRetry.isVisible = state is EpisodesState.Error
    }

    /** Platforms the series is on; the API does not link single episodes or know the region. */
    private fun showWhereToWatch(episode: EpisodeItem) {
        val sheet = WatchSheetBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext())
        sheet.sheetTitle.text = listOfNotNull(
            getString(R.string.watch_where_title, episode.number), episode.title
        ).joinToString(" · ")
        sheet.note.isVisible = streaming.isNotEmpty()
        if (streaming.isEmpty()) {
            sheet.sheetSubtitle.setText(R.string.watch_where_none)
        }
        streaming.forEach { link ->
            sheet.platforms.addView(platformRow(link.name.orEmpty()) {
                dialog.dismiss()
                link.url?.let(::openUrl)
            })
        }
        val malUrl = episode.url ?: animeUrl
        sheet.malLink.isVisible = malUrl != null
        sheet.malLink.setTextColor(accent)
        sheet.malLink.setOnClickListener {
            dialog.dismiss()
            malUrl?.let(::openUrl)
        }
        dialog.setContentView(sheet.root)
        dialog.behavior.skipCollapsed = true
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()
    }

    private fun platformRow(name: String, onClick: () -> Unit) = LinearLayout(requireContext()).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val horizontal = dp(22f).toInt()
        val vertical = dp(14f).toInt()
        setPadding(horizontal, vertical, horizontal, vertical)
        val ripple = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, ripple, true)
        setBackgroundResource(ripple.resourceId)
        setOnClickListener { onClick() }
        contentDescription = name
        addView(TextView(context).apply {
            text = name
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        addView(ImageView(context).apply {
            setImageResource(R.drawable.ic_open_in_new)
            imageTintList = ColorStateList.valueOf(accent)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }, LinearLayout.LayoutParams(dp(20f).toInt(), dp(20f).toInt()))
    }

    private fun statChip(text: String, iconRes: Int? = null) = Chip(requireContext()).apply {
        this.text = text
        isClickable = false
        isCheckable = false
        setEnsureMinTouchTargetSize(false)
        chipMinHeight = dp(28f)
        chipStrokeWidth = 0f
        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.sheet_chip_unselected))
        setTextColor(ContextCompat.getColor(context, R.color.black))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        if (iconRes != null) {
            chipIcon = ContextCompat.getDrawable(context, iconRes)
            chipIconTint = ColorStateList.valueOf(SCORE_STAR_COLOR)
            chipIconSize = dp(16f)
            isChipIconVisible = true
        }
    }

    private fun infoRow(label: String, value: String) = LinearLayout(requireContext()).apply {
        orientation = LinearLayout.HORIZONTAL
        val padding = dp(10f).toInt()
        setPadding(0, padding, 0, padding)
        addView(TextView(context).apply {
            text = label
            setTextColor(ContextCompat.getColor(context, R.color.textInputLayoutHint))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f))
        addView(TextView(context).apply {
            text = value
            gravity = Gravity.END
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.6f))
    }

    private fun divider() = View(requireContext()).apply {
        setBackgroundColor(ContextCompat.getColor(context, R.color.sheet_chip_unselected))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1f).toInt())
    }

    private fun dp(value: Float) = value * resources.displayMetrics.density

    /** Opens the YouTube app when installed, the browser otherwise. */
    private fun openTrailer(youtubeId: String, watchUrl: String) {
        val app = Intent(Intent.ACTION_VIEW, "vnd.youtube:$youtubeId".toUri())
        try {
            startActivity(app)
        } catch (noApp: ActivityNotFoundException) {
            openUrl(watchUrl)
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    companion object {
        private const val GENRE_TAG = "genre"
        private const val TAB_EPISODES = 1
        private val SCORE_STAR_COLOR = "#F5B301".toColorInt()

        fun newInstance(malId: Int) =
            AnimeDetailFragment().apply {
                arguments = Bundle().apply { putInt(ANIME_ID_PARAM, malId) }
            }
    }
}
