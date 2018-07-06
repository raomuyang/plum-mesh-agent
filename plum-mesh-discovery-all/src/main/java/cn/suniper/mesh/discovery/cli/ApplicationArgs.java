package cn.suniper.mesh.discovery.cli;


import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.OutputStream;

/**
 * @author Rao Mengnan
 *         on 2018/7/6.
 */
public class ApplicationArgs {


    @Option(name = CONFIG_PATH_PARAMETER, usage = CONFIG_PATH_DESC)
    private String configPath;

    @Option(name = AUTO_TCP_CLIENT_PARAMETER, usage = AUTO_TCP_CLIENT_DESC, forbids = AUTO_HTTP_CLIENT_PARAMETER)
    private boolean autoTcpClient;

    @Option(name = AUTO_HTTP_CLIENT_PARAMETER, usage = AUTO_HTTP_CLIENT_DESC, forbids = AUTO_TCP_CLIENT_PARAMETER)
    private boolean autoHttpClient;

    @Option(name = HELP_PARAMETER, usage = HELP_DESC)
    private boolean help;

    private ApplicationArgs() {

    }

    public static ApplicationArgs parser(String[] args) throws CmdLineException {
        ApplicationArgs applicationArgs = new ApplicationArgs();
        CmdLineParser parser = new CmdLineParser(applicationArgs);
        parser.parseArgument(args);
        return applicationArgs;
    }

    public static void getHelp(OutputStream out) {
        CmdLineParser parser = new CmdLineParser(new ApplicationArgs());
        parser.printUsage(out);
    }

    public String getConfigPath() {
        return configPath;
    }

    public boolean isAutoTcpClient() {
        return autoTcpClient;
    }

    public boolean isAutoHttpClient() {
        return autoHttpClient;
    }

    public boolean isHelp() {
        return help;
    }

    private static final String CONFIG_PATH_PARAMETER = "plum.config";
    private static final String AUTO_TCP_CLIENT_PARAMETER = "plum.auto.tcp";
    private static final String AUTO_HTTP_CLIENT_PARAMETER = "plum.auto.http";
    private static final String HELP_PARAMETER = "help";
    private static final String CONFIG_PATH_DESC = "specify the config path";
    private static final String AUTO_TCP_CLIENT_DESC = "auto generate tcp client by config file";
    private static final String AUTO_HTTP_CLIENT_DESC = "auto generate http client by config file";
    private static final String HELP_DESC = "get help";


    public enum Keys {
        CONFIG_PATH(CONFIG_PATH_PARAMETER, CONFIG_PATH_DESC),
        AUTO_TCP_CLIENT(AUTO_TCP_CLIENT_PARAMETER, AUTO_TCP_CLIENT_DESC),
        AUTO_HTTP_CLIENT(AUTO_HTTP_CLIENT_PARAMETER, AUTO_HTTP_CLIENT_DESC),
        HELP(HELP_PARAMETER, HELP_DESC);

        private String name;
        private String desc;

        Keys(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }

        public String parameter() {
            return this.name;
        }

        public String desc() {
            return this.desc;
        }
    }

}
