# Installation

Feinrip requires Java 8, ffmpeg, mplayer, mencoder, mkvmerge, and transcode.

On RedHat/Fedora based systems, execute this line to install all dependencies. You might need to set up [RPM fusion](http://rpmfusion.org/) before.

```
yum install java-1.8.0-openjdk ffmpeg mplayer mencoder mkvtoolnix transcode
```

After installation, just execute the compiled jar file:

```
java -jar feinrip.jar
```
