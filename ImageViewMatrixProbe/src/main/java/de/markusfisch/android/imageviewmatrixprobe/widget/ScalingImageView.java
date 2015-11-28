package de.markusfisch.android.imageviewmatrixprobe.widget;

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
	private float minScale;

	public ScalingImageView( Context context, AttributeSet attr )
	{
		super( context, attr );

		setScaleType( ImageView.ScaleType.MATRIX );
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

		if( !changed )
			return;

		bounds.set( left, top, right, bottom );
		centerInside( bounds );
	}

	protected void centerInside( RectF rect )
	{
		Drawable drawable = getDrawable();

		if( drawable == null )
			return;

		drawableWidth = drawable.getIntrinsicWidth();
		drawableHeight = drawable.getIntrinsicHeight();

		float rw = rect.width();
		float rh = rect.height();

		minScale = drawableWidth > rw || drawableHeight > rh ?
			Math.min(
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
		transformMatrix.getValues( values );

		float scale = values[Matrix.MSCALE_X];
		float dx = 0;
		float dy = 0;

		if( pointerCount == 1 )
		{
			int id = event.getPointerId( 0 );

			dx = event.getX( 0 )-originX.get( id );
			dy = event.getY( 0 )-originY.get( id );
		}
		else if( pointerCount > 1 )
		{
			transformGesture.set( event, 0, 1 );

			float mod =
				transformGesture.length/
				originGesture.length;

			if( mod*scale < minScale )
			{
				mod = minScale/scale;
				scale = minScale;
			}
			else
				scale *= mod;

			transformMatrix.postScale(
				mod,
				mod,
				originGesture.pivotX,
				originGesture.pivotY );

			dx = transformGesture.pivotX-originGesture.pivotX;
			dy = transformGesture.pivotY-originGesture.pivotY;
		}

		// align to bounds
		{
			float minX = bounds.width()-drawableWidth*scale;
			float minY = bounds.height()-drawableHeight*scale;

			float ox = values[Matrix.MTRANS_X];
			float x = ox+dx;
			float oy = values[Matrix.MTRANS_Y];
			float y = ox+dy;

			if( x > bounds.left )
			{
				dx = bounds.left-ox;
			}
			/*else if( x < minX )
			{
android.util.Log.d( "mfdbg", "mfdbg: dx("+dx+") x("+x+") < minX("+minX+") = dx'("+(minX-ox)+")" );
				dx = minX-ox;
			}*/

android.util.Log.d( "mfdbg", "mfdbg: dx("+dx+") x("+x+")" );
			transformMatrix.postTranslate( dx, dy );
		}

		setImageMatrix( transformMatrix );
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
