import threading
import socket
import json
import sys

#HOST = '127.0.0.1'  # Endereco IP do Servidor
HOST = '20.1.1.113'  # Endereco IP do Servidor
PORT = 5000            # Porta que o Servidor esta
udp = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
dest = (HOST, PORT)

"""'onde': {
'coordinates': ('-31.781023', '-52.323445')
},"""

sessao = {
    'palavras': ['computacao', 'programadoreshtml'],
    'onde': 'Pair{-31.781023, -52.323445}',
    'nome': sys.argv[1]
}

#msg = bytes(sys.argv[1].encode())
msg = json.dumps(sessao)
msg = bytes(msg.encode())

def setInterval(func, time):
    e = threading.Event()
    while not e.wait(time):
        func()

def foo():
    print("sent", sys.argv[1])
    udp.sendto (msg, dest)
    test, cliente = udp.recvfrom(1024)
    print(test)

setInterval(foo, 1)




#udp.close()
