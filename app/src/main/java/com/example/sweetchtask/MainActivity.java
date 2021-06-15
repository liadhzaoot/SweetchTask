package com.example.sweetchtask;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MainActivity extends AppCompatActivity {

    String Url; // picture url
    String StorezipFileLocation; // zip file path
    String DirectoryName; // picture file path
    String picName = ""; // picture name
    boolean isFirstUrl = false; // flag to manage the url picture display

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imv = (ImageView) findViewById(R.id.mainImv); // imageView

        // delete the .png files. avoid from memory overload
        deletePicFiles(getExternalFilesDir(null).getAbsolutePath());

        try {
            // get the different url each time the application launch
            Url = getUrl();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // download zip from url
        DownloadZipfile mew = new DownloadZipfile();
        try {
            mew.execute(Url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // unzip
        try {
            unzip();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // show the unzip image to the screen
        File imgFile = new File(DirectoryName + File.separator + picName);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imv.setImageBitmap(myBitmap);
        }

    }

    /**
     * Each time the app is launched the function return
     * different Url image
     *
     * @return
     * @throws IOException
     */
    public String getUrl() throws IOException {
        File directory = getExternalFilesDir(null); // files directory
        String url1 = "https://test-assets-mobile.s3-us-west-2.amazonaws.com/125%402.zip"; // url1
        String url2 = "https://test-assets-mobile.s3-us-west-2.amazonaws.com/127%402.zip"; // url2
        String url; // final url
        directory = getExternalFilesDir(null);

        // create a manage url file
        File file = new File(directory, "ManageUrlFile.txt");
        file.createNewFile();

        // check if the file is empty
        //if it is empty, write false
        if (file.length() == 0) {
            writeToManageUrlFile(file, isFirstUrl);
            return url1;
        }

        // read from manage Url File
        isFirstUrl = readFromManageUrlFile(file);

        // check and init the different url
        if (isFirstUrl) {
            isFirstUrl = false;
            url = url1;
        } else {
            isFirstUrl = true;
            url = url2;
        }

        // write to manage url file
        writeToManageUrlFile(file, isFirstUrl);

        // return the url path
        return url;
    }

    /**
     * Return the boolean that was read from the manage url File
     *
     * @param file
     * @return
     * @throws IOException
     */
    public Boolean readFromManageUrlFile(File file) throws IOException {
        Boolean res;
        FileInputStream fin = new FileInputStream(file);
        DataInputStream din = new DataInputStream(fin);
        res = din.readBoolean();
        din.close();
        return res;
    }

    /**
     * Write the boolean flag into Manage url file
     *
     * @param file
     * @param isFirstUrl
     * @throws IOException
     */
    public void writeToManageUrlFile(File file, Boolean isFirstUrl) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        DataOutputStream dos = new DataOutputStream(fos);
        dos.writeBoolean(isFirstUrl);
        dos.close();
    }

    /**
     * Download the zip File from URL in different Thread
     */
    class DownloadZipfile extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... aurl) {
            File directory;
            File file;
            try {
                // Create zip File
                directory = getExternalFilesDir(null);
                file = new File(directory, "ZipFile.zip");
                file.createNewFile();

                // Update the zip path
                StorezipFileLocation = file.getAbsolutePath();
                // Update the unzip File
                DirectoryName = getExternalFilesDir(null).getAbsolutePath();

                // Read and download the file from url
                URL u = new URL(Url);
                URLConnection conn = u.openConnection();
                int contentLength = conn.getContentLength();
                DataInputStream stream = new DataInputStream(u.openStream());
                byte[] buffer = new byte[contentLength];
                stream.readFully(buffer);
                stream.close();

                // Write the Buffer
                FileOutputStream oFile = new FileOutputStream(file, false);
                DataOutputStream fos = new DataOutputStream(oFile);
                fos.write(buffer);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                return e.getMessage(); // swallow a 404
            } catch (IOException e) {
                return e.getMessage(); // swallow a 404
            }
            return null;
        }

    }

    /**
     * Delete the .png files. avoid from memory overload
     * @param Path
     */
    public void deletePicFiles(String Path)
    {
        File folder = new File(Path);
        File[] listOfFiles = folder.listFiles(); // get the files in folder
        String fileName;
        // run over the files in the folder
        for (int i = 0; i < listOfFiles.length; i++) {
            // check if the file is file and not a directory
            if (listOfFiles[i].isFile()) {
                fileName = listOfFiles[i].getName(); // save file name
                if(fileName.length()>4) // check if the file name has more then 4 characters
                    // check if the last 4 characters are ".png"
                    if(fileName.substring(fileName.length()-4).equals(".png"))
                        if(listOfFiles[i].delete()) // delete the .png file
                            Log.e("MyError","delete success");
                        else
                            Log.e("MyError","not deleted");
            }
        }
    }

    /**
     * This is the method for unzip file.
     * which is store your location. And unzip folder will store as per your desire location.
     *
     * the method
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void unzip() throws IOException, ExecutionException, InterruptedException {
        UnzipUtil d = new UnzipUtil(StorezipFileLocation, DirectoryName);
        picName = d.getPicName();
    }

}


