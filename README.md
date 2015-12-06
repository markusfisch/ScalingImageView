ScalingImageView
================

An android.widget.ImageView that transforms its drawable according to
user input.

![Screencast](http://markusfisch.github.io/ScalingImageView/screencast.gif)

Only [ImageView.ScaleType.CENTER][scaletype]:

> Center the image in the view, but perform no scaling.

	    +-------+ FRAME
	    |       |
	  +-|-------|-+
	  | | IMAGE | |
	  +-|-------|-+
	    |       |
	    +-------+

And [ImageView.ScaleType.CENTER_CROP][scaletype]:

> Scale the image uniformly (maintain the image's aspect ratio) so that both
> dimensions (width and height) of the image will be equal to or larger than
> the corresponding dimension of the view.

	      FRAME
	+---+-------+---+
	|   |       |   |
	|   |       |   |
	| I |M  A  G| E |
	|   |       |   |
	|   |       |   |
	+---+-------+---+

And [ImageView.ScaleType.CENTER_INSIDE][scaletype] (default) are supported:

> Scale the image uniformly (maintain the image's aspect ratio) so that both
> dimensions (width and height) of the image will be equal to or less than the
> corresponding dimension of the view.

	    +-------+ FRAME
	    |       |
	    +-------+
	    | IMAGE |
	    +-------+
	    |       |
	    +-------+

How to use
----------

Just drop de.markusfisch.android.scalingimageview.widget.ScalingImageView
into your project and use it instead of an ImageView.

Maybe in a layout:

	<de.markusfisch.android.scalingimageview.widget.ScalingImageView
		xmlns:android="http://schemas.android.com/apk/res/android"
		android:layout_width="match_parent"
		android:layout_height="match_parent"
		android:src="@drawable/your_drawable"/>

Or just from source:

	ScalingImageView scalingImageView = new ScalingImageView( context );

Changing the scale type must happen in source since reading attributes
would require a [declare-styleable][styleable] resource and a bit of
overhead I think would outweigh its value for this.

So you need to call setScaleType() like this:

	scalingImageView.setScaleType( ImageView.ScaleType.CENTER_CROP );

Remember, only CENTER, CENTER_CROP and CENTER_INSIDE are supported.
CENTER_INSIDE is the default.

Demo
----

This is a demo app you may use to see and try if this widget is what
you're searching for. Either import it into Android Studio or, if you're
not on that thing from Redmond, just type make to build, install and run.

License
-------

This widget is so basic, it should be Public Domain. And it is.

[scaletype]: https://developer.android.com/reference/android/widget/ImageView.ScaleType.html
[styleable]: https://developer.android.com/training/custom-views/create-view.html
