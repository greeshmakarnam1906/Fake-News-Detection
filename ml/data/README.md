# Dataset Download Instructions

The production application and runtime backend **do not require this CSV file**. The trained machine learning model is already bundled in `backend/src/main/resources/model.json` (and `ml/models/model.json`).

If you wish to re-run the pure Java training pipeline from scratch:

1. Download the public George McIntire Fake and Real News dataset:
   ```bash
   curl -L https://raw.githubusercontent.com/joolsa/fake_real_news_dataset/master/fake_or_real_news.csv.zip -o dataset.zip
   tar -xf dataset.zip
   rm dataset.zip
   ```
   Ensure the uncompressed file is named `fake_or_real_news.csv` in this directory (`ml/data/fake_or_real_news.csv`).

2. Execute the Java model trainer from the `backend/` directory:
   ```bash
   cd ../backend
   .\mvnw.cmd -Ptrain-model exec:java   # Windows
   ./mvnw -Ptrain-model exec:java       # Linux/macOS
   ```
