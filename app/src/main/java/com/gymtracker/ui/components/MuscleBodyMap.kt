package com.gymtracker.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gymtracker.R
import com.gymtracker.domain.MuscleLoad
import com.gymtracker.domain.MuscleGroup
import com.gymtracker.ui.theme.fatigueColor

private const val OVERLAY_ALPHA = 0.55f
private const val IMAGE_RATIO = 470f / 836f

@Composable
fun MuscleBodyMap(
    loadData: List<MuscleLoad>,
    modifier: Modifier = Modifier,
    showFront: Boolean = true,
    selectedMuscle: MuscleGroup? = null,
    accessibilityDescription: String,
    onMuscleTap: (MuscleGroup?) -> Unit = {}
) {
    val loadMap = loadData.associateBy { it.muscle }
    val masks = if (showFront) frontMasks else backMasks
    val context = LocalContext.current

    val maskHitTargets = remember(showFront) {
        masks.map { (muscle, maskRes) ->
            val bitmap = BitmapFactory.decodeResource(context.resources, maskRes).asImageBitmap()
            Pair(muscle, bitmap.toPixelMap())
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "muscleGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .aspectRatio(IMAGE_RATIO)
            .semantics { contentDescription = accessibilityDescription }
            .pointerInput(showFront) {
                detectTapGestures { offset ->
                    val xPct = (offset.x / size.width).coerceIn(0f, 1f)
                    val yPct = (offset.y / size.height).coerceIn(0f, 1f)
                    val tapped = maskHitTargets.firstOrNull { (_, pixelMap) ->
                        val px = (xPct * pixelMap.width).toInt().coerceIn(0, pixelMap.width - 1)
                        val py = (yPct * pixelMap.height).toInt().coerceIn(0, pixelMap.height - 1)
                        pixelMap[px, py].alpha > 0f
                    }
                    onMuscleTap(tapped?.first)
                }
            }
    ) {
        Image(
            painter = painterResource(id = if (showFront) R.drawable.body_front else R.drawable.body_back),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        masks.forEach { (muscle, maskRes) ->
            val load = loadMap[muscle]?.loadScore ?: 0f
            Image(
                painter = painterResource(id = maskRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(
                    fatigueColor(load).copy(alpha = OVERLAY_ALPHA),
                    BlendMode.SrcIn
                )
            )
            if (muscle == selectedMuscle) {
                Image(
                    painter = painterResource(id = maskRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(
                        Color.White.copy(alpha = glowAlpha),
                        BlendMode.SrcIn
                    )
                )
            }
        }
    }
}

val frontMuscles: Set<MuscleGroup> by lazy { frontMasks.map { it.first }.toSet() }
val backMuscles: Set<MuscleGroup> by lazy { backMasks.map { it.first }.toSet() }

private val frontMasks: List<Pair<MuscleGroup, Int>> = listOf(
    MuscleGroup.NECK to R.drawable.mask_front_neck,
    MuscleGroup.FRONT_DELTS to R.drawable.mask_front_front_delts,
    MuscleGroup.CHEST_UPPER to R.drawable.mask_front_chest_upper,
    MuscleGroup.CHEST_MID to R.drawable.mask_front_chest_mid,
    MuscleGroup.CHEST_LOWER to R.drawable.mask_front_chest_lower,
    MuscleGroup.BICEPS_LONG_HEAD to R.drawable.mask_front_biceps_long_head,
    MuscleGroup.BICEPS_SHORT_HEAD to R.drawable.mask_front_biceps_short_head,
    MuscleGroup.BRACHIALIS to R.drawable.mask_front_brachialis,
    MuscleGroup.SERRATUS to R.drawable.mask_front_serratus,
    MuscleGroup.ABS to R.drawable.mask_front_abs,
    MuscleGroup.OBLIQUES to R.drawable.mask_front_obliques,
    MuscleGroup.FOREARMS to R.drawable.mask_front_forearms,
    MuscleGroup.HIP_FLEXORS to R.drawable.mask_front_hip_flexors,
    MuscleGroup.ADDUCTORS to R.drawable.mask_front_adductors,
    MuscleGroup.QUADS to R.drawable.mask_front_quads,
    MuscleGroup.CALVES to R.drawable.mask_front_calves
)

private val backMasks: List<Pair<MuscleGroup, Int>> = listOf(
    MuscleGroup.NECK to R.drawable.mask_back_neck,
    MuscleGroup.REAR_DELTS to R.drawable.mask_back_rear_delts,
    MuscleGroup.TRAPS to R.drawable.mask_back_traps,
    MuscleGroup.UPPER_BACK to R.drawable.mask_back_upper_back,
    MuscleGroup.SIDE_DELTS to R.drawable.mask_back_side_delts,
    MuscleGroup.TRICEPS_LATERAL to R.drawable.mask_back_triceps_lateral,
    MuscleGroup.TRICEPS_LONG to R.drawable.mask_back_triceps_long,
    MuscleGroup.TRICEPS_MEDIAL to R.drawable.mask_back_triceps_medial,
    MuscleGroup.LATS to R.drawable.mask_back_lats,
    MuscleGroup.TERES to R.drawable.mask_back_teres,
    MuscleGroup.GLUTE_MEDIUS to R.drawable.mask_back_glute_medius,
    MuscleGroup.LOWER_BACK to R.drawable.mask_back_lower_back,
    MuscleGroup.GLUTES to R.drawable.mask_back_glutes,
    MuscleGroup.HAMSTRINGS to R.drawable.mask_back_hamstrings,
    MuscleGroup.CALVES to R.drawable.mask_back_calves
)
