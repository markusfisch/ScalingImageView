package de.markusfisch.android.imageviewmatrixprobe.fragment;

import de.markusfisch.android.imageviewmatrixprobe.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ImageFragment extends Fragment
{
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
				false )) == null )
			return null;

		return view;
	}
}
