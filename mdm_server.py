from flask import Flask, request, jsonify

app = Flask(__name__)

devices = {}  # Almacena dispositivos
commands = {}  # Almacena comandos pendientes

@app.route('/register', methods=['POST'])
def register():
    data = request.json
    device_id = data.get("device_id")
    if not device_id:
        return jsonify({"error": "Faltan datos"}), 400
    devices[device_id] = {"status": "registered"}
    commands[device_id] = []
    return jsonify({"message": "Dispositivo registrado"}), 200

@app.route('/command', methods=['POST'])
def send_command():
    data = request.json
    device_id = data.get("device_id")
    command = data.get("command")
    if device_id not in devices:
        return jsonify({"error": "Dispositivo no registrado"}), 404
    commands[device_id].append(command)
    return jsonify({"message": f"Comando '{command}' enviado a {device_id}"}), 200

@app.route('/commands/<device_id>', methods=['GET'])
def get_commands(device_id):
    if device_id not in devices:
        return jsonify({"error": "Dispositivo no encontrado"}), 404
    device_commands = commands.get(device_id, [])
    commands[device_id] = []  # Limpia comandos después de enviarlos
    return jsonify({"commands": device_commands})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)




















