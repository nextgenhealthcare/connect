/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.webreach.mirth.client.core.Client;
import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.core.ListHandlerException;
import com.webreach.mirth.client.core.SystemEventListHandler;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.ServerConfiguration;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.ChannelStatus.State;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.SystemEventFilter;
import com.webreach.mirth.model.util.ImportConverter;
import com.webreach.mirth.util.PropertyVerifier;

public class Shell {
    private Client client;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy_HH-mm-ss.SS");
    private String currentUser = new String();
    private PrintWriter out;
    private PrintWriter err;

    public Shell(String[] args) {
        out = new PrintWriter(System.out, true);
        err = new PrintWriter(System.out, true);
        run(args);
    }

    public static void main(String[] args) {
        new Shell(args);
    }

    private void run(String[] args) {
        Option serverOption = OptionBuilder.withArgName("address").hasArg().withDescription("server address").create("a");
        Option userOption = OptionBuilder.withArgName("user").hasArg().withDescription("user login").create("u");
        Option passwordOption = OptionBuilder.withArgName("password").hasArg().withDescription("user password").create("p");
        Option scriptOption = OptionBuilder.withArgName("script").hasArg().withDescription("script file").create("s");
        Option versionOption = OptionBuilder.withArgName("version").hasArg().withDescription("version").create("v");
        Option helpOption = new Option("h", "help");

        Options options = new Options();
        options.addOption(serverOption);
        options.addOption(userOption);
        options.addOption(passwordOption);
        options.addOption(scriptOption);
        options.addOption(versionOption);
        options.addOption(helpOption);

        CommandLineParser parser = new GnuParser();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("a") && line.hasOption("u") && line.hasOption("p") && line.hasOption("v")) {
                String server = line.getOptionValue("a");
                String user = line.getOptionValue("u");
                String password = line.getOptionValue("p");
                String version = line.getOptionValue("v");
                String script = line.getOptionValue("s");

                runShell(server, user, password, version, script);
            } else if (line.hasOption("h")) {
                new HelpFormatter().printHelp("Shell", options);
            } else {
                new HelpFormatter().printHelp("Shell", options);
                error("all of -a, -u, -p, and -v options must be supplied");
                System.exit(2);
            }
        } catch (ParseException e) {
            error("Could not parse input arguments.");
            System.exit(2);
        }
    }

    private void runShell(String server, String user, String password, String version, String script) {
        try {
            client = new Client(server);
            if (!client.login(user, password, version)) {
                error("Could not login to server.");
                return;
            }
            out.println("Connected to Mirth server @ " + server + " (" + client.getVersion() + ")");
            currentUser = user;

            if (script != null) {
                runScript(script);
            } else {
                runConsole();
            }
            client.logout();
            out.println("Disconnected from server.");
        } catch (ClientException ce) {
            ce.printStackTrace();
        } catch (IOException ioe) {
            error("Could not load script file.");
        }
    }

    private void runScript(String script) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(script));
        String statement = null;

        try {
            while ((statement = reader.readLine()) != null) {
                out.println("Executing statement: " + statement);
                executeStatement(statement);
            }
        } catch (QuitShell e) {
            // do nothing
        } finally {
            reader.close();
        }
    }

    private void runConsole() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String statement = null;
        writePrompt();
        try {
            while ((statement = reader.readLine()) != null) {
                executeStatement(statement);
                writePrompt();
            }
            out.println(); // want newline before "Disconnected" message
        } catch (QuitShell e) {
            // do nothing
        } finally {
            reader.close();
        }
    }

    private void error(String message) {
        err.println("Error: " + message);
    }

    private void writePrompt() {
        out.print("$");
        out.flush();
    }

    private void executeStatement(String command) {
        try {
            Token[] arguments = tokenizeCommand(command);

            if (arguments.length >= 1) {
                Token arg1 = arguments[0];
                if (arg1 == Token.HELP) {
                    commandHelp(arguments);
                    return;
                } else if (arg1 == Token.USER) {
                    if (arguments.length < 2) {
                        error("invalid number of arguments.");
                        return;
                    }
                    Token arg2 = arguments[1];
                    if (arg2 == Token.LIST) {
                        commandUserList(arguments);
                    } else if (arg2 == Token.ADD) {
                        commandUserAdd(arguments);
                    } else if (arg2 == Token.REMOVE) {
                        commandUserRemove(arguments);
                    } else if (arg2 == Token.CHANGEPW) {
                        commandUserChangePassword(arguments);
                    }
                } else if (arg1 == Token.START) {
                    commandStartAll(arguments);
                } else if (arg1 == Token.STOP) {
                    commandStopAll(arguments);
                } else if (arg1 == Token.PAUSE) {
                    commandPauseAll(arguments);
                } else if (arg1 == Token.RESUME) {
                    commandResumeAll(arguments);
                } else if (arg1 == Token.DEPLOY) {
                    commandDeploy(arguments);
                } else if (arg1 == Token.EXPORTCFG) {
                    commandExportConfig(arguments);
                } else if (arg1 == Token.IMPORTCFG) {
                    commandImportConfig(arguments);
                } else if (arg1 == Token.IMPORT) {
                    commandImport(arguments);
                } else if (arg1 == Token.IMPORTSCRIPT) {
                    commandImportScript(arguments);
                } else if (arg1 == Token.EXPORTSCRIPT) {
                    commandExportScript(arguments);
                } else if (arg1 == Token.STATUS) {
                    commandStatus(arguments);
                } else if (arg1 == Token.EXPORT) {
                    commandExport(arguments);
                } else if (arg1 == Token.SHUTDOWN) {
                    commandShutdown(arguments);
                } else if (arg1 == Token.CHANNEL) {
                    String syntax = "invalid number of arguments. Syntax is: channel start|stop|pause|resume|stats|remove|enable|disable <id|name>, channel rename <id|name> newname, or channel list|stats";
                    if (arguments.length < 2) {
                        error(syntax);
                        return;
                    } else if (arguments.length < 3 && arguments[1] != Token.LIST && arguments[1] != Token.STATS) {
                        error(syntax);
                        return;
                    }

                    Token comm = arguments[1];

                    if (comm == Token.STATS && arguments.length < 3) {
                        commandAllChannelStats(arguments);
                    } else if (comm == Token.LIST) {
                        commandChannelList(arguments);
                    } else if (comm == Token.DISABLE) {
                        commandChannelDisable(arguments);
                    } else if (comm == Token.ENABLE) {
                        commandChannelEnable(arguments);
                    } else if (comm == Token.REMOVE) {
                        commandChannelRemove(arguments);
                    } else if (comm == Token.START) {
                        commandChannelStart(arguments);
                    } else if (comm == Token.STOP) {
                        commandChannelStop(arguments);
                    } else if (comm == Token.PAUSE) {
                        commandChannelPause(arguments);
                    } else if (comm == Token.RESUME) {
                        commandChannelResume(arguments);
                    } else if (comm == Token.STATS) {
                        commandChannelStats(arguments);
                    } else if (comm == Token.RENAME) {
                        commandChannelRename(arguments);
                    } else {
                        error("unknown channel command " + comm);
                    }
                } else if (arg1 == Token.CLEAR) {
                    commandClear(arguments);
                } else if (arg1 == Token.RESETSTATS) {
                    commandResetstats(arguments);
                } else if (arg1 == Token.DUMP) {
                    if (arguments.length >= 2) {
                        Token arg2 = arguments[1];

                        if (arg2 == Token.STATS) {
                            commandDumpStats(arguments);
                        } else if (arg2 == Token.EVENTS) {
                            commandDumpEvents(arguments);
                        } else {
                            error("unknown dump command: " + arg2);
                        }
                    } else {
                        error("missing dump commands.");
                    }
                } else if (arg1 == Token.QUIT) {
                    throw new QuitShell();
                } else {
                    error("unknown command: " + command);
                }
            }
        } catch (ClientException e) {
            e.printStackTrace(err);
        }
    }

    /** Split <code>command</code> into an array of tokens. */
    private Token[] tokenizeCommand(String command) {
        List<Token> tokens = new ArrayList<Token>();
        StringBuilder currentToken = null; // not in a token yet
        char[] chars = command.toCharArray();
        boolean inQuotes = false;
        for (int idx = 0; idx < chars.length; idx++) {
            char ch = chars[idx];
            if (currentToken == null) { // currently between tokens
                if (ch == ' ') {
                    // ignore spaces between tokens (including leading space)
                    continue;
                } else {
                    // start a new token
                    currentToken = new StringBuilder();
                }
            }

            if (inQuotes && ch != '"') {
                // add another char (possibly space) to the current token
                currentToken.append(ch);
            } else if (inQuotes && ch == '"') {
                // no longer in quotes: ignore the " char and switch modes
                inQuotes = false;
            } else if (!inQuotes && ch == '"') {
                // now in quotes: ignore the " char and switch modes
                inQuotes = true;
            } else if (!inQuotes && ch == ' ') {
                // end of current token
                addToken(tokens, currentToken);
                currentToken = null;
            } else if (!inQuotes && ch == '#') {
                // start of comment: stop tokenizing now (ie. treat it as end of
                // line)
                break;
            } else if (!inQuotes) {
                // any other char outside of quotes: just append to current
                // token
                currentToken.append(ch);
            } else {
                // impossible state because of the first two clauses above
                throw new IllegalStateException("impossible state in tokenizer: inQuotes=" + inQuotes + ", char=" + ch);
            }
        }
        addToken(tokens, currentToken);

        // out.println("token list: " + tokens);
        Token[] arguments = new Token[tokens.size()];
        tokens.toArray(arguments);
        return arguments;
    }

    private void addToken(List<Token> tokens, StringBuilder currentText) {
        if (currentText == null || currentText.length() == 0) {
            // empty or commented line
            return;
        }
        String text = currentText.toString();
        Token token = Token.getKeyword(text);
        if (token == null) {
            try {
                token = Token.intToken(text);
            } catch (NumberFormatException e) {
                token = Token.stringToken(text);
            }
        }
        tokens.add(token);
    }

    private void commandHelp(Token[] arguments) {
        out.println("Available Commands:");
        out.println("status\n\tReturns status of deployed channels\n");
        out.println("start\n\tStarts all Channels\n");
        out.println("stop\n\tStops all Channels\n");
        out.println("pause\n\tPauses all Channels\n");
        out.println("resume\n\tResumes all Channels\n");
        out.println("deploy [timeout]\n\tDeploys all Channels with optional timeout (in seconds)\n");
        out.println("import \"path\" [force]\n\tImports channel specified by <path>.  Optional 'force' overwrites existing channels.\n");
        out.println("export id|\"name\"|* \"path\"\n\tExports the specified channel to <path>\n");
        out.println("importcfg \"path\"\n\tImports configuration specified by <path>\n");
        out.println("exportcfg \"path\"\n\tExports the configuration to <path>\n");
        out.println("importscript Deploy|Preprocessor|Postprocessor|Shutdown \"path\"\n\tImports global script specified by <path>\n");
        out.println("exportscript Deploy|Preprocessor|Postprocessor|Shutdown \"path\"\n\tExports global script specified by <path>\n");
        out.println("channel start|stop|pause|resume|stats id|\"name\"|*\n\tPerforms specified channel action\n");
        out.println("channel remove|enable|disable id|\"name\"|*\n\tRemove, enable or disable specified channel\n");
        out.println("channel list\n\tLists all Channels\n");
        out.println("clear\n\tRemoves all messages from all Channels\n");
        out.println("resetstats\n\tRemoves all stats from all Channels\n");
        out.println("dump stats|events \"path\"\n\tDumps stats or events to specified file\n");
        out.println("user list\n\tReturns a list of the current users\n");
        out.println("user add username \"password\" \"name\" \"email\"\n\tAdds the specified user\n");
        out.println("user remove id|username\n\tRemoves the specified user\n");
        out.println("user changepw id|username \"newpassword\"\n\tChanges the specified user's password\n");
        out.println("shutdown\n\tShuts down the server\n");
        out.println("quit\n\tQuits Mirth Shell");
    }

    private void commandUserList(Token[] arguments) throws ClientException {
        List<User> users = client.getUser(null);
        out.println("ID\tUser Name\tName\t\t\tEmail");
        for (Iterator<User> iter = users.iterator(); iter.hasNext();) {
            User user = iter.next();
            out.println(user.getId() + "\t" + user.getUsername() + "\t\t" + user.getFirstName() + "\t\t" + user.getLastName() + "\t\t" + user.getOrganization() + "\t\t" + user.getEmail());
        }
    }

    private void commandUserAdd(Token[] arguments) throws ClientException {
        if (arguments.length < 8) {
            error("invalid number of arguments. Syntax is user add username \"password\" \"firstName\" \"lastName\" \"organization\" \"email\"");
            return;
        }
        String username = arguments[2].getText();
        if (username.length() < 1) {
            error("unable to add user: username too short.");
            return;
        }

        String password = arguments[3].getText();
        String firstName = arguments[4].getText();
        String lastName = arguments[5].getText();
        String organization = arguments[6].getText();
        String email = arguments[7].getText();
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setOrganization(organization);
        user.setEmail(email);

        List<User> users = client.getUser(null);
        for (Iterator<User> iter = users.iterator(); iter.hasNext();) {
            User luser = iter.next();
            if (luser.getUsername().equalsIgnoreCase(username)) {
                error("unable to add user: username in use.");
                return;
            }
        }

        try {
            client.updateUser(user, password);
            out.println("User \"" + username + "\" added successfully.");
        } catch (Exception e) {
            error("unable to add user \"" + username + "\": " + e);
        }
    }

    private void commandUserRemove(Token[] arguments) throws ClientException {
        if (arguments.length < 3) {
            error("invalid number of arguments. Syntax is user remove username|id");
            return;
        }
        String key = arguments[2].getText();
        if (key.equalsIgnoreCase(currentUser)) {
            error("cannot remove current user.");
            return;
        }
        List<User> users = client.getUser(null);
        for (Iterator<User> iter = users.iterator(); iter.hasNext();) {
            User user = iter.next();
            if (user.getId().toString().equalsIgnoreCase(key) || user.getUsername().equalsIgnoreCase(key)) {
                client.removeUser(user);
                out.println("User \"" + user.getUsername() + "\" successfully removed.");
                return;
            }
        }
    }

    private void commandUserChangePassword(Token[] arguments) throws ClientException {
        if (arguments.length < 4) {
            error("invalid number of arguments. Syntax is user changepw username|id \"newpassword\"");
            return;
        }
        String key = arguments[2].getText();
        String newPassword = arguments[3].getText();
        List<User> users = client.getUser(null);
        for (Iterator<User> iter = users.iterator(); iter.hasNext();) {
            User user = iter.next();
            if (user.getId().toString().equalsIgnoreCase(key) || user.getUsername().equalsIgnoreCase(key)) {
                client.updateUser(user, newPassword);
                out.println("User \"" + user.getUsername() + "\" password updated.");
                return;
            }
        }
    }

    // XXX isn't this the same as "channel start *"?
    private void commandStartAll(Token[] arguments) throws ClientException {
        for (ChannelStatus channel : client.getChannelStatusList()) {
            if (channel.getState().equals(State.STOPPED) || channel.getState().equals(State.PAUSED)) {
                if (channel.getState().equals(State.PAUSED)) {
                    client.resumeChannel(channel.getChannelId());
                    out.println("Channel " + channel.getName() + " Resumed");
                } else {
                    client.startChannel(channel.getChannelId());
                    out.println("Channel " + channel.getName() + " Started");
                }
            }
        }
    }

    // XXX isn't this the same as "channel stop *"?
    private void commandStopAll(Token[] arguments) throws ClientException {
        for (ChannelStatus channel : client.getChannelStatusList()) {
            if (channel.getState().equals(State.STARTED) || channel.getState().equals(State.PAUSED)) {
                client.stopChannel(channel.getChannelId());
                out.println("Channel " + channel.getName() + " Stopped");
            }
        }
    }

    // XXX isn't this the same as "channel pause *"?
    private void commandPauseAll(Token[] arguments) throws ClientException {
        for (ChannelStatus channel : client.getChannelStatusList()) {
            if (channel.getState().equals(State.STARTED)) {
                client.pauseChannel(channel.getChannelId());
                out.println("Channel " + channel.getName() + " Paused");
            }
        }
    }

    // XXX isn't this the same as "channel resume *"?
    private void commandResumeAll(Token[] arguments) throws ClientException {
        for (ChannelStatus channel : client.getChannelStatusList()) {
            if (channel.getState().equals(State.PAUSED)) {
                client.resumeChannel(channel.getChannelId());
                out.println("Channel " + channel.getName() + " Resumed");
            }
        }
    }

    private void commandDeploy(Token[] arguments) throws ClientException {
        out.println("Deploying Channels");
        List<Channel> channels = client.getChannel(null);

        boolean hasChannels = false;
        for (Iterator iter = channels.iterator(); iter.hasNext();) {
            Channel channel = (Channel) iter.next();
            if (channel.isEnabled()) {
                hasChannels = true;
                break;
            }
        }
        client.deployChannels();
        if (hasChannels) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            List<ChannelStatus> channelStatus = client.getChannelStatusList();
            int limit = 60; // 30 second limit
            if (arguments.length > 1 && arguments[1] instanceof IntToken) {
                limit = ((IntToken) arguments[1]).getValue() * 2; // multiply
                                                                    // by two
                                                                    // because
                                                                    // our sleep
                                                                    // is 500ms
            }
            while (channelStatus.size() == 0 && limit > 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                channelStatus = client.getChannelStatusList();
                limit--;
            }
            if (limit > 0) {
                out.println("Channels Deployed");
            } else {
                out.println("Deployment Timed out");
            }
        } else {
            out.println("No Channels to Deploy");
        }
    }

    private void commandExportConfig(Token[] arguments) throws ClientException {
        String path = arguments[1].getText();
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        try {
            ServerConfiguration configuration = client.getServerConfiguration();
            String backupDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            configuration.setDate(backupDate);

            File fXml = new File(path);
            out.println("Exporting Configuration");
            String configurationXML = serializer.toXML(configuration);
            writeFile(fXml, configurationXML, false);
        } catch (IOException e) {
            error("unable to write file " + path + ": " + e);
        }

        out.println("Configuration Export Complete.");
    }

    private void commandImportConfig(Token[] arguments) throws ClientException {
        String path = arguments[1].getText();
        File fXml = new File(path);
        if (!fXml.exists()) {
            error("" + path + " not found");
            return;
        } else if (!fXml.canRead()) {
            error("cannot read " + path);
            return;
        } else {
            ObjectXMLSerializer serializer = new ObjectXMLSerializer();
            try {
                client.setServerConfiguration((ServerConfiguration) serializer.fromXML(readFile(fXml)));
            } catch (IOException e) {
                error("cannot read " + path);
                e.printStackTrace();
                return;
            }

        }
        out.println("Configuration Import Complete.");
    }

    private void commandImport(Token[] arguments) throws ClientException {
        String path = arguments[1].getText();

        boolean force = false;
        if (arguments.length >= 3 && arguments[2] == Token.FORCE) {
            force = true;
        }

        File fXml = new File(path);
        if (!fXml.exists()) {
            error("" + path + " not found");
        } else if (!fXml.canRead()) {
            error("cannot read " + path);
        } else {
            doImportChannel(fXml, force);
        }
    }

    private void commandExportScript(Token[] arguments) throws ClientException {
        String name = arguments[1].getText();

        if (name.equals("deploy")) {
            name = "Deploy";
        } else if (name.equals("preprocessor")) {
            name = "Proprocessor";
        } else if (name.equals("postprocessor")) {
            name = "Postprocessor";
        } else if (name.equals("shutdown")) {
            name = "Shutdown";
        }

        String path = arguments[2].getText();

        try {
            Map<String, String> scripts = client.getGlobalScripts();
            String script = scripts.get(name);
            File fXml = new File(path);
            out.println("Exporting " + name + " script");
            writeFile(fXml, script, false);
        } catch (IOException e) {
            error("unable to write file " + path + ": " + e);
        }

        out.println("Script Export Complete.");
    }

    private void commandImportScript(Token[] arguments) throws ClientException {
        String name = arguments[1].getText();

        if (name.equals("deploy")) {
            name = "Deploy";
        } else if (name.equals("preprocessor")) {
            name = "Proprocessor";
        } else if (name.equals("postprocessor")) {
            name = "Postprocessor";
        } else if (name.equals("shutdown")) {
            name = "Shutdown";
        }

        String path = arguments[2].getText();

        File fXml = new File(path);

        if (!fXml.exists()) {
            error("" + path + " not found");
        } else if (!fXml.canRead()) {
            error("cannot read " + path);
        } else {
            doImportScript(name, fXml);
        }
        out.println(name + " script import complete");
    }

    private void commandStatus(Token[] arguments) throws ClientException {
        out.println("ID\t\t\t\t\tStatus\t\tName");
        List<ChannelStatus> channels = client.getChannelStatusList();
        for (Iterator<ChannelStatus> iter = channels.iterator(); iter.hasNext();) {
            ChannelStatus channel = iter.next();

            out.println(channel.getChannelId() + "\t" + channel.getState().toString() + "\t\t" + channel.getName());
        }
    }

    private void commandExport(Token[] arguments) throws ClientException {
        if (arguments.length < 3) {
            error("invalid number of arguments. Syntax is: export id|name|all \"path\"");
            return;
        }

        StringToken key = (StringToken) arguments[1];
        String path = arguments[2].getText();
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        List<Channel> channels = client.getChannel(null);
        if (key == Token.WILDCARD) {
            for (Iterator iter = channels.iterator(); iter.hasNext();) {
                try {
                    Channel channel = (Channel) iter.next();
                    File fXml = new File(path + channel.getName() + ".xml");
                    out.println("Exporting " + channel.getName());
                    String channelXML = serializer.toXML(channel);
                    writeFile(fXml, channelXML, false);
                } catch (IOException e) {
                    error("unable to write file " + path + ": " + e);
                }
            }
            out.println("Export Complete.");
            return;
        } else {
            File fXml = new File(path);

            for (Iterator iter = channels.iterator(); iter.hasNext();) {
                Channel channel = (Channel) iter.next();
                if (key.equalsIgnoreCase(channel.getName()) != key.equalsIgnoreCase(channel.getId())) {
                    out.println("Exporting " + channel.getName());
                    String channelXML = serializer.toXML(channel);
                    try {
                        writeFile(fXml, channelXML, false);
                    } catch (IOException e) {
                        error("unable to write file " + path + ": " + e);
                    }
                    out.println("Export Complete.");
                    return;
                }
            }
        }
    }

    private void commandAllChannelStats(Token[] arguments) throws ClientException {
        // out.println("Mirth Channel Statistics Dump: "
        // + (new Date()).toString() + "\n");
        out.println("Received\tFiltered\tSent\t\tError\t\tName");

        List<Channel> channels = client.getChannel(null);

        for (Iterator iter = channels.iterator(); iter.hasNext();) {
            Channel channel = (Channel) iter.next();
            ChannelStatistics stats = client.getStatistics(channel.getId());
            out.println(stats.getReceived() + "\t\t" + stats.getFiltered() + "\t\t" + stats.getSent() + "\t\t" + stats.getError() + "\t\t" + channel.getName());
        }
    }

    private void commandChannelList(Token[] arguments) throws ClientException {
        List<Channel> allChannels = client.getChannel(null);
        out.println("ID\t\t\t\t\tEnabled\t\tName");
        String enable = "";
        for (Iterator<Channel> iter = allChannels.iterator(); iter.hasNext();) {
            Channel channel = iter.next();
            if (channel.isEnabled()) {
                enable = "ENABLED";
            } else {
                enable = "DISABLED";
            }
            out.println(channel.getId() + "\t" + enable + "\t\t" + channel.getName());
        }
    }

    private void commandChannelDisable(Token[] arguments) throws ClientException {
        for (Channel channel : getMatchingChannels(arguments[2])) {
            if (channel.isEnabled()) {
                channel.setEnabled(false);
                client.updateChannel(channel, true);
                out.println("Channel '" + channel.getName() + "' Disabled");
            }
        }
    }

    private void commandChannelEnable(Token[] arguments) throws ClientException {
        for (Channel channel : getMatchingChannels(arguments[2])) {
            if (!channel.isEnabled()) {
                channel.setEnabled(true);
                client.updateChannel(channel, true);
                out.println("Channel '" + channel.getName() + "' Enabled");
            }
        }
    }

    private void commandChannelRemove(Token[] arguments) throws ClientException {
        for (Channel channel : getMatchingChannels(arguments[2])) {
            if (channel.isEnabled()) {
                channel.setEnabled(false);
            }
            client.removeChannel(channel);
            out.println("Channel '" + channel.getName() + "' Removed");
        }
    }

    private void commandChannelStart(Token[] arguments) throws ClientException {
        for (ChannelStatus channel : getMatchingChannelStatuses(arguments[2])) {
            if (channel.getState().equals(State.PAUSED) || channel.getState().equals(State.STOPPED)) {
                if (channel.getState().equals(State.PAUSED)) {
                    client.resumeChannel(channel.getChannelId());
                    out.println("Channel '" + channel.getName() + "' Resumed");
                } else {
                    client.startChannel(channel.getChannelId());
                    out.println("Channel '" + channel.getName() + "' Started");
                }
            }
        }
    }

    private void commandChannelStop(Token[] arguments) throws ClientException {
        for (ChannelStatus channel : getMatchingChannelStatuses(arguments[2])) {
            if (channel.getState().equals(State.PAUSED) || channel.getState().equals(State.STARTED)) {
                client.stopChannel(channel.getChannelId());
                out.println("Channel '" + channel.getName() + "' Stopped");
            }
        }
    }

    private void commandChannelPause(Token[] arguments) throws ClientException {
        for (ChannelStatus channel : getMatchingChannelStatuses(arguments[2])) {
            if (channel.getState().equals(State.STARTED)) {
                client.pauseChannel(channel.getChannelId());
                out.println("Channel '" + channel.getName() + "' Paused");
            }
        }
    }

    private void commandChannelResume(Token[] arguments) throws ClientException {
        for (ChannelStatus channel : getMatchingChannelStatuses(arguments[2])) {
            if (channel.getState().equals(State.PAUSED)) {
                client.resumeChannel(channel.getChannelId());
                out.println("Channel '" + channel.getName() + "' Resumed");
            }
        }
    }

    private void commandChannelStats(Token[] arguments) throws ClientException {
        for (ChannelStatus channel : getMatchingChannelStatuses(arguments[2])) {
            ChannelStatistics stats = client.getStatistics(channel.getChannelId());
            out.println("Channel Stats for " + channel.getName());
            out.println("Received: " + stats.getReceived());
            out.println("Filtered: " + stats.getFiltered());
            out.println("Sent: " + stats.getSent());
            out.println("Error: " + stats.getError());
        }
    }

    private void commandChannelRename(Token[] arguments) throws ClientException {
        for (Channel channel : getMatchingChannels(arguments[2])) {
            String oldName = channel.getName();
            channel.setName(arguments[3].getText());
            if (checkChannelName(channel.getName(), channel.getId())) {
                client.updateChannel(channel, true);
                out.println("Channel '" + oldName + "' renamed to '" + channel.getName() + "'");
            }
        }
    }

    private void commandShutdown(Token[] arguments) throws ClientException {
        client.shutdown();
        throw new QuitShell();
    }

    /**
     * Checks to see if the passed in channel id already exists
     */
    public boolean checkChannelId(String id) throws ClientException {
        for (Channel channel : client.getChannel(null)) {
            if (channel.getId().equalsIgnoreCase(id)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks to see if the passed in channel name already exists and is
     * formatted correctly
     */
    public boolean checkChannelName(String name, String id) throws ClientException {
        if (name.equals("")) {
            out.println("Channel name cannot be empty.");
            return false;
        }

        if (name.length() > 40) {
            out.println("Channel name cannot be longer than 40 characters.");
            return false;
        }

        // Following code copied from MirthFieldConstaints, must be the same to
        // check for valid channel names the same way.
        char[] chars = name.toCharArray();
        for (char c : chars) {
            int cVal = (int) c;
            if ((cVal < 65 || cVal > 90) && (cVal < 97 || cVal > 122) && (cVal != 32) && (cVal != 45) && (cVal != 95)) {
                try {
                    if (Double.isNaN(Double.parseDouble(c + ""))) {
                        out.println("Channel name cannot have special characters besides hyphen, underscore, and space.");
                        return false;
                    }
                } catch (Exception e) {
                    out.println("Channel name cannot have special characters besides hyphen, underscore, and space.");
                    return false;
                }
            }
        }

        for (Channel channel : client.getChannel(null)) {
            if (channel.getName().equalsIgnoreCase(name) && !channel.getId().equals(id)) {
                out.println("Channel \"" + name + "\" already exists.");
                return false;
            }
        }
        return true;
    }

    private List<Channel> getMatchingChannels(Token key) throws ClientException {
        List<Channel> result = new ArrayList();
        for (Channel channel : client.getChannel(null)) {
            if (matchesChannel(key, channel.getName(), channel.getId())) {
                result.add(channel);
            }

            // What if the key matches *two* channels, e.g. it's the ID of one
            // and
            // the name of another? Unlikely but possible...
            if (key != Token.WILDCARD)
                break;
        }
        return result;
    }

    // Yuck: this is nearly identical to getMatchingChannels(), but there does
    // not appear to be a way to go from Channel to ChannelStatus (or
    // vice-versa). If
    // there was, all channel methods could operate on a Channel object (or a
    // ChannelStatus
    // object), and we would only need one getMatching...() method.
    private List<ChannelStatus> getMatchingChannelStatuses(Token key) throws ClientException {
        List<ChannelStatus> result = new ArrayList();
        for (ChannelStatus status : client.getChannelStatusList()) {
            if (matchesChannel(key, status.getName(), status.getChannelId())) {
                result.add(status);
            }

            // Again, what if the key matches two channels?
            if (key != Token.WILDCARD)
                break;

        }
        return result;
    }

    private boolean matchesChannel(Token key, String name, String id) {
        if (key == Token.WILDCARD)
            return true;
        StringToken skey = (StringToken) key;
        return skey.equalsIgnoreCase(name) || skey.equalsIgnoreCase(id);
    }

    private void commandClear(Token[] arguments) throws ClientException {
        List<Channel> channels = client.getChannel(null);

        for (Iterator iter = channels.iterator(); iter.hasNext();) {
            Channel channel = (Channel) iter.next();
            client.clearMessages(channel.getId());
        }
    }

    private void commandResetstats(Token[] arguments) throws ClientException {
        List<Channel> channels = client.getChannel(null);

        for (Iterator iter = channels.iterator(); iter.hasNext();) {
            Channel channel = (Channel) iter.next();
            client.clearStatistics(channel.getId(), true, true, true, true, true, true);
        }
    }

    private void commandDumpEvents(Token[] arguments) throws ClientException {
        String dumpFilename = arguments[2].getText();
        dumpFilename = replaceValues(dumpFilename);

        StringBuilder builder = new StringBuilder();
        builder.append("Mirth Event Log Dump: " + (new Date()).toString() + "\n");
        builder.append("Id, Event, Date, Description, Level\n");

        File dumpFile = new File(dumpFilename);
        SystemEventListHandler eventListHandler = client.getSystemEventListHandler(new SystemEventFilter(), 20, false);
        try {
            List<SystemEvent> events = eventListHandler.getFirstPage();

            // create the new empty file.
            writeFile(dumpFile, "", false);

            while (events.size() != 0) {
                for (Iterator iter = events.iterator(); iter.hasNext();) {
                    SystemEvent event = (SystemEvent) iter.next();
                    builder.append(event.getId() + ", " + event.getEvent() + ", " + formatDate(event.getDate()) + ", " + event.getDescription() + ", " + event.getLevel() + "\n");
                }

                writeFile(dumpFile, builder.toString(), true);
                builder.delete(0, builder.length());

                events = eventListHandler.getNextPage();
            }

        } catch (ListHandlerException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            error("Could not write file: " + dumpFile.getAbsolutePath());
        }

        out.println("Events written to " + dumpFilename);
    }

    private void commandDumpStats(Token[] arguments) throws ClientException {
        String dumpFilename = arguments[2].getText();
        dumpFilename = replaceValues(dumpFilename);

        StringBuilder builder = new StringBuilder();
        builder.append("Mirth Channel Statistics Dump: " + (new Date()).toString() + "\n");
        builder.append("Name, Received, Filtered, Sent, Error\n");

        List<Channel> channels = client.getChannel(null);

        for (Iterator iter = channels.iterator(); iter.hasNext();) {
            Channel channel = (Channel) iter.next();
            ChannelStatistics stats = client.getStatistics(channel.getId());
            builder.append(channel.getName() + ", " + stats.getReceived() + ", " + stats.getFiltered() + ", " + stats.getSent() + ", " + stats.getError() + "\n");
        }

        File dumpFile = new File(dumpFilename);

        try {
            writeFile(dumpFile, builder.toString(), false);
            out.println("Stats written to " + dumpFilename);
        } catch (IOException e) {
            error("Could not write file: " + dumpFile.getAbsolutePath());
        }
    }

    public static String readFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder contents = new StringBuilder();
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                contents.append(line + "\n");
            }
        } finally {
            reader.close();
        }

        return contents.toString();
    }

    private void doImportScript(String name, File scriptFile) throws ClientException {
        String script = "";

        try {
            script = readFile(scriptFile);
        } catch (Exception e) {
            error("invalid script file.");
            return;
        }

        Map<String, String> scriptMap = new HashMap<String, String>();
        scriptMap.put(name, script);
        client.setGlobalScripts(scriptMap);
    }

    private void doImportChannel(File importFile, boolean force) throws ClientException {
        String channelXML = "";

        try {
            channelXML = ImportConverter.convertChannelFile(importFile);
        } catch (Exception e1) {
            error("invalid channel file.");
            return;
        }

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        Channel importChannel;

        try {
            importChannel = (Channel) serializer.fromXML(channelXML.replaceAll("\\&\\#x0D;\\n", "\n").replaceAll("\\&\\#x0D;", "\n"));
            PropertyVerifier.checkChannelProperties(importChannel);
            PropertyVerifier.checkConnectorProperties(importChannel, client.getConnectorMetaData());

        } catch (Exception e) {
            error("invalid channel file.");
            return;
        }

        String channelName = importChannel.getName();
        String tempId = client.getGuid();

        // Check to see that the channel name doesn't already exist.
        if (!checkChannelName(channelName, importChannel.getId())) {
            if (!force) {
                importChannel.setRevision(0);
                importChannel.setName(tempId);
                importChannel.setId(tempId);
            } else {
                for (Channel channel : client.getChannel(null)) {
                    if (channel.getName().equalsIgnoreCase(channelName)) {
                        importChannel.setId(channel.getId());
                    }
                }
            }
        }
        // If the channel name didn't already exist, make sure the id doesn't
        // exist either.
        else if (!checkChannelId(importChannel.getId())) {
            importChannel.setId(tempId);
        }

        importChannel.setVersion(client.getVersion());
        client.updateChannel(importChannel, true);
        out.println("Channel '" + channelName + "' imported successfully.");
    }

    private String replaceValues(String source) {
        source = source.replaceAll("\\$\\{date\\}", getTimeStamp());
        return source;
    }

    private void writeFile(File file, String data, boolean append) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));

        try {
            writer.write(data);
            writer.flush();
        } finally {
            writer.close();
        }
    }

    private String getTimeStamp() {
        Date currentTime = new Date();
        return formatter.format(currentTime);
    }

    private String formatDate(Calendar date) {
        return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS:%1$tL", date);
    }
}