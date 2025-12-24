from flask import Flask, jsonify, request
from jsonschema import validate, ValidationError
import random
import threading
import time
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

app = Flask(__name__)

# Инициализация Flask-Limiter
limiter = Limiter(
    get_remote_address,  # Функция для получения IP клиента
    app=app,
    default_limits=["100 per hour"]  # Лимит запросов по умолчанию (100 запросов в час)
)

# Начальная температура
current_temperature = 25
target_temperature = 25
air_conditioner_on = False  # Состояние кондиционера

# JSON Schema для валидации
target_temperature_schema = {
    "type": "object",
    "properties": {
        "target_temperature": {
            "type": "integer",
            "minimum": 16,
            "maximum": 40
        }
    },
    "required": ["target_temperature"],
    "additionalProperties": False
}

air_conditioner_schema = {
    "type": "object",
    "properties": {
        "state": {
            "type": "boolean"
        }
    },
    "required": ["state"],
    "additionalProperties": False
}

def update_temperature():
    """Фоновое обновление температуры."""
    global current_temperature
    while True:
        if air_conditioner_on:
            if current_temperature < target_temperature:
                current_temperature += 1
            elif current_temperature > target_temperature:
                current_temperature -= 1
        else:
            current_temperature += random.randint(-1, 1)

        print(f"Updated temperature: {current_temperature}")
        time.sleep(5)

@app.before_request
def limit_request_size():
    """Ограничивает размер тела запроса."""
    MAX_REQUEST_SIZE = 1024 * 1024  # 1 MB
    if request.content_length and request.content_length > MAX_REQUEST_SIZE:
        return jsonify({"error": f"Размер запроса превышает допустимый предел: {MAX_REQUEST_SIZE} байт"}), 413

@app.route('/temperature', methods=['GET'])
@limiter.limit("10 per minute")
def get_temperature():
    """Возвращает текущую температуру."""
    return jsonify({"temperature": current_temperature})

@app.route('/set_target_temperature', methods=['POST'])
@limiter.limit("5 per minute")
def set_target_temperature():
    """Устанавливает целевую температуру."""
    global target_temperature
    data = request.json

    try:
        validate(instance=data, schema=target_temperature_schema)
    except ValidationError as e:
        return jsonify({"error": f"Invalid input: {e.message}"}), 400

    target_temperature = data["target_temperature"]
    return jsonify({"status": "success", "target_temperature": target_temperature})

@app.route('/air_conditioner', methods=['POST'])
@limiter.limit("5 per minute")
def toggle_air_conditioner():
    """Включает или выключает кондиционер."""
    global air_conditioner_on
    data = request.json

    try:
        validate(instance=data, schema=air_conditioner_schema)
    except ValidationError as e:
        return jsonify({"error": f"Invalid input: {e.message}"}), 400

    air_conditioner_on = data["state"]
    status = "on" if air_conditioner_on else "off"
    return jsonify({"status": "success", "air_conditioner": status})

@app.route('/')
@limiter.exempt
def index():
    """Приветственное сообщение."""
    return 'Welcome to the Smart Home!'

if __name__ == '__main__':
    temperature_thread = threading.Thread(target=update_temperature)
    temperature_thread.daemon = True
    temperature_thread.start()

    app.run(host='0.0.0.0', port=5000)
