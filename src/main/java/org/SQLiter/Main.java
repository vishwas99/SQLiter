package org.SQLiter;

import org.SQLiter.core.Header;
import org.SQLiter.core.HeaderContent;

public class Main {

    static void main() {
        IO.println("Hello and welcome!");
        Header header = new Header();
        HeaderContent headerContent = new HeaderContent();
        header.createDB("UwU");
        header.createTable("UwUTable", "Please work");
        header.createTable("Another One", "DjKhalid");
        headerContent.parseAndGetAllTables();
    }

}
