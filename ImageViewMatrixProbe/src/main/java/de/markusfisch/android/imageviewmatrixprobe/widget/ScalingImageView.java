package de.markusfisch.android.imageviewmatrixprobe.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ScalingImageView extends ImageView
{
	private static final int MAX_POINTERS = 24;

	private final Matrix origin = new Matrix();
	private final Matrix transformed = new Matrix();
	private final float originX[] = new float[MAX_POINTERS];
	private final float originY[] = new float[MAX_POINTERS];
	private final Length originLength = new Length();
	private final Length length = new Length();

	public ScalingImageView( Context context, AttributeSet attr )
	{
		super( context, attr );

		setScaleType( ImageView.ScaleType.MATRIX );
		setImageMatrix( origin );
	}

	@Override
	public boolean onTouchEvent( MotionEvent event )
	{
		final int pointerCount = Math.min(
			event.getPointerCount(),
			MAX_POINTERS-1 );
		int ignore = -1;

		switch( event.getActionMasked() )
		{
			// the number of pointers changed so
			// (re)initialize the transformation
			case MotionEvent.ACTION_POINTER_UP:
				ignore = event.getActionIndex();
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				setOriginMatrix();
				setPointerOrigins( event, pointerCount );
				setOriginLength( event, pointerCount, ignore );
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

	//private void initTransform( MotionEvent event, int pointerCount )

	private void setOriginMatrix()
	{
		origin.set( transformed );
	}

	private void setPointerOrigins( MotionEvent event, int pointerCount )
	{
		for( int n = pointerCount; n-- > 0; )
		{
			int id = event.getPointerId( n );

			originX[id] = event.getX( n );
			originY[id] = event.getY( n );
		}
	}

	private void setOriginLength(
		MotionEvent event,
		int pointerCount,
		int ignore )
	{
		if( pointerCount < 2 )
			return;

		int p1 = 0;
		int p2 = 1;

		if( ignore > -1 &&
			pointerCount > 2 )
		{
			p1 = p2 = 0xffff;

			for( int n = 0; n < pointerCount; ++n )
			{
				if( n != ignore )
				{
					if( p1 == 0xffff )
						p1 = n;
					else if( p2 == 0xffff )
						p2 = n;
					else
						break;
				}
			}
		}

		originLength.set( event, p1, p2 );
	}

	private void transformImage( MotionEvent event, int pointerCount )
	{
		// get the origin as it was at the beginning of the
		// transforming gesture since each move event shall
		// transform from the same origin
		transformed.set( origin );

		if( pointerCount == 1 )
		{
			int id = event.getPointerId( 0 );
			transformed.postTranslate(
				event.getX( 0 )-originX[id],
				event.getY( 0 )-originY[id] );
		}
		else if( pointerCount > 1 )
		{
			length.set( event, 0, 1 );

			float d = length.length/originLength.length;
			transformed.postScale(
				d,
				d,
				originLength.pivotX,
				originLength.pivotY );

			transformed.postTranslate(
				length.pivotX-originLength.pivotX,
				length.pivotY-originLength.pivotY );
		}

		setImageMatrix( transformed );
	}

	private class Length
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
