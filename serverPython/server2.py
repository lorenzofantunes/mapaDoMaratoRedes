import socket
import threading

class TCP ():
    def __init__(self, host, port):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.socket.bind((host, port))
        self.socket.listen(1)
        #self.socket.settimeout(30)

    def deal(self, conn, addr, data):
        print(data)
        conn.sendall(bytes('podecre'.encode()))

    def start(self):
        while True:
            try:
                conn, addr = self.socket.accept()
            except socket.timeout:
                continue

            #print(conn)
            #print(addr)

            # cria a thread
            while True:
                data = conn.recv(1024)
                if not data: break
                            
                t = threading.Thread(target=self.deal, args=(conn, addr, data))
                t.start()

HOST = '192.168.1.8'
PORT = 5000

tcpServer = TCP(HOST, PORT)
tcpServer.start()
