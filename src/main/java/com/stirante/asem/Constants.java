package com.stirante.asem;

import java.util.regex.Pattern;

/**
 * Created by stirante
 */
public class Constants {
    public static final Pattern IS_COMMENTED = Pattern.compile("^\\s*;.+$");
    public static final Pattern TO_COMMENT = Pattern.compile("^(\\s*)(.+)$");
    public static final Pattern WORD = Pattern.compile("[\\w.]+");
    public static final Pattern MNEMONIC = Pattern.compile("^\\s*(\\w+)$");
    public static final Pattern WHITESPACE = Pattern.compile("^(\\s*).*$");
    public static final String[] MNEMONIC_ARRAY = new String[]{"ACALL", "ADD", "ADDC", "AJMP", "ANL", "CJNE", "CLR", "CPL", "DA", "DEC", "DIV", "DJNZ", "INC", "JB", "JBC", "JC", "JMP", "JNB"
            , "JNC", "JNZ", "JZ", "LCALL", "LJMP", "MOV", "MOVC", "MOVX", "MUL", "NOP", "ORL", "POP", "PUSH", "RET", "RETI", "RL", "RLC", "RR", "RRC", "SETB", "SJMP", "SUBB", "SWAP", "XCH", "XCHD", "XRL"
    };
    public static final double VERSION = 1.65;
}
