import socket
import json
import database
from bson import Binary, Code
from bson.json_util import dumps, CANONICAL_JSON_OPTIONS

"""sessao = {
    'palavras': ['macaco'],
    'onde': ('-31.781023', '-52.323445'),
    'nome': 'lorenzofantunes'
}

db, client = database.connect('maroto')
session_id = database.insert(db, sessao)
print(session_id)

for x in (database.pesquisarPorTempoEspacoPalavra(db, 100, -31.781023, -52.323445, ['computacao', 'macaco'])):
    print(x)
"""

#####################################################################

db, client = database.connect('maroto')

# Creating the server
#HOST = '127.0.0.1'     # Endereco IP do Servidor
#HOST = '189.27.148.91'     # Endereco IP do Servidor
HOST = ''     # Endereco IP do Servidor
PORT = 5000            # Porta que o Servidor esta
udp = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
orig = (HOST, PORT)
udp.bind(orig)
while True:
    msg, cliente = udp.recvfrom(1024)
    print(msg)
    msg = str(msg.decode())
    msg = json.loads(msg)
    pair = msg['onde'].replace('Pair{', '').replace('}', '').split(', ')
    msg['onde'] = ((pair[0]), (pair[1]))

    if isinstance(msg['palavras'], str):
        msg['palavras'] = list(set(msg['palavras'].split()))

    session_id = database.insert(db, msg)

    retorno = database.pesquisarPorTempoEspacoPalavra(db, 5, msg['onde']['coordinates'][0], msg['onde']['coordinates'][1], msg['palavras'], msg['nome'])
    pessoas = []
    for x in retorno:
        palavras = []
        for palavra in x['palavras']:
            if (palavra in msg['palavras']):
                palavras.append(palavra)
        x['palavras'] = palavras
        x['lat'] = x['onde']['coordinates'][0]
        x['long'] = x['onde']['coordinates'][1]
        x.pop('onde', None)
        pessoas.append(x)

    print('achou:', len(pessoas))
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM) # UDP
    pessoas = dumps(pessoas, json_options=CANONICAL_JSON_OPTIONS)
    sock.sendto(bytes(pessoas, "utf-8"), (cliente[0], cliente[1]))
udp.close()
