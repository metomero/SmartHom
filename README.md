# SmartHom

Why we have to get out of the our warm bed for turn off the light at the middle of the night? Do you often ask yourself this question? Then my SmartHom system perfectly suits for you. I completely inspired from laziness and I have created my own basic smart home system for this purpose. I hope you will use my project and improve it for yourself. At the same time I will improve my project and share it of course but I prefer that you will improve it, you can do it.

So, let begin.

# Prerequirements

First of all, we need some electronic parts to build it.

* Raspberry Pi 3 or Zero W
* Arduino (Uno, Nano etc...)
* NRF24LF Transceiver Module (Two or more)
* Some leds, resistors, 10 uf capacitors etc...
* RGB LED Strip
* Relay

### Raspberry Pi

First of all, linux operating system must be installed on the raspberry pi. I am using ubuntu 18.04.

We have to get some library for raspberry pi.

* GPIO
* NRF24
* spidev

##### Setup spi

First, lets make our updates.

```
sudo apt-get update
```

We need spi communucation for nrf24 module. Enable SPI on Raspberry pi.

```
$ sudo raspi-config
```

Select Interfacing Options -> SPI<br>

Then, reboot.

##### Setup spidev

Download spidev library from github.

```
$ wget https://github.com/Gadgetoid/py-spidev/archive/master.zip
```

Extract it.

```
$ unzip master.zip
```

Run the [setup.py](http://setup.py) file in the folder we extracted from the file

```
$ cd py-spidev-master
$ python setup.py
```

##### Setup lib_nrf24

For using nrf24 module on raspberry pi, we use BLavery library. Clone it or just downloas as a zip file from,

[https://github.com/BLavery/lib_nrf24](https://github.com/BLavery/lib_nrf24)

Here, we have to add a line into lib_nrf24.py file.<br>

In "def begin(self, csn_pin ...function," after line 373 "self.spidev.open(0, csn_pin)" add:

```
self.spidev.max_speed_hz = 4000000
```

and save it.<br>

Then copy this file to python library location (python 2 or 3 location)

```
$ cp lib_nrf24.py /usr/lib/python2.7
```

# Circuit Diagram

### Nrf24 Pins

![NRF24 Pins](https://hayaletveyap.com/wp-content/uploads/2020/03/nRF24L01-Wireless-Modul-Pinout.png)

### Master

![SmartHom Master](https://i.hizliresim.com/ThnZOU.png)

### Slave

![SmartHom Slave](https://i.hizliresim.com/cPwqHI.png)

I will add more info in detail soon, but until then you can look at the connection scheme of the nrf24 module for raspberry pi and arduino. You have to pay attention to CE and CSN pins and their correct transfer to code.

# How does system works?
![SmartHom Slave](https://i.hizliresim.com/Qa8XyZ.png)
System is very simple. We had one master(Pi) one slave(arduino) and mobile app to communicate via master.

Raspberry pi is a server that listens messages from mobile app and according to incoming message send a another message to slave.

Raspberry pi server is a multi-threat server. It accepts multiple users at the same time.

I used tcp ip communication between the mobile application and the server. Messaging between the whole system is in simple text format, text communication to be simple. I think it was very simple.

When the raspberry pi code is running ([start.py](http://start.py)), after making the necessary settings for the nrf24 module, it switches to listening for the mobile application.

When the user opens the application, it connects to the system already on the local network

In my scenario, there are multiple users defined on the system. Each of them can enter the system by entering their own passwords. I wanted it to be like a different user account for everyone in a home.

For this purpose, I created a file called "users". Each line in this file contains a user definition. There are three words in each line. The first word is the user name, the second word is the user password, and the third is the user permission.

```
//users
//username | password | permission
test1 1234 admin
```

You can login to the system with your own password by typing your user name and password and authorization in the file.

I haven't had a code that i use the user permission for now, but I'm thinking of using it in the future.

There are only three features in the system yet;

* Open Lamp
* Open RGB
* Close all

But in the future I will add many more features we can control to the smart home system.

The slave does one of these three jobs according to the message it receives.

The led lights we connect in rgb light mode slowly switch between colors. I will add speed control to this in the future.

In lamp mode, our 220v directly connected lamp works with relay.

# Start System

First setup the circuit of all systems. Be sure to make the circuit connections correctly.

On raspberry pi, just start [start.py](http://start.py).

```
python start.py
```

For arduino, upload the codes and run them.

Install apk file for mobile application. You can find the entire Android project from the files.

## License

MIT
