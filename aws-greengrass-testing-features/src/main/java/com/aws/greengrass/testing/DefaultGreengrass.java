package com.aws.greengrass.testing;

import com.aws.greengrass.testing.api.Greengrass;
import com.aws.greengrass.testing.api.device.exception.CommandExecutionException;
import com.aws.greengrass.testing.api.device.model.CommandInput;
import com.aws.greengrass.testing.model.TestContext;
import com.aws.greengrass.testing.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultGreengrass implements Greengrass {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGreengrass.class);
    private static final long TIMEOUT_IN_SECONDS = 30L;
    private final String envStage;
    private final String region;
    private final Platform platform;
    private int greengrassProcess;
    private final TestContext testContext;

    public DefaultGreengrass(
            final Platform platform,
            String envStage,
            String region,
            TestContext testContext) {
        this.platform = platform;
        this.envStage = envStage;
        this.region = region;
        this.testContext = testContext;
    }

    @Override
    public void install() {
        platform.commands().execute(CommandInput.builder()
                .line("java").addArgs(
                        "-Droot=" + testContext.installRoot(),
                        "-Dlog.store=FILE",
                        "-jar", testContext.installRoot().resolve("greengrass/lib/Greengrass.jar").toString(),
                        "--aws-region", region,
                        "--env-stage", envStage,
                        "--start", "false")
                .timeout(TIMEOUT_IN_SECONDS)
                .build());
    }

    @Override
    public void start() {
        String loaderPath = "alts/current/distro/bin/loader";
        platform.commands().makeExecutable(testContext.installRoot().resolve(loaderPath));
        greengrassProcess = platform.commands().executeInBackground(CommandInput.builder()
                .workingDirectory(testContext.installRoot())
                .line(loaderPath)
                .timeout(TIMEOUT_IN_SECONDS)
                .build());
        LOGGER.info("Starting greengrass on pid {}", greengrassProcess);
    }

    @Override
    synchronized public void stop() {
        try {
            if (greengrassProcess != 0) {
                platform.commands().killAll(greengrassProcess);
                LOGGER.info("Stopped greengrass on pid {}", greengrassProcess);
                greengrassProcess = 0;
            }
        } catch (CommandExecutionException e) {
            LOGGER.warn("Failed to kill process {}: {}", greengrassProcess, e.getMessage());
        }
    }
}
