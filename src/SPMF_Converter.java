import java.io.*;
import java.util.*;

/**
 * Converts sequential pattern mining datasets from the SPMF format
 * (item IDs only) to the Quantitative SPMF (QSDB) format used in
 * high-utility sequential pattern mining experiments.
 *
 * Input format (SPMF):
 *   Each line: "item1 item2 -1 item3 -1 -2"
 *   - Non-negative integer: item ID
 *   - -1: itemset separator
 *   - -2: end of sequence
 *
 * Output format:
 *   1. <name>_seq.txt (QSDB): "item1[q1] item2[q2] -1 item3[q3] -1 -2"
 *      Quantities are drawn from a weighted uniform distribution over
 *      [1, 10].
 *   2. <name>_eui.txt (external utility table): "itemID:profit"
 *      Profits are drawn from a log-normal distribution and clamped to
 *      [1, 1000].
 *
 * The random seed is fixed at 42 so that, for a given SPMF input, the
 * output QSDB files are byte-identical across runs.
 */
public class SPMF_Converter {

    private static final Random random = new Random(42);

    private static final String OUTPUT_DIR = "datasets";
    private static final String SOURCE_DIR = "datasets";

    public static void main(String[] args) {
        String[] inputFiles = {
                "BMS1_SPMF.txt",
                "E_SHOP.txt",
                "KOSARAK.txt",
                "ONLINE_RETAIL_II_ALL.txt",
                "FIFA.txt",
                "LEVIATHAN.txt",
                "SIGN.txt",
                "ONLINE_RETAIL_II_BEST.txt"
        };

        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) dir.mkdirs();

        System.out.println("========== BATCH CONVERSION STARTED ==========");
        System.out.println("[*] Random seed = 42 (reproducible output)");

        for (String fileName : inputFiles) {
            String fullInputPath = SOURCE_DIR.isEmpty()
                    ? fileName
                    : SOURCE_DIR + File.separator + fileName;
            processSingleFile(fullInputPath, fileName);
        }

        System.out.println("==============================================");
    }

    /**
     * Converts a single SPMF file into the matching _seq.txt and _eui.txt pair.
     *
     * @param fullPath     full path to the SPMF input file
     * @param originalName original file name, used to derive the output file names
     */
    private static void processSingleFile(String fullPath, String originalName) {
        File inputFile = new File(fullPath);
        if (!inputFile.exists()) {
            System.err.println("[!] ERROR: input file not found at: "
                    + inputFile.getAbsolutePath());
            return;
        }

        String baseName = originalName.replace(".txt", "");
        String seqOut = OUTPUT_DIR + File.separator + baseName + "_seq.txt";
        String euiOut = OUTPUT_DIR + File.separator + baseName + "_eui.txt";

        // Collect distinct item IDs in ascending order.
        Set<Integer> itemRegistry = new TreeSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(seqOut))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("@")) continue;

                String[] tokens = line.trim().split("\\s+");
                for (String t : tokens) {
                    try {
                        int id = Integer.parseInt(t);
                        if (id >= 0) {
                            // Weighted uniform quantity: 70% in [1,2], 20% in [3,5], 10% in [6,10].
                            int r = random.nextInt(100);
                            int q;
                            if (r < 70) q = random.nextInt(2) + 1;
                            else if (r < 90) q = random.nextInt(3) + 3;
                            else q = random.nextInt(5) + 6;

                            bw.write(id + "[" + q + "] ");
                            itemRegistry.add(id);
                        } else {
                            // -1 (itemset separator) or -2 (end of sequence).
                            bw.write(id + " ");
                        }
                    } catch (NumberFormatException e) {
                        // Skip non-numeric tokens.
                    }
                }
                bw.write("\n");
            }

            generateEUI(euiOut, itemRegistry);
            System.out.println("[OK] Converted: " + originalName
                    + " (" + itemRegistry.size() + " items)");

        } catch (IOException e) {
            System.err.println("[ERROR] " + originalName + ": " + e.getMessage());
        }
    }

    /**
     * Generates the external utility (profit) table for all distinct items.
     *
     * Profits follow a log-normal distribution (exp of N(2.5, 1)) and are
     * clamped to [1, 1000] to avoid non-positive utilities and arithmetic
     * overflow when computing upper bounds.
     *
     * @param outputPath path of the output EUI file
     * @param items      distinct item IDs in ascending order
     */
    private static void generateEUI(String outputPath, Set<Integer> items) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("# ItemID:Profit (Log-Normal Distribution seed=42)\n");
            for (int id : items) {
                double logNormal = Math.exp(random.nextGaussian() * 1.0 + 2.5);
                long profit = Math.round(Math.max(1, Math.min(1000, logNormal)));
                writer.write(id + ":" + profit + "\n");
            }
        }
    }
}
