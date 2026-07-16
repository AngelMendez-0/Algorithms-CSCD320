import java.io.*;
import java.util.Scanner;

public class SequenceAlignment {
    private static final int FROM_DIAGONAL = 0;
    private static final int FROM_LEFT     = 1;
    private static final int FROM_UP       = 2;

    private static final char GAP_CHAR = '-';

    private static final String INPUT_FILE  = "input.txt";
    private static final String OUTPUT_FILE = "output.txt";

    public static void main(String[] args) throws IOException {
        String[] input = readInput(INPUT_FILE);
        String seq1  = input[0];
        String seq2  = input[1];

        int matchScore    = Integer.parseInt(input[2]);
        int mismatchScore = Integer.parseInt(input[3]);
        int gapScore      = Integer.parseInt(input[4]);

        String[] aligned = align(seq1, seq2, matchScore, mismatchScore, gapScore);
        int score = computeScore(aligned[0], aligned[1], matchScore, mismatchScore, gapScore);

        writeOutput(OUTPUT_FILE, score, aligned[0], aligned[1]);
    } // end of main

    private static String[] readInput(String filename) throws IOException {
        String[] result = new String[5];

        try (Scanner sc = new Scanner(new File(filename))) {
            result[0] = sc.nextLine().trim();
            result[1] = sc.nextLine().trim();
            result[2] = sc.nextLine().split(" ")[1];
            result[3] = sc.nextLine().split(" ")[1];
            result[4] = sc.nextLine().split(" ")[1];

        } // end of try

        return result;
    } // end of readinput

    private static void writeOutput(String filename, int score, String aligned1, String aligned2) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println(score);
            pw.println(aligned1);
            pw.println(aligned2);
        } // end of try

    } // end of writeOutput

    private static String[] align(String seq1, String seq2, int matchScore, int mismatchScore, int gapScore) {
        int m = seq1.length();
        int n = seq2.length();

        int[][] dp    = new int[m + 1][n + 1];
        int[][] trace = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0]    = i * gapScore;
            trace[i][0] = FROM_UP;

        } // end of for

        for (int j = 0; j <= n; j++) {
            dp[0][j]    = j * gapScore;
            trace[0][j] = FROM_LEFT;

        } // end of for

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                boolean match = seq1.charAt(i - 1) == seq2.charAt(j - 1);
                int charScore = match ? matchScore : mismatchScore;

                int scoreDiag = dp[i - 1][j - 1] + charScore;
                int scoreUp   = dp[i - 1][j]     + gapScore;
                int scoreLeft = dp[i][j - 1]      + gapScore;

                if (scoreDiag >= scoreUp && scoreDiag >= scoreLeft) {
                    dp[i][j]    = scoreDiag;
                    trace[i][j] = FROM_DIAGONAL;

                } else if (scoreUp >= scoreLeft) {
                    dp[i][j]    = scoreUp;
                    trace[i][j] = FROM_UP;

                } else {
                    dp[i][j]    = scoreLeft;
                    trace[i][j] = FROM_LEFT;

                } // end of if else

            } // end of for
        } // end of for

        return traceback(seq1, seq2, trace, m, n);
    } // end of align

    private static String[] traceback(String seq1, String seq2, int[][] trace, int m, int n) {
        StringBuilder a1 = new StringBuilder();
        StringBuilder a2 = new StringBuilder();

        int i = m, j = n;

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && trace[i][j] == FROM_DIAGONAL) {
                a1.append(seq1.charAt(i - 1));
                a2.append(seq2.charAt(j - 1));
                i--;
                j--;
            } else if (i > 0 && (j == 0 || trace[i][j] == FROM_UP)) {
                a1.append(seq1.charAt(i - 1));
                a2.append(GAP_CHAR);
                i--;
            } else {
                a1.append(GAP_CHAR);
                a2.append(seq2.charAt(j - 1));
                j--;
            } // end of if else

        } // end of while

        return new String[]{ a1.reverse().toString(), a2.reverse().toString() };

    }  // end of traceBack

    private static int computeScore(String aligned1, String aligned2, int matchScore, int mismatchScore, int gapScore) {
        int score = 0;

        for (int k = 0; k < aligned1.length(); k++) {
            char c1 = aligned1.charAt(k);
            char c2 = aligned2.charAt(k);

            if (c1 == GAP_CHAR || c2 == GAP_CHAR) {
                score += gapScore;
            } else if (c1 == c2) {
                score += matchScore;
            } else {
                score += mismatchScore;
            } // end of if else

        } // end of for

        return score;
    } // end of computeScore

} // end of sequence alignment
