package com.example.tripheo2410.galaxsee.main_activity

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.SeekBar
import com.example.tripheo2410.galaxsee.*
import com.example.tripheo2410.galaxsee.ar_fragment.CustomArFragment
import com.example.tripheo2410.galaxsee.planet_rendering.Planet
import com.example.tripheo2410.galaxsee.planet_rendering.RotatingNode
import com.example.tripheo2410.galaxsee.planet_rendering.SolarSettings
import com.example.tripheo2410.galaxsee.dialog_handling.DemoUtils
import com.example.tripheo2410.galaxsee.dialog_handling.ResolveDialogFragment
import com.example.tripheo2410.galaxsee.dialog_handling.SnackbarHelper
import com.example.tripheo2410.galaxsee.firebase_manager.StorageManager
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {
    private val snackbarHelper = SnackbarHelper()
    private lateinit var fragment: CustomArFragment
    private  var cloudAnchor: Anchor? = null
    private lateinit var storageManager: StorageManager
    private lateinit var activity: Activity
    private val solarSettings = SolarSettings()
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
    private var solarControlsRenderable: ViewRenderable? = null

    // True once scene is loaded
    private var hasFinishedLoading = false

    companion object {
        // Astronomical units to meters ratio. Used for positioning the planets of the solar system.
        private const val AU_TO_METERS = 0.5f
    }
    private enum class AppAnchorState {
        NONE,
        HOSTING,
        HOSTED,
        RESOLVING,
        RESOLVED
    }

    private var appAnchorState = AppAnchorState.NONE

    /** on create */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity = this
        storageManager = StorageManager(this)
        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as CustomArFragment
        fragment.planeDiscoveryController.hide()
        fragment.arSceneView.scene.setOnUpdateListener(this::onUpdateFrame)
        val resolveButton : Button = findViewById(R.id.resolve_button)
        val clearButton : Button = findViewById(R.id.clear_button)
        initPlanetModel()
        initClearButton(clearButton)
        initResolveButton(resolveButton)
        planeListener(resolveButton)

    }

    /** set up plane listener of ar fragment*/
    private fun planeListener(resolveButton: Button) {
        fragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, _: MotionEvent ->
            if (plane.type !== Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }
            resolveButton.isEnabled = false
            val newAnchor = fragment.arSceneView.session.hostCloudAnchor(hitResult.createAnchor())

            setCloudAnchor(newAnchor)

            appAnchorState = AppAnchorState.HOSTING
            snackbarHelper.showMessage(this, "Now hosting anchor...")
            placeObject(fragment, cloudAnchor, Uri.parse("Sol.sfb"))
        }
    }


    /** set up resolve button*/
    private fun initResolveButton(resolveButton : Button) {

        resolveButton.setOnClickListener {
            resolveButton.isEnabled = false
            if (cloudAnchor != null) {
                snackbarHelper.showMessageWithDismiss(parent, "Please clear Anchor")
                return@setOnClickListener
            }
            val dialog = ResolveDialogFragment()
            dialog.setCancelListener(object: ResolveDialogFragment.CancelListener {
                override fun onCancelPressed() {
                    resolveButton.isEnabled = true
                }
            })
            dialog.setOkListener(object : ResolveDialogFragment.OkListener {
                override fun onOkPressed(dialogValue: String) {
                    val shortCode = Integer.parseInt(dialogValue)
                    storageManager.getCloudAnchorID(shortCode, object: StorageManager.CloudAnchorIdListener {
                        override fun onCloudAnchorIdAvailable(cloudAnchorId: String?) {
                            val resolvedAnchor = fragment.arSceneView.session.resolveCloudAnchor(cloudAnchorId)
                            setCloudAnchor(resolvedAnchor)
                            placeObject(fragment, cloudAnchor, Uri.parse("Sol.sfb"))
                            snackbarHelper.showMessage(activity, "Now Resolving Anchor...")
                            appAnchorState = AppAnchorState.RESOLVING
                        }

                    })
                }

            })
            dialog.show(supportFragmentManager, "Resolve")

        }
    }

    /** set up clear button*/
    private fun initClearButton(clearButton : Button) {

        clearButton.setOnClickListener {
            resolve_button.isEnabled = true
            setCloudAnchor(null)
        }
    }

    /** create completable future model so planets are rendered background out of main thread*/
    private fun initPlanetModel() {
        val solarControlsStage = ViewRenderable.builder().setView(this, R.layout.solar_controls).build()
        // Build all the planet models.
        val sunStage : CompletableFuture<ModelRenderable> = ModelRenderable.builder().setSource(this, Uri.parse("Sol.sfb")).build()
        val mercuryStage : CompletableFuture<ModelRenderable> = ModelRenderable.builder().setSource(this, Uri.parse("Mercury.sfb")).build()
        val venusStage : CompletableFuture<ModelRenderable> = ModelRenderable.builder().setSource(this, Uri.parse("Venus.sfb")).build()
        val earthStage : CompletableFuture<ModelRenderable> = ModelRenderable.builder().setSource(this, Uri.parse("Earth.sfb")).build()
        val lunaStage : CompletableFuture<ModelRenderable> = ModelRenderable.builder().setSource(this, Uri.parse("Luna.sfb")).build()
        val marsStage : CompletableFuture<ModelRenderable> = ModelRenderable.builder().setSource(this, Uri.parse("Mars.sfb")).build()
        val jupiterStage : CompletableFuture<ModelRenderable> = ModelRenderable.builder().setSource(this, Uri.parse("Jupiter.sfb")).build()
        val saturnStage : CompletableFuture<ModelRenderable> = ModelRenderable.builder().setSource(this, Uri.parse("Saturn.sfb")).build()
        val uranusStage : CompletableFuture<ModelRenderable> = ModelRenderable.builder().setSource(this, Uri.parse("Uranus.sfb")).build()
        val neptuneStage : CompletableFuture<ModelRenderable> = ModelRenderable.builder().setSource(this, Uri.parse("Neptune.sfb")).build()

        // Build a renderable from a 2D View.
        CompletableFuture.allOf(
                sunStage,
                mercuryStage,
                venusStage,
                earthStage,
                lunaStage,
                marsStage,
                jupiterStage,
                saturnStage,
                uranusStage,
                neptuneStage,
                solarControlsStage)
                .handle<Any> { _, throwable ->
                    if (throwable != null) {
                        DemoUtils.displayError(this, "Unable to load renderable", throwable)
                        return@handle null
                    }

                    try {
                        sunRenderable = sunStage.get()
                        mercuryRenderable = mercuryStage.get()
                        venusRenderable = venusStage.get()
                        earthRenderable = earthStage.get()
                        lunaRenderable = lunaStage.get()
                        marsRenderable = marsStage.get()
                        jupiterRenderable = jupiterStage.get()
                        saturnRenderable = saturnStage.get()
                        uranusRenderable = uranusStage.get()
                        neptuneRenderable = neptuneStage.get()
                        solarControlsRenderable = solarControlsStage.get()
                        // Everything finished loading successfully.
                        hasFinishedLoading = true

                    } catch (ex: InterruptedException) {
                        DemoUtils.displayError(this, "Unable to load renderable", ex)
                    }

                    null
                }
    }

    /** check every frame for appropriate message for state*/
    @Synchronized
    private fun checkUpdatedAnchor() {
        if (appAnchorState !== AppAnchorState.HOSTING && appAnchorState !== AppAnchorState.RESOLVING) {
            return
        }
        val cloudState = cloudAnchor!!.cloudAnchorState
        if (appAnchorState === AppAnchorState.HOSTING) {
            if (cloudState.isError) {
                snackbarHelper.showMessageWithDismiss(this, "Error hosting anchor.. $cloudState")
                appAnchorState = AppAnchorState.NONE
            } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                storageManager.nextShortCode(object: StorageManager.ShortCodeListener {
                    override fun onShortCodeAvailable(shortCode: Int?) {
                        if (shortCode == null) {
                            snackbarHelper.showMessageWithDismiss(activity, "Could not get shortCode")
                            return
                        }
                        storageManager.storeUsingShortCode(shortCode, cloudAnchor!!.cloudAnchorId)

                        snackbarHelper.showMessageWithDismiss(activity, "Anchor hosted! Cloud Short Code: $shortCode")
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


    /** pass a renderable to addNoteToScene */
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

    /** create solar system and place it correctly using anchor coordinate */
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val sun = TransformableNode(fragment.transformationSystem)
        sun.renderable = renderable
        sun.localScale = Vector3(0.5f, 0.5f, 0.5f)
        sun.setParent(anchorNode)
        sun.localPosition = Vector3(0.0f, 0.5f, 0.0f)

        val sunVisual = Node()
        sunVisual.setParent(sun)
        sunVisual.renderable = sunRenderable
        sunVisual.localScale = Vector3(0.5f, 0.5f, 0.5f)

        val solarControls = Node()
        solarControls.setParent(sun)
        solarControls.renderable = solarControlsRenderable
        solarControls.localPosition = Vector3(0.0f, 0.25f, 0.0f)

        // orbit speed changing
        val solarControlsView = solarControlsRenderable!!.view
        val orbitSpeedBar = solarControlsView.findViewById<SeekBar>(R.id.orbitSpeedBar)
        orbitSpeedBar.progress = (solarSettings.orbitSpeedMultiplier * 10.0f).toInt()
        orbitSpeedBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        val ratio = progress.toFloat() / orbitSpeedBar.max.toFloat()
                        solarSettings.orbitSpeedMultiplier = ratio * 10.0f
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })

        //rotation speed changing
        val rotationSpeedBar = solarControlsView.findViewById<SeekBar>(R.id.rotationSpeedBar)
        rotationSpeedBar.progress = (solarSettings.rotationSpeedMultiplier * 10.0f).toInt()
        rotationSpeedBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        val ratio = progress.toFloat() / rotationSpeedBar.max.toFloat()
                        solarSettings.rotationSpeedMultiplier = ratio * 10.0f
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })

        // Toggle the solar controls on and off by tapping the sun.
        sunVisual.setOnTapListener { hitTestResult, motionEvent -> solarControls.isEnabled = !solarControls.isEnabled }

        createPlanet("Mercury", sun, 0.4f, 47f, mercuryRenderable, 0.019f)

        createPlanet("Venus", sun, 0.7f, 35f, venusRenderable, 0.0475f)

        val earth = createPlanet("Earth", sun, 1.0f, 29f, earthRenderable, 0.05f)

        createPlanet("Moon", earth, 0.15f, 100f, lunaRenderable, 0.018f)

        createPlanet("Mars", sun, 1.5f, 24f, marsRenderable, 0.0265f)

        createPlanet("Jupiter", sun, 2.2f, 13f, jupiterRenderable, 0.16f)

        createPlanet("Saturn", sun, 3.5f, 9f, saturnRenderable, 0.1325f)

        createPlanet("Uranus", sun, 5.2f, 7f, uranusRenderable, 0.1f)

        createPlanet("Neptune", sun, 6.1f, 5f, neptuneRenderable, 0.074f)
        fragment.arSceneView.scene.addChild(anchorNode)
        //fragment.arSceneView.scene.addChild(solarSystem)
        sun.select()
    }

    /** set clound anchor or reset it*/
    private fun setCloudAnchor(newAnchor: Anchor?) {
        if (cloudAnchor != null) {
            cloudAnchor!!.detach()
        }
        cloudAnchor = newAnchor
        appAnchorState = AppAnchorState.NONE
        snackbarHelper.hide(this)
    }

    /** create requested Planet */
    private fun createPlanet(
            name: String,
            parent: Node,
            auFromParent: Float,
            orbitDegreesPerSecond: Float,
            renderable: ModelRenderable?,
            planetScale: Float): Node {
        val orbit = RotatingNode(solarSettings, true)
        orbit.setDegreesPerSecond(orbitDegreesPerSecond)
        orbit.setParent(parent)
        // Create the planet and position it relative to the sun.
        val planet = Planet(this, name, planetScale, renderable!!, solarSettings)
        planet.setParent(orbit)
        planet.localPosition = Vector3(auFromParent * AU_TO_METERS, 0.0f, 0.0f)

        return planet
    }
}
