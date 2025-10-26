package org.SQLiter;

import org.SQLiter.header.Header;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static void main() {
        IO.println("Hello and welcome!");
        Header header = new Header();
        header.createDB("UwU");
    }

}
