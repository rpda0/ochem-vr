/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.balloons;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRPositionAnimation;
import org.gearvrf.animation.GVRTransformAnimation;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import org.gearvrf.GVRPicker;
import org.gearvrf.IPickEvents;
import org.gearvrf.GVRPicker.GVRPickedObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.concurrent.Future;

public class BalloonMain extends GVRMain {

    public class PickHandler implements IPickEvents
    {
        public GVRSceneObject PickedObject = null;

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {

        }
        public void onExit(GVRSceneObject sceneObj) {

        }
        long lastPickEvent;

        public void onNoPick(GVRPicker picker)
        {
            if (!enableMenuPick)
                return;
            if (PickedObject == menu)
                return;
            if (PickedObject != null)
            {
                PickedObject.getTransform().setPositionZ(-5.0f);
                Log.d("NO PICK", "send back");
                lastPickEvent = System.currentTimeMillis();
            }
            PickedObject = null;
        }
        public void onPick(GVRPicker picker)
        {
            lastPickEvent = System.currentTimeMillis();

            GVRPickedObject picked = picker.getPicked()[0];
            PickedObject = picked.hitObject;

            if (!enableMenuPick)
                return;

            if (PickedObject == menu)
                return;
            PickedObject.getTransform().setPositionZ(-4.5f);
            Log.d("PICK", "bring forward");
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
    }

    private GVRScene mScene = null;
    private PickHandler mPickHandler;
    boolean enableMenuPick;
    GVRSceneObject menu;

    GVRSceneObject submenu_down, submenu_left, submenu_right;

    GVRSceneObject makeButton(GVRContext ctx, float rotation_angle)
    {
        GVRSceneObject button = null;
        try
        {
            // load image as button
            button = ctx.loadModel("menu_button.obj");
            //button = ctx.loadModel("menu_button.obj");
            GVRSceneObject.BoundingVolume bv = button.getBoundingVolume();

            // scale and rotate image
            GVRTransform trans = button.getTransform();
            float sf = 1.0f / bv.radius;
            trans.setScale(sf, sf, sf);
            trans.rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);
            trans.rotateByAxis(rotation_angle, 0.0f, 0.0f, 1.0f);

            // attach light
            GVRDirectLight sunLight = new GVRDirectLight(ctx);
            sunLight.setAmbientIntensity(0.4f, 0.4f, 0.4f, 1.0f);
            sunLight.setDiffuseIntensity(0.6f, 0.6f, 0.6f, 1.0f);
            button.attachComponent(sunLight);

            // attach to collider
            GVRSphereCollider collider = new GVRSphereCollider(ctx);
            collider.setRadius(1.0f);
            button.attachComponent(collider);

            // bv = button.getBoundingVolume();
            //trans.setPosition(-bv.center.x, -bv.center.y, -bv.center.z + 0.1f);
        }
        catch (IOException ex)
        {
            Log.e("Button", "Cannot load FBX" + ex.getMessage());
        }
        return button;
    }

    @Override
    public void onInit(GVRContext context) {
        enableMenuPick = false;
        /*
         * Set the background color
         */
        mScene = context.getNextMainScene();
        mScene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mScene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        /*
         * Add the environment and menu
         */
        GVRSceneObject environment = makeEnvironment(context);
        mScene.addSceneObject(environment);

        // main menu
        menu = makeButton(context, 0.0f);
        menu.getTransform().setPosition(0.0f, 0.0f, -3.0f);

        //menu.getTransform().setPositionZ(-5.0f);
        mScene.addSceneObject(menu);

        // three sub menu buttons
        submenu_down = makeButton(context, 180.0f);
        submenu_left = makeButton(context, 60.0f);
        submenu_right = makeButton(context, -60.0f);
        submenu_down.getTransform().setPosition(0.0f, 0.0f, -5.0f);
        submenu_left.getTransform().setPosition(0.0f, 0.0f, -5.0f);
        submenu_right.getTransform().setPosition(0.0f, 0.0f, -5.0f);
        mScene.addSceneObject(submenu_down);
        mScene.addSceneObject(submenu_left);
        mScene.addSceneObject(submenu_right);

        /*
         * Respond to picking events
         */
        mScene.getMainCameraRig().getOwnerObject().attachComponent(new GVRPicker(context, mScene));
        mPickHandler = new PickHandler();
        mScene.getEventReceiver().addListener(mPickHandler);
    }

    GVRSceneObject makeMenu (GVRContext context) {
        Future<GVRTexture> futureTexture = context
                .loadFutureTexture(new GVRAndroidResource(context,
                        R.drawable.icon));
        // setup material
        GVRMaterial material = new GVRMaterial(context);
        material.setMainTexture(futureTexture);

        GVRSceneObject cubeSceneObject = new GVRCubeSceneObject(context,
                true, material);
        GVRRenderData cubeRenderData = cubeSceneObject.getRenderData();

        GVRSphereCollider collider = new GVRSphereCollider(context);
        collider.setRadius(1.0f);
        cubeSceneObject.attachComponent(collider);

        cubeSceneObject.setName("menu");
        //cubeRenderData.setShaderTemplate(GVRPhongShader.class);
        //cubeRenderData.setAlphaBlend(true);
        //cubeRenderData.setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        cubeSceneObject.getTransform().setPositionZ(-3.0f);
        return cubeSceneObject;
    }

    GVRSceneObject makeEnvironment(GVRContext context)
    {
        Future<GVRTexture> futureBackground = context
                .loadFutureCubemapTexture(new GVRAndroidResource(context,
                        R.raw.earth));
        GVRMaterial material = new GVRMaterial(context, GVRMaterial.GVRShaderType.Cubemap.ID);
        material.setMainTexture(futureBackground);
        GVRSphereSceneObject environment = new GVRSphereSceneObject(context, 18, 36, false, material , 4, 4);
        environment.getTransform().setScale(20.0f, 20.0f, 20.0f);
        GVRDirectLight sunLight = new GVRDirectLight(context);
        sunLight.setAmbientIntensity(0.4f, 0.4f, 0.4f, 1.0f);
        sunLight.setDiffuseIntensity(0.6f, 0.6f, 0.6f, 1.0f);
        environment.attachComponent(sunLight);
        return environment;
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
    }

    public void onTouchEvent(MotionEvent event)
    {
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                if (mPickHandler.PickedObject != null)
                {
                    enableMenuPick = true;
                    GVRPositionAnimation transformAnimation_0 = new GVRPositionAnimation(submenu_down, 2.0f, 0.0f, -2.1f, -5.0f);
                    GVRPositionAnimation transformAnimation_1 = new GVRPositionAnimation(submenu_left, 2.0f, -1.5f, 1.5f, -5.0f);
                    GVRPositionAnimation transformAnimation_2 = new GVRPositionAnimation(submenu_right, 2.0f, 1.5f, 1.5f, -5.0f);

                    transformAnimation_0.start(getGVRContext().getAnimationEngine());
                    transformAnimation_1.start(getGVRContext().getAnimationEngine());
                    transformAnimation_2.start(getGVRContext().getAnimationEngine());
                }
                break;

            default:
                break;
        }
    }

}
