ScalingImageView
================

An android.widget.ImageView that transforms its drawable according to
user input.

Only ScaleType.CENTER_INSIDE:

	    +-------+ FRAME
	    |       |
	    +-------+
	    | IMAGE |
	    +-------+
	    |       |
	    +-------+

And ScaleType.CENTER_CROP are supported:

	      FRAME
	+---+-------+---+
	|   |       |   |
	|   |       |   |
	| I |M  A  G| E |
	|   |       |   |
	|   |       |   |
	+---+-------+---+
