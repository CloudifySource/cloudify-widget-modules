Description
===========


A REST service that exposes pool management

Admin user can create new accounts.

Accounts can define pools.

Installation
============

The build is available at [http://get.gsdev.info/cloudify-widget-pool-manager-website/]

we are overriding existing builds, so when you point to a build, you might get

a different build each time.


## Installation Steps

 * Create an executable file [/etc/sysconfig/widget-pool-manager](./build/SYSCONF_EXAMPLE)
 * Create a [website-context.xml file](./build/website-context.example.xml) and configure it in your sysconfig file
 * Run the following line of code

````

yum -y install dos2unix && wget --no-cache --no-check-certificate -O - http://get.gsdev.info/cloudify-widget-pool-manager-website/1.0.0/install.sh | dos2unix | bash

````

if installation went successfully you will have the following commands available on your command line

```` 

service widget-pool status
service widget-pool start
service widget-pool stop
service widget-pool upgrade

````


API
====

TBD
