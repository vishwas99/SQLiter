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
    Page nextPage;
    int nextPageNumber = -1;
    boolean isHeader;

    public Page(byte[] byteArray, boolean isHeader){
         this.pageNumber = SequenceManager.next("PageNumber");
         this.byteArray = Arrays.copyOfRange(byteArray, 0, pageSize - nextPageNumberSize);
         if(byteArray.length > pageSize){
//             Create Next Page
//             1. Add new no of Bytes Used
//             2. Recursively try to fit content and create new Pages
             this.nextPage = new Page(Arrays.copyOfRange(byteArray, pageSize - nextPageNumberSize, byteArray.length), isHeader);
             this.nextPageNumber = SequenceManager.current("PageNumber");
         }
         this.bytesUsed = Parser.getNumberOfUsedBytes(this.byteArray);
    }

    @Override
    public String toString(){
        return "PageNumber : " + pageNumber + " bytesUsed : " + bytesUsed + " Next Page Number : " + nextPageNumber;
    }

}
