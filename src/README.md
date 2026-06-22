# SPMF to Quantitative SPMF (QSDB) Dataset Converter

A professional, high-performance Java utility designed to convert sequential pattern mining datasets from the standard **SPMF format** (containing only Item IDs) into the **Quantitative SPMF / QSDB format** (incorporating both item quantities and external utilities/profits). This tool is specifically tailored for benchmarking and performance evaluation in **High Utility Sequential Pattern Mining (HUSPM)** and **Regular High Utility Sequential Pattern Mining (RHUSPM)** research.

---

## 📌 1. Overview & Academic Context

In the field of data mining, the open-source **SPMF library** (developed by Prof. Philippe Fournier-Viger) serves as a gold-standard benchmark platform, offering a wide array of real-world sequence datasets (e.g., *BIBLE*, *SIGN*, *KOSARAK*, *E-SHOP*, etc.). However, many sequence datasets hosted on the official SPMF website are formatted strictly for traditional Sequential Pattern Mining. This means they only record item sequence progression and lack two critical dimensions required for Utility Mining:
1. **Internal Utility (Quantity):** The number of units of an item purchased within a specific transaction or event.
2. **External Utility (Profit):** The unit profit, financial weight, or relative importance of each item in the system.

This utility bridges the gap by synthetically generating realistic quantitative data and external utility tables based on mathematically sound statistical distributions, ensuring scientific integrity for algorithmic testing.

---

## ⚙️ 2. Conversion Mechanism & Statistical Distributions

To accurately simulate real-world transactional behavior and asset value distributions, the converter employs two distinct probabilistic models:

### A. Item Quantity Generation (Internal Utility)
As the tool parses each item within a transaction sequence, an internal quantity is allocated using a **Weighted Uniform Distribution**:
* **High-Frequency Case (70% probability):** Generates a quantity between `1` and `2` (simulating fast-moving consumer goods or typical retail purchases).
* **Moderate Case (20% probability):** Generates a quantity between `3` and `5`.
* **Bulk Case (10% probability):** Generates a quantity between `6` and `10` (simulating wholesale behavior or high-volume item purchases).

### B. Unit Profit Generation (External Utility)
The external utility profile for each unique Item ID is established using a **Log-Normal Distribution** (derived from a transformed Gaussian random variable with $\mu=0, \sigma=1$).
* **Real-World Philosophy:** In actual business scenarios, the vast majority of items exhibit low-to-moderate profit margins (ranging between 5 and 30 currency units), while only a small fraction of luxury or specialized items fetch exceptionally high profits.
* **Arithmetic Safety (Clamping):** The generated profit values are capped at a maximum of `1000` and floored at a minimum of `1`. This constraint completely eliminates negative utilities and prevents **Arithmetic Overflow** errors during the computation of mathematical upper bounds such as `IAUUB`, `PEAU`, or `MFUUB`.

### 🔬 C. Experimental Reproducibility
A critical requirement for peer-reviewed academic publications is that experimental results must be perfectly reproducible. To guarantee this, the tool hardcodes a fixed **`Random Seed = 42`**.
> **Impact:** No matter how many times you execute this converter on a given SPMF input file, the output quantitative sequences and external utility values will remain **100% identical**. This eliminates stochastic noise when conducting ablation studies or comparing pruning strategy execution times across different algorithms.

---

## 📥 3. Data Format Specifications

### Input Format (Standard SPMF)
Each line represents a transaction sequence. Itemsets are separated by `-1`, and the entire sequence is terminated by `-2`.
```text
item1 item2 -1 item3 -1 -2

Note: These raw text files should be downloaded directly from the official SPMF dataset repository.

Output Format (QSDB Standard)
Upon execution, the tool populates the datasets/ directory with two distinct files per dataset:

1. Quantitative Sequence File (<dataset_name>_seq.txt)
Each item ID is appended with its generated quantity enclosed in square brackets []. Structural delimiters (-1 and -2) are preserved.

Plaintext
item1[q1] item2[q2] -1 item3[q3] -1 -2
Example: 1[2] 2[1] -1 3[5] -1 -2

2. External Utility Information File (<dataset_name>_eui.txt)
A separate registry detailing the standalone profit value for every unique item present in the database, ordered chronologically by Item ID. The standard colon : delimiter is utilized.

Plaintext
# ItemID:Profit (Log-Normal Distribution seed=42)
item1:profit1
item2:profit2
Example: 1:15\n2:142\n3:8

🚀 4. Execution & Usage Guide
Prerequisites
Ensure the Java Development Kit (JDK 8 or higher) is installed on your local environment.

Structure your project root directory as follows:

Plaintext
📂 project-root/
├── 📄 SPMF_Converter.java
└── 📂 datasets/
    ├── 📄 BMS1_SPMF.txt
    ├── 📄 SIGN.txt
    └── (Other raw .txt datasets downloaded from the SPMF website)
Running the Converter
Open your terminal or command prompt at the project root directory and execute the following sequential commands:

Bash
# Step 1: Compile the Java source code
javac SPMF_Converter.java

# Step 2: Run the compiled class
java SPMF_Converter
Terminal Output Log
Plaintext
========== BẮT ĐẦU CHUYỂN ĐỔI ĐỒNG LOẠT ==========
[*] Random seed = 42 (kết quả tái tạo được)
[OK] Đã chuyển đổi: BMS1_SPMF.txt (3340 items)
[OK] Đã chuyển đổi: SIGN.txt (267 items)
...
==================================================
Once completed, navigate to the datasets/ folder to verify the creation of the <name>_seq.txt and <name>_eui.txt pairs.

📊 5. Pre-configured Datasets
The main method is pre-programmed to automatically process an array of widely used benchmark datasets available on the SPMF repository:

BMS1_SPMF.txt

E_SHOP.txt

KOSARAK.txt

ONLINE_RETAIL_II_ALL.txt

FIFA.txt

LEVIATHAN.txt

SIGN.txt

ONLINE_RETAIL_II_BEST.txt

Developer Tip: To process a new dataset downloaded from