package com.example.tripheo2410.galaxsee

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {
    private val snackbarHelper = SnackbarHelper()
    private lateinit var fragment: CustomArFragment
    private  var cloudAnchor: Anchor? = null
    private lateinit var storageManager: StorageManager
    private lateinit var activity: Activity
    private var installRequested: Boolean = false
    private var gestureDetector: GestureDetector? = null
    private val solarSettings = SolarSettings()
    private var arSceneView: ArSceneView? = null
    private var loadingMessageSnackbar: Snackbar? = null
    private var sunRenderable: ModelRenderable? = null
    private var mercuryRenderable: ModelRenderable? = null
    private var venusRenderable: ModelRenderable? = null
    private var earthRenderable: ModelRenderable? = null
    private var lunaRenderable: ModelRenderable? = null
    private var marsRenderable: ModelRenderable? = null
    private var jupiterRenderable: ModelRenderable? = null
    private var saturnRenderable: ModelRenderable? = null
    private var uranusRenderable: ModelRenderable? = null
    private var neptuneRenderable: ModelRenderable? = null
    private val solarControlsRenderable: ViewRenderable? = null

    // True once scene is loaded
    private var hasFinishedLoading = false

    // True once the scene has been placed.
    private var hasPlacedSolarSystem = false
    companion object {
        private val RC_PERMISSIONS = 0x123

        // Astronomical units to meters ratio. Used for positioning the planets of the solar system.
        private val AU_TO_METERS = 0.5f
    }
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
        activity = this
        storageManager =  StorageManager(this)
        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as CustomArFragment
        fragment.planeDiscoveryController.hide()
        fragment.arSceneView.scene.setOnUpdateListener(this::onUpdateFrame)
        val clearButton : Button = findViewById(R.id.clear_button)
        clearButton.setOnClickListener {
            setCloudAnchor(null)
        }

        val resolveButton : Button = findViewById(R.id.resolve_button)
        resolveButton.setOnClickListener {
            if (cloudAnchor != null) {
                snackbarHelper.showMessageWithDismiss(parent, "Please clear Anchor")
                return@setOnClickListener
            }
            val dialog = ResolveDialogFragment()
            dialog.setOkListener(object : ResolveDialogFragment.OkListener {
                override fun onOkPressed(dialogValue: String) {
                    val shortCode = Integer.parseInt(dialogValue)
                    storageManager.getCloudAnchorID(shortCode, object: StorageManager.CloudAnchorIdListener {
                        override fun onCloudAnchorIdAvailable(cloudAnchorId: String?) {
                            val resolvedAnchor = fragment.arSceneView.session.resolveCloudAnchor(cloudAnchorId)
                            setCloudAnchor(resolvedAnchor)
                            placeObject(fragment, cloudAnchor, Uri.parse("model.sfb"))
                            snackbarHelper.showMessage(activity, "Now Resolving Anchor...")
                            appAnchorState = AppAnchorState.RESOLVING
                        }

                    })
                }

            })
            dialog.show(supportFragmentManager, "Resolve")

        }
        fragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (plane.getType() !== Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }
            val newAnchor = fragment.arSceneView.session.hostCloudAnchor(hitResult.createAnchor())

            setCloudAnchor(newAnchor)

            appAnchorState = AppAnchorState.HOSTING
            snackbarHelper.showMessage(this, "Now hosting anchor...")
            placeObject(fragment, cloudAnchor, Uri.parse("model.sfb"))
        }

    }

    @Synchronized
    private fun checkUpdatedAnchor() {
        if (appAnchorState !== AppAnchorState.HOSTING && appAnchorState !== AppAnchorState.RESOLVING) {
            return
        }
        val cloudState = cloudAnchor!!.getCloudAnchorState()
        if (appAnchorState === AppAnchorState.HOSTING) {
            if (cloudState.isError) {
                snackbarHelper.showMessageWithDismiss(this, "Error hosting anchor.. $cloudState")
                appAnchorState = AppAnchorState.NONE
            } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                storageManager.nextShortCode(object: StorageManager.ShortCodeListener {
                    override fun onShortCodeAvailable(shortCode: Int?) {
                        if (shortCode == null) {
                            snackbarHelper.showMessageWithDismiss(activity, "Could not get shortCode")
                            return@onShortCodeAvailable
                        }
                        storageManager.storeUsingShortCode(shortCode!!, cloudAnchor!!.getCloudAnchorId())

                        snackbarHelper.showMessageWithDismiss(activity, "Anchor hosted! Cloud Short Code: " + shortCode!!)
                    }

                })

                appAnchorState = AppAnchorState.HOSTED
            }
        } else if (appAnchorState === AppAnchorState.RESOLVING) {
            if (cloudState.isError) {
                snackbarHelper.showMessageWithDismiss(this, "Error resolving anchor.. $cloudState")
                appAnchorState = AppAnchorState.NONE
            } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                snackbarHelper.showMessageWithDismiss(this, "Anchor resolved successfully")
                appAnchorState = AppAnchorState.RESOLVED
            }
        }

    }

    private fun onUpdateFrame(frameTime: FrameTime) {
        checkUpdatedAnchor()
    }



    private fun placeObject(fragment: ArFragment, anchor: Anchor?, model: Uri) {
        ModelRenderable.builder()
                .setSource(fragment.context, model)
                .build()
                .thenAccept { renderable -> addNodeToScene(fragment, anchor!!, renderable) }
                .exceptionally { throwable ->
                    val builder = android.support.v7.app.AlertDialog.Builder(this)
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

    private fun setCloudAnchor(newAnchor: Anchor?) {
        if (cloudAnchor != null) {
            cloudAnchor!!.detach()
        }

        cloudAnchor = newAnchor
        appAnchorState = AppAnchorState.NONE
        snackbarHelper.hide(this)
    }
}
