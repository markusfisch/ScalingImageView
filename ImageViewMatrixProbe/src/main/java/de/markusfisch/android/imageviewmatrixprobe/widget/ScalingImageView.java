package de.markusfisch.android.imageviewmatrixprobe.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ScalingImageView extends ImageView
{
	private static int MAX_POINTERS = 10;

	private float originX[] = new float[MAX_POINTERS];
	private float originY[] = new float[MAX_POINTERS];
	private float identityDist;
	private float centerX;
	private float centerY;
	private Matrix matrix = new Matrix();
	private Matrix transforming = new Matrix();

	public ScalingImageView( Context context, AttributeSet attr )
	{
		super( context, attr );

		clearOrigins();

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
				setMatrixFromImageMatrix();
				clearOrigins();
				setOrigins( event, pointerCount );
				initScaling( event, pointerCount );
				return true;
			case MotionEvent.ACTION_MOVE:
				transformImage( event, pointerCount );
				return true;
			case MotionEvent.ACTION_POINTER_UP:
				setMatrixFromImageMatrix();
				clearOrigins();
				setOrigins( event, pointerCount );
				initScaling( event, pointerCount );
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				setMatrixFromImageMatrix();
				clearOrigins();
				return true;
		}

		return onTouchEvent( event );
	}

	private void setMatrixFromImageMatrix()
	{
		matrix.set( getImageMatrix() );
	}

	private void clearOrigins()
	{
		for( int n = MAX_POINTERS; n-- > 0; )
			originX[n] = -1;
	}

	private void setOrigins( MotionEvent event, int pointerCount )
	{
		for( int n = pointerCount; n-- > 0; )
		{
			int id = getPointerId( event, n );

			if( originX[id] < 0 )
			{
				originX[id] = event.getX( n );
				originY[id] = event.getY( n );
			}
		}
	}

	private void initScaling( MotionEvent event, int pointerCount )
	{
		if( pointerCount < 2 )
			return;

		float x1 = event.getX( 0 );
		float y1 = event.getY( 0 );
		float x2 = event.getX( 1 );
		float y2 = event.getY( 1 );

		identityDist = dist( x1, y1, x2, y2 );
		centerX = (x1+x2)*.5f;
		centerY = (y1+y2)*.5f;
	}

	private void transformImage( MotionEvent event, int pointerCount )
	{
		if( pointerCount < 1 )
			return;

		transforming.set( matrix );

		/*if( pointerCount == 1 )
		{
			int id = getPointerId( event, 0 );

			transforming.postTranslate(
				event.getX( 0 )-originX[id],
				event.getY( 0 )-originY[id] );
		}
		else*/ if( pointerCount > 1 )
		{
			float d = dist(
				event.getX( 0 ),
				event.getY( 0 ),
				event.getX( 1 ),
				event.getY( 1 ) )/identityDist;

			transforming.postScale(
				d,
				d,
				centerX,
				centerY );
		}

		int id = getPointerId( event, 0 );

		transforming.postTranslate(
			event.getX( 0 )-originX[id],
			event.getY( 0 )-originY[id] );

		setImageMatrix( transforming );
	}

	private static int getPointerId( MotionEvent event, int index )
	{
		/*return Math.min(
			event.getPointerId( index ),
			MAX_POINTERS-1 );*/

		return event.getPointerId( index );
	}

	private static float dist( float x1, float y1, float x2, float y2 )
	{
		float dx = x2-x1;
		float dy = y2-y1;

		//return Math.sqrt( dx*dx + dy*dy );
		return dx*dx + dy*dy;
	}

/*
		Drawable drawable = getDrawable();
		drawable.getIntrinsicWidth();
		drawable.getIntrinsicHeight();

s 0 x
0 s y
0 0 0

private static String matrixToString( Matrix m )
{
	String s = "";
	float values[] = new float[9];
	m.getValues( values );
	for( int n = 0, l = values.length; n < l; ++n )
		s += values[n]+",";
	return s;
}
*/
}
