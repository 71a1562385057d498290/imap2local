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

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.SortTerm;
import ro.kovari.imap.auth.ImapAuthenticator;
import ro.kovari.imap.config.Configuration;
import ro.kovari.imap.exception.ImapException;

import javax.mail.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/** Class representing a remote IMAP store */
public class ImapStore {

    private final Properties sessionProperties = new Properties();
    private Store store = null;
    private String folderSeparator = null;
    private String accountName = null;



    /**
     * Create a new {@link ImapStore}
     * @param host the IMAP server
     * @param protocol the IMAP {@link Protocol} to use: either IMAP or IMAPS
     * @param additionalConfig additional IMAP configuration properties
     */
    public ImapStore(String host, Protocol protocol, Configuration additionalConfig) {
        String protocolName = protocol.name().toLowerCase();

        sessionProperties.put("mail.store.protocol", protocolName);
        sessionProperties.put("mail." + protocolName + ".user", "");
        sessionProperties.put("mail." + protocolName + ".host", host);
        sessionProperties.put("mail." + protocolName + ".partialfetch", additionalConfig.isPartialFetchEnabled());
        sessionProperties.put("mail." + protocolName + ".fetchsize", additionalConfig.getFetchSize());
        sessionProperties.computeIfAbsent("mail." + protocolName + ".ssl.trust",
                val -> additionalConfig.getSslTrustedHosts());
    }



    /** Connect to the remote IMAP server */
    public void connect() {
        ImapAuthenticator authenticator = new ImapAuthenticator();
        Session session = Session.getInstance(sessionProperties, authenticator);

        try {
            store = session.getStore();
            PasswordAuthentication auth = authenticator.getPasswordAuthentication();

            if (!store.isConnected()) {
                store.connect(auth.getUserName(), auth.getPassword());
            }
            folderSeparator = String.valueOf(store.getDefaultFolder().getSeparator());
            accountName = auth.getUserName();

        } catch (MessagingException e) {
            // not much to do if connection or authentication fails
            throw new ImapException("Exception getting IMAP store!", e);
        }
    }



    /**
     * Get the IMAP account name
     * @return the IMAP account name
     */
    public String getAccountName() {
        if (!hasValidState()) {
            throw new ImapException("Invalid IMAP store state!");
        }
        return accountName;
    }



    /**
     * Get the folder separator
     * @return the folder separator
     */
    public String getSeparator() {
        if (!hasValidState()) {
            throw new ImapException("Invalid IMAP store state!");
        }
        return folderSeparator;
    }



    /**
     * Get the messages from an IMAP folder.<br>
     * If the server supports the SORT extension the messages will be returned sorted
     * based on the arrival date and time.
     * @param folder the IMAP folder
     * @return an array of {@link Message} objects, representing the messages from this folder
     */
    public Message[] getMessages(Folder folder) {
        if (!hasValidState()) {
            throw new ImapException("Invalid IMAP store state!");
        }

        /*
        // Search example with OR term
        RecipientStringTerm recipient1 =
                new RecipientStringTerm(Message.RecipientType.TO, "recipient@example.test");
        RecipientStringTerm recipient2 =
                new RecipientStringTerm(Message.RecipientType.TO, "another-recipient@example.test");

        OrTerm orTerm = new OrTerm(recipient1, recipient2);
        try {
            return ((IMAPFolder) folder).search(orTerm);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
        */

        try {
            folder.open(Folder.READ_ONLY);
            // if the server supports it, get the messages sorted
            // based on the arrival date and time
            if (((IMAPStore) store).hasCapability("SORT")) {
                return ((IMAPFolder) folder).getSortedMessages(
                        new SortTerm[] { SortTerm.ARRIVAL }
                );
            }

            return folder.getMessages();

        } catch (MessagingException e) {
            throw new ImapException("Exception getting messages!", e);
        }
    }



    /**
     * Get all folders from the root folder of the default namespace
     * @return all folders from the root folder of the default namespace
     */
    public List<Folder> fetchFolders() {
        if (!hasValidState()) {
            throw new ImapException("Invalid IMAP store state!");
        }

        try {
            return fetchFolders(store.getDefaultFolder());

        } catch (MessagingException e) {
            throw new ImapException("Exception getting IMAP folders", e);
        }
    }



    /**
     * Get all folders from the specified {@link Folder}
     * @param folder the {@link Folder}
     * @return all folders from the specified {@link Folder}
     * @throws MessagingException in case of error
     */
    private List<Folder> fetchFolders(Folder folder)
            throws MessagingException {

        if (folder == null)
            throw new IllegalArgumentException("Folder can't be null!");

        List<Folder> folderList = new ArrayList<>();
        if (folder.exists()
                && (folder.getType() & Folder.HOLDS_MESSAGES) == Folder.HOLDS_MESSAGES) {
            folderList.add(folder);
        }

        Folder[] folders = folder.list();
        for (Folder folderItem : folders) {
            folderList.addAll(fetchFolders(folderItem));
        }
        return folderList;
    }



    /**
     * Check if the {@link Store} is in a valid state
     * @return true if the {@link Store} is in a valid state, false otherwise
     */
    private boolean hasValidState() {
        return (store != null) && (store.isConnected());
    }
}
