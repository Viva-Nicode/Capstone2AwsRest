try:
    import numpy as np
    import tensorflow as tf
    from PIL import Image
    from sys import argv

    classification_model = tf.keras.models.load_model("/home/ubuntu/Capstone2AwsRest/src/main/resources/garbage_classifier")
    categoris = np.array(["battery", "cardboard", "clothes", "glass", "heating pad", "metal", "plastic", "shoes", "toothbrush"])

    def load_img(path):
        img = tf.io.read_file(path)
        img = tf.image.decode_jpeg(img, channels=3)
        return img

    def runDetector(path):
        img = load_img(path)
        imgn = img.numpy()
        image_pil = Image.fromarray(np.uint8(imgn)).convert("RGB")
        cropped_img = image_pil.resize((224, 224))
        cropped_img = np.array(cropped_img) / 255.0
        cropped_img = np.expand_dims(cropped_img, axis=0)
        pred = classification_model.predict(cropped_img)
        idx = np.array(pred).argmax()
        result = categoris[idx]
        score = pred[0][idx]
        return f"\"name\" : \"{result}\", \"score\" : {score}"

    print("{ " + runDetector(argv[1]) + " }")
except Exception as e:
    print(e)
