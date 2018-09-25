package com.example.tripheo2410.galaxsee

import android.support.v7.app.AppCompatActivity
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.core.Plane.Type.*
import android.R.attr.fragment
import android.net.sip.SipSession
import android.R.attr.fragment
import android.R.attr.fragment






class MainActivity : AppCompatActivity() {
    private val snackbarHelper = SnackbarHelper()
    private lateinit var fragment : CustomArFragment
    private var cloudAnchor : Anchor? = null
    private lateinit var storageManager: StorageManager
    private enum class AppAnchorState {
        NONE,
        HOSTING,
        HOSTED,
        RESOLVING,
        RESOLVED
    }

    private var appAnchorState = AppAnchorState.NONE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        storageManager = StorageManager(this)
        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as CustomArFragment
        fragment.planeDiscoveryController.hide()
        fragment.arSceneView.scene.setOnUpdateListener(this::onUpdateFrame)
        val clearButton : Button = findViewById(R.id.clear_button)
        clearButton.setOnClickListener {
            setCloudAnchor(null)
        }
        val resolveButton : Button = findViewById(R.id.resolve_button)
        resolveButton.setOnClickListener {
            if (cloudAnchor != null){
                snackbarHelper.showMessageWithDismiss(getParent(), "Please clear Anchor");
                return@setOnClickListener
            }
            val dialog = ResolveDialogFragment()

            dialog.show(supportFragmentManager, "Resolve")
        }
        fragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, _: MotionEvent ->
            if (plane.type != HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }
            var newAnchor = fragment.arSceneView.session.hostCloudAnchor(hitResult.createAnchor())
            setCloudAnchor(newAnchor)
            var appAnchorState = AppAnchorState.HOSTING
            snackbarHelper.showMessage(this, "Now hosting anchor...")
            placeObject(fragment, cloudAnchor, Uri.parse("ArcticFox_Posed.sfb"))
        }

    }
    /**
     *  Build renderable model, pass fragment, anchor and renderable to addNodetoScene
     */

    private fun placeObject(fragment: ArFragment, anchor: Anchor?, model: Uri) {
        ModelRenderable.builder()
                .setSource(fragment.context, model)
                .build()
                .thenAccept { renderable -> addNodeToScene(fragment, anchor!!, renderable) }
                .exceptionally { throwable ->
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(throwable.message)
                            .setTitle("Error!")
                    val dialog = builder.create()
                    dialog.show()
                    null
                }

    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }

    /**
    * Ensure only one cloudAnchor in activity
    */

    private fun setCloudAnchor(newAnchor: Anchor?) {
        cloudAnchor?.detach()
        cloudAnchor = newAnchor
        appAnchorState = AppAnchorState.NONE
        snackbarHelper.hide(this)
    }

    /**
    * Check finished hosting on every frame
    */

    private fun onUpdateFrame(frameTime: FrameTime) {
        checkUpdatedAnchor()
    }

    @Synchronized
    private fun checkUpdatedAnchor() {
        if (appAnchorState !== AppAnchorState.HOSTING) {
            return
        }
        val cloudState = cloudAnchor!!.getCloudAnchorState()

        if (appAnchorState === AppAnchorState.HOSTING) {
            if (cloudState.isError) {
                snackbarHelper.showMessageWithDismiss(this, "Error hosting anchor.. $cloudState")
                appAnchorState = AppAnchorState.NONE
            } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                snackbarHelper.showMessageWithDismiss(this, "Anchor hosted with id " + cloudAnchor!!.getCloudAnchorId())
                appAnchorState = AppAnchorState.HOSTED
            }
        }
    }




}
