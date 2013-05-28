Introduction
============

This is a really basic Java programme using jlibfprint to work with Finger Print readers.

Uses jlibfprint wrapper from https://code.google.com/p/jlibfprint/.

Usage
=====

You can enroll people and verify prints.

Enrolling
---------

```
java -cp '.:JlibFprint.jar' FingerPrint enroll yourname
```

Verifying
---------

```
java -cp '.:JlibFprint.jar' FingerPrint verify [debug]
```

On verification, the programme will tell you whose print was just scanned.

Debug mode is activated by adding a second parameter - the value is irrelevant.

Troubleshooting
---------------

If, on running the enroll, you get a bunch of scrolling output asking you to scan your print... it's likely that the user you're using doesn't have access to the fingerprint scanner. Try running the programme as root and, if that
solves the problem, create a udev rule to give you permission to access the device. Example as follows:

On plugging in the USB fingerprint scanner, run ```dmesg```, and get the idVendor and idProduct of the device. Note these down.

Create a group called fingerprint:

```
groupadd fingerprint
```

Add your user to the fingerprint group (user, in this case, is pi):

```
usermod --append -G fingerprint pi
```

Remember to log out and back in to the user you changed the groups of to ensure it takes effect.

Create a file called ```99-usb.rules``` in your udev rules.d directory. In my case, ```/etc/udev/rules.d/```

Put the following in that file, remembering to replace the idVendor and idProduct with the ones you took from dmesg:

```
SUBSYSTEMS=="usb", ATTRS{idVendor}=="08ff", ATTRS{idProduct}=="2580", SYMLINK+="sub20", GROUP="fingerprint", MODE="660"
```

Restart udev:

```
/etc/init.d/udev restart
```

Unplug and replug the USB device. With any luck, you'll be able to access it as the regular user.

Installing
==========

Instructions are based on installing on Debian (Raspbian) on an RaspberryPi (armhf architecture). From memory, they don't differ from when I installed on an AMD64 Debian install.

I worked as a normal user named 'pi'. I'm assuming you have git and all the things you need to compile (gcc et al) as well as the Java JDK.

First install the required libraries:

```
apt-get install libusb-1.0-0-dev libnss3-dev libglib2.0-dev libmagickcore-dev libmagick++-dev
```

Make a directory under the user's home directory to do the installs into:

```
mkdir ~/fprint
cd ~/fprint
```

Clone this repository:

```
git clone https://github.com/phil-lavin/libfprint-java.git
```

Make a directory and download/unpack libfprint. Note I'm using 0.5.1 which is the latest version at time of writing. Check the libfprint site for the latest now:

```
mkdir libfprint
cd libfprint/
wget http://people.freedesktop.org/~hadess/libfprint-0.5.1.tar.xz
tar -xJf libfprint-0.5.1.tar.xz
```

Find ```img.c``` and edit it. In my case, it's at ```./libfprint-0.5.1/libfprint/img.c```

In ```img.c```, find the function ```int fpi_img_compare_print_data``` and add ```API_EXPORTED``` before it such that it looks like ```API_EXPORTED int fpi_img_compare_print_data```

Change into the unpacked libfprint directory:

```
cd libfprint-0.5.1
```

Compile libfprint:

```
./configure
make
sudo make install
```

Change back into the install directory you initially created:

```
cd ~/fprint
```

Make a directory and download/unpack Jlibfprint. Again, be sure to use the latest version from Jlibfprint's Google Code page:

```
mkdir jlibfprint
cd jlibfprint
wget http://jlibfprint.googlecode.com/files/Jlibfprint-0.1.tar.gz
tar -zxvf Jlibfprint-0.1.tar.gz
```

Change into the unpacked JlibFprint_jni directory:

```
cd JlibFprint_jni
```

Open the file ```Makefile``` for editing and find the line ```ADD_INCLUDES=```.

You need to add 4 things here, in the same style as the example below:

1. The path to the libfprint source directory. In my case, ```/home/pi/fprint/libfprint/libfprint-0.5.1```
2. The path to the libfprint/nbis/include directory. In my case, ```/home/pi/fprint/libfprint/libfprint-0.5.1/libfprint/nbis/include```
3. The system path which contains ```jni.h``` (hint use the locate command). In my case, ```/usr/lib/jvm/jdk-7-oracle-armhf/include```
4. The system path which contains ```jni_md.h``` (hint use the locate command). In my case, ```/usr/lib/jvm/jdk-7-oracle-armhf/include/linux```

A full example of that line is:

```
ADD_INCLUDES=-I/home/pi/fprint/libfprint/libfprint-0.5.1 -I/home/pi/fprint/libfprint/libfprint-0.5.1/libfprint/nbis/include -I/usr/lib/jvm/jdk-7-oracle-armhf/include -I/usr/lib/jvm/jdk-7-oracle-armhf/include/linux
```

Now compile Jlibfprint:

```
make all
```

Copy the newly compiled binary from the dist directory to your system library path. In my case:

```
sudo cp dist/Default/GNU-Linux-x86/libJlibFprint_jni.so /usr/lib/
```

Change directory into the git checkout of this repository. In my case:

```
cd ~/fprint/libfprint-java
```

Compile the Java:

```
javac -cp '.:JlibFprint.jar' FingerPrint.java
```

You can now run the programme as per the above Enrolling and Verifying instructions.

