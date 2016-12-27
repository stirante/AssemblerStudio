package com.stirante.asem.syntax;

import static com.stirante.asem.syntax.ArgumentVerifier.Type.*;

/**
 * Created by stirante
 */
public class MnemonicArguments {
    public static ArgumentVerifier.Type[][] JZ = {
            {CODE_ADDR}
    };
    public static ArgumentVerifier.Type[][] ACALL = {
            {CODE_ADDR}
    };
    public static ArgumentVerifier.Type[][] LCALL = {
            {CODE_ADDR}
    };
    public static ArgumentVerifier.Type[][] LJMP = {
            {CODE_ADDR}
    };
//    public static ArgumentVerifier.Type[][] MOV = {
//            {AT_R0, DATA},
//            {AT_R1, DATA},
//            {AT_R0, A},
//            {AT_R1, A},
//            {AT_R0, IRAM_ADDR},
//            {AT_R1, IRAM_ADDR},
//            {A, DATA},
//            {A, AT_R0},
//            {A, AT_R1},
//            {A, R0},
//            {A, R1},
//            {A, R2},
//            {A, R3},
//            {A, R4},
//            {A, R5},
//            {A, R6},
//            {A, R7},
//            {A, IRAM_ADDR},
//            {DPTR, DATA_16},
//            {R0, DATA},
//            {R1, DATA},
//            {R2, DATA},
//            {R3, DATA},
//            {R4, DATA},
//            {R5, DATA},
//            {R6, DATA},
//            {R7, DATA},
//            {R0, A},
//            {R1, A},
//            {R2, A},
//            {R3, A},
//            {R4, A},
//            {R5, A},
//            {R6, A},
//            {R7, A},
//            {R0, IRAM_ADDR},
//            {R1, IRAM_ADDR},
//            {R2, IRAM_ADDR},
//            {R3, IRAM_ADDR},
//            {R4, IRAM_ADDR},
//            {R5, IRAM_ADDR},
//            {R6, IRAM_ADDR},
//            {R7, IRAM_ADDR},
//            {BIT_ADDR, C},
//            {IRAM_ADDR, DATA},
//            {IRAM_ADDR, AT_R0},
//            {IRAM_ADDR, AT_R1},
//            {IRAM_ADDR, R0},
//            {IRAM_ADDR, R1},
//            {IRAM_ADDR, R2},
//            {IRAM_ADDR, R3},
//            {IRAM_ADDR, R4},
//            {IRAM_ADDR, R5},
//            {IRAM_ADDR, R6},
//            {IRAM_ADDR, R7},
//            {IRAM_ADDR, A},
//            {IRAM_ADDR, IRAM_ADDR}
//    };
}
