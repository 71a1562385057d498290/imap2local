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

import ro.kovari.imap.config.ConfigurationService;
import ro.kovari.imap.store.ImapStore;
import ro.kovari.imap.store.MaildirLocalStore;
import ro.kovari.imap.store.Protocol;

import java.io.Console;

//TODO 1. handle case when IMAP folder separator is different from File.separator
//TODO 2. maybe implement UID based ImapStore -> MaildirLocalStore synchronization


public class Main {

    public static void main(String[] args) {
        System.out.println("  ___                        _                    _ ");
        System.out.println(" |_ _| _ __   __ _  _ __    | |    ___  __  __ _ | |");
        System.out.println("  | | | '  \\ / _` || '_ \\ _ | |__ / _ \\/ _|/ _` || |");
        System.out.println(" |___||_|_|_|\\__,_|| .__/(_)|____|\\___/\\__|\\__,_||_|");
        System.out.println("                   |_|                              ");
        System.out.println("                                                    ");

        Console console = System.console();

        // get configuration parameters from the user
        String maildirStoreLocation = console.readLine("Enter local output folder: ");
        String imapServer = console.readLine("Enter IMAP server: ");
        String useImapSSL = console.readLine("Use IMAP over SSL (default YES): ");

        Protocol imapProtocol =
                useImapSSL.equals("") // accept the default value
                        || useImapSSL.equalsIgnoreCase("y")
                        || useImapSSL.equalsIgnoreCase("yes") ? Protocol.IMAPS : Protocol.IMAP;

        // create the IMAP and the local Maildir stores
        ImapStore imapStore = new ImapStore(imapServer, imapProtocol, ConfigurationService.getImapConfiguration());
        MaildirLocalStore localStore = new MaildirLocalStore(maildirStoreLocation);

        // save the messages
        ImapLocal.imap2Local(imapStore, localStore);
    }
}
