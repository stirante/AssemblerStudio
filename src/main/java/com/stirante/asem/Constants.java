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
    public static final String NEW_TEMPLATE = ";This is just simple example, which writes 'Hello, Assembler Studio! :)' to LCD.\n" +
            ";It's not perfect and I'm not responsible for any bugs or issues it may have.\n" +
            ";It's main purpose is to just make sure everything works and also showcase some of the features.\n" +
            ";If you find a bug or issue in this code, send me an email at brzozowski.s.piotr@gmail.com\n" +
            "ljmp\t_reset\n" +
            "org\t0100h\n" +
            "_reset:\n" +
            "\tljmp\t_init\n" +
            "_init:\n" +
            "\tmov\tR0,#0ff80h\t\t;commands byte\n" +
            "\tmov\tR1,#0ff82h\t\t;state byte\n" +
            "\n" +
            "\t;try hovering over mov mnemonic or A register for some info\n" +
            "\tmov\tA,#1\t\t\t;clear display\n" +
            "\t\n" +
            "\t;CTRL + right click on _write to jump to the definition\n" +
            "\tlcall\t_write\n" +
            "\tmov\tA,#00001111b\t;display on/off control DISPLAY=1, CURSOR=1, BLINKING=1\n" +
            "\tacall\t_write\n" +
            "\n" +
            "\tmov\tA,#00000110b\t;entry mode set INCREMENT/DECREMENT=1, SHIFT MODE=0\n" +
            "\tacall\t_write\n" +
            "\n" +
            "\tmov\tA,#00111000b\t;function set DATA LENGTH=1,NUMBER=1,FONT=0\n" +
            "\tacall\t_write\n" +
            "\n" +
            "\tinc\tR0\t\t;change mode to data input\n" +
            "\tmov\tDPTR,#_string\n" +
            "\tljmp\t_writeText\n" +
            "_write:\n" +
            "\tmov\tR2,A\t\t;save A to R2\n" +
            "_isBusy:\n" +
            "\tmovx\tA,@R1\t\t;get LCD state\n" +
            "\tjb\tACC.7,_isBusy\t;if LCD is busy, then jump to _isBusy\n" +
            "\tmov\tA,R2\t\t;restore A from R2\n" +
            "\tmovx\t@R0,A\t\t;write value to command byte\n" +
            "\tret\n" +
            "_writeText:\n" +
            "\tclr\tA\n" +
            "\tmovc\tA,@A+DPTR\n" +
            "\tjz\t_textEnd\t;if byte is 0, then it's end of string\n" +
            "\tacall\t_write\n" +
            "\tinc\tDPTR\n" +
            "\tsjmp\t_writeText\n" +
            "_textEnd:\n" +
            "\tdec\tR0\t\t;adres wpisu instrukcji\n" +
            "_loop:\n" +
            "\tljmp\t_loop\n" +
            "\t\n" +
            "_string:\n" +
            "\t;probably it's not the best idea to just fill spaces, until you hit the next line\n" +
            "\tdb\t\"Hello, Assembler                        Studio! :)\",0\n" +
            "end";
    public static final double VERSION = 1.9;
}
