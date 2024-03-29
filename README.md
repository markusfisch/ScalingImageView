# ScalingImageView

An ImageView that transforms its drawable according to touch input.

Supports rotated images and keeps transformation when you exchange the
image for another one with a different size.

All of that in ~400 lines of code (excluding blanks and comments).

![Screencast](http://markusfisch.github.io/ScalingImageView/screencast.gif)

## How to include

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
		implementation 'com.github.markusfisch:ScalingImageView:1.2.0'
	}

### Android Archive

Alternatively you may just download the latest `aar` from
[Releases](https://github.com/markusfisch/ScalingImageView/releases) and put it
into `app/libs` in your app.

Then make sure your `app/build.gradle` contains the following line in the
`dependencies` block:

	dependencies {
		implementation fileTree(dir: 'libs', include: '*')
		...
	}

## How to use

Maybe in a layout:

	<de.markusfisch.android.scalingimageview.widget.ScalingImageView
		android:layout_width="match_parent"
		android:layout_height="match_parent"
		android:src="@drawable/your_drawable"/>

Or from java:

	import de.markusfisch.android.scalingimageview.widget.ScalingImageView;

	ScalingImageView scalingImageView = new ScalingImageView(context);

Changing the scale type must happen in source since reading attributes
would require a [declare-styleable][styleable] resource and some overhead
I think would outweigh its value for this.

So you need to call `setScaleType()`:

	scalingImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

Only `CENTER`, `CENTER_CROP` and `CENTER_INSIDE` are supported.
`CENTER_INSIDE` is the default.

Please note that using `android:adjustViewBounds="true"` will implicitly
set ScaleType to `FIT_CENTER` what is not supported and will result in an
UnsupportedOperationException.

## Supported Scale Types

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

## Image Rotation

You can either set any image rotation (in degrees) at any time with
`setImageRotation()`:

	scalingImageView.setImageRotation(90f);

Or allow the user to freely rotate the image with `setFreeRotation()`:

	scalingImageView.setFreeRotation(true);

If you want to know the angle and pivot of the rotation, you can use
`getImageRotation()`, `getPivotX()` and `getPivotY()`. Just make sure to
always invoke `getImageRotation()` first as it also updates the pivot point.

## Demo

In app/ you'll find a demo.
Either import it into Android Studio or, if you're not on that thing from
Redmond, just type make to build, install and run.

## License

This widget is so basic, it should be Public Domain. And it is.

[scaletype]: https://developer.android.com/reference/android/widget/ImageView.ScaleType.html
[styleable]: https://developer.android.com/training/custom-views/create-view.html
