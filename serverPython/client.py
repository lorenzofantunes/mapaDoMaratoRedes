import socket
import json

HOST = '127.0.0.1'  # Endereco IP do Servidor
PORT = 5000            # Porta que o Servidor esta
udp = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
dest = (HOST, PORT)
print('Para sair use CTRL+X\n')

sessao = {
    'id': '12345',
    'palavras': ['computacao', 'programadoreshtml'],
    'onde': ('-31.781023', '-52.323445'),
    'nome': 'Lorenzo F. Antunes'
}

msg = input()
msg = json.dumps(sessao)
msg = bytes(msg.encode())

while msg != '\x18':
    udp.sendto (msg, dest)
    msg = input()
    msg = json.dumps(sessao)
    msg = bytes(msg.encode())
udp.close()
