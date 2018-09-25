package com.example.tripheo2410.galaxsee

import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import android.R.attr.fragment
import android.R.attr.fragment
import com.google.ar.core.Plane.Type.*


class MainActivity : AppCompatActivity() {
    private val snackbarHelper = SnackbarHelper()
    private lateinit var fragment : CustomArFragment
    private var cloudAnchor : Anchor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as CustomArFragment
        fragment.planeDiscoveryController.hide()
        var clearButton : Button = findViewById(R.id.clear_button)
        clearButton.setOnClickListener {
            setCloudAnchor(null)
        }
        fragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (plane.type != HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }

            val newAnchor = hitResult.createAnchor()

            setCloudAnchor(newAnchor)
            placeObject(fragment, cloudAnchor, Uri.parse("ArcticFox_Posed.sfb"))
        }

    }
    /*
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

    /*
    * Ensure only one cloudAnchor in activity
    */

    private fun setCloudAnchor(newAnchor: Anchor?) {
        cloudAnchor?.detach()
        cloudAnchor = newAnchor
    }
}
