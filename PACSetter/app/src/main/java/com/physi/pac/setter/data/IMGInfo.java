package com.physi.pac.setter.data;

public class IMGInfo {

    private String thumbnailPath, filePath;
    private String fileName;

    public IMGInfo(){
    }

    public IMGInfo(String thumbnailPath, String filePath){
        this.filePath = filePath;
        this.thumbnailPath = thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath, String fileName){
        this.thumbnailPath = thumbnailPath;
        this.fileName = fileName;
    }

    public void setFilePath(String filePath){
        this.filePath = filePath;
        this.fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    public String getThumbnailPath(){
        return thumbnailPath;
    }

    public String getFilePath(){
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }
}
