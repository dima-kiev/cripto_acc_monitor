package com.ridnaxata;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LDTTest {

    private static String src = "24/04/2018, 08:54:05";

    private static String PTRN01 = "M/d/u, h:mm:ss a";
    private static String PTRN02 = "d/M/u, H:mm:ss";

    public static void main(String[] args) {

        LocalDateTime prsd = LocalDateTime.parse(src, DateTimeFormatter.ofPattern(PTRN02));

    }

}
