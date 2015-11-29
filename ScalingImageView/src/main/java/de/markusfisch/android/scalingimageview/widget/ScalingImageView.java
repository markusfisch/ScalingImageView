package de.markusfisch.android.scalingimageview.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ScalingImageView extends ImageView
{
	private final Matrix originMatrix = new Matrix();
	private final Matrix transformMatrix = new Matrix();
	private final SparseArray<Float> originX = new SparseArray<Float>();
	private final SparseArray<Float> originY = new SparseArray<Float>();
	private final Gesture originGesture = new Gesture();
	private final Gesture transformGesture = new Gesture();
	private final RectF bounds = new RectF();
	private final float values[] = new float[9];

	private float drawableWidth;
	private float drawableHeight;
	private boolean centersVertical;
	private float minScale;
	private ImageView.ScaleType scaleType =
		ImageView.ScaleType.CENTER_INSIDE;

	public ScalingImageView( Context context, AttributeSet attr )
	{
		super( context, attr );

		super.setScaleType( ImageView.ScaleType.MATRIX );
	}

	@Override
	public void setScaleType( ImageView.ScaleType scaleType )
	{
		if( scaleType != ImageView.ScaleType.CENTER_CROP &&
			scaleType != ImageView.ScaleType.CENTER_INSIDE )
			throw new UnsupportedOperationException();

		this.scaleType = scaleType;
		center( bounds );
	}

	@Override
	public ScaleType getScaleType()
	{
		return scaleType;
	}

	@Override
	public boolean onTouchEvent( MotionEvent event )
	{
		final int pointerCount = event.getPointerCount();
		int ignoreIndex = -1;

		switch( event.getActionMasked() )
		{
			// the number of pointers changed so
			// (re)initialize the transformation
			case MotionEvent.ACTION_POINTER_UP:
				// ignore the pointer that has gone up
				ignoreIndex = event.getActionIndex();
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				initTransform(
					event,
					pointerCount,
					ignoreIndex );
				return true;
			// the position of the pointer(s) changed
			// so transform accordingly
			case MotionEvent.ACTION_MOVE:
				transform( event, pointerCount );
				return true;
			// end of transformation
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				return true;
		}

		return onTouchEvent( event );
	}

	@Override
	protected void onLayout(
		boolean changed,
		int left,
		int top,
		int right,
		int bottom )
	{
		super.onLayout(
			changed,
			left,
			top,
			right,
			bottom );

		setBounds( left, top, right, bottom );
		center( bounds );
	}

	protected void setBounds(
		float left,
		float top,
		float right,
		float bottom )
	{
		bounds.set( left, top, right, bottom );
	}

	protected void setBounds( RectF rect )
	{
		bounds.set( rect );
	}

	protected void center( RectF rect )
	{
		Drawable drawable = getDrawable();

		if( drawable == null )
			return;

		drawableWidth = drawable.getIntrinsicWidth();
		drawableHeight = drawable.getIntrinsicHeight();

		float rw = rect.width();
		float rh = rect.height();

		centersVertical = rw*drawableHeight < rh*drawableWidth;

		minScale = drawableWidth > rw || drawableHeight > rh ?
			scaleType == ImageView.ScaleType.CENTER_INSIDE ?
				Math.min(
					rw/drawableWidth,
					rh/drawableHeight ) :
				Math.max(
					rw/drawableWidth,
					rh/drawableHeight ) :
			1f;

		transformMatrix.setScale( minScale, minScale );
		transformMatrix.postTranslate(
			rect.left+Math.round( (rw - drawableWidth*minScale)*.5f ),
			rect.top+Math.round( (rh - drawableHeight*minScale)*.5f ) );

		setImageMatrix( transformMatrix );
	}

	private void initTransform(
		MotionEvent event,
		int pointerCount,
		int ignoreIndex )
	{
		originMatrix.set( transformMatrix );

		// try to find two pointers that are down;
		// pointerCount may include a pointer that
		// has gone up (ignoreIndex)
		int p1 = 0xffff;
		int p2 = 0xffff;

		for( int n = 0; n < pointerCount; ++n )
		{
			int id = event.getPointerId( n );

			originX.put( id, event.getX( n ) );
			originY.put( id, event.getY( n ) );

			if( n == ignoreIndex ||
				p2 != 0xffff )
				continue;

			if( p1 == 0xffff )
				p1 = n;
			else if( p2 == 0xffff )
				p2 = n;
		}

		if( p2 != 0xffff )
			originGesture.set( event, p1, p2 );
	}

	private void transform( MotionEvent event, int pointerCount )
	{
		transformMatrix.set( originMatrix );

		if( pointerCount == 1 )
		{
			int id = event.getPointerId( 0 );

			transformMatrix.postTranslate(
				event.getX( 0 )-originX.get( id ),
				event.getY( 0 )-originY.get( id ) );
		}
		else if( pointerCount > 1 )
		{
			transformGesture.set( event, 0, 1 );

			float scale = fitScale(
				originMatrix,
				transformGesture.length/originGesture.length );

			transformMatrix.postScale(
				scale,
				scale,
				originGesture.pivotX,
				originGesture.pivotY );

			transformMatrix.postTranslate(
				transformGesture.pivotX-originGesture.pivotX,
				transformGesture.pivotY-originGesture.pivotY );
		}

		if( fitRect( transformMatrix ) )
			initTransform( event, pointerCount, -1 );

		setImageMatrix( transformMatrix );
	}

	private float fitScale( Matrix matrix, float scale )
	{
		matrix.getValues( values );
		float originScale = values[Matrix.MSCALE_X];

		return originScale*scale < minScale ?
			minScale/originScale :
			scale;
	}

	private boolean fitRect( Matrix matrix )
	{
		matrix.getValues( values );

		float scale = values[Matrix.MSCALE_X];
		float x = values[Matrix.MTRANS_X];
		float y = values[Matrix.MTRANS_Y];
		float w = scale*drawableWidth;
		float h = scale*drawableHeight;
		float bw = bounds.width();
		float bh = bounds.height();
		float minX = bounds.right-w;
		float minY = bounds.bottom-h;
		float dx = 0;
		float dy = 0;

		if( scaleType == ImageView.ScaleType.CENTER_INSIDE )
		{
			if( centersVertical )
			{
				dx = Math.max( minX-x, Math.min( bounds.left-x, 0 ) );
				dy = h > bh ?
					Math.max( minY-y, Math.min( bounds.top-y, 0 ) ) :
					(bounds.top+Math.round( (bh-h)*.5f ))-y;
			}
			else
			{
				dx = w > bw ?
					Math.max( minY-y, Math.min( bounds.top-y, 0 ) ) :
					(bounds.left+Math.round( (bw-w)*.5f ))-x;
				dy = Math.max( minY-y, Math.min( bounds.top-y, 0 ) );
			}
		}
		else if( scaleType == ImageView.ScaleType.CENTER_CROP )
		{
			if( centersVertical )
			{
				dx = Math.max( minX-x, Math.min( bounds.left-x, 0 ) );
				dy = Math.min( bounds.top-y, Math.max( minY-y, 0 ) );
			}
			else
			{
				dx = Math.min( bounds.left-x, Math.max( minX-x, 0 ) );
				dy = Math.max( minY-y, Math.min( bounds.top-y, 0 ) );
			}
		}

		if( dx != 0 || dy != 0 )
		{
			matrix.postTranslate( dx, dy );
			return true;
		}

		return false;
	}

	private class Gesture
	{
		public float length;
		public float pivotX;
		public float pivotY;

		public void set( MotionEvent event, int p1, int p2 )
		{
			float x1 = event.getX( p1 );
			float y1 = event.getY( p1 );
			float x2 = event.getX( p2 );
			float y2 = event.getY( p2 );
			float dx = x2-x1;
			float dy = y2-y1;

			length = (float)Math.sqrt( dx*dx + dy*dy );
			pivotX = (x1+x2)*.5f;
			pivotY = (y1+y2)*.5f;
		}
	}
}
