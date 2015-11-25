package de.markusfisch.android.imageviewmatrixprobe.widget;

import android.content.Context;
import android.graphics.Matrix;
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

	public ScalingImageView( Context context, AttributeSet attr )
	{
		super( context, attr );

		setScaleType( ImageView.ScaleType.MATRIX );
		setImageMatrix( originMatrix );
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
				ignoreIndex = event.getActionIndex();
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				initTransform( event, pointerCount, ignoreIndex );
				return true;
			// the position of the pointer(s) changed
			// so transform accordingly
			case MotionEvent.ACTION_MOVE:
				transformImage( event, pointerCount );
				return true;
			// end of transformation
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				return true;
		}

		return onTouchEvent( event );
	}

	private void initTransform(
		MotionEvent event,
		int pointerCount,
		int ignoreIndex )
	{
		originMatrix.set( transformMatrix );

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

	private void transformImage( MotionEvent event, int pointerCount )
	{
		// get the originMatrix as it was at the beginning of the
		// transforming gesture since each move event shall
		// transform from the same originMatrix
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

			float d = transformGesture.length/originGesture.length;
			transformMatrix.postScale(
				d,
				d,
				originGesture.pivotX,
				originGesture.pivotY );

			transformMatrix.postTranslate(
				transformGesture.pivotX-originGesture.pivotX,
				transformGesture.pivotY-originGesture.pivotY );
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
/*
		Drawable drawable = getDrawable();
		drawable.getIntrinsicWidth();
		drawable.getIntrinsicHeight();

		float values[] = new float[9];
		matrix.getValues( values );

s 0 x
0 s y
0 0 0

		scale = values[0];
		x = values[2];
		y = values[5];
*/
}
