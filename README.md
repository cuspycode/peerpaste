PeerPaste
=========

PeerPaste lets you copy and paste text between two devices that are present on the same local network (LAN). The data transfer is encrypted, so you can transfer confidential things like passwords via PeerPaste without having to worry about eavesdroppers. The encryption key is transferred by scanning a QR code with the mobile's camera, so there is no public-key cloud infrastructure involved, or any cloud services at all actually. It's all peer-to-peer on the local network.

Features:

* Automatic service discovery &mdash; once a device starts the app, other devices on the same local network will discover it automatically, so no configuration is needed.
* Encrypted data &mdash; the pasted content is encrypted in transit.
* Peer-to-peer key sharing &mdash; the encryption keys are shared out-of-band via scanning of QR codes. There is no dependency on any central authority that has to be trusted.
* Completely cloudless &mdash; The app does not involve any external servers. It's all peer-to-peer on the local network.

## How to install Android version

You can find it on the Google Play Store. It is recommended that you try the free demo version first:  
https://play.google.com/store/apps/details?id=com.cuspycode.android.demo.peerpaste

The unrestricted paid version is available here:  
https://play.google.com/store/apps/details?id=com.cuspycode.android.peerpaste

Version 2.0 should work on Android 7.0 and later, although I have so far only tested it on Android 13.

## How to install iOS version

The app is not yet available for iOS.

## How to install generic desktop/laptop version

First you need to have Java SE 8 installed on your machine. Later Java versions will likely work too, although so far I have only tested it on Java 11. Then download `peerpaste.jar` and the shell script `peerpaste`. The shell script is a trivial wrapper that will work on any machine that supports a POSIX shell.

Run the command `./peerpaste` to start the server.

Add "`--help`" to get a list of command-line options. See the FAQ section below for some useful options.

Stop the peerpaste server by typing ^C, or terminate it with the `kill` command if you are running it in the background.

## How to install MacOS version

This is just the generic version packaged as a MacOS app for convenience. To install, just download `PeerPaste.zip` and unzip it. The result is a MacOS app that you can move to the Applications folder, or put in the dock, or wherever you want. Java SE is a prerequisite, just like for the generic version.

## FAQ

Frequently Asked Questions, preloaded with a few anticipated questions.

* [[General] The app is running, but why don't my other devices see it?](#faq0100)
* [[General] It keeps complaining that the crypto key isn't working!](#faq0200)
* [[General] Why should I trust your app to handle my secret data?](#faq0300)
* [[Android] Why do I get a Cryptographic failure error?](#faq0400)
* [[Android] Why does the mobile app cost money?](#faq0500)
* [[Android] How can I try out the mobile app before I decide whether or not to pay for it?](#faq0600)
* [[Android] Why is the mobile app closed source?](#faq0700)
* [[iOS] Is there a version available for iOS?](#faq0800)
* [[General] How do I change the service name advertised by mDNS?](#faq0900)
* [[General] How do I bind the listening port to a different IP address?](#faq1000)
* [[Linux desktop] The clipboard is cleared when the application exits!](#faq1100)
* [[Linux desktop] Why is the text so fuzzy?](#faq1200)
* [[Linux headless] What if I'm not running a graphic console?](#faq1300)
* [[Desktop] What if I want to copy/paste between two desktops or laptops?](#faq1400)

<a id="faq0100"></a>
#### [General] The app is running, but why don't my other devices see it?

This is most likely caused by network connectivity issues:

* Make sure your devices or your router don't block mDNS or mDNS-advertised traffic.
* If the peer devices are connected to different LANs, they will be on different broadcast domains, and mDNS will not work by default. This can be solved by setting up proper IP multicast that works across both LANs, or by ensuring that both devices are connected to the same LAN.

Another possibility is that you have an exceptional network configuration that causes the desktop server to pick the wrong IP address to advertise on mDNS, which makes it unavailable to other devices even though they are connected to the same LAN. This can be remedied by following the instructions in the FAQ answer [*How do I bind the listening port to a different IP address?*](#faq1000)

<a id="faq0200"></a>
#### [General] It keeps complaining that the crypto key isn't working!

Normally you just click on "Settings" and delete the key on the mobile device, and then try again. But sometimes you need to delete the shared key on both devices. The easiest way to do that on the desktop application is to delete the file `.local/share/peerpaste/peerpaste-data.json` in your home directory, or if you are comfortable editing JSON files you can delete just the appropriate entry in this file.

<a id="faq0300"></a>
#### [General] Why should I trust your app to handle my secret data?

One major use case for PeerPaste is to paste passwords or password-reset links or signup confirmation links etc, from one device to another. This of course requires trusting that the app is secure. The encryption and decryption code used in the desktop edition is published here on GitHub. It uses industry standard practices, and it's open for anyone to audit. The exact same code is used in the Android version, with the exception of a couple of trivial differences in how the `Base64` library is invoked due to trivial differences in the Java library versions.

<a id="faq0400"></a>
#### [Android] Why do I get a Cryptographic failure error?

Either your Android version is too old to support the required crypto algorithm (AES-128/GCM), or you live in a country with an evil government that restricts the use of strong cryptography. In either case this means that you cannot run PeerPaste on your Android device, and unfortunately there is nothing I can do about it.

<a id="faq0500"></a>
#### [Android] Why does the mobile app cost money?

I would like to recover some of my costs for developing and publishing the app. Also, this is an app that I would happily pay for myself if someone else had authored it, so why shouldn't I expect the same from you?

<a id="faq0600"></a>
#### [Android] How can I try out the mobile app before I decide whether or not to pay for it?

You can install the free demo app `PeerPaste Demo`. If the demo version works on your device, then the real version will also work.

<a id="faq0700"></a>
#### [Android] Why is the mobile app closed source?

Because I currently have no incentive to do the work involved in publishing the source code for the mobile app. This might change in the future however.

<a id="faq0800"></a>
#### [iOS] Is there a version available for iOS?

Not yet, but maybe it could happen if there is enough demand to motivate the effort.

<a id="faq0900"></a>
#### [General] How do I change the service name advertised by mDNS?

The desktop application uses the hostname by default. But the service name can be changed by providing a command-line argument, e.g. `--name "Steve's Laptop"`.

On Android the service name depends on whether the "Nearby devices" permission has been enabled or not. If enabled, the Bluetooth device name will be used. Otherwise the service name will be a combination of manufacturer and model name, e.g. "Sony XQ-BT52". Note: the "Nearby devices" permission is only used to retrieve the Bluetooth device name. Earlier Android API versions did not require any special permission for this, but for some reason the Android maintainers chose to restrict the feature.

<a id="faq1000"></a>
#### [General] How do I bind the listening port to a different IP address?

This is only possible in the desktop application. You can either provide the IP address explicitly, via e.g. `--address 192.0.2.3`, or you can specify the network interface, e.g. `--interface eth1`.

You can also specify the port number via e.g. `--port 12345`. The default is a random port number picked by the operating system.

<a id="faq1100"></a>
#### [Linux desktop] The clipboard is cleared when the application exits!

This is a known bug in the Xorg/X11 clipboard design. The X clipboard is only a reference to the original data, so when the originating application exits, the reference doesn't refer to anything anymore. The solution is to either use the clipboard data before exiting peerpaste desktop, or by running a clipboard manager (e.g. `xsel` or some other alternative).

<a id="faq1200"></a>
#### [Linux desktop] Why is the text so fuzzy?

By default, the desktop application uses full pixel antialiasing on Linux, which looks fuzzy when compared to the more commonly used subpixel antialiasing. The reason is that Java can't determine the subpixel arrangement when running on Linux. But you can enable subpixel antialiasing explicitly by providing the command-line argument `--aa <VALUE>` where `<VALUE>` is one of:

* `lcd_hrgb`
* `lcd_hbgr`
* `lcd_vrgb`
* `lcd_vbgr`
* `lcd` &mdash; same as `lcd_hrbg`
* `default` &mdash; revert to Java's default, which may or may not be no antialiasing at all.

Try them one at a time until you find one that looks good on your desktop screen.

<a id="faq1300"></a>
#### [Linux headless] What if I'm not running a graphic console?

The desktop application still works when running in headless mode, but you'll need to add the `--show-data` option to show received data, or the `--paste <TEXT>` option when sending data.

<a id="faq1400"></a>
#### [Desktop] What if I want to copy/paste between two desktops or laptops?

This is possible to accomplish via the current generic desktop/laptop application, but only if you first add an encryption key via manual editing of `~/.local/share/peerpaste/peerpaste-data.json` on both devices. This may possibly be improved in future versions. However, there is a much simpler solution for this case, if you have SSH access from one machine to the other:
* `ssh $HOSTNAME pbpaste` &mdash; prints the remote MacOS clipboard over an encrypted SSH connection.
* `ssh $HOSTNAME DISPLAY=:0 xsel` &mdash; prints the remote X11/UNIX clipboard over an encrypted SSH connection.

I don't use Windows, but I'm sure a similar solution exists there, you'll just have to find it on your own.
