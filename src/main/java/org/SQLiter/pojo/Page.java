package org.SQLiter.pojo;

import org.SQLiter.config.Config;
import org.SQLiter.utils.Parser;
import org.SQLiter.utils.SequenceManager;

import java.util.Arrays;

public class Page {

    private final int nextPageNumberSize = Integer.parseInt(Config.getProperty("nextPageNumberSize"));
    private final int pageSize = Integer.parseInt(Config.getProperty("pageSizeValue"));
    int pageNumber;
    byte[] byteArray = new byte[pageSize];
    int bytesUsed;
    int nextPageNumber = -1;
    boolean isHeader;

    public Page(byte[] byteArray, boolean isHeader){
        IO.println("Creating Page with : " + Arrays.toString(byteArray));
         this.pageNumber = SequenceManager.current("pageNumber");
         this.bytesUsed = Parser.getNumberOfUsedBytes(this.byteArray);
         SequenceManager.next("pageNumber");
         this.byteArray = byteArray;
         this.isHeader = isHeader;
    }

    @Override
    public String toString(){
        return "PageNumber : " + pageNumber + " bytesUsed : " + bytesUsed + " Next Page Number : " + nextPageNumber;
    }

}
