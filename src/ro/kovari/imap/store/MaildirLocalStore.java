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
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Class representing a local Maildir store<br>
 * Note: this is not a complete implementation of the Maildir specification
 */
public class MaildirLocalStore {

    private static final String MAILDIR_FOLDER = "Maildir";
    private static final String MAILDIR_FOLDER_SEPARATOR = ".";
    private static final String MAILDIR_MARKER_FILE = "maildirfolder";

    private static final String DEFAULT_MAILBOX = "INBOX";

    private final File maildirStoreLocation;


    /**
     * Generate a random long to be used as the starting point for the message ids,
     * regardless of the maildir folder. On every new message written to the store,
     * the counter is increased by one.
     */
    private static long nextMessageId;

    static {
        int pwr = 17;
        nextMessageId = ThreadLocalRandom.current().nextLong(
                (long) Math.pow(10, pwr), (long) Math.pow(10, pwr + 1)
        );
    }


    /**
     * Get the ID to be used for the next message
     * @return the ID to be used for the next message
     */
    public static long getNextMessageId() {
        return nextMessageId++;
    }



    /**
     * Create a new {@link MaildirLocalStore}
     * @param location the location of the new {@link MaildirLocalStore}
     */
    public MaildirLocalStore(String location) {
        if (location == null)
            throw new IllegalArgumentException("Invalid Maildir store location!");

        maildirStoreLocation = location.isEmpty() ?
                new File(MAILDIR_FOLDER) : new File(location, MAILDIR_FOLDER);
    }



    /**
     * Get the location of this {@link MaildirLocalStore}
     * @return the location of this {@link MaildirLocalStore}
     */
    public File getMaildirStoreLocation() {
        return maildirStoreLocation;
    }



    /**
     * Create a new local {@link MaildirFolder} from the specified IMAP folder full name.<br>
     * Note: see Maildir++ for more details
     * @param imapFolderName the IMAP folder full name
     * @param imapFolderSeparator the IMAP folder separator
     * @return the newly created {@link MaildirFolder}
     */
    public MaildirFolder flatten(String imapFolderName, String imapFolderSeparator) {
        StringBuilder sb = new StringBuilder("");

        if (!imapFolderName.equalsIgnoreCase(DEFAULT_MAILBOX)) {
            sb.append(MAILDIR_FOLDER_SEPARATOR)
                    .append(imapFolderName.replace(imapFolderSeparator, MAILDIR_FOLDER_SEPARATOR));
        }
        return new MaildirFolder(new File(maildirStoreLocation, sb.toString()));
    }



    /**
     * Create the actual {@link MaildirFolder} along with its subfolders<br>
     * Note: this will also create the {@link MaildirLocalStore}
     * @param folder the {@link MaildirFolder}
     */
    public void createMaildirFolder(MaildirFolder folder) {
        File maildirFolder = folder.getMaildirFolder();

        if (!maildirFolder.exists() && !maildirFolder.mkdirs()) {
            throw new MaildirException("Unable to create Maildir folder!");
        }

        File maildirFolderMarker = new File(maildirFolder, MAILDIR_MARKER_FILE);
        try {
            maildirFolderMarker.createNewFile();

        } catch (IOException e) {
            throw new MaildirException("Unable to create marker file!");
        }

        if (!folder.getCurFolder().exists() && !folder.getCurFolder().mkdir()) {
            throw new MaildirException("Unable to create 'cur' sub-folder!");
        }

        if (!folder.getNewFolder().exists() && !folder.getNewFolder().mkdir()) {
            throw new MaildirException("Unable to create 'new' sub-folder!");
        }

        if (!folder.getTmpFolder().exists() && !folder.getTmpFolder().mkdir()) {
            throw new MaildirException("Unable to create 'tmp' sub-folder!");
        }
    }
}
