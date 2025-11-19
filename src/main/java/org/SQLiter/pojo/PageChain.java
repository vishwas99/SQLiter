package org.SQLiter.pojo;

import org.SQLiter.utils.SequenceManager;

import java.util.*;

public class PageChain {
    Map<Integer, Page> pageList;

    public PageChain(byte[] headerArray, boolean isHeader, int pageSize, int bytesUsed, int nextPageSize){

        if(isHeader){
            pageList = new HashMap<>();
        }

        if(headerArray.length % (pageSize + bytesUsed + nextPageSize) != 0){
            throw new IllegalArgumentException("Must send pagified array with multiples of size : " + pageSize + bytesUsed + nextPageSize + " but received with size : " + headerArray.length);
        }

        IO.println("Page Chain Input :  " + Arrays.toString(headerArray));
        int noOfPages = (headerArray.length) / (pageSize + bytesUsed + nextPageSize);
        IO.println("No of Pages : " + noOfPages);
        for(int i=0; i<noOfPages; i++){
            int start = i*noOfPages;
            int end = start + (pageSize + bytesUsed + nextPageSize);
            assert pageList != null;
            pageList.put(SequenceManager.current("pageNumber"), new Page(Arrays.copyOfRange(headerArray, start, end), true));
        }

    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Integer, Page> page: pageList.entrySet()){
            sb.append("\n");
            sb.append(page.getKey()).append(" ").append(page.getValue().toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
