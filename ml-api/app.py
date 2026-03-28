from flask import Flask, request, jsonify
from flask_cors import CORS
from PIL import Image
import random
import io
import time

app = Flask(__name__)
CORS(app)  # Enable CORS for all domains, appropriate for development

# Mock ML Model implementation
# In a real scenario, you'd load a model (e.g. TensorFlow/PyTorch) here

DISEASE_CLASSES = {
    "Apple": ["Apple Scab", "Black Rot", "Cedar Apple Rust", "Healthy"],
    "Corn": ["Cercospora Leaf Spot", "Common Rust", "Northern Leaf Blight", "Healthy"],
    "Tomato": ["Bacterial Spot", "Early Blight", "Late Blight", "Healthy"],
    "General": ["Powdery Mildew", "Downy Mildew", "Blight", "Healthy"]
}

TREATMENTS = {
    "Apple Scab": "Apply fungicides containing captan, myclobutanil or try a sulfur spray.",
    "Black Rot": "Prune out dead or diseased wood. Remove all mummified fruit.",
    "Bacterial Spot": "Apply copper-based fungicides early in the season.",
    "Early Blight": "Remove affected leaves and apply fungicide. Avoid overhead watering.",
    "Healthy": "No treatment needed. Keep up the good work!",
    "Default": "Consult a local agricultural extension for specific chemical controls. Ensure good air circulation and avoid overhead watering."
}

def mock_predict(image_bytes, crop_type):
    """
    Simulates a machine learning inference step.
    Sleeps for 1-2 seconds and returns a random viable prediction.
    """
    time.sleep(random.uniform(1.0, 2.5))  # simulate processing time
    
    # Select disease pool based on crop type
    pool = DISEASE_CLASSES.get(crop_type, DISEASE_CLASSES["General"])
    
    # 70% chance of being diseased if we pretend to be a realistic testing scenario
    if random.random() > 0.3:
        diseases = [d for d in pool if d != "Healthy"]
        disease = random.choice(diseases) if diseases else "Unknown Disease"
        confidence = random.uniform(0.75, 0.99)
    else:
        disease = "Healthy"
        confidence = random.uniform(0.90, 0.99)
        
    treatment = TREATMENTS.get(disease, TREATMENTS["Default"])
        
    return {
        "diseaseName": disease,
        "confidenceScore": round(confidence, 4),
        "suggestedTreatment": treatment
    }

@app.route('/predict', methods=['POST'])
def predict():
    if 'file' not in request.files:
        return jsonify({"error": "No file part in the request"}), 400
        
    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400
        
    crop_type = request.form.get('cropType', 'General')
    
    try:
        # Read the image
        img_bytes = file.read()
        # You'd normally preprocess the image here e.g.:
        # img = Image.open(io.BytesIO(img_bytes)).convert("RGB")
        # img = img.resize((224, 224))
        
        prediction_result = mock_predict(img_bytes, crop_type)
        return jsonify(prediction_result), 200
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/ping', methods=['GET'])
def ping():
    return jsonify({"status": "ML API is running!"}), 200

if __name__ == '__main__':
    # Run on port 5000 
    app.run(host='0.0.0.0', port=5000, debug=True)
