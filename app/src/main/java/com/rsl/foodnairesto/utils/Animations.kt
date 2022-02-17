package com.rsl.foodnairesto.utils

import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import androidx.recyclerview.widget.RecyclerView
import android.animation.ObjectAnimator
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.utils.custom_views.GridRecyclerView


object Animations {

    fun runLayoutAnimationForRecyclerView(mRecyclerViewTables: RecyclerView, resID: Int) {
        val context = mRecyclerViewTables.context
        val controller = AnimationUtils.loadLayoutAnimation(context, resID)

        mRecyclerViewTables.layoutAnimation = controller
        mRecyclerViewTables.adapter!!.notifyDataSetChanged()
        mRecyclerViewTables.scheduleLayoutAnimation()
    }

    fun slideUp(view: View) {
        view.visibility = View.VISIBLE
        val animate = TranslateAnimation(
            0f, // fromXDelta
            0f, // toXDelta
            view.height.toFloat(), // fromYDelta
            0f
        )                // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    fun slideDown(view: View) {
        val animate = TranslateAnimation(
            0f, // fromXDelta
            0f, // toXDelta
            0f, // fromYDelta
            view.height.toFloat()
        )          // toYDelta
        animate.duration = 500
        view.startAnimation(animate)

        view.visibility = View.INVISIBLE
    }

    fun slideLeft(view: View) {
        val animate = TranslateAnimation(
            0f, // fromXDelta
            (-view.width).toFloat(), // toXDelta
            0f, // fromYDelta
            0f
        )          // toYDelta
        animate.duration = 300
        view.startAnimation(animate)
        //mGuideLineView.setGuidelinePercent(0);
        view.visibility = View.GONE
    }

    fun slideRight(view: View) {
        view.visibility = View.VISIBLE
        //mGuideLineView.setGuidelinePercent(0.25f);
        val animate = TranslateAnimation(
            (-view.width).toFloat(), // fromXDelta
            0f, // toXDelta
            0f, // fromYDelta
            0f
        )          // toYDelta
        animate.duration = 300
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    fun rotateClockwise(view: View) {
        val rotate = ObjectAnimator.ofFloat(view, "rotation", 0f, 180f)
        rotate.duration = 500
        rotate.start()
    }

    fun rotateAntiClockwise(view: View) {
        val rotate = ObjectAnimator.ofFloat(view, "rotation", 180f, 0f)
        rotate.duration = 500
        rotate.start()
    }

    fun runLayoutAnimationFallDown(mRecyclerView: RecyclerView){
        val animation =
            AnimationUtils.loadLayoutAnimation(mRecyclerView.context, R.anim.layout_animation_fall_down)
        mRecyclerView.layoutAnimation = animation
        mRecyclerView.scheduleLayoutAnimation()
    }

    fun runGridLayoutAnimationFallDown(mRecyclerView: GridRecyclerView){
        val animation =
            AnimationUtils.loadLayoutAnimation(mRecyclerView.context, R.anim.grid_layout_animation_from_bottom)
        mRecyclerView.layoutAnimation = animation
        mRecyclerView.scheduleLayoutAnimation()
    }
}