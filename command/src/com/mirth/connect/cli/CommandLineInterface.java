/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.ListHandlerException;
import com.mirth.connect.client.core.PaginatedEventList;
import com.mirth.connect.client.core.PaginatedMessageList;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.InvalidChannel;
import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.MessageImportResult;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.User;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.util.MessageExporter;
import com.mirth.connect.util.MessageImporter;
import com.mirth.connect.util.MessageImporter.MessageImportException;
import com.mirth.connect.util.MessageImporter.MessageImportInvalidPathException;
import com.mirth.connect.util.messagewriter.MessageWriter;
import com.mirth.connect.util.messagewriter.MessageWriterException;
import com.mirth.connect.util.messagewriter.MessageWriterFactory;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class CommandLineInterface {
    private String DEFAULT_CHARSET = "UTF-8";
    private Client client;
    private boolean debug;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy_HH-mm-ss.SS");
    private String currentUser = new String();
    private PrintWriter out;
    private PrintWriter err;

    public CommandLineInterface(String[] args) {
        out = new PrintWriter(System.out, true);
        err = new PrintWriter(System.out, true);
        run(args);
    }

    public static void main(String[] args) {
        System.setProperty("log4j.configuration", "log4j-cli.properties");
        new CommandLineInterface(args);
    }

    @SuppressWarnings("static-access")
    private void run(String[] args) {
        Option serverOption = OptionBuilder.withArgName("address").hasArg().withDescription("server address").create("a");
        Option userOption = OptionBuilder.withArgName("user").hasArg().withDescription("user login").create("u");
        Option passwordOption = OptionBuilder.withArgName("password").hasArg().withDescription("user password").create("p");
        Option scriptOption = OptionBuilder.withArgName("script").hasArg().withDescription("script file").create("s");
        Option versionOption = OptionBuilder.withArgName("version").hasArg().withDescription("version").create("v");
        Option configOption = OptionBuilder.withArgName("config file").hasArg().withDescription("path to default configuration [default: mirth-cli-config.properties]").create("c");
        Option helpOption = new Option("h", "help");
        Option debugOption = new Option("d", "debug");

        Options options = new Options();
        options.addOption(configOption);
        options.addOption(serverOption);
        options.addOption(userOption);
        options.addOption(passwordOption);
        options.addOption(scriptOption);
        options.addOption(versionOption);
        options.addOption(helpOption);
        options.addOption(debugOption);

        CommandLineParser parser = new GnuParser();

        try {
            CommandLine line = parser.parse(options, args);

            // Bail out early if they just want help
            if (line.hasOption("h")) {
                new HelpFormatter().printHelp("Shell", options);
                System.exit(0);
            }

            Properties configDefaults = new Properties();
            try {
                configDefaults.load(new FileInputStream(line.getOptionValue("c", "conf" + File.separator + "mirth-cli-config.properties")));
            } catch (IOException e) {
                // Only error out if they tried to load the config
                if (line.hasOption("c")) {
                    error("We could not find the file: " + line.getOptionValue("c"), null);
                    System.exit(2);
                }
            }

            String server = line.getOptionValue("a", configDefaults.getProperty("address"));
            String user = line.getOptionValue("u", configDefaults.getProperty("user"));
            String password = line.getOptionValue("p", configDefaults.getProperty("password"));
            String version = line.getOptionValue("v", configDefaults.getProperty("version"));
            String script = line.getOptionValue("s", configDefaults.getProperty("script"));

            if ((server != null) && (user != null) && (password != null) && (version != null)) {
                runShell(server, user, password, version, script, line.hasOption("d"));
            } else {
                new HelpFormatter().printHelp("Shell", options);
                error("all of address, user, password, and version options must be supplied as arguments or in the default configuration file", null);
                System.exit(2);
            }
        } catch (ParseException e) {
            error("Could not parse input arguments.", e);
            System.exit(2);
        }
    }

    private void runShell(String server, String user, String password, String version, String script, boolean debug) {
        try {
            client = new Client(server);
            this.debug = debug;

            if (client.login(user, password, version).getStatus() != LoginStatus.Status.SUCCESS) {
                error("Could not login to server.", null);
                return;
            }

            String serverVersion = client.getVersion();
            try {
                ObjectXMLSerializer.getInstance().init(serverVersion);
            } catch (Exception e) {
            }

            out.println("Connected to Mirth Connect server @ " + server + " (" + serverVersion + ")");
            currentUser = user;

            if (script != null) {
                runScript(script);
            } else {
                runConsole();
            }
            client.cleanup();
            client.logout();
            out.println("Disconnected from server.");
        } catch (ClientException ce) {
            ce.printStackTrace();
        } catch (IOException ioe) {
            error("Could not load script file.", ioe);
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
        } catch (Quit e) {
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
        } catch (Quit e) {
            // do nothing
        } finally {
            reader.close();
        }
    }

    private void error(String message, Throwable t) {
        err.println("Error: " + message);

        if ((t != null) && debug) {
            err.println(ExceptionUtils.getStackTrace(t));
        }
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
                        error("invalid number of arguments.", null);
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
                } else if (arg1 == Token.DEPLOY) {
                    commandDeploy(arguments);
                } else if (arg1 == Token.EXPORTCFG) {
                    commandExportConfig(arguments);
                } else if (arg1 == Token.IMPORTCFG) {
                    commandImportConfig(arguments);
                } else if (arg1 == Token.IMPORT) {
                    commandImport(arguments);
                } else if (arg1 == Token.IMPORTALERTS) {
                    commandImportAlerts(arguments);
                } else if (arg1 == Token.EXPORTALERTS) {
                    commandExportAlerts(arguments);
                } else if (arg1 == Token.IMPORTSCRIPTS) {
                    commandImportScripts(arguments);
                } else if (arg1 == Token.EXPORTSCRIPTS) {
                    commandExportScripts(arguments);
                } else if (arg1 == Token.IMPORTCODETEMPLATES) {
                    commandImportCodeTemplates(arguments);
                } else if (arg1 == Token.EXPORTCODETEMPLATES) {
                    commandExportCodeTemplates(arguments);
                } else if (arg1 == Token.IMPORTMESSAGES) {
                    commandImportMessages(arguments);
                } else if (arg1 == Token.EXPORTMESSAGES) {
                    commandExportMessages(arguments);
                } else if (arg1 == Token.STATUS) {
                    commandStatus(arguments);
                } else if (arg1 == Token.EXPORT) {
                    commandExport(arguments);
                } else if (arg1 == Token.CHANNEL) {
                    String syntax = "invalid number of arguments. Syntax is: channel start|stop|pause|resume|stats|remove|enable|disable <id|name>, channel rename <id|name> newname, or channel list|stats";
                    if (arguments.length < 2) {
                        error(syntax, null);
                        return;
                    } else if (arguments.length < 3 && arguments[1] != Token.LIST && arguments[1] != Token.STATS) {
                        error(syntax, null);
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
                    } else if (comm == Token.DEPLOY) {
                        commandChannelDeploy(arguments);
                    } else if (comm == Token.UNDEPLOY) {
                        commandChannelUndeploy(arguments);
                    } else {
                        error("unknown channel command " + comm, null);
                    }
                } else if (arg1 == Token.CLEARALLMESSAGES) {
                    commandClearAllMessages(arguments);
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
                            error("unknown dump command: " + arg2, null);
                        }
                    } else {
                        error("missing dump commands.", null);
                    }
                } else if (arg1 == Token.QUIT) {
                    throw new Quit();
                } else {
                    error("unknown command: " + command, null);
                }
            }
        } catch (ClientException e) {
            e.printStackTrace(err);
        }
    }

    private boolean hasInvalidNumberOfArguments(Token[] arguments, int expected) {
        if ((arguments.length - 1) < expected) {
            error("invalid number of arguments.", null);
            return true;
        }

        return false;
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
        if (currentText == null || StringUtils.isEmpty(currentText.toString())) {
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
        out.println("deploy [timeout]\n\tDeploys all Channels with optional timeout (in seconds)\n");
        out.println("import \"path\" [force]\n\tImports channel specified by <path>.  Optional 'force' overwrites existing channels.\n");
        out.println("export id|\"name\"|* \"path\"\n\tExports the specified channel to <path>\n");
        out.println("importcfg \"path\"\n\tImports configuration specified by <path>\n");
        out.println("exportcfg \"path\"\n\tExports the configuration to <path>\n");
        out.println("importalert \"path\" [force]\n\tImports alert specified by <path>.  Optional 'force' overwrites existing alerts.\n");
        out.println("exportalert id|\"name\"|* \"path\"\n\tExports the specified alert to <path>\n");
        out.println("importscripts \"path\"\n\tImports global script specified by <path>\n");
        out.println("exportscripts \"path\"\n\tExports global script to <path>\n");
        out.println("importcodetemplates \"path\"\n\tImports code templates specified by <path>\n");
        out.println("exportcodetemplates \"path\"\n\tExports code templates to <path>\n");
        out.println("importmessages \"path\" id\n\tImports messages specified by <path> into the channel specified by <id>\n");
        out.println("exportmessages \"path/file-pattern\" id [xml|raw|processedraw|transformed|encoded|response] [pageSize]\n\tExports all messages for channel specified by <id> to <path>\n");
        out.println("channel undeploy|deploy|start|stop|pause|resume|stats id|\"name\"|*\n\tPerforms specified channel action\n");
        out.println("channel remove|enable|disable id|\"name\"|*\n\tRemove, enable or disable specified channel\n");
        out.println("channel list\n\tLists all Channels\n");
        out.println("clearallmessages\n\tRemoves all messages from all Channels (running channels will be restarted)\n");
        out.println("resetstats [lifetime]\n\tRemoves all stats from all Channels. Optional 'lifetime' includes resetting lifetime stats.\n");
        out.println("dump stats|events \"path\"\n\tDumps stats or events to specified file\n");
        out.println("user list\n\tReturns a list of the current users\n");
        out.println("user add username \"password\" \"firstName\" \"lastName\" \"organization\" \"email\"\n\tAdds the specified user\n");
        out.println("user remove id|username\n\tRemoves the specified user\n");
        out.println("user changepw id|username \"newpassword\"\n\tChanges the specified user's password\n");
        out.println("quit\n\tQuits Mirth Connect Shell");
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
            error("invalid number of arguments. Syntax is user add username \"password\" \"firstName\" \"lastName\" \"organization\" \"email\"", null);
            return;
        }
        String username = arguments[2].getText();
        if (username.length() < 1) {
            error("unable to add user: username too short.", null);
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
                error("unable to add user: username in use.", null);
                return;
            }
        }

        try {
            List<String> responses = client.checkOrUpdateUserPassword(user, password);
            if (responses != null) {
                for (String response : responses) {
                    out.println(response);
                }
                return;
            }
            client.updateUser(user);
            // Get the new user object that contains the user id
            User newUser = client.getUser(user).get(0);
            responses = client.checkOrUpdateUserPassword(newUser, password);

            if (responses != null) {
                System.out.println("User \"" + username + "\" has been created but the password could not be set:");
                for (String response : responses) {
                    out.println(response);
                }
            } else {
                out.println("User \"" + username + "\" added successfully.");
            }
        } catch (Exception e) {
            error("unable to add user \"" + username + "\": " + e, e);
        }
    }

    private void commandUserRemove(Token[] arguments) throws ClientException {
        if (arguments.length < 3) {
            error("invalid number of arguments. Syntax is user remove username|id", null);
            return;
        }
        String key = arguments[2].getText();
        if (key.equalsIgnoreCase(currentUser)) {
            error("cannot remove current user.", null);
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
            error("invalid number of arguments. Syntax is user changepw username|id \"newpassword\"", null);
            return;
        }
        String key = arguments[2].getText();
        String newPassword = arguments[3].getText();
        List<User> users = client.getUser(null);
        for (Iterator<User> iter = users.iterator(); iter.hasNext();) {
            User user = iter.next();
            if (user.getId().toString().equalsIgnoreCase(key) || user.getUsername().equalsIgnoreCase(key)) {
                List<String> responses = client.checkOrUpdateUserPassword(user, newPassword);
                if (responses != null) {
                    for (String response : responses) {
                        out.println(response);
                    }
                } else {
                    out.println("User \"" + user.getUsername() + "\" password updated.");
                }
                return;
            }
        }
    }

    private void commandDeploy(Token[] arguments) throws ClientException {
        out.println("Deploying Channels");
        List<Channel> channels = client.getChannels(null);

        boolean hasChannels = false;
        for (Channel channel : channels) {
            if (channel.isEnabled()) {
                hasChannels = true;
                break;
            }
        }

        client.redeployAllChannels();

        if (hasChannels) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<DashboardStatus> channelStatus = client.getChannelStatusList();
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
        if (hasInvalidNumberOfArguments(arguments, 1)) {
            return;
        }

        String path = arguments[1].getText();
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();

        try {
            ServerConfiguration configuration = client.getServerConfiguration();
            String backupDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            configuration.setDate(backupDate);

            File fXml = new File(path);
            out.println("Exporting Configuration");
            String configurationXML = serializer.serialize(configuration);
            FileUtils.writeStringToFile(fXml, configurationXML);
        } catch (IOException e) {
            error("unable to write file " + path + ": " + e, e);
        }

        out.println("Configuration Export Complete.");
    }

    private void commandImportConfig(Token[] arguments) throws ClientException {
        if (hasInvalidNumberOfArguments(arguments, 1)) {
            return;
        }

        String path = arguments[1].getText();
        File fXml = new File(path);
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();

        try {
            client.setServerConfiguration(serializer.deserialize(FileUtils.readFileToString(fXml), ServerConfiguration.class));
        } catch (IOException e) {
            error("cannot read " + path, e);
            return;
        }

        out.println("Configuration Import Complete.");
    }

    private void commandImport(Token[] arguments) throws ClientException {
        if (hasInvalidNumberOfArguments(arguments, 1)) {
            return;
        }

        String path = arguments[1].getText();

        boolean force = false;
        if (arguments.length >= 3 && arguments[2] == Token.FORCE) {
            force = true;
        }

        File fXml = new File(path);
        doImportChannel(fXml, force);
    }

    private void commandImportAlerts(Token[] arguments) throws ClientException {
        if (hasInvalidNumberOfArguments(arguments, 1)) {
            return;
        }

        String path = arguments[1].getText();

        boolean force = false;
        if (arguments.length >= 3 && arguments[2] == Token.FORCE) {
            force = true;
        }

        File fXml = new File(path);
        doImportAlert(fXml, force);
    }

    private void commandExportAlerts(Token[] arguments) throws ClientException {
        if (arguments.length < 3) {
            error("invalid number of arguments. Syntax is: export id|name|* \"path\"", null);
            return;
        }

        Token key = arguments[1];
        String path = arguments[2].getText();
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        List<AlertModel> alerts = client.getAlert(null);
        if (key == Token.WILDCARD) {
            for (AlertModel alert : alerts) {
                try {
                    File fXml = new File(path + alert.getName() + ".xml");
                    out.println("Exporting " + alert.getName());
                    String alertXML = serializer.serialize(alert);
                    FileUtils.writeStringToFile(fXml, alertXML);
                } catch (IOException e) {
                    error("unable to write file " + path + ": " + e, e);
                }
            }
            out.println("Export Complete.");
            return;
        } else {
            File fXml = new File(path);
            StringToken skey = Token.stringToken(key.getText());

            for (AlertModel alert : alerts) {
                if (skey.equalsIgnoreCase(alert.getName()) != skey.equalsIgnoreCase(alert.getId())) {
                    out.println("Exporting " + alert.getName());
                    String alertXML = serializer.serialize(alert);
                    try {
                        FileUtils.writeStringToFile(fXml, alertXML);
                    } catch (IOException e) {
                        error("unable to write file " + path + ": " + e, e);
                    }
                    out.println("Export Complete.");
                    return;
                }
            }
        }
    }

    private void commandExportScripts(Token[] arguments) throws ClientException {
        if (hasInvalidNumberOfArguments(arguments, 1)) {
            return;
        }

        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        String path = arguments[1].getText();
        File fXml = new File(path);

        try {
            String scriptsXml = serializer.serialize(client.getGlobalScripts());
            out.println("Exporting scripts");
            FileUtils.writeStringToFile(fXml, scriptsXml);
        } catch (IOException e) {
            error("unable to write file " + path + ": " + e, e);
        }

        out.println("Script Export Complete.");
    }

    private void commandImportScripts(Token[] arguments) throws ClientException {
        if (hasInvalidNumberOfArguments(arguments, 1)) {
            return;
        }

        String path = arguments[1].getText();
        File fXml = new File(path);
        doImportScript(fXml);
        out.println("Scripts Import Complete");
    }

    private void commandExportCodeTemplates(Token[] arguments) throws ClientException {
        if (hasInvalidNumberOfArguments(arguments, 1)) {
            return;
        }

        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        String path = arguments[1].getText();

        try {
            List<CodeTemplate> codeTemplates = client.getCodeTemplate(null);
            File fXml = new File(path);
            out.println("Exporting code templates");
            String codeTemplatesXml = serializer.serialize(codeTemplates);
            FileUtils.writeStringToFile(fXml, codeTemplatesXml);
        } catch (IOException e) {
            error("unable to write file " + path + ": " + e, e);
        }

        out.println("Code Templates Export Complete.");
    }

    private void commandImportCodeTemplates(Token[] arguments) throws ClientException {
        if (hasInvalidNumberOfArguments(arguments, 1)) {
            return;
        }

        String path = arguments[1].getText();
        File fXml = new File(path);

        try {
            List<CodeTemplate> codeTemplates = ObjectXMLSerializer.getInstance().deserializeList(FileUtils.readFileToString(fXml), CodeTemplate.class);
            removeInvalidItems(codeTemplates, CodeTemplate.class);
            client.updateCodeTemplates(codeTemplates);

            if (codeTemplates.size() > 0) {
                out.println("Code Templates Import Complete");
            }
        } catch (IOException e) {
            error("cannot read " + path, e);
        } catch (SerializerException e) {
            error("invalid file: " + path, e);
        }
    }

    private void commandImportMessages(Token[] arguments) {
        if (hasInvalidNumberOfArguments(arguments, 2)) {
            return;
        }

        String path = arguments[1].getText();
        final String channelId = arguments[2].getText();

        MessageWriter importer = new MessageWriter() {
            @Override
            public boolean write(Message message) throws MessageWriterException {
                try {
                    client.importMessage(channelId, message);
                } catch (ClientException e) {
                    throw new MessageWriterException(e);
                }

                return true;
            }

            @Override
            public void close() throws MessageWriterException {}
        };

        try {
            MessageImportResult result = new MessageImporter().importMessages(path, true, importer, new File(".").getAbsolutePath());
            out.println(result.getSuccessCount() + " out of " + result.getTotalCount() + " messages imported successfully.");
        } catch (InterruptedException e) {
            error("Message import was interrupted.", null);
        } catch (MessageImportException e) {
            error("An error occurred while attempting to import messages", e);
        } catch (MessageImportInvalidPathException e) {
            error(e.getMessage(), e);
        }
    }

    private void commandExportMessages(Token[] arguments) {
        if (hasInvalidNumberOfArguments(arguments, 2)) {
            return;
        }

        // file path
        String path = arguments[1].getText();
        File fXml = new File(path);

        // message filter
        MessageFilter filter = new MessageFilter();
        String channelId = arguments[2].getText();

        // export mode
        ContentType contentType = null;

        if (arguments.length >= 4) {
            String modeArg = arguments[3].getText();

            if (StringUtils.equals(modeArg, "raw")) {
                contentType = ContentType.RAW;
            } else if (StringUtils.equals(modeArg, "processedraw")) {
                contentType = ContentType.PROCESSED_RAW;
            } else if (StringUtils.equals(modeArg, "transformed")) {
                contentType = ContentType.TRANSFORMED;
            } else if (StringUtils.equals(modeArg, "encoded")) {
                contentType = ContentType.ENCODED;
            } else if (StringUtils.equals(modeArg, "sent")) {
                contentType = ContentType.SENT;
            } else if (StringUtils.equals(modeArg, "response")) {
                contentType = ContentType.RESPONSE;
            } else if (StringUtils.equals(modeArg, "responsetransformed")) {
                contentType = ContentType.RESPONSE_TRANSFORMED;
            } else if (StringUtils.equals(modeArg, "processedresponse")) {
                contentType = ContentType.PROCESSED_RESPONSE;
            }
        }

        // page size
        int pageSize = 100;

        if (arguments.length == 5) {
            pageSize = NumberUtils.toInt(arguments[4].getText());
        }

        int messageCount = 0;

        try {
            out.println("Exporting messages to file: " + fXml.getPath());

            PaginatedMessageList messageList = new PaginatedMessageList();
            messageList.setChannelId(channelId);
            messageList.setClient(client);
            messageList.setIncludeContent(true);
            messageList.setMessageFilter(filter);
            messageList.setPageSize(pageSize);

            MessageWriterOptions writerOptions = new MessageWriterOptions();
            writerOptions.setBaseFolder(new File(".").getPath());
            writerOptions.setContentType(contentType);
            writerOptions.setDestinationContent(false);
            writerOptions.setEncrypt(false);
            writerOptions.setRootFolder(FilenameUtils.getFullPath(fXml.getAbsolutePath()));
            writerOptions.setFilePattern(FilenameUtils.getName(fXml.getAbsolutePath()));
            writerOptions.setArchiveFormat(null);
            writerOptions.setCompressFormat(null);

            MessageWriter messageWriter = MessageWriterFactory.getInstance().getMessageWriter(writerOptions, client.getEncryptor());

            messageCount = new MessageExporter().exportMessages(messageList, messageWriter);
            messageWriter.close();
        } catch (Exception e) {
            Throwable cause = ExceptionUtils.getRootCause(e);
            error("unable to write file " + path + ": " + cause, cause);
        }

        out.println("Messages Export Complete. " + messageCount + " Messages Exported.");
    }

    private void commandStatus(Token[] arguments) throws ClientException {
        out.println("ID\t\t\t\t\tStatus\t\tName");
        List<DashboardStatus> channels = client.getChannelStatusList();
        for (Iterator<DashboardStatus> iter = channels.iterator(); iter.hasNext();) {
            DashboardStatus channel = iter.next();

            out.println(channel.getChannelId() + "\t" + channel.getState().toString() + "\t\t" + channel.getName());
        }
    }

    private void commandExport(Token[] arguments) throws ClientException {
        if (arguments.length < 3) {
            error("invalid number of arguments. Syntax is: export id|name|* \"path\"", null);
            return;
        }

        Token key = arguments[1];
        String path = arguments[2].getText();
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        List<Channel> channels = client.getChannels(null);
        if (key == Token.WILDCARD) {
            for (Channel channel : channels) {
                try {
                    File fXml = new File(path + channel.getName() + ".xml");
                    out.println("Exporting " + channel.getName());
                    String channelXML = serializer.serialize(channel);
                    FileUtils.writeStringToFile(fXml, channelXML);
                } catch (IOException e) {
                    error("unable to write file " + path + ": " + e, e);
                }
            }
            out.println("Export Complete.");
            return;
        } else {
            File fXml = new File(path);
            StringToken skey = Token.stringToken(key.getText());

            for (Channel channel : channels) {
                if (skey.equalsIgnoreCase(channel.getName()) != skey.equalsIgnoreCase(channel.getId())) {
                    out.println("Exporting " + channel.getName());
                    String channelXML = serializer.serialize(channel);
                    try {
                        FileUtils.writeStringToFile(fXml, channelXML);
                    } catch (IOException e) {
                        error("unable to write file " + path + ": " + e, e);
                    }
                    out.println("Export Complete.");
                    return;
                }
            }
        }
    }

    private void commandAllChannelStats(Token[] arguments) throws ClientException {
        out.println("Received\tFiltered\tQueued\t\tSent\t\tErrored\t\tName");

        List<DashboardStatus> channelStatuses = client.getChannelStatusList();

        for (DashboardStatus channelStatus : channelStatuses) {
            ChannelStatistics stats = client.getStatistics(channelStatus.getChannelId());
            out.println(stats.getReceived() + "\t\t" + stats.getFiltered() + "\t\t" + channelStatus.getQueued() + "\t\t" + stats.getSent() + "\t\t" + stats.getError() + "\t\t" + channelStatus.getName());
        }
    }

    private void commandChannelList(Token[] arguments) throws ClientException {
        List<Channel> allChannels = client.getChannels(null);
        out.println("ID\t\t\t\t\tEnabled\t\tName");
        String enable = "";
        for (Iterator<Channel> iter = allChannels.iterator(); iter.hasNext();) {
            Channel channel = iter.next();
            if (channel.isEnabled()) {
                enable = "YES";
            } else {
                enable = "NO";
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
            if (!(channel instanceof InvalidChannel) && !channel.isEnabled()) {
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
        for (DashboardStatus channel : getMatchingChannelStatuses(arguments[2])) {
            if (channel.getState().equals(DeployedState.PAUSED) || channel.getState().equals(DeployedState.STOPPED)) {
                if (channel.getState().equals(DeployedState.PAUSED)) {
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
        for (DashboardStatus channel : getMatchingChannelStatuses(arguments[2])) {
            if (channel.getState().equals(DeployedState.PAUSED) || channel.getState().equals(DeployedState.STARTED)) {
                client.stopChannel(channel.getChannelId());
                out.println("Channel '" + channel.getName() + "' Stopped");
            }
        }
    }

    private void commandChannelPause(Token[] arguments) throws ClientException {
        for (DashboardStatus channel : getMatchingChannelStatuses(arguments[2])) {
            if (channel.getState().equals(DeployedState.STARTED)) {
                client.pauseChannel(channel.getChannelId());
                out.println("Channel '" + channel.getName() + "' Paused");
            }
        }
    }

    private void commandChannelResume(Token[] arguments) throws ClientException {
        for (DashboardStatus channel : getMatchingChannelStatuses(arguments[2])) {
            if (channel.getState().equals(DeployedState.PAUSED)) {
                client.resumeChannel(channel.getChannelId());
                out.println("Channel '" + channel.getName() + "' Resumed");
            }
        }
    }

    private void commandChannelStats(Token[] arguments) throws ClientException {
        for (DashboardStatus channel : getMatchingChannelStatuses(arguments[2])) {
            ChannelStatistics stats = client.getStatistics(channel.getChannelId());
            out.println("Channel Stats for " + channel.getName());
            out.println("Received: " + stats.getReceived());
            out.println("Filtered: " + stats.getFiltered());
            out.println("Queued: " + channel.getQueued());
            out.println("Sent: " + stats.getSent());
            out.println("Errored: " + stats.getError());
        }
    }

    private void commandChannelRename(Token[] arguments) throws ClientException {
        for (Channel channel : getMatchingChannels(arguments[2])) {
            if (!(channel instanceof InvalidChannel)) {
                String oldName = channel.getName();
                channel.setName(arguments[3].getText());
                if (checkChannelName(channel.getName(), channel.getId())) {
                    client.updateChannel(channel, true);
                    out.println("Channel '" + oldName + "' renamed to '" + channel.getName() + "'");
                }
            }
        }
    }

    private void commandChannelDeploy(Token[] arguments) throws ClientException {
        Set<String> channelIds = new LinkedHashSet<String>();

        for (Channel channel : getMatchingChannels(arguments[2])) {
            channelIds.add(channel.getId());
        }

        client.deployChannels(channelIds);
    }

    private void commandChannelUndeploy(Token[] arguments) throws ClientException {
        Set<String> channelIds = new LinkedHashSet<String>();

        for (Channel channel : getMatchingChannels(arguments[2])) {
            channelIds.add(channel.getId());
        }

        client.undeployChannels(channelIds);
    }

    /**
     * Checks to see if the passed in channel id already exists
     */
    public boolean checkChannelId(String id) throws ClientException {
        for (Channel channel : client.getChannels(null)) {
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
        if (StringUtils.isEmpty(name)) {
            out.println("Channel name cannot be empty.");
            return false;
        } else if (name.length() > 40) {
            out.println("Channel name cannot be longer than 40 characters.");
            return false;
        }

        Pattern alphaNumericPattern = Pattern.compile("^[a-zA-Z_0-9\\-\\s]*$");
        Matcher matcher = alphaNumericPattern.matcher(name);

        if (!matcher.find()) {
            out.println("Channel name cannot have special characters besides hyphen, underscore, and space.");
            return false;
        }

        for (Channel channel : client.getChannels(null)) {
            if (channel.getName().equalsIgnoreCase(name) && !channel.getId().equals(id)) {
                out.println("Channel \"" + name + "\" already exists.");
                return false;
            }
        }
        return true;
    }

    private List<Channel> getMatchingChannels(Token key) throws ClientException {
        List<Channel> result = new ArrayList<Channel>();

        for (Channel channel : client.getChannels(null)) {
            if (matchesChannel(key, channel.getName(), channel.getId())) {
                result.add(channel);
            }

            // What if the key matches *two* channels, e.g. it's the ID of one
            // and
            // the name of another? Unlikely but possible...
            // if (result.size() > 0 && key != Token.WILDCARD)
            // break;
        }
        return result;
    }

    // Yuck: this is nearly identical to getMatchingChannels(), but there does
    // not appear to be a way to go from Channel to ChannelStatus (or
    // vice-versa). If
    // there was, all channel methods could operate on a Channel object (or a
    // ChannelStatus
    // object), and we would only need one getMatching...() method.
    private List<DashboardStatus> getMatchingChannelStatuses(Token key) throws ClientException {
        List<DashboardStatus> result = new ArrayList<DashboardStatus>();

        for (DashboardStatus status : client.getChannelStatusList()) {
            if (matchesChannel(key, status.getName(), status.getChannelId())) {
                result.add(status);
            }

            // Again, what if the key matches two channels?
            // if (key != Token.WILDCARD)
            // break;

        }
        return result;
    }

    private boolean matchesChannel(Token key, String name, String id) {
        if (key == Token.WILDCARD)
            return true;
        StringToken skey = (StringToken) key;
        return skey.equalsIgnoreCase(name) || skey.equalsIgnoreCase(id);
    }

    private void commandClearAllMessages(Token[] arguments) throws ClientException {
        Set<String> channelIds = new HashSet<String>();

        for (Channel channel : client.getChannels(null)) {
            channelIds.add(channel.getId());
        }

        client.clearMessages(channelIds, true, false);
    }

    private void commandResetstats(Token[] arguments) throws ClientException {
        boolean lifetime = false;
        if (arguments.length >= 2 && arguments[1] == Token.LIFETIME) {
            lifetime = true;
        }

        if (lifetime) {
            client.clearAllStatistics();
        } else {
            List<DashboardStatus> channelStatuses = client.getChannelStatusList();

            Map<String, List<Integer>> channelConnectorMap = new HashMap<String, List<Integer>>();

            for (DashboardStatus status : channelStatuses) {
                String channelId = status.getChannelId();
                Integer metaDataId = status.getMetaDataId();

                List<Integer> metaDataIds = channelConnectorMap.get(channelId);

                if (metaDataIds == null) {
                    metaDataIds = new ArrayList<Integer>();
                    channelConnectorMap.put(channelId, metaDataIds);
                }

                metaDataIds.add(metaDataId);

                if (CollectionUtils.isNotEmpty(status.getChildStatuses())) {
                    for (DashboardStatus childStatus : status.getChildStatuses()) {
                        metaDataIds.add(childStatus.getMetaDataId());
                    }
                }
            }

            client.clearStatistics(channelConnectorMap, true, true, true, true);
        }
    }

    private void commandDumpEvents(Token[] arguments) throws ClientException {
        if (hasInvalidNumberOfArguments(arguments, 2)) {
            return;
        }

        String dumpFilename = arguments[2].getText();
        dumpFilename = replaceValues(dumpFilename);

        StringBuilder builder = new StringBuilder();
        builder.append("Mirth Connect Event Log Dump: " + (new Date()).toString() + "\n");
        builder.append(ServerEvent.getExportHeader() + "\n");

        File dumpFile = new File(dumpFilename);

        try {
            int maxEventId = client.getMaxEventId();
            EventFilter filter = new EventFilter();
            filter.setMaxEventId(maxEventId);

            PaginatedEventList eventList = new PaginatedEventList();
            eventList.setClient(client);
            eventList.setPageSize(20);
            eventList.setEventFilter(filter);

            int pageNumber = 1;

            while (eventList.loadPageNumber(pageNumber)) {
                for (ServerEvent event : eventList) {
                    builder.append(event.toExportString() + "\n");
                }

                pageNumber++;
            }

            FileUtils.writeStringToFile(dumpFile, builder.toString());
        } catch (ListHandlerException lhe) {
            lhe.printStackTrace();
        } catch (IOException ioe) {
            error("Could not write file: " + dumpFile.getAbsolutePath(), ioe);
        } catch (Exception e) {
            error("Could not retrieve events", e);
            e.printStackTrace();
        }

        out.println("Events written to " + dumpFilename);
    }

    private void commandDumpStats(Token[] arguments) throws ClientException {
        if (hasInvalidNumberOfArguments(arguments, 2)) {
            return;
        }

        String dumpFilename = arguments[2].getText();
        dumpFilename = replaceValues(dumpFilename);

        StringBuilder builder = new StringBuilder();
        builder.append("Mirth Channel Statistics Dump: " + (new Date()).toString() + "\n");
        builder.append("Name, Received, Filtered, Queued, Sent, Errored\n");

        List<DashboardStatus> channelStatuses = client.getChannelStatusList();

        for (DashboardStatus channelStatus : channelStatuses) {
            ChannelStatistics stats = client.getStatistics(channelStatus.getChannelId());
            builder.append(channelStatus.getName() + ", " + stats.getReceived() + ", " + stats.getFiltered() + ", " + channelStatus.getQueued() + ", " + stats.getSent() + ", " + stats.getError() + "\n");
        }

        File dumpFile = new File(dumpFilename);

        try {
            FileUtils.writeStringToFile(dumpFile, builder.toString());
            out.println("Stats written to " + dumpFilename);
        } catch (IOException e) {
            error("Could not write file: " + dumpFile.getAbsolutePath(), e);
        }
    }

    private void doImportScript(File scriptFile) throws ClientException {
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        String scriptsXml = new String();

        try {
            scriptsXml = FileUtils.readFileToString(scriptFile);
        } catch (Exception e) {
            error("invalid script file.", e);
            return;
        }

        Map<String, String> scriptsMap = serializer.deserialize(scriptsXml, Map.class);
        client.setGlobalScripts(scriptsMap);
    }

    private void doImportChannel(File importFile, boolean force) throws ClientException {
        Channel importChannel = null;

        try {
            String channelXML = FileUtils.readFileToString(importFile);
            importChannel = ObjectXMLSerializer.getInstance().deserialize(channelXML, Channel.class);
        } catch (Exception e1) {
            error("invalid channel file.", e1);
            return;
        }

        String channelName = importChannel.getName();
        String tempId = client.getGuid();

        // Check to see that the channel name doesn't already exist.
        if (!checkChannelName(channelName, tempId)) {
            if (!force) {
                importChannel.setRevision(0);
                importChannel.setName(tempId);
                importChannel.setId(tempId);
            } else {
                for (Channel channel : client.getChannels(null)) {
                    if (channel.getName().equalsIgnoreCase(channelName)) {
                        // If overwriting, use the old revision number and id
                        importChannel.setRevision(channel.getRevision());
                        importChannel.setId(channel.getId());
                    }
                }
            }
        } else {
            // Start the revision number over for a new channel
            importChannel.setRevision(0);

            // If the channel name didn't already exist, make sure
            // the id doesn't exist either.
            if (!checkChannelId(importChannel.getId())) {
                importChannel.setId(tempId);
            }

        }

        client.updateChannel(importChannel, true);
        out.println("Channel '" + channelName + "' imported successfully.");
    }

    private void doImportAlert(File importFile, boolean force) throws ClientException {
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        List<AlertModel> alertList;

        try {
            alertList = (List<AlertModel>) serializer.deserializeList(FileUtils.readFileToString(importFile).replaceAll("\\&\\#x0D;\\n", "\n").replaceAll("\\&\\#x0D;", "\n"), AlertModel.class);
        } catch (Exception e) {
            error("invalid alert file.", e);
            return;
        }

        removeInvalidItems(alertList, AlertModel.class);

        for (AlertModel importAlert : alertList) {
            String alertName = importAlert.getName();
            String tempId = client.getGuid();

            // Check to see that the alert name doesn't already exist.
            if (!checkAlertName(alertName)) {
                if (!force) {
                    importAlert.setName(tempId);
                    importAlert.setId(tempId);
                } else {
                    for (AlertModel alert : client.getAlert(null)) {
                        if (alert.getName().equalsIgnoreCase(alertName)) {
                            // If overwriting, use the old id
                            importAlert.setId(alert.getId());
                        }
                    }
                }
            }

            client.updateAlert(importAlert);
            out.println("Alert '" + alertName + "' imported successfully.");
        }
    }

    private boolean checkAlertName(String name) throws ClientException {
        if (name.equals("")) {
            out.println("Alert name cannot be empty.");
            return false;
        }

        Pattern alphaNumericPattern = Pattern.compile("^[a-zA-Z_0-9\\-\\s]*$");
        Matcher matcher = alphaNumericPattern.matcher(name);

        if (!matcher.find()) {
            out.println("Alert name cannot have special characters besides hyphen, underscore, and space.");
            return false;
        }

        for (AlertModel alert : client.getAlert(null)) {
            if (alert.getName().equalsIgnoreCase(name)) {
                out.println("Alert \"" + name + "\" already exists.");
                return false;
            }
        }
        return true;
    }

    private String replaceValues(String source) {
        source = source.replaceAll("\\$\\{date\\}", getTimeStamp());
        return source;
    }

    private String getTimeStamp() {
        Date currentTime = new Date();
        return formatter.format(currentTime);
    }

    /**
     * Removes items from the list that are not of the expected class.
     */
    private void removeInvalidItems(List<?> list, Class<?> expectedClass) {
        int originalSize = list.size();

        for (int i = 0; i < list.size(); i++) {
            if (!expectedClass.isInstance(list.get(i))) {
                list.remove(i--);
            }
        }

        if (list.size() < originalSize) {
            if (list.size() == 0) {
                out.println("The imported object(s) are not of the expected class: " + expectedClass.getSimpleName());
            } else {
                out.println("One or more imported objects were skipped, because they are not of the expected class: " + expectedClass.getSimpleName());
            }
        }
    }
}