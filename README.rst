feinrip
=======

Feinrip is a GUI frontend for converting mpeg files to mkv files.

It is written in Java and runs on Linux systems. It may run on Mac and (with a few tweaks) on Windows. If you made it run on these OS, please send in a patch.

Installation
------------

Feinrip requires Java 8, ffmpeg, mplayer, mencoder, mkvmerge, and transcode.

On RedHat/Fedora based systems, execute this line to install all dependencies. You might need to set up `RPM fusion <http://rpmfusion.org/>`_ before:

    ``yum install java-1.8.0-openjdk ffmpeg mplayer mencoder mkvmerge transcode``

After installation, just execute the jar file:

    ``java -jar feinrip.jar``

Development
-----------

The project is hosted at `GitHub <https://github.com/shred/feinrip>`_.

Licenses
--------

Feinrip consists of three modules.

feinrip-gui is distributed under the terms of GNU General Public License V3.

feinrip-dvb and feinrip-lsdvd are distributed under the terms of GNU Lesser General Public License V3.

Font: "Calligraffiti" by `Font Squirrel <http://www.fontsquirrel.com/fonts/calligraffiti>`_, distributed under the terms of Apache License V2.

Icons: "GNOME icon theme" by `The GNOME Project <http://www.gnome.org>`_, distributed under the terms of GNU Lesser General Public License V3 or Creative Commons BY-SA 3.0 license.
