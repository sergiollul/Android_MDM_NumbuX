from flask import Flask, request, jsonify, render_template_string

app = Flask(__name__)

current_command = "none"

@app.route("/")
def home():
    return render_template_string("""
        <h1>📱 MDM Remote Control</h1>
        <form action="/set_command" method="post">
            <button name="cmd" value="lock">🔒 Lock Phone</button>
            <button name="cmd" value="unlock">🔓 Unlock Phone</button>
        </form>
        <p>Current command: <b>{{cmd}}</b></p>
    """, cmd=current_command)

@app.route("/set_command", methods=["POST"])
def set_command():
    global current_command
    current_command = request.form.get("cmd")
    return f"Command set to {current_command} <br><a href='/'>Back</a>"

@app.route("/command")
def get_command():
    return jsonify({"command": current_command})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
