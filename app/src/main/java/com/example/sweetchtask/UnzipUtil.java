package com.example.sweetchtask;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class that manage to unzip the file
 */
public class UnzipUtil
{
    private String zipFile;
    private String location;
    private String picName;

    /**
     * Constructor
     * @param zipFile
     * @param location
     */
    public UnzipUtil(String zipFile, String location)
    {
        this.zipFile = zipFile; // zip file path
        this.location = location; // unzip file path

        unzip(this.zipFile,this.location); // unzip function
        dirChecker("");
    }

    /**
     * unzip Method
     * Sweetch tester ReadMe: i know that every zipFile has just one file (image).
     *                        the function handle a general case where the ZIP has multiply files.
     *                        To make the code generic and beautiful :) .
     * @param zipFilePath
     * @param destDir
     */
    void unzip(String zipFilePath, String destDir) {
    File dir = new File(destDir); // create file

    // create output directory if it doesn't exist
    if(!dir.exists()) dir.mkdirs();
    FileInputStream fis;

    //buffer for read and write data to file
    byte[] buffer = new byte[1024];

    try {
        fis = new FileInputStream(zipFilePath);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry ze = zis.getNextEntry();

        //read the zip files
        while(ze != null){
            this.picName = ze.getName();
            File newFile = new File(destDir + File.separator + this.picName);

            //create directories for sub directories in zip
            new File(newFile.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();

            //close this ZipEntry
            zis.closeEntry();
            ze = zis.getNextEntry();
        }
        //close last ZipEntry
        zis.closeEntry();
        zis.close();
        fis.close();
    } catch (IOException e) {
        e.printStackTrace();
    }

}

    /**
     * create directory if not exist
     * @param dir
     */
    private void dirChecker(String dir)
    {
        File f = new File(location + dir);
        if(!f.isDirectory())
        {
            f.mkdirs();
        }
    }

    /**
     * return unzip file name
     * @return
     */
    public String getPicName() {
        return picName;
    }
}