package com.example.tripheo2410.galaxsee

import android.content.Context
import android.view.MotionEvent
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable

class Planet (
    private val context: Context,
    private val planetName: String,
    private val planetScale: Float,
    private val planetRenderable: ModelRenderable,
    private val solarSettings: SolarSettings) : Node(), Node.OnTapListener {
    private var infoCard: Node? = null
    companion object {

        private val INFO_CARD_Y_POS_COEFF = 0.55f

        init {
            setOnTapListener(this)
        }

        override fun onUpdate(frameTime: FrameTime?) {
            if (infoCard == null) {
                return
            }

            // Typically, getScene() will never return null because onUpdate() is only called when the node
            // is in the scene.
            // However, if onUpdate is called explicitly or if the node is removed from the scene on a
            // different thread during onUpdate, then getScene may be null.
            if (scene == null) {
                return
            }
            val cameraPosition = scene.camera.worldPosition
            val cardPosition = infoCard!!.worldPosition
            val direction = Vector3.subtract(cameraPosition, cardPosition)
            val lookRotation = Quaternion.lookRotation(direction, Vector3.up())
            infoCard!!.worldRotation = lookRotation
        }

        override fun onTap(hitTestResult: HitTestResult?, motionEvent: MotionEvent?) {
            if (infoCard == null) {
                return
            }

            infoCard!!.isEnabled = !infoCard!!.isEnabled
        }
    }
}
