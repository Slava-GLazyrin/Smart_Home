from flask import Flask, jsonify, request
from flask_cors import CORS
from jsonschema import validate, ValidationError
import random
import threading
import time
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
import logging

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})

# Инициализация Flask-Limiter
limiter = Limiter(
    get_remote_address,  # Функция для получения IP клиента
    app=app,
    default_limits=["100 per hour"]  # Лимит запросов по умолчанию (100 запросов в час)
)

# Настройка логгирования
logging.basicConfig(level = logging.INFO)
logger = logging.getLogger(__name__)

# Начальная температура
current_temperature = 25
target_temperature = 25
air_conditioner_on = False  # Состояние кондиционера
temperature_lock = threading.Lock() # Блокировка для температуры
ac_lock = threading.Lock() # Блокировка для состояния кондиционера

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
    global current_temperature, target_temperature, air_conditioner_on
    while True:
        with temperature_lock:
            if air_conditioner_on:
                if current_temperature < target_temperature:
                    current_temperature += 1
                elif current_temperature > target_temperature:
                    current_temperature -= 1
            else:
                current_temperature += random.randint(-1, 1)
            
            # Ограничение диапазона
            current_temperature = max(16, min(40, current_temperature))

        logger.info(f"Обновленная температура: {current_temperature}°C (Целевая температура: {target_temperature}°C Кондиционер: {'ВКЛ' if air_conditioner_on else 'ВЫКЛ'})")
        time.sleep(5)

@app.before_request
def limit_request_size():
    """Ограничивает размер тела запроса."""
    MAX_REQUEST_SIZE = 1024 * 1024  # 1 MB

    try:
        if request.content_length and request.content_length > MAX_REQUEST_SIZE:
            logger.warning(f"Request too large: {request.content_length} bytes")
            return jsonify({"error": "Request too large", "max_size": MAX_REQUEST_SIZE, "your_size": request.content_length}), 413
    except Exception as e:
        logger.error(f"Error checking request size: {e}")
        return jsonify({"error": "Internal server error"}), 500

@app.errorhandler(404)
def not_found_error(error)
    """Обработчик для 404 ошибки"""
    logger.warning(f"404 error: {request.url}")
    return jsonify({"error": "Endpoint not found", "avaliable_endpoints": ["/", "/temperature", "/set_target_temperature", "/air_conditioner"]}), 404

@app.errorhandler(500)
def internal_error(error)
    """Обработчик для 500 ошибки"""
    logger.warning(f"500 error: {error}")
    return jsonify({"error": "Internal server error", "message": "Something went wrong on the server"}), 500

@app.route('/temperature', methods=['GET'])
@limiter.limit("10 per minute")
def get_temperature():
    """Возвращает текущую температуру с обработкой."""
    try:
        with temperature_lock:
            temp = current_temperature
        
        return jsonify ({"temperature": temp, "unit": "celsius", "timestamp": time.time(), "status": "success"})
    except Exception as e:
        logger.error(f"Error getting temperature: {e}")
        return jsonify({"error": "Could not retrive temperature", "status": "error"}), 500

@app.route('/set_target_temperature', methods=['POST'])
@limiter.limit("5 per minute")
def set_target_temperature():
    """Устанавливает целевую температуру."""
    global target_temperature
    data = request.json

    try:
        validate(instance=data, schema=target_temperature_schema)
    except ValidationError as e:
        logger.error(f"Validation error: {e.message}")
        return jsonify({"error": f"Invalid input: {e.message}"}), 400

    with temperature_lock:
        target_temperature = data["target_temperture"]

    logger.info(f"Target temperature set to: {target_temperature}°C")
    return jsonify({"status": "success", "target_temperature": target_temperature, "message": f"Target temperature set to {target_temperature}°C"})

@app.route('/air_conditioner', methods=['POST'])
@limiter.limit("5 per minute")
def toggle_air_conditioner():
    """Включает или выключает кондиционер."""
    global air_conditioner_on
    data = request.json

    try:
        validate(instance=data, schema=air_conditioner_schema)
    except ValidationError as e:
        logger.error(f"Validation error: {e.message}")
        return jsonify({"error": f"Invalid input: {e.message}"}), 400
    
    with ac_lock:
        air_conditioner_on = data["state"]
    
    status = "on" if air_conditioner_on else "off"
    logger.info(f"Air conditioner turned {status}")
    return jsonify({"status": "success", "air_conditioner": status, "message": f"Air conditioner turned {status}"})

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
