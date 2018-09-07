PeerPaste Desktop
=================

This repository hosts the desktop edition of PeerPaste, a program that allows cut & paste between peers on a network. It is the companion application to the mobile PeerPaste app, which is currently available for Android on the Google Play Store.

Features:

* Automatic service discovery &mdash; once a device starts the app, other devices on the same local network will discover it automatically, so no configuration is needed.
* Encrypted data &mdash; the content that is pasted is encrypted in transit.
* Peer-to-peer key sharing &mdash; the encryption keys are shared via scanning out-of-band QR codes. There is no dependency on any centralized authority.
* Completely cloudless &mdash; Apart from the initial install (and any voluntary updates), no external servers are involved, ever.

## How to install

First you need to have Java installed on your machine. Then download `peerpaste.jar` and the shell script `peerpaste`. The jar will work on any machine that supports Java 8 or later. The shell script is a trivial wrapper that will work on any machine that supports a POSIX shell.

## How to run

Run the command `./peerpaste` to start the server.

Add "`--help`" to get a list of command-line options. See the FAQ section below for some useful options.

Stop the peerpaste server by typing ^C, or terminate it with the `kill` command if you are running it in the background.

## FAQ

Frequently Asked Questions, preloaded with a few anticipated questions.

#### [General] The app is running, but why don't my other devices see it?

This is most likely caused by network connectivity issues:

* Make sure your devices or your router don't block mDNS or mDNS-advertised traffic.
* If the peer devices are connected to different LANs, they will be on different broadcast domains, and mDNS will not work by default. This can be solved by setting up proper IP multicast that works across both LANs, or by ensuring that both devices are connected to the same LAN.

Another possibility is that you have an exceptional network configuration that causes the desktop server to pick the wrong IP address to advertise on mDNS, which makes it unavailable to other devices even though they are connected to the same LAN. This can be remedied by following the instructions in the FAQ answer *How do I bind the listening port to a different IP address?*

#### [General] Why should I trust your app to handle my secret data?

One major use case for PeerPaste is to paste passwords or password-reset links or signup confirmation links etc, from one device to another. This of course requires trusting that the app is secure. The encryption and decryption code used in the desktop edition is published here on GitHub. It uses industry standard practices, and it's open for anyone to audit. The exact same code is used in the Android version, with the exception of a couple of trivial differences in how the `Base64` library is called due to differences in the Java library versions.

#### [Android] Why do I get a Cryptographic failure error?

Either your Android version is too old to support the required crypto algorithm (AES-128/GCM), or you live in a country with a fascist government that restricts the use of strong cryptography. In either case this means that you cannot run PeerPaste on your Android device, and unfortunately there is nothing I can do about it.

#### [Android] Why does the Android app cost money?

Because I would like to recover some of my costs for developing and publishing the Android app.

#### [Android] Why is the Android app closed source?

Because I don't want to give away the source until I get compensation for my expenses for publishing the Android app.

#### [iOS] Is there a version available for iOS?

Not yet, but maybe it will happen if there is enough demand.

#### [General] How do I change the service name advertised by mDNS?

On Android the service name is the same as the Bluetooth device name. So to change it, go to the Bluetooth settings and open "Rename this device" where you can choose a new name.

In the desktop application you can change the service name by providing a command-line argument, e.g. `--name "Steve's Laptop"`.

#### [General] How do I bind the listening port to a different IP address?

This is only possible in the desktop application. You can either provide the IP address explicitly, via e.g. `--address 192.0.2.3`, or you can specify the network interface, e.g. `--interface eth1`.

You can also specify the port number via e.g. `--port 12345`. The default is a random port number picked by the operating system.

#### [Linux desktop] The clipboard is cleared when the application exits!

This is a known bug in the Xorg/X11 clipboard design. The X clipboard is only a reference to the original data, so when the originating application exits, the reference doesn't refer to anything anymore. The solution is to either use the clipboard data before exiting peerpaste desktop, or by running a clipboard manager (e.g. `xsel` or some other alternative).

#### [Linux desktop] Why is the text so fuzzy?

By default, the desktop application uses full pixel antialiasing on Linux, which looks fuzzy when compared to the more commonly used subpixel antialiasing. The reason is that Java can't determine the subpixel arrangement when running on Linux. But you can enable subpixel antialiasing explicitly by providing the command-line argument `--aa <VALUE>` where `<VALUE>` is one of:

* `lcd_hrgb`
* `lcd_hbgr`
* `lcd_vrgb`
* `lcd_vbgr`
* `lcd` &mdash; same as `lcd_hrbg`
* `default` &mdash; revert to Java's default, which may or may not be no antialiasing at all.

Try them one at a time until you find one that looks good on your desktop screen.

#### [Linux headless] What if I'm not running a graphic console?

The desktop application has a headless mode which allows you to send and receive data via the text console's stdin and stdout.

