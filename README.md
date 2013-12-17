Introduction
============

This is a really basic Java programme using jlibfprint to work with Finger Print readers.

Usage
=====

You can enroll people and verify prints.

Enrolling
---------

java -cp '.:JlibFprint.jar' FingerPrint enroll yourname

Verifying
---------

java -cp '.:JlibFprint.jar' FingerPrint verify [debug]

On verification, the programme will tell you whose print was just scanned.

Debug mode is activated by adding a second parameter - the value is irrelevant.

Installing
==========

To come when I've replicated it...
