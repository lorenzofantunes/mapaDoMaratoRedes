import socket
import threading
import sys

HOST = '192.168.1.8'
PORT = 5000
MESSAGE = sys.argv[1]
"""with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect((HOST, PORT))
    s.sendall(b'Hello, world')
    data = s.recv(1024)
print('Received', repr(data))"""

def setInterval(func, time):
    e = threading.Event()
    while not e.wait(time):
        func()

def foo():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.connect((HOST, PORT))
    server.sendall(bytes(MESSAGE.encode()))
    data = server.recv(1024)
    print(data)
    server.close()

setInterval(foo, 1)
