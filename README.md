Jin
===

Jin is a graphical chess client for the [Free Internet Chess
Service](http://freechess.org/) (FICS). It was written by [Alexander
Maryanovsky](mailto:msasha@gmail.com) and has its [own
website](http://jinchess.com). The main development trunk for Jin is hosted on
[Sourceforge](http://sourceforge.net/projects/jin/). That project has been
dormant for some years.

This GitHub repository is a fork of the Jin project designed to fix bugs and
add features that are missing from Jin.

The instructions that follow are primarily intended for Java programmers who
are interested in hacking on Jin.

Getting Jin
===========

1. Install `git`. If you need help, you can follow [these
   instructions](http://git-scm.com/book/en/Getting-Started-Installing-Git).
2. Clone this repository: `git clone https://github.com/sjamesr/jin.git`.

You will now have a directory called `jin` in your current working directory.

Building and running Jin
========================

This fork of Jin uses [Gradle](http://gradle.org/) as its build system. So,

1. Build jin using the Gradle bootstrap script: `./gradlew assemble`
2. Run Jin: `./gradlew run`

Developing with IntelliJ
========================

IntelliJ supports Gradle natively. You should be able to open the Jin directory
in IntelliJ and start coding right away.

