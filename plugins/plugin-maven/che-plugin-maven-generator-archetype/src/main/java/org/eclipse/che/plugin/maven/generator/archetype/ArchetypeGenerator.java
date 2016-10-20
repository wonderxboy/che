/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.generator.archetype;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.core.util.Watchdog;
import org.eclipse.che.api.core.util.WebsocketLineConsumer;
import org.eclipse.che.ide.maven.tools.MavenUtils;
import org.eclipse.che.plugin.maven.generator.archetype.dto.MavenArchetype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Generates projects with maven-archetype-plugin.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ArchetypeGenerator {
    private static final Logger     LOG            = LoggerFactory.getLogger(ArchetypeGenerator.class);

    /**
     * Generates a new project from the specified archetype.
     *
     * @param archetype
     *         archetype from which need to generate new project
     * @param groupId
     *         groupId of new project
     * @param artifactId
     *         artifactId of new project
     * @param version
     *         version of new project
     * @return generating task
     * @throws ServerException
     *         if an error occurs while generating project
     */
    public void generateFromArchetype(File workDir, @NotNull MavenArchetype archetype, @NotNull String groupId, @NotNull String artifactId,
                                      @NotNull String version) throws ServerException {
        Map<String, String> archetypeProperties = new HashMap<>();
        archetypeProperties.put("-DinteractiveMode", "false"); // get rid of the interactivity of the archetype plugin
        archetypeProperties.put("-DarchetypeGroupId", archetype.getGroupId());
        archetypeProperties.put("-DarchetypeArtifactId", archetype.getArtifactId());
        archetypeProperties.put("-DarchetypeVersion", archetype.getVersion());
        archetypeProperties.put("-DgroupId", groupId);
        archetypeProperties.put("-DartifactId", artifactId);
        archetypeProperties.put("-Dversion", version);
        if (archetype.getRepository() != null) {
            archetypeProperties.put("-DarchetypeRepository", archetype.getRepository());
        }
        if (archetype.getProperties() != null) {
            archetypeProperties.putAll(archetype.getProperties());
        }
        final CommandLine commandLine = createCommandLine(archetypeProperties);

        try {
            execute(commandLine.toShellCommand(), workDir, 100);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    void execute(String[] commandLine, File workDir, int timeout) throws TimeoutException, IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(commandLine).redirectErrorStream(true).directory(workDir);
        WebsocketLineConsumer websocketLineConsumer = new WebsocketLineConsumer("maven-archetype");

        // process will be stopped after timeout
        Watchdog watcher = new Watchdog(timeout, TimeUnit.SECONDS);

        try {
            final Process process = pb.start();
            final ValueHolder<Boolean> isTimeoutExceeded = new ValueHolder<>(false);
            watcher.start(() -> {
                isTimeoutExceeded.set(true);
                ProcessUtil.kill(process);
            });
            // consume logs until process ends
            ProcessUtil.process(process, websocketLineConsumer);
            process.waitFor();
            if (isTimeoutExceeded.get()) {
                LOG.error("Generation project tome expired : command-line " + commandLine);
                throw new TimeoutException();
            } else if (process.exitValue() != 0) {
                throw new IOException("Process failed. Exit code " + process.exitValue());
            }
        } finally {
            watcher.stop();
        }
    }

    private CommandLine createCommandLine(Map<String, String> archetypeProperties) throws ServerException {
        final CommandLine commandLine = new CommandLine(MavenUtils.getMavenExecCommand());
        commandLine.add("--batch-mode");
        commandLine.add("org.apache.maven.plugins:maven-archetype-plugin:RELEASE:generate");
        commandLine.add(archetypeProperties);
        return commandLine;
    }

}