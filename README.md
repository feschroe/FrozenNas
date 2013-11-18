FrozenNas
=========

What is it?
-----------

FrozenNas is a Amazon S3 client supporting Glacier storage class in combination with [EncFS](http://en.wikipedia.org/wiki/EncFS) for Android.
Files stored in Amazon S3 using Glacier storage class have to be "restored" first. 3 to 5 hours later a download is possible.
For privacy reasons it makes sense to encrypt your files before uploading them to the cloudservice like Amazon S3. Using EncFS filenames can also
be encrypted. FrozenNas lets you select, restore, download and decrypt your files from Amazon S3. 
 
Why to use it?
--------------



WARNING: At the time of writing upload and storage using Glacier storage class is kind of cheap. Instead __restoring__ can cause significant costs! It is based on file size and the complete amount of data stored.
Try to avoid restoring data (especially big files), which does not have to. Please refer to the S3 price list for details.  

Installation
------------

You can find the most current code at [github.com/feschroe/FrozenNas] (https://github.com/feschroe/FrozenNas).
It can be compiled using the Android Development Tools Bundle. Following libraries are used:

[Amazon Android SDK] (http://aws.amazon.com/sdkforandroid/)
+ aws-android-sdk-1.6.1-core
+ aws-android-sdk-1.6.1-s3

[Apache Commons IO]
+ commons-io-2.4 (http://commons.apache.org/proper/commons-io/)

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

To install the application on your Android devic

1. install the APK file
2. copy the ".encfs6.xml" file which was used to encrypt your data to your android device (the filename is irrelevant)
3. open FrozenNas application
4. click the settings icon in the upper right of the action bar
5. set all preferences
+ S3 Access Key - can be found using [AWS console] (http://aws.amazon.com/console/) 
+ S3 Secret Key - can be found using [AWS console] (http://aws.amazon.com/console/)
+ S3 Bucket - S3 bucket, where your data is stored
+ Encrypted Root Folder - Root folder, which contains encrypted files and folders
+ EncFS password - Your secret EncFS password
+ EncFS Volume File - Choose the xml file, which was used to encrypt your data (usually ".encfs6.xml")
+ Days Of Availability After Restore - Remember: For every day a file is restored from the Glacier an additional copy is created in S3. This copy apply for the usual S3 charges.

Cryptographic Software Notice
-----------------------------

Please be aware that this is work-in-progress in an very early alpha stage. You are entering your AWS credentials
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