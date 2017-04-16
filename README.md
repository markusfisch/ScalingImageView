ScalingImageView
================

An ImageView that transforms its drawable according to touch input.

![Screencast](http://markusfisch.github.io/ScalingImageView/screencast.gif)

How to include
--------------

### Gradle

Add the JitPack repository in your root build.gradle at the end of
repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Then add the dependency in your app/build.gradle:

	dependencies {
		compile 'com.github.markusfisch:ScalingImageView:1.0.0'
	}

### Manually

Just drop [ScalingImageView.java][src] into your project and use it instead
of an ImageView.

How to use
----------

Maybe in a layout:

	<de.markusfisch.android.scalingimageview.widget.ScalingImageView
		android:layout_width="match_parent"
		android:layout_height="match_parent"
		android:src="@drawable/your_drawable"/>

Or from java:

	ScalingImageView scalingImageView = new ScalingImageView(context);

Changing the scale type must happen in source since reading attributes
would require a [declare-styleable][styleable] resource and a bit of
overhead I think would outweigh its value for this.

So you need to call setScaleType() like this:

	scalingImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

Only CENTER, CENTER_CROP and CENTER_INSIDE are supported.
CENTER_INSIDE is the default.

Scale Types
-----------

### [ImageView.ScaleType.CENTER][scaletype]

> Center the image in the view, but perform no scaling.

	    +-------+ FRAME
	    |       |
	  +-|-------|-+
	  | | IMAGE | |
	  +-|-------|-+
	    |       |
	    +-------+

### [ImageView.ScaleType.CENTER_CROP][scaletype]

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

### [ImageView.ScaleType.CENTER_INSIDE][scaletype]

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

Demo
----

In app/ you'll find a demo.
Either import it into Android Studio or, if you're not on that thing from
Redmond, just type make to build, install and run.

License
-------

This widget is so basic, it should be Public Domain. And it is.

[src]: https://github.com/markusfisch/ScalingImageView/blob/master/scalingimageview/src/main/java/de/markusfisch/android/scalingimageview/widget/ScalingImageView.java
[scaletype]: https://developer.android.com/reference/android/widget/ImageView.ScaleType.html
[styleable]: https://developer.android.com/training/custom-views/create-view.html
