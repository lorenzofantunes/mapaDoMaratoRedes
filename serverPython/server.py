import socket
import json
import database

sessao = {
    'id': '12345',
    'palavras': ['computacao', 'programadoreshtml'],
    'onde': ('-31.781023', '-52.323445'),
    'quem': 'Lorenzo F. Antunes'
}

db, client = database.connect('maroto')
session_id = database.insert(db, sessao)
print(session_id)



"""
# Creating the server
HOST = '127.0.0.1'     # Endereco IP do Servidor
PORT = 5000            # Porta que o Servidor esta
udp = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
orig = (HOST, PORT)
udp.bind(orig)
while True:
    msg, cliente = udp.recvfrom(1024)
    msg = str(msg.decode())
    msg = json.loads(msg)
    print('\nRequest por:', cliente)
    printar = msg['nome'] + ' (' + msg['onde'][0] + ', ' + msg['onde'][1] + ') ' + ': '
    print(printar, end='')
    for palavra in msg['palavras']:
        print(palavra, ', ', end='')
udp.close()
"""
