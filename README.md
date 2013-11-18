FrozenNas
=========

What is it?
-----------

FrozenNas is an [Amazon S3] (http://aws.amazon.com/s3/) client supporting [glacier storage class] (http://docs.aws.amazon.com/AmazonS3/latest/dev/object-archival.html) in combination with [EncFS](http://en.wikipedia.org/wiki/EncFS) for Android.
Files stored in Amazon S3 using glacier storage class have to be "restored" first. 3 to 5 hours later a download is possible.
For privacy reasons it makes sense to encrypt your files before uploading them to a cloudservice like Amazon S3. Using [EncFS](http://en.wikipedia.org/wiki/EncFS) supports content as well as filename 
encryption. FrozenNas lets you select, restore, download and decrypt files from Amazon S3, which content and filenames were encrypted using [EncFS](http://en.wikipedia.org/wiki/EncFS) or compatibles (e.g. [Boxcryptor classic] (https://www.boxcryptor.com/en/boxcryptor-classic))
 
Why to use it?
--------------

Cloud data storage is a useful service to replace your home NAS [Network Attached Storage] (http://en.wikipedia.org/wiki/Network-attached_storage).
Unfortunately bigger amounts of data can still be pricey. Amazon offers to store your files using a special glacier storage class to minimize costs.
This comes with limited availability. Files with this storage class has to be "restored", which usually takes 3-5 hours before they can be accessed.
For files which are rarely needed like your home video collection, this can be an acceptable limitation. You will definitely want to watch all those birthday/wedding/christmas videos
at some point in time again, but most of the files will not be accessed for long periods of time.

Uploading data into the cloud can be a privacy issue. Therefore it should be encrypted beforehand. [EncFS](http://en.wikipedia.org/wiki/EncFS) is a filebased
encryption method using ciphers like AES. Optionally it can encrypt filenames as well.

FrozenNas solves following problem: Data and names of your files are encrypted by EncFS and uploaded to Amazon S3. To minimize costs glacier storage class is used.
Various software and the AWS console is available to restore a specific file from the glacier. But which one is it? The filenames are encrypted! FrozenNas lets you
browse your S3 bucket presenting decrypted filenames. It provides the possibility to initiate a restore request or download an already restored file directly to your
Android device.

__WARNING__: At the time of writing upload and storage using glacier storage class is kind of cheap. Instead _restoring_ can cause significant costs! It is based on file size and the complete amount of data stored.
Try to avoid restoring data (especially big files), which does not have to. Please refer to the S3 price list for details.  

Installation
------------

You can find the most current code at [github.com/feschroe/FrozenNas] (https://github.com/feschroe/FrozenNas).
It can be compiled using the Android Development Tools Bundle. Following libraries are used:

[Amazon Android SDK] (http://aws.amazon.com/sdkforandroid/)
+ aws-android-sdk-1.7.0-core
+ aws-android-sdk-1.7.0-s3

[Apache Commons IO] (http://commons.apache.org/proper/commons-io/)
+ commons-io-2.4 

[Java EncFS Reader] (https://code.google.com/p/jefsr/)
+ jefsr-0.5

[Bouncy Castle Crypto APIs] (http://www.bouncycastle.org/)
+ sc-light-jdk15on-1.47.0.2
+ scpkix-jdk15on-1.47.0.2
+ scprov-jdk15on-1.47.0.2

[XStream] (http://xstream.codehaus.org/)
+ xstream-1.4.4

[aFileDialog] (https://code.google.com/p/afiledialog/)
+ aFileDialog-1.02-Full.zip
aFileDialog source code has to be present as "is Library" project, cause it is referenced by FrozenNas project.

To install the application on your Android device:

1. install the APK file
2. copy the ".encfs6.xml" file, which was used to encrypt your data, to your android device (the filename is irrelevant)
3. open FrozenNas application
4. click the settings icon in the upper right of the action bar
5. set all preferences
+ S3 Access Key - Your access S3 key, can be found/generated using [AWS console] (http://aws.amazon.com/console/) 
+ S3 Secret Key - Your secret S3 key, can be found/generated using [AWS console] (http://aws.amazon.com/console/)
+ S3 Bucket - S3 bucket, where your data is stored e.g. "mybucket" (without double quotes)
+ Encrypted Root Folder - Root folder, which contains encrypted files and folders e.g. "mydata.bc/" (without double quotes, no leading "/")
+ EncFS password - Your secret EncFS password
+ EncFS Volume File - Choose the xml file, which was used to encrypt your data (usually ".encfs6.xml")
+ Days Of Availability After Restore - Remember: For every day a file is restored from the glacier an additional copy is created in S3. This copy apply for the usual S3 charges.

Cryptographic Software Notice
-----------------------------

Please be aware that this is work-in-progress in a very early alpha stage. You are entering your AWS credentials
and EncFS password/keyfile at your own risk. Your passwords are currently stored in plain text. No measures
have been taken to prevent them from being stolen or deleted. 

Licensing
---------

Please see the file called LICENSE.

The Latest Version
------------------

2013.11.18 First release

Documentation
-------------

README.md is currently the only documentation available.

Additiona Credits
-----------------

FrozenNas is using the beautiful [ice-cube icon] (http://openclipart.org/detail/181701/ice-cube-by-lekamie-181701) by [lekamie] (http://openclipart.org/user-detail/lekamie)

Contacts
--------

Felix Schroeder <mail@frozennas.org>