package org.squiddev.cctweaks.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import org.squiddev.cctweaks.core.registry.IClientModule;
import org.squiddev.cctweaks.core.registry.Module;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

/**
 * Holds/calculates various bits of rendering information
 * This is adapted from EnderIO (https://github.com/SleepyTrousers/EnderIO/)
 */
public class RenderInfo extends Module implements IClientModule {
	private static double tanFovRad;
	private static Camera currentView = new Camera();
	private static int ticksInGame;
	private static Vector2d mid;

	@SubscribeEvent
	public void onWorldRenderLast(RenderWorldLastEvent event) {
		ticksInGame++;

		Minecraft mc = Minecraft.getMinecraft();
		Vector3d eye = getEyePosition(mc.thePlayer);
		Vector3d lookAt = getLookVector(mc.thePlayer);
		lookAt.add(eye);

		Matrix4d mv = createMatrixAsLookAt(eye, lookAt, new Vector3d(0, 1, 0));

		float fov = Minecraft.getMinecraft().gameSettings.fovSetting;
		Matrix4d pr = createProjectionMatrixAsPerspective(fov, 0.05f, mc.gameSettings.renderDistanceChunks * 16, mc.displayWidth, mc.displayHeight);
		currentView.setProjectionMatrix(pr);
		currentView.setViewMatrix(mv);
		currentView.setViewport(0, 0, mc.displayWidth, mc.displayHeight);

		tanFovRad = Math.tanh(Math.toRadians(fov));

		mid = new Vector2d(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
		mid.scale(0.5);
	}

	private static Matrix4d createProjectionMatrixAsPerspective(double fovDegrees, double near, double far, int viewportWidth, int viewportHeight) {

		Matrix4d matrix = new Matrix4d();
		// for impl details see gluPerspective doco in OpenGL reference manual
		double aspect = (double) viewportWidth / (double) viewportHeight;

		double theta = (Math.toRadians(fovDegrees) / 2d);
		double f = Math.cos(theta) / Math.sin(theta);

		double a = (far + near) / (near - far);
		double b = (2d * far * near) / (near - far);

		matrix.set(new double[]{f / aspect, 0, 0, 0, 0, f, 0, 0, 0, 0, a, b, 0, 0, -1, 0});

		return matrix;
	}

	private static Matrix4d createMatrixAsLookAt(Vector3d eyePos, Vector3d lookAtPos, Vector3d upVec) {
		Vector3d eye = new Vector3d(eyePos);
		Vector3d lookAt = new Vector3d(lookAtPos);
		Vector3d up = new Vector3d(upVec);

		Vector3d forwardVec = new Vector3d(lookAt);
		forwardVec.sub(eye);
		forwardVec.normalize();

		Vector3d sideVec = new Vector3d();
		sideVec.cross(forwardVec, up);
		sideVec.normalize();

		Vector3d upVed = new Vector3d();
		upVed.cross(sideVec, forwardVec);
		upVed.normalize();

		Matrix4d mat = new Matrix4d(sideVec.x, sideVec.y, sideVec.z, 0, upVed.x, upVed.y, upVed.z, 0, -forwardVec.x, -forwardVec.y, -forwardVec.z, 0, 0, 0, 0, 1);

		eye.negate();
		mat.transform(eye);
		mat.setTranslation(eye);

		return mat;
	}

	public static Vector3d getEyePosition(Entity entity) {
		return new Vector3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
	}

	public static Vector3d getLookVector(Entity entity) {
		Vec3 vector = entity.getLookVec();
		return new Vector3d(vector.xCoord, vector.yCoord, vector.zCoord);
	}

	@Override
	public void clientInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static int getTicks() {
		return ticksInGame;
	}

	public static Camera getView() {
		return currentView;
	}

	public static double getTanFovRad() {
		return tanFovRad;
	}

	public static Vector2d getMidpoint() {
		return mid;
	}

}
