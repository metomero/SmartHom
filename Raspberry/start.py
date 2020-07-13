import RPi.GPIO as GPIO
GPIO.setmode(GPIO.BCM)
from lib_nrf24 import NRF24
import spidev
import socket
import threading
import time

GPIO.setup(4, GPIO.OUT, initial = GPIO.HIGH )


HOST = ''
PORT = 23456

userNames = []
userPasswords = []
userPermissions = []

s_addrs = [0xc2, 0xc2, 0xc2, 0xc2, 0xc2]
#s_addrs  = list(bytearray("1Node", 'utf-8'))
#s_addrs = []
#
#for x in temp:
#    s_addrs.append(hex(x))


print(s_addrs)

radio = NRF24(GPIO, spidev.SpiDev())
radio.begin(0, 17)
time.sleep(1)
radio.setPayloadSize(32)
radio.setChannel(0x60)

radio.setDataRate(NRF24.BR_250KBPS)
radio.setPALevel(NRF24.PA_MAX)
radio.setAutoAck(False)
radio.enableDynamicPayloads()
radio.enableAckPayload()
radio.openWritingPipe(s_addrs)
radio.stopListening()

radio.printDetails()

class clientThread(threading.Thread):
    client = None
    client_name = ""

    def __init__(self, clt):
        super(clientThread, self).__init__()
        #threading.Thread.__init__(self)
        self.client = clt

    def isRealClient(self):
        while True:
            data = self.client.recv(1024)
            data = data.decode('utf-8')
            if data == "AUSH":
                self.client.send("Yep".encode('utf-8'))
                break
            else:
                return False
        return True

    def isUser(self):
        for i in range(3):
            data = self.client.recv(1024)
            password = data.decode('utf-8')

            idx = checkUserIdx(password)
            if idx == -1:
                self.client.send("denied".encode('utf-8'))
            else:
                self.client.send((userNames[idx]).encode('utf-8'))
                self.client_name = userNames[idx]
                return True
        return False
 
    def run(self):
        try:
            isSign = False
            client = self.client

            if not self.isRealClient():
                client.close()
                return

            if not self.isUser():
                client.close()
                return

	    while(True):
		order = (client.recv(1024)).decode('utf-8')

		if(order == "Quit"):
		    client.close()
		    break
		if(order == "RGB_on"):
		    radio.write(list("OpenRDog"))
		    print("rgb")
		if(order == "Lamp_on"):
		    radio.write(list("OpenLDog"))
		    print("lamp on")
		if(order == "Close_all"):
		    radio.write(list("LampODog"))
		    print("close all")

		time.sleep(0.1)

        except Exception as ex:
            #pass
            # print("Error On Listening.")
            print(ex)

        return


def signal_listener():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.bind(('', PORT))

        while True:
            s.listen(1)
            print("Waiting for connection.")

            conn, addr = s.accept()

            print(str(conn.getpeername()) + str(conn.getsockname()) + " connected.")

            time.sleep(0.2)

            pThread = clientThread(conn)
            pThread.start()

    except Exception as ex:
        print(ex)
        pThread.notify()
        conn.close()
        s.close()
	GPIO.output(4, GPIO.LOW)
        pass


def readUsersData():
    file = open("users", 'r')
    lines = file.readlines()

    for i in range(len(lines)):
        words = lines[i].split(' ')
        userNames.append(words[0])
        userPasswords.append(words[1])
        userPermissions.append(words[2])
    file.close()

def checkUserIdx(password):
    try:
        idx = userPasswords.index(password)
        return idx
    except ValueError:
        return -1

if __name__ == '__main__':
    try:
        readUsersData()
        signal_listener()
    except Exception as err: 
        print(err)

