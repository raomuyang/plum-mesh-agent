package cn.suniper.mesh.discovery.cli;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/29.
 */
public class AppParametersTest {
    @Test
    public void testParser() throws Exception {

        String[] args = new String[] {
                AppParameters.Keys.OKHTTP_CLIENT.parameter()
        };
        AppParameters parameters = AppParameters.parser(args);
        assertTrue(parameters.isOkHttpClient());

        args = new String[] {
                AppParameters.Keys.CONFIG_PATH.parameter(), "test"
        };
        parameters = AppParameters.parser(args);
        assertEquals("test", parameters.getConfigPath());

        args = new String[] {
                AppParameters.Keys.AUTO_TCP_CLIENT.parameter()
        };
        parameters = AppParameters.parser(args);
        assertTrue(parameters.isAutoTcpClient());

        args = new String[] {
                AppParameters.Keys.HELP.parameter(),
        };
        parameters = AppParameters.parser(args);
        assertTrue(parameters.isHelp());

    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildFailedCauseEnableTcp() {
        AppParameters.newBuilder()
                .setConfigPath("test")
                .enableAutoTcp()
                .enableOkHttp()
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildFailedCauseEnableHttp() {
        AppParameters.newBuilder()
                .setConfigPath("test")
                .enableOkHttp()
                .enableAutoTcp()
                .build();
    }

}