package com.indwealth.core

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.animateFromBottom() {
    val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_from_bottom)

    layoutAnimation = controller
    adapter?.notifyDataSetChanged()
    scheduleLayoutAnimation()
}

/**
 * Animate view to wrap height
 */
fun View.expand(duration: Long = 200L) {
    measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    val targetHeight = measuredHeight

    // Set initial height to 0 and show the view
    layoutParams.height = 0
    visibility = View.VISIBLE

    val anim = ValueAnimator.ofInt(measuredHeight, targetHeight)
    anim.interpolator = AccelerateInterpolator()
    anim.duration = duration
    anim.addUpdateListener { animation ->
        val layoutParams = layoutParams
        layoutParams.height = (targetHeight * animation.animatedFraction).toInt()
        this.layoutParams = layoutParams
    }
    anim.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            // At the end of animation, set the height to wrap content
            // This fix is for long views that are not shown on screen
            val layoutParams = layoutParams
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    })
    anim.start()
}