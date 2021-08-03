/*
 * Project: imap.local
 *
 * Copyright (c) Attila Kovari
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */



package ro.kovari.imap.store;

import ro.kovari.imap.exception.MaildirException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;


/** Class representing a Maildir folder */
public class MaildirFolder {

    private final File maildirFolder;



    /**
     * Create a new {@link MaildirFolder} from the given {@link File}
     * @param file the {@link File}
     */
    public MaildirFolder(File file) {
        maildirFolder = file;
    }



    /**
     * Get the {@link File} representing this {@link MaildirFolder}
     * @return the {@link File} representing this {@link MaildirFolder}
     */
    public File getMaildirFolder() {
        return maildirFolder;
    }



    /**
     * Get the name of the {@link File} representing this {@link MaildirFolder}
     * @return the {@link MaildirFolder} name
     */
    public String getMaildirFolderName() {
        return maildirFolder.getName();
    }



    /**
     * Get the 'cur' subfolder of this {@link MaildirFolder}
     * @return the 'cur' subfolder of this {@link MaildirFolder}
     */
    public File getCurFolder() {
        return new File(maildirFolder, SubFolder.CUR.name);
    }



    /**
     * Get the 'new' subfolder of this {@link MaildirFolder}
     * @return the 'new' subfolder of this {@link MaildirFolder}
     */
    public File getNewFolder() {
        return new File(maildirFolder, SubFolder.NEW.name);
    }



    /**
     * Get the 'tmp' subfolder of this {@link MaildirFolder}
     * @return the 'tmp' subfolder of this {@link MaildirFolder}
     */
    public File getTmpFolder() {
        return new File(maildirFolder, SubFolder.TMP.name);
    }



    /**
     * Get a subfolder of this {@link MaildirFolder}
     * @return the subfolder of this {@link MaildirFolder}
     */
    public File getSubFolder(SubFolder subFolder) {
        return new File(maildirFolder, subFolder.name);
    }



    /**
     * Generate a file name
     * @return the generated file name
     */
    private String generateUniqueFileName() {
        String hostname = "localhost"; // default hostname, just in case
        long currentTime = Instant.now().getEpochSecond();

        try {
            hostname = InetAddress.getLocalHost().getHostName();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return currentTime + "."
                + MaildirLocalStore.getNextMessageId() + "." + hostname;
    }



    /**
     * Get an output stream for writing a new message to the specified {@link MaildirFolder.SubFolder}
     * @return the new {@link OutputStream}
     */
    public OutputStream getOutputStream(SubFolder subFolder) {
        // side note: when the subfolder is CUR, maybe we could automatically append the
        // maildir info to each message file name, indicating that the message was seen (<msg-file-name>:2,S)
        File file = new File(getSubFolder(subFolder), generateUniqueFileName());
        try {
            return new FileOutputStream(file);

        } catch (FileNotFoundException e) {
            throw new MaildirException("File not found!", e);
        }
    }



    /** Enum representing the subfolders of a {@link MaildirFolder} */
    public enum SubFolder {
        CUR("cur"), NEW("new"), TMP("tmp");

        private final String name;



        SubFolder(String subFolderName) {
            name = subFolderName;
        }
    }
}
