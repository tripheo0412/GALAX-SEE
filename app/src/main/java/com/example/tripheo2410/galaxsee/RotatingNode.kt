package com.example.tripheo2410.galaxsee

import android.animation.ObjectAnimator
import com.google.ar.sceneform.Node

class RotatingNode(private val solarSettings: SolarSettings, private val isOrbit: Boolean) : Node() {
    private var orbitAnimation: ObjectAnimator? = null
    private var degreesPerSecond = 90.0f
    private var lastSpeedMultiplier = 1.0f
    private val animationDuration: Long
        get() = (1000 * 360 / (degreesPerSecond * speedMultiplier)).toLong()
    private val speedMultiplier: Float
        get() = if (isOrbit) {
            solarSettings.orbitSpeedMultiplier
        } else {
            solarSettings.rotationSpeedMultiplier
        }

    /** Sets rotation speed  */
    fun setDegreesPerSecond(degreesPerSecond: Float) {
        this.degreesPerSecond = degreesPerSecond
    }

    override fun onActivate() {
        startAnimation()
    }

    override fun onDeactivate() {
        stopAnimation()
    }
}