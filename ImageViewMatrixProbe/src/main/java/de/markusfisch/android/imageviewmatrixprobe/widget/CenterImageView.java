package de.markusfisch.android.imageviewmatrixprobe.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;

public class CenterImageView extends ScalingImageView
{
	private final RectF bounds = new RectF();
	private final float values[] = new float[9];

	private float drawableWidth;
	private float drawableHeight;
	private boolean centersVertical;
	private float minScale;

	public CenterImageView( Context context, AttributeSet attr )
	{
		super( context, attr );
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

		bounds.set( left, top, right, bottom );
		centerImage( bounds );
	}

	protected void centerImage( RectF rect )
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

	@Override
	protected float fitScale( Matrix matrix, float scale )
	{
		matrix.getValues( values );
		float originScale = values[Matrix.MSCALE_X];

		return originScale*scale < minScale ?
			minScale/originScale :
			scale;
	}

	@Override
	protected void fitRect( Matrix matrix )
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

		if( centersVertical )
			matrix.postTranslate(
				Math.max( minX-x, Math.min( bounds.left-x, 0 ) ),
				h > bh ?
					Math.max( minY-y, Math.min( bounds.top-y, 0 ) ) :
					(bounds.top+Math.round( (bh-h)*.5f ))-y );
		else
			matrix.postTranslate(
				w > bw ?
					Math.max( minY-y, Math.min( bounds.top-y, 0 ) ) :
					(bounds.left+Math.round( (bw-w)*.5f ))-x,
				Math.max( minY-y, Math.min( bounds.top-y, 0 ) ) );
	}
}

