package de.markusfisch.android.imageviewmatrixprobe.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ScalingImageView extends ImageView
{
	private static int MAX_POINTERS = 10;

	private float pointerX[] = new float[MAX_POINTERS];
	private float pointerY[] = new float[MAX_POINTERS];
	private int pointersDown = 0;
	private float identityDist;
	private float centerX;
	private float centerY;
	private Matrix matrix = new Matrix();
	private Matrix transforming = new Matrix();

	public ScalingImageView( Context context, AttributeSet attr )
	{
		super( context, attr );

		setScaleType( ImageView.ScaleType.MATRIX );
		setImageMatrix( matrix );
	}

	@Override
	public boolean onTouchEvent( MotionEvent event )
	{
		final int pointerCount = Math.min(
			event.getPointerCount(),
			MAX_POINTERS );

		switch( event.getActionMasked() )
		{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				setOffset( event, pointerCount );
				return true;
			case MotionEvent.ACTION_MOVE:
				transformImage( event, pointerCount );
				return true;
			case MotionEvent.ACTION_POINTER_UP:
				pointersDown = pointerCount;
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				matrix.set( getImageMatrix() );
				pointersDown = 0;
				return true;
		}

		return onTouchEvent( event );
	}

	private void setOffset( MotionEvent event, int pointerCount )
	{
		for( int n = pointerCount; n-- > pointersDown; )
		{
			pointerX[n] = event.getX( n );
			pointerY[n] = event.getY( n );
		}

		pointersDown = pointerCount;

		if( pointerCount > 1 )
		{
			identityDist = dist( event );
			centerX = (event.getX( 0 )+event.getX( 1 ))*.5f;
			centerY = (event.getY( 0 )+event.getY( 1 ))*.5f;
		}
	}

	private void transformImage( MotionEvent event, int pointerCount )
	{
		if( pointerCount < 1 )
			return;

		transforming.set( matrix );

		if( pointerCount == 1 )
		{
			float x = event.getX( 0 )-pointerX[0];
			float y = event.getY( 0 )-pointerY[0];

			transforming.postTranslate( x, y );
		}
		else if( pointerCount > 1 )
		{
			float d = dist( event )/identityDist;

			transforming.postScale(
				d,
				d,
				centerX,
				centerY );
		}

		setImageMatrix( transforming );
	}

	private static float dist( MotionEvent event )
	{
		float dx = event.getX( 1 )-event.getX( 0 );
		float dy = event.getY( 1 )-event.getY( 0 );

		return dx*dx + dy*dy;
	}
}
