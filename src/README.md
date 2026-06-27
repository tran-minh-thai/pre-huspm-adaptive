# SPMF to Quantitative SPMF (QSDB) Dataset Converter

A small Java utility that converts sequential pattern mining datasets from
the standard **SPMF format** (item IDs only) into the **Quantitative SPMF
(QSDB) format** used by High-Utility Sequential Pattern Mining (HUSPM)
algorithms. For every input file, the tool produces (i) a quantitative
sequence file and (ii) an external utility table.

## 1. Background

The SPMF library (Fournier-Viger et al.) distributes a wide range of
real-world sequence datasets (e.g., *BIBLE*, *SIGN*, *KOSARAK*, *E-SHOP*).
Most of these files are released in the format used by traditional
sequential pattern mining and only encode item sequences. Utility-based
mining additionally requires:

1. **Internal utility (quantity):** the number of units of an item that
   occurs in a transaction or event.
2. **External utility (profit):** the unit profit of each item.

This converter augments raw SPMF files with synthetic quantities and
profits drawn from fixed statistical distributions, so the resulting
quantitative datasets can be used directly by HUSPM benchmarks.

## 2. Synthetic distributions

### 2.1 Item quantity (internal utility)

For each item occurrence the converter draws a quantity from a piecewise
uniform distribution:

| Range | Probability | Interpretation                  |
|-------|-------------|---------------------------------|
| 1–2   | 70%         | typical retail purchase         |
| 3–5   | 20%         | moderate quantity               |
| 6–10  | 10%         | bulk / wholesale-like purchase  |

### 2.2 Unit profit (external utility)

For each distinct item ID the converter samples a profit from a
log-normal distribution `exp(N(2.5, 1))` and clamps the value to
`[1, 1000]`. Clamping prevents non-positive utilities and avoids
arithmetic overflow when upper bounds such as `IAUUB`, `PEAU`, or
`MFUUB` are computed.

### 2.3 Reproducibility

The pseudo-random generator is seeded with a fixed value (`seed = 42`),
so running the converter on the same SPMF input always yields
byte-identical QSDB files. This makes ablation studies and cross-method
runtime comparisons reproducible.

## 3. File formats

### 3.1 Input (SPMF)

Each line is one sequence. Itemsets are separated by `-1` and the
sequence is terminated by `-2`:

```
item1 item2 -1 item3 -1 -2
```

Raw SPMF files can be downloaded from the official SPMF dataset page.

### 3.2 Output

For each input file `<name>.txt` the converter writes two files into
`datasets/`:

**`<name>_seq.txt`** — quantitative sequence file. Every item ID is
followed by its quantity in square brackets; the `-1` / `-2` markers are
preserved.

```
item1[q1] item2[q2] -1 item3[q3] -1 -2
```

Example: `1[2] 2[1] -1 3[5] -1 -2`

**`<name>_eui.txt`** — external utility table. One `itemID:profit` pair
per line, sorted by item ID:

```
# ItemID:Profit (Log-Normal Distribution seed=42)
1:15
2:142
3:8
```

## 4. Usage

### 4.1 Requirements

- JDK 8 or higher.

### 4.2 Expected directory layout

```
project-root/
├── SPMF_Converter.java
└── datasets/
    ├── BMS1_SPMF.txt
    ├── SIGN.txt
    └── ... (other raw SPMF files)
```

### 4.3 Compile and run

From the project root:

```bash
javac SPMF_Converter.java
java SPMF_Converter
```

### 4.4 Sample console output

```
========== BATCH CONVERSION STARTED ==========
[*] Random seed = 42 (reproducible output)
[OK] Converted: BMS1_SPMF.txt (3340 items)
[OK] Converted: SIGN.txt (267 items)
...
==============================================
```

After the run, `datasets/` will contain the `<name>_seq.txt` /
`<name>_eui.txt` pair for each processed input.

## 5. Default input list

The `main` method processes the following SPMF benchmark files by
default:

- `BMS1_SPMF.txt`
- `E_SHOP.txt`
- `KOSARAK.txt`
- `ONLINE_RETAIL_II_ALL.txt`
- `FIFA.txt`
- `LEVIATHAN.txt`
- `SIGN.txt`
- `ONLINE_RETAIL_II_BEST.txt`

To convert additional files, place them under `datasets/` and append
their names to the `inputFiles` array in `SPMF_Converter.java`.
