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



package ro.kovari.imap.auth;


import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.io.Console;
import java.util.Arrays;


public class ImapAuthenticator extends Authenticator {

    public ImapAuthenticator() {
        super();
    }



    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        Console console = System.console();
        // read username and password from the console
        String username = console.readLine("Enter username: ");
        char[] password = console.readPassword("Enter password: ");

        PasswordAuthentication auth = new PasswordAuthentication(username, String.valueOf(password));
        // doesn't really help, since JavaMail's PasswordAuthentication stores the password in a String
        Arrays.fill(password, '\0');

        return auth;
    }
}
