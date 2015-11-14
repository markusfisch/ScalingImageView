package de.markusfisch.android.imageviewmatrixprobe.fragment;

import de.markusfisch.android.imageviewmatrixprobe.R;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageFragment extends Fragment
{
	private ImageView imageView;

	@Override
	public View onCreateView(
		LayoutInflater inflater,
		ViewGroup container,
		Bundle state )
	{
		View view;

		if( (view = inflater.inflate(
				R.layout.fragment_image,
				container,
				false )) == null ||
			(imageView = (ImageView)view.findViewById(
				R.id.dummy )) == null )
			return null;

		Matrix m = new Matrix( imageView.getImageMatrix() );

		m.postTranslate( 100, 100 );

		imageView.setImageMatrix( m );

		return view;
	}
}
