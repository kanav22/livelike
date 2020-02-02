package com.indwealth.core.util.view

import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar

class ProgressBarAnimation
/**
 * @param fullDuration - time required to fill progress from 0% to 100%
 */
(private val mProgressBar: ProgressBar, fullDuration: Long) : Animation() {
    private var mTo: Int = 0
    private var mFrom: Int = 0
    private val mStepDuration: Long

    init {
        mStepDuration = fullDuration / mProgressBar.max
    }

    fun setProgress(progress: Int) {
        var progress = progress
        if (progress < 0) {
            progress = 0
        }
        if (progress > mProgressBar.max) {
            progress = mProgressBar.max
        }
        mTo = progress
        mFrom = mProgressBar.progress
        duration = Math.abs(mTo - mFrom) * mStepDuration
        mProgressBar.startAnimation(this)
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val value = mFrom + (mTo - mFrom) * interpolatedTime
        mProgressBar.progress = value.toInt()
    }
}