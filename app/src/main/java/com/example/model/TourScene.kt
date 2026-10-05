package com.example.model

data class TourHotspot(
    val id: String,
    val title: String,
    val xPercent: Float, // 0.0f .. 1.0f relative to panorama width
    val yPercent: Float, // 0.0f .. 1.0f relative to panorama height
    val targetSceneId: String? = null,
    val infoText: String? = null
)

data class TourScene(
    val id: String,
    val name: String,
    val drawableResId: Int,
    val hotspots: List<TourHotspot> = emptyList()
)
