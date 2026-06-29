# JWhisper Client GUI

JWhisper Client GUI is a part of JWhisper platform, providing a GUI client.
It's used for sending messages to other users via relay server.

## Prerequisites

To run the app you need to have `JDK 25+` installed.

To build the app from sources you will also need some modern version of `Maven 3.X` installed.

## Build

To build the app use the next command:
```bash
mvn clean package
```

Output is the `.zip` file in `target` folder.
Unpack the ZIP archive where you want the app to live.

Done!

## Run

### Server run

Before using the client application, you should set up and run relay server.

### Client run

To run the app execute the next command:
```bash
java -jar jw-client-gui-1.0.0.jar
```

#### Local setup screen (first run)

Depending on existence of your identity key store (i.e. whether it's your first execution or not),
you will be prompted to create a master password for a new key store.
This process should ensure existence of `identity.p12` key store, where your personal signing and encryption keys
will be stored. **Do not forget the password!**

After that you will be redirected to the Login screen.

#### Login screen

If you already have your identity keys created, you will see Login screen.

In the top-right corner you can the gear button that should redirect you to the Settings screen.

In the middle on the screen you should see "Username" and "Master password" fields 
together with Login and Register buttons used for corresponding operations.

After successful registration you should see corresponding message.
After successful login you should be redirected to Home screen.

#### Settings screen

Here you can configure connection settings as relay server `hostname` and `port`.
To persist changes click "Save" button.
The configuration is stored in `config.json` file.

There is also a field to provide relay server TLS certificate.
It can be provided either as PEM string or read from file.
To import it to truststore you should provide the master password and click "Import" button.
Initially the truststore (`truststore.p12`) is empty, so you should have at least one trusted certificate.

When you are done, click "Return" button to come back to the Login screen.

#### Home screen

This is the main chat window.

On the left side you can see "Conversations" list.
This is the list of sessions (chats) with other users.
To initiate a new chat click the "+" (plus) button and provide recipient's username.
To select a chat with a specific user, just click on its username.

In the middle of the screen you should see a chat window.
Select a chat, type message and press "ENTER" or "Send" to send the message.
Here you will also see incoming message from you interlocutor.

To finish press "Logout".

#### Exit

To exit the application simply close it.

## Communication

Whole communication with relay and end clients in encrypted.
Moreover, end-to-end communication (messages between individual users) is end-to-end encrypted (E2EE),
which effectively means nobody (even relay server) **can't read the messages**. To read the messages you should
own the private keys of the recipient, otherwise it's impossible to read them. All the messages are also signed
using the sender's private key, which ensures that the message was actually sent by the sender mentioned
in the message.

TL;DR communication between users is secure and no one else can read your messages.

## Developer docs

To build the `javadoc` you can use the next command:
```bash
mvn clean javadoc:aggregate
```
