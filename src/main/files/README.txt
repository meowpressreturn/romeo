Romeo ${project.artifact.version}, "Havana" Edition
-----------------------------

Last updated: 2023-06-19

Romeo requires a  Java 8 or above Java Runtime Environment (JRE) to run.
You may download an Open JDK implementation if Java is not present
on your computer already. Java 8 is quite old now. At the time of writing
the latest Long-Term-Support version is 17 and can be downloaded from:
https://adoptium.net/

(The above link is for the Eclipse "Temurin" builds of OpenJDK Java, but
Romeo should be fine with any build of Java so long as it is version 8 
or above).

IMPORTANT: A unit.csv file MUST be obtained (it may be downloaded from
the UltraCorps(tm) units help page of the particular game you are in). 
Since version 0.6.3, Romeo will prompt you for the location of the unit.csv
file on startup. If you do not supply one it will not be able to start.

If you are upgrading a database from an older version of Romeo then it
is highly reccomended that you make a backup of it first as Romeo will update
its structure to be compatible with the current version and this process will
usually make it incompatible with the older version. 

If this process fails, for example you get an "Exception caught running 
initialisers" error message during startup you might need to delete the 
'database' folder before trying again as the initialisers may have left it 
in a partially upgraded and unusable state. (This is a technical limitation 
in the current versions of Romeo).

As of 0.6.4 Romeo uses an executable jar file, so for most users it should no
longer be necessary to use the .bat file to run Romeo. Instead you can double
click the romeo jar file. 

If this doesn't work for you, or you want to see Romeo's logging output then
you can still run Romeo from the command line. A Romeo.bat file is provided for 
Windows users to do this easily. A .sh equivalent has been provided for use
by users on Linux and Max systems. nb: You may need to set the execute permission
for romeo.sh or run it via the bash command.

Do be aware that Romeo is a Java 'Application', and not merely a Java 'Applet'.
This means that it runs with FULL PERMISSIONS (like a non-java .exe file would) 
so you want to take care before you run it that you have obtained it from a 
source you trust and that it hasn't been modified. 
(This is why I publish the file signatures along with the download links in the 
forum post).  

If you are finding problems with using Romeo, check the console window
for exception stacktraces. (You will need to load it with romeo.bat to get
console logging displayed).

If when running the romeo.bat file the console windows only flashes open for a
second and immediately closes again without Romeo starting it usually indicates
some issue with your Java installation (or more typically, your lack thereof). 

Try opening a command shell and typing java -version
to see what version you have installed, and whether your OS can find it.
(I have also heard of this issue occuring in cases where a 32 bit Java JRE
was installed on a 64 bit machine). As per Sun/Oracle's completely logical
version numbering system, Java 8 will report its version as 1.8.something.
For example:
  java version "1.8.0_92"
  Java(TM) SE Runtime Environment (build 1.8.0_92-b14)
  Java HotSpot(TM) 64-Bit Server VM (build 25.92-b14, mixed mode)

More modern Java versions show more sensible numbers. My machine at the time of
writing has Java 17 and shows this:
  openjdk version "17.0.7" 2023-04-18
  OpenJDK Runtime Environment Temurin-17.0.7+7 (build 17.0.7+7)
  OpenJDK 64-Bit Server VM Temurin-17.0.7+7 (build 17.0.7+7, mixed mode, sharing)

Romeo will create a database folder in its directory when it starts up.
While Romeo is NOT running you can make backup copies of this. (It locks the
database files exclusively while it is running). If you have an older version
of Romeo installed with data you want, you can copy the database folder from it
over to the new one. Romeo is smart enough to update it when it starts up. 

There were some major revisions to this upgrading process in 0.6.3 that should
allow even the oldest of romeo database folders to be imported into a modern
version of Romeo. 

If you need to track more than one game at a time you should create additional
seperate Romeo installations. 

The default unit and xfactor data is _only_ imported when a particular installation
of Romeo starts up the first time and creates its database folder. (At this time it
looks for known unit names and links up the appropriate x-factors with them)

Romeo will look in its config folder for unit.csv to import units into the database
from. If it isn't there it will prompt you for one, which it then copies into the
config folder. (In previous versions of Romeo, the 'config' folder was named as 
'resources' and you may still see some references to this in the documentation. 
In 0.6.4 it was split into a resources folder which lives inside the jar file now,
and the config folder which has been left out of the jar file to faciliate simple
maintainance activities on the config files it contains).

If you need to reset this to try to import again then you should delete (or rename)
the database folder so that Romeo will try to recreate it. nb: Ensure Romeo is
not currently running before trying to remove or rename the database folder as
Romeo will lock some of the files in there while it is running.

The unit.csv file does not contain any xfactor definitions, acronymns, or 
scanner ranges.  Romeo therefore attempts to identify units to set defaults for
based on a signature (MD5) of the unit name. If the units have been renamed since
this version of  Romeo  was released then the acronymns and xfactors will not be able
to be added automatically. You may do it manually via the Romeo user interface then. 
(e.g. The Biobomb has been named as either the A-90 or U-90 at various times)

