# Machine Learning Technical Documentation

## 1. Dataset Provenance, Source & Licensing

The machine-learning pipeline uses the public benchmark **George McIntire Fake and Real News Dataset** (`fake_or_real_news.csv`):

- **Dataset Source:**
  - Original Author: George McIntire
  - Primary Archive: [joolsa/fake_real_news_dataset](https://github.com/joolsa/fake_real_news_dataset)
  - Kaggle Reference: [Fake and Real News Dataset by George McIntire](https://www.kaggle.com/datasets/jillanisofttech/fake-or-real-news)
  - Direct Download Link: `https://raw.githubusercontent.com/joolsa/fake_real_news_dataset/master/fake_or_real_news.csv.zip`
- **Dataset License:**
  - Released under the **MIT License / Open Academic Public Access**.
  - Permitted for academic research, evaluation, and educational distribution.
- **Dataset Characteristics:**
  - Raw CSV rows: 6,335 articles.
  - Columns: `id` (index), `title`, `text`, `label`.
  - Binary labels: `FAKE` (1) and `REAL` (0).
  - Articles span journalistic themes (elections, domestic governance, science, healthcare, and geopolitical affairs).

---

## 2. Runtime Model Artifact Provenance — Which `model.json` is Loaded?

> [!IMPORTANT]
> **Runtime Model Location:**
> At application startup, the Spring Boot application strictly loads **`backend/src/main/resources/model.json`** via Spring's `classpath:model.json` resource loader.
>
> The duplicate copy located at `ml/models/model.json` is strictly an export destination written by `ModelTrainer.java` when offline training completes. The Spring Boot backend **never** reads from `ml/models/model.json` at runtime. Both files are kept in sync whenever `mvn -Ptrain-model exec:java` is executed.

---

## 3. Text Preprocessing & Label Leakage Removal

In many news datasets, genuine articles originate from wire services and frequently contain attribution headers such as `WASHINGTON (Reuters) -` or `(AP)`. If left untreated, naive classifiers achieve artificially high accuracy simply by learning the presence of wire tags rather than analyzing linguistic syntax or prose.

### Preprocessing Steps in `TextPreprocessor.java`:
1. **Wire Leakage Stripping:** Evaluates regex patterns to strip publisher tags from the beginning and body of articles:
   - `(?i)^[\w\s,/-]+?\s*\(reuters\)\s*[-–—:]?\s*`
   - `(?i)^[\w\s,/-]+?\s*\(ap\)\s*[-–—:]?\s*`
   - `(?i)^reuters\s*[-–—:]?\s*`
   - `(?i)\breuters\b`, `\bap photo\b`, `\bgetty images\b`
2. **Unicode Normalization:** Applies NFD normalization and strips combining diacritical marks (`\p{InCombiningDiacriticalMarks}+`) to prevent encoding artifacts.
3. **Punctuation & Character Filtering:** Normalizes text to lowercase, removes URLs/mentions (`https?://\S+`), and eliminates non-alphanumeric symbols.
4. **Stop-Word Removal:** Excludes standard uninformative English functional words while preserving sentiment and context tokens.
5. **Deduplication:** Computes prefix hash fingerprints across normalized texts, filtering out near-duplicate articles.
   - Result: **6,297 distinct, valid articles** retained.

---

## 4. Feature Extraction (TF-IDF)

Feature extraction operates in pure Java without third-party ML runtime bloat:

1. **Vocabulary Filtering:**
   - Evaluates document frequency ($df$) across the training set ($N_{train} = 5,037$).
   - Excludes terms with $df < 4$ (filtering rare typos) and terms with $df > 0.70 \times N_{train}$ (filtering overly ubiquitous words).
   - Ranks candidate terms by document frequency and selects the top **6,000 informative vocabulary features**.
2. **Smoothed Inverse Document Frequency (IDF):**
   $$\text{IDF}(w) = \ln\left(\frac{1 + N_{train}}{1 + df(w)}\right) + 1.0$$
3. **Sublinear Term Frequency Scaling:**
   $$\text{TF}(w, d) = 1 + \ln(\text{count}(w, d))$$
4. **L2 Vector Normalization:**
   $$\hat{x} = \frac{x}{\|x\|_2}$$
   Ensures that long articles do not disproportionately dominate short articles.

---

## 5. Classifier Architecture & Optimization

The classifier is an **L2-Regularized Logistic Regression** implemented from first principles in Java:

- **Mathematical Formulation:**
  $$z = \sum_{j=1}^{V} w_j x_j + b$$
  $$P(\text{Fake} \mid x) = \sigma(z) = \frac{1}{1 + e^{-z}}$$
- **Optimization Algorithm:** Stochastic Gradient Descent (SGD) with learning rate decay:
  $$\eta_t = \frac{\eta_0}{1 + 0.05 \times \text{epoch}}$$
  - Initial learning rate $\eta_0 = 0.5$
  - Epochs: 35
  - Regularization parameter $\lambda = 10^{-4}$ (Ridge L2 penalty $\frac{\lambda}{2} \|w\|^2$)
  - Fixed random seed: 42

---

## 6. Actual Evaluation Metrics (Measured from Executed Run)

Evaluation was performed on the held-out 20% test split ($N_{test} = 1,260$ articles) using a stratified split. The results recorded in `ml/reports/evaluation.json` are:

```json
{
  "accuracy": 0.9333333333333333,
  "precision": 0.9198767334360555,
  "recall": 0.9491255961844197,
  "f1": 0.9342723004694835,
  "totalSamples": 6297,
  "trainSamples": 5037,
  "testSamples": 1260,
  "confusionMatrix": {
    "truePositives": 597,
    "falsePositives": 52,
    "trueNegatives": 579,
    "falseNegatives": 32
  }
}
```

### Metric Summary:
- **Accuracy:** **93.33%**
- **Precision (Fake):** **91.99%**
- **Recall (Fake):** **94.91%**
- **F1-Score:** **0.9343**

---

## 7. Decision Rules & Uncertain Zone

To prevent overconfidence, the model does not output a binary hard prediction. Instead, scores are categorized into three operational zones:

| Model Score ($p$) | Categorical Verdict | Recommended Action |
| :--- | :--- | :--- |
| **$p \le 0.40$** | `Likely Genuine` | Resembles stylistic structures of verified journalism. Standard corroboration suggested. |
| **$0.40 < p < 0.60$** | `Uncertain / Needs Verification` | Mixed or weak lexical signals. Manual fact-checking strictly required. |
| **$p \ge 0.60$** | `Likely Fake` | Contains high frequency of rhetorical and lexical markers typical of fabricated stories. |

---

## 8. Model Explainability Mechanism

The system computes token-level contributions for every word present in the user's submitted text:
$$c_i = w_i \times x_i$$
- **Positive Contribution ($c_i > 0$):** Terms whose presence increased the model score toward `Likely Fake` (e.g., hyperbole, unverified sensationalism).
- **Negative Contribution ($c_i < 0$):** Terms whose presence lowered the score toward `Likely Genuine` (e.g., technical reporting vocabulary, formal citations).

The top 5 positive and top 5 negative terms are returned in the API and displayed directly to the user in the UI.

---

## 9. Known Limitations & Bias

1. **Stylistic vs. Factual Distinction:** The model evaluates style, prose structure, and lexical markers. It cannot fact-check real-world figures or dates. A factually false statement written in dry, academic language may yield a low score.
2. **Satire & Parody:** Parody articles frequently mimic sensationalism and may be flagged as `Likely Fake` despite having comedic rather than malicious intent.
3. **Distribution Shift:** Emerging terminology, slang, breaking news, and foreign event coverage not represented in the training data may produce uncertain or suboptimal classifications.
