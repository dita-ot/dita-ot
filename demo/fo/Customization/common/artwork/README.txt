About fo/Customization/common/artwork
=============================================

This folder houses custom artwork files that override the standard
ones in fo/cfg/common/artwork.  These files are used
to graphically identify different types of DITA <note> element.

The mapping between <note> type and graphic is contained in a subset of
the locale-dependent variable files, such as

	fo/cfg/common/vars

The variables that control <note> graphics all follow the form

  <variable id="{type} Note Image Path"> {AIS Path of image file} </variable>

where {type} contains a possible value for the <note> @type attribute.


