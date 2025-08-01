package de.markusfisch.android.scalingimageview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ScalingImageView extends ImageView {
	private static final double RAD2DEG = 180.0 / Math.PI;

	private final SparseArray<PointF> initialPoint = new SparseArray<>();
	private final Matrix initialMatrix = new Matrix();
	private final Matrix transformMatrix = new Matrix();
	private final Tapeline initialTapeline = new Tapeline();
	private final Tapeline transformTapeline = new Tapeline();
	private final RectF drawableRect = new RectF();
	private final RectF mappedRect = new RectF();
	private final RectF bounds = new RectF();
	private final float[] transformMatrixValues = new float[9];

	private GestureDetector gestureDetector;
	private ImageView.ScaleType scaleType = ImageView.ScaleType.CENTER_INSIDE;
	private boolean reinit = false;
	private boolean restrictTranslation = true;
	private boolean freeRotation = false;
	private float lastWidth;
	private float lastHeight;
	private float lastRotation;
	private float magnifyScale = 4f;
	private float minWidth;
	private float rotation;

	public ScalingImageView(Context context) {
		super(context);
		init(context);
	}

	public ScalingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ScalingImageView(
			Context context,
			AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@Override
	public void setImageMatrix(Matrix matrix) {
		transformMatrix.set(matrix);
		fitImageAndSetMinWidth(bounds, new Matrix());
		if (restrictTranslation) {
			fitTranslate(transformMatrix, getDrawableRect(), bounds);
		}
		super.setImageMatrix(transformMatrix);
	}

	@Override
	public void setScaleType(ImageView.ScaleType scaleType) {
		if (scaleType != ImageView.ScaleType.CENTER &&
				scaleType != ImageView.ScaleType.CENTER_CROP &&
				scaleType != ImageView.ScaleType.CENTER_INSIDE) {
			throw new UnsupportedOperationException("ScaleType " +
					scaleType.toString() + " is not supported");
		}

		this.scaleType = scaleType;
		center(bounds);
		invalidate();
	}

	@Override
	public ScaleType getScaleType() {
		return scaleType;
	}

	// This is only listening for gestures, not clicks.
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event)) {
			return true;
		}

		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				initTransform(event, -1);
				return true;
			case MotionEvent.ACTION_MOVE:
				transform(event);
				return true;
			case MotionEvent.ACTION_POINTER_UP:
				initTransform(event,
						// Ignore the pointer that has gone up.
						event.getActionIndex());
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				return true;
		}

		return super.onTouchEvent(event);
	}

	/**
	 * Set if image translation should be restricted to the bounds.
	 * Default is true.
	 *
	 * @param restrict true if translation should be restricted
	 */
	public void setRestrictTranslation(boolean restrict) {
		restrictTranslation = restrict;
	}

	/**
	 * Returns true if image translation is restricted.
	 */
	public boolean getRestrictTranslation() {
		return restrictTranslation;
	}

	/**
	 * Set whether the image can be freely rotated. Default is false.
	 *
	 * @param freeRotation true if the image should be freely rotated
	 */
	public void setFreeRotation(boolean freeRotation) {
		this.freeRotation = freeRotation;
	}

	/**
	 * Returns true if the image can be freely rotated.
	 */
	public boolean getFreeRotation() {
		return freeRotation;
	}

	/**
	 * Set multiplier for how much a double tap will magnify the image.
	 * Default is 4.
	 *
	 * @param scale magnifying scale
	 */
	public void setMagnifyScale(float scale) {
		magnifyScale = scale;
	}

	/**
	 * Return double tap magnification multiplier.
	 */
	public float getMagnifyScale() {
		return magnifyScale;
	}

	/**
	 * Set minimum width of image in view.
	 * Default is bounds.
	 *
	 * @param width minimum width of image in view in pixels
	 */
	public void setMinWidth(float width) {
		minWidth = width;
	}

	/**
	 * Return minimum width of image in view.
	 */
	public float getMinWidth() {
		return minWidth;
	}

	/**
	 * Set image rotation.
	 *
	 * @param degrees rotation in degrees
	 */
	public void setImageRotation(float degrees) {
		if (degrees == rotation) {
			return;
		}

		rotation = degrees;
		requestLayout();
	}

	/**
	 * Return current image rotation.
	 */
	public float getImageRotation() {
		if (freeRotation) {
			transformMatrix.getValues(transformMatrixValues);
			return (float) (Math.atan2(-transformMatrixValues[1],
					transformMatrixValues[4]) * RAD2DEG);
		} else {
			return rotation;
		}
	}

	/**
	 * Return X coordinate of pivot point.
	 */
	public float getPivotX() {
		return transformMatrixValues[2];
	}

	/**
	 * Return Y coordinate of pivot point.
	 */
	public float getPivotY() {
		return transformMatrixValues[5];
	}

	/**
	 * Return rectangle in image that is in view bounds.
	 */
	public Rect getRectInBounds() {
		RectF srcRect = getDrawableRect();
		RectF dstRect = new RectF();
		transformMatrix.mapRect(dstRect, srcRect);

		float scale = dstRect.width() / srcRect.width();
		return new Rect(
				Math.round((bounds.left - dstRect.left) / scale),
				Math.round((bounds.top - dstRect.top) / scale),
				Math.round((bounds.right - dstRect.left) / scale),
				Math.round((bounds.bottom - dstRect.top) / scale));
	}

	/**
	 * Return normalized rectangle in image that is in view bounds.
	 */
	public RectF getNormalizedRectInBounds() {
		RectF dstRect = getMappedRect();
		float w = dstRect.width();
		float h = dstRect.height();
		return new RectF(
				(bounds.left - dstRect.left) / w,
				(bounds.top - dstRect.top) / h,
				(bounds.right - dstRect.left) / w,
				(bounds.bottom - dstRect.top) / h);
	}

	/**
	 * Return rectangle of transformed image in view coordinates.
	 */
	public RectF getMappedRect() {
		transformMatrix.mapRect(mappedRect, getDrawableRect());
		return mappedRect;
	}

	/**
	 * Set bounds in whose to draw image in view.
	 *
	 * @param left   left coordinate in view
	 * @param top    top coordinate in view
	 * @param right  right coordinate in view
	 * @param bottom bottom coordinate in view
	 */
	public void setBounds(
			float left,
			float top,
			float right,
			float bottom) {
		bounds.set(left, top, right, bottom);
	}

	/**
	 * Set bounds in whose to draw image in view.
	 *
	 * @param rect rectangle in view
	 */
	public void setBounds(RectF rect) {
		bounds.set(rect);
	}

	/**
	 * Return bounds in whose to draw image in view.
	 */
	public RectF getBounds() {
		return bounds;
	}

	@Override
	protected void onLayout(
			boolean changed,
			int left,
			int top,
			int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		// Use a separate method to layout the image so it's possible
		// to override just layoutImage() without overriding this
		// onLayout() with this invocation in it.
		layoutImage(changed, left, top, right, bottom);
	}

	/**
	 * Layout image.
	 *
	 * @param changed true when layout has changed
	 * @param left    layout left coordinate
	 * @param top     layout top coordinate
	 * @param right   layout right coordinate
	 * @param bottom  layout bottom coordinate
	 */
	protected void layoutImage(
			boolean changed,
			int left,
			int top,
			int right,
			int bottom) {
		if (changed) {
			setBounds(left, top, right, bottom);
		}
		centerRemap();
	}

	/**
	 * Center image and remap the current transformation to make the
	 * image fill the exact same rectangle as the previous one.
	 */
	protected void centerRemap() {
		RectF dr = getDrawableRect();
		float newWidth = dr.width();
		float newHeight = dr.height();
		if (minWidth == 0 || inBounds() || rotation != lastRotation ||
				Math.abs(lastWidth / lastHeight - newWidth / newHeight) > .001f) {
			center(bounds);
		} else if (lastWidth != newWidth || lastHeight != newHeight) {
			transformMatrix.preScale(lastWidth / newWidth,
					lastHeight / newHeight);
			invalidateTransformation();
			fitImageAndSetMinWidth(bounds, new Matrix());
			super.setImageMatrix(transformMatrix);
		}
		lastWidth = newWidth;
		lastHeight = newHeight;
		lastRotation = rotation;
	}

	/**
	 * Center image in given rectangle.
	 *
	 * @param rect reference rectangle
	 */
	protected void center(RectF rect) {
		fitImageAndSetMinWidth(rect, transformMatrix);
		super.setImageMatrix(transformMatrix);
	}

	/**
	 * Returns true if the image is within the bounds.
	 */
	protected boolean inBounds() {
		RectF rect = getMappedRect();
		return rect.width() <= bounds.width() &&
				rect.height() <= bounds.height();
	}

	/**
	 * Reinitialize ongoing transformation.
	 */
	protected void invalidateTransformation() {
		reinit = true;
	}

	/**
	 * Fit image into minimum rectangle.
	 *
	 * @param rect   minimum rectangle in view
	 * @param matrix resulting matrix
	 * @return minimum width of image in view in pixels
	 */
	protected float fitImage(RectF rect, Matrix matrix) {
		// Don't try to store the drawable dimensions by overriding
		// setImageDrawable() since it is called in the ImageView's
		// constructor and no referenced member of this object will
		// have been initialized yet. So it's best to simply request
		// the dimensions when they are required only.
		RectF srcRect = getDrawableRect();

		if (rect == null || matrix == null) {
			return 0f;
		}

		float dw = srcRect.width();
		float dh = srcRect.height();
		float rw = rect.width();
		float rh = rect.height();

		if (dw < 1 || dh < 1 || rw < 1 || rh < 1) {
			return 0f;
		}

		// First rotate the image.
		RectF dstRect = new RectF();
		matrix.setTranslate(dw * -.5f, dh * -.5f);
		matrix.postRotate(rotation);
		matrix.mapRect(dstRect, srcRect);

		// Then center the bounding rectangle of the rotated image.
		float scale = getScaleFactorForScaleType(scaleType,
				rw / dstRect.width(),
				rh / dstRect.height());
		matrix.postScale(scale, scale);
		matrix.postTranslate(
				Math.round(rect.left + rw * .5f),
				Math.round(rect.top + rh * .5f));

		matrix.mapRect(dstRect, srcRect);

		return dstRect.width();
	}

	private static float getScaleFactorForScaleType(
			ScaleType scaleType,
			float width, float height) {
		switch (scaleType) {
			case CENTER:
				return 1f;
			case CENTER_INSIDE:
				return Math.min(width, height);
			case CENTER_CROP:
				return Math.max(width, height);
			default:
				throw new UnsupportedOperationException("ScaleType " +
						scaleType + " is not supported");
		}
	}

	private void init(Context context) {
		super.setScaleType(ImageView.ScaleType.MATRIX);
		gestureDetector = new GestureDetector(
				context,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onDoubleTap(MotionEvent event) {
						magnify(event.getX(), event.getY(), magnifyScale);
						initTransform(event, -1);
						return true;
					}
				});
	}

	private RectF getDrawableRect() {
		Drawable drawable;
		int w = 0;
		int h = 0;

		if ((drawable = getDrawable()) != null) {
			w = drawable.getIntrinsicWidth();
			h = drawable.getIntrinsicHeight();
		}

		return new RectF(0, 0, w, h);
	}

	private void initTransform(MotionEvent event, int ignoreIndex) {
		initialMatrix.set(transformMatrix);
		drawableRect.set(getDrawableRect());

		// Try to find two pointers that are down.
		// "event" may contain a pointer that has gone up and must be ignored.
		int p1 = 0xffff;
		int p2 = 0xffff;

		for (int i = 0, l = event.getPointerCount(); i < l; ++i) {
			initialPoint.put(event.getPointerId(i), new PointF(
					event.getX(i),
					event.getY(i)));

			if (i == ignoreIndex) {
				continue;
			} else if (p1 == 0xffff) {
				p1 = i;
			} else {
				p2 = i;
				break;
			}
		}

		if (p2 != 0xffff) {
			initialTapeline.set(event, p1, p2);
		}

		reinit = false;
	}

	private void transform(MotionEvent event) {
		if (reinit) {
			initTransform(event, -1);
			reinit = false;
		}

		transformMatrix.set(initialMatrix);

		int pointerCount = event.getPointerCount();
		if (pointerCount == 1) {
			int i = event.getActionIndex();
			PointF point = initialPoint.get(event.getPointerId(i));
			if (point != null) {
				transformMatrix.postTranslate(
						event.getX(i) - point.x,
						event.getY(i) - point.y);
			}
		} else if (pointerCount > 1) {
			transformTapeline.set(event, 0, 1);

			float scale = fitScale(
					initialMatrix,
					drawableRect,
					transformTapeline.length / initialTapeline.length);

			transformMatrix.postScale(
					scale,
					scale,
					initialTapeline.pivotX,
					initialTapeline.pivotY);

			if (freeRotation) {
				transformMatrix.postRotate(
						transformTapeline.angle - initialTapeline.angle,
						transformTapeline.pivotX,
						transformTapeline.pivotY);
			}

			transformMatrix.postTranslate(
					transformTapeline.pivotX - initialTapeline.pivotX,
					transformTapeline.pivotY - initialTapeline.pivotY);
		}

		if (restrictTranslation &&
				fitTranslate(transformMatrix, drawableRect, bounds)) {
			initTransform(event, -1);
		}

		super.setImageMatrix(transformMatrix);
	}

	private float fitScale(
			Matrix matrix,
			RectF rect,
			float scale) {
		RectF dstRect = new RectF();
		matrix.mapRect(dstRect, rect);

		float w = dstRect.width();
		return w * scale < minWidth
				? minWidth / w
				: scale;
	}

	private static boolean fitTranslate(
			Matrix matrix,
			RectF rect,
			RectF frame) {
		RectF dstRect = new RectF();
		matrix.mapRect(dstRect, rect);

		float x = dstRect.left;
		float y = dstRect.top;
		float w = dstRect.width();
		float h = dstRect.height();
		float fw = frame.width();
		float fh = frame.height();
		float minX = frame.right - w;
		float minY = frame.bottom - h;
		float dx = w > fw
				? Math.max(minX - x, Math.min(frame.left - x, 0))
				: (frame.left + Math.round((fw - w) * .5f)) - x;
		float dy = h > fh
				? Math.max(minY - y, Math.min(frame.top - y, 0))
				: (frame.top + Math.round((fh - h) * .5f)) - y;

		if (dx != 0 || dy != 0) {
			matrix.postTranslate(dx, dy);
			return true;
		}

		return false;
	}

	private void magnify(float x, float y, float scale) {
		RectF rc = getMappedRect();
		int mappedWidth = Math.round(rc.width());
		int mappedHeight = Math.round(rc.height());
		int boundsWidth = Math.round(bounds.width());
		int boundsHeight = Math.round(bounds.height());
		if ((mappedWidth == boundsWidth && mappedHeight <= boundsHeight) ||
				(mappedWidth <= boundsWidth && mappedHeight == boundsHeight)) {
			transformMatrix.postScale(scale, scale, x, y);
		} else {
			fitImage(bounds, transformMatrix);
		}
		super.setImageMatrix(transformMatrix);
	}

	private void fitImageAndSetMinWidth(RectF rect, Matrix matrix) {
		minWidth = fitImage(rect, matrix);
	}

	private static class Tapeline {
		private float length;
		private float pivotX;
		private float pivotY;
		private float angle;

		private void set(MotionEvent event, int p1, int p2) {
			float x1 = event.getX(p1);
			float y1 = event.getY(p1);
			float x2 = event.getX(p2);
			float y2 = event.getY(p2);
			float dx = x2 - x1;
			float dy = y2 - y1;

			length = (float) Math.hypot(dx, dy);
			pivotX = (x1 + x2) * .5f;
			pivotY = (y1 + y2) * .5f;

			angle = (float) (Math.atan2(dy, dx) * RAD2DEG);
		}
	}
}
