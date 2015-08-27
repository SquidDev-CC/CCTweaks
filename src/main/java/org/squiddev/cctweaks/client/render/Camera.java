package org.squiddev.cctweaks.client.render;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import java.awt.*;

/**
 * Used to convert world to screen coordinates
 * This is adapted from EnderIO (https://github.com/SleepyTrousers/EnderIO/)
 */
public class Camera {
	private Rectangle viewport;

	private Matrix4d projectionTranspose;
	private Matrix4d projectionMatrix;

	private Matrix4d viewTranspose;
	private Matrix4d viewMatrix;

	public boolean isValid() {
		return viewMatrix != null && projectionMatrix != null && viewport != null;
	}

	public Vector2d getScreenPoint(Vector3d point3d) {
		Vector4d transPoint = new Vector4d(point3d.x, point3d.y, point3d.z, 1);

		viewMatrix.transform(transPoint);
		projectionMatrix.transform(transPoint);

		int halfWidth = viewport.width / 2;
		int halfHeight = viewport.height / 2;
		Vector2d screenPos = new Vector2d(transPoint.x, transPoint.y);
		screenPos.scale(1 / transPoint.w);
		screenPos.x = screenPos.x * halfWidth + halfWidth;
		screenPos.y = -screenPos.y * halfHeight + halfHeight;

		return screenPos;
	}

	public void setViewport(int x, int y, int width, int height) {
		viewport = new Rectangle(x, y, width, height);
	}

	public void setProjectionMatrix(Matrix4d matrix) {
		if (projectionMatrix == null) {
			projectionMatrix = new Matrix4d();
			projectionTranspose = new Matrix4d();
		}
		projectionMatrix.set(matrix);
		projectionTranspose.set(matrix);
		projectionTranspose.transpose();
	}

	public void setViewMatrix(Matrix4d matrix) {
		if (viewMatrix == null) {
			viewMatrix = new Matrix4d();
			viewTranspose = new Matrix4d();
		}
		viewMatrix.set(matrix);
		viewTranspose.set(matrix);
		viewTranspose.transpose();
	}
}
