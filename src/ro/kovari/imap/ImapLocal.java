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



package ro.kovari.imap;

import ro.kovari.imap.store.ImapStore;
import ro.kovari.imap.store.MaildirFolder;
import ro.kovari.imap.store.MaildirLocalStore;
import ro.kovari.imap.utils.StreamUtils;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


/** A bridge between the remote IMAP store and the local Maildir store */
public class ImapLocal {

    /**
     * Save messages from an {@link ImapStore} to a {@link MaildirLocalStore}
     * @param imapStore the remote {@link ImapStore}
     * @param localStore the {@link MaildirLocalStore}
     */
    public static void imap2Local(ImapStore imapStore, MaildirLocalStore localStore) {
        imapStore.connect();
        List<Folder> imapFolders = imapStore.fetchFolders();
        System.out.println(System.lineSeparator());

        for (Folder imapFolder : imapFolders) {
            String imapFolderName = imapFolder.getFullName();
            System.out.println("Processing folder: " + imapFolderName);

            MaildirFolder maildirFolder = localStore.flatten(imapFolderName, imapStore.getSeparator());
            localStore.createMaildirFolder(maildirFolder);

            Message[] messages = imapStore.getMessages(imapFolder);
            if (messages.length != 0) {
                saveMessages(maildirFolder, messages);

            } else {
                System.out.print("Folder empty. Moving on!");
            }
            System.out.println(System.lineSeparator());
        }
        System.out.println("Done!"); // all done, download completed!
    }



    /**
     * Save the messages into the specified {@link MaildirFolder}
     * @param maildirFolder the {@link MaildirFolder}
     * @param messages the messages to be saved
     */
    private static void saveMessages(MaildirFolder maildirFolder, Message[] messages) {
        long totalCount = messages.length;
        long idx = 1;

        for (Message message : messages) {
            System.out.print("Downloading message " + idx + " of " + totalCount);
            System.out.print("\r");

            OutputStream maildirOutputStream = null;
            try {
                // currently, no IMAP flags are taken into account;
                // all messages are saved into the 'new' subfolder of the current maildir folder
                maildirOutputStream = maildirFolder.getOutputStream(MaildirFolder.SubFolder.NEW);
                message.writeTo(maildirOutputStream);

            } catch (MessagingException | IOException e) {
                e.printStackTrace();

            } finally {
                StreamUtils.closeStream(maildirOutputStream);
            }
            idx++;
        }
    }
}
