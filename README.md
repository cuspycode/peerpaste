PeerPaste Desktop
=================

This is the desktop edition of PeerPaste, a program that allows cut & paste between peers on a network. It is the companion application for the mobile PeerPaste app, which is currently available for Android on the Google Play Store.

Features:

* Automatic service discovery -- once a device starts the app, other devices on the same local network will discover it automatically, so no configuration is needed.
* Encrypted data -- the content that is pasted is encrypted in transit.
* Peer-to-peer key sharing -- the encryption keys are shared via scanning of out-of-band QR codes. There is no dependency on any centralized authority.
* Completely cloudless -- Apart from the initial install (and any voluntary updates), no external servers are involved.

## How to install

First you need to have Java 8 installed on your machine. Then download `peerpaste.jar` and optionally download the shell script `peerpaste-server`. The jar will work on any machine that supports Java 8. The shell script will work on any machine that supports POSIX shell.

## How to run

Run the command `./peerpaste-server`. Bla bla bla service name.

## FAQ

Frequently Asked Questions (preloaded with anticipated questions).

### [General] The app is running, but why don't other devices see it?

This is most likely caused by network connectivity issues:

* Make sure any firewalls on your devices or on your access point don't block mDNS or mDNS-advertised traffic.
* The peer devices are connected to different LANs. This means they are on different broadcast domains, and mDNS will not work by default. This can be solved by setting up proper IP multicast that works across both LANs, or by ensuring that both devices are connected to the same LAN.

### [General] Why should I trust your app to handle my secret data?

One major use case for PeerPaste is to paste passwords or password-reset links or signup confirmation links etc, from one device to another. This, of course, requires trusting that the app is secure. The encryption and decryption code used in the desktop edition is published here on GitHub. It uses standard industry practices, and it's open for anyone to audit. The exact same code is used in the Android version, with the exception of a couple of trivial differences in how the `Base64` library is called in Java2SE, as compared to Android.

### [Android] Why do I get a Cryptographic failure error?

Either your Android version is too old to support the required crypto algorithm (AES-128/GCM), or you live in a country with a fascist government that restricts the use of strong cryptography. In either case this means that you cannot run PeerPaste on your device, and unfortunately there is nothing I can do about it.

### [Android] Why does the Android app cost money?

The Android app costs money on the Play Store simply because it costs me money to publish the app there. My original motivation to develop this app was that it was something I needed for myself. Then I thought why not share it with the world? OK, there are two ways to do that: 1) submit it to the Play Store, and 2) make it available on a website for sideloading. I might consider 2) in the future, but right now 1) is my choice and that means I need to pay the publishing costs.

### [Android] Why is the Android app closed source?

Because at the moment I don't see any net benefits coming from making it open source. (omforumlera)

### [General] How do I change the service name advertised by mDNS?

### [General] How do I bind the listening port to a different IP address?

### [Linux desktop] Why is the text so fuzzy?


