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
import org.eclipse.che.ide.maven.tools.MavenArtifact;
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
     * Generates a new project from the specified archetype by given maven artifact descriptor.
     *
     * @param archetype
     *         archetype from which need to generate new project
     * @param mavenArtifact
     *         maven artifact descriptor
     * @throws ServerException
     *         if an error occurs while generating project
     */
    public void generateFromArchetype(File workDir, @NotNull MavenArchetype archetype, MavenArtifact mavenArtifact) throws ServerException {
        Map<String, String> archetypeProperties = new HashMap<>();
        archetypeProperties.put("-DinteractiveMode", "false"); // get rid of the interactivity of the archetype plugin
        archetypeProperties.put("-DarchetypeGroupId", archetype.getGroupId());
        archetypeProperties.put("-DarchetypeArtifactId", archetype.getArtifactId());
        archetypeProperties.put("-DarchetypeVersion", archetype.getVersion());
        archetypeProperties.put("-DgroupId", mavenArtifact.getGroupId());
        archetypeProperties.put("-DartifactId", mavenArtifact.getArtifactId());
        archetypeProperties.put("-Dversion", mavenArtifact.getVersion());
        if (archetype.getRepository() != null) {
            archetypeProperties.put("-DarchetypeRepository", archetype.getRepository());
        }
        if (archetype.getProperties() != null) {
            archetypeProperties.putAll(archetype.getProperties());
        }
        final CommandLine commandLine = createCommandLine(archetypeProperties);
        try {
            execute(commandLine.toShellCommand(), workDir);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Execute maven archetype command
     *
     * @param commandLine
     *         command to execution e.g.
     *         mvn archetype:generate -DarchetypeGroupId=<archetype-groupId>  -DarchetypeArtifactId=<archetype-artifactId>
     *               -DarchetypeVersion=<archetype-version> -DgroupId=<my.groupid>      -DartifactId=<my-artifactId>
     * @param workDir
     *         folder where command will execute in common use root dir of workspace
     * @throws TimeoutException
     * @throws IOException
     * @throws InterruptedException
     */
    private void execute(String[] commandLine, File workDir) throws TimeoutException, IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(commandLine).redirectErrorStream(true).directory(workDir);
        WebsocketLineConsumer websocketLineConsumer = new WebsocketLineConsumer("maven-archetype");

        // process will be stopped after timeout
        Watchdog watcher = new Watchdog(60, TimeUnit.SECONDS);

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
                LOG.error("Generation project time expired : command-line " + commandLine);
                throw new TimeoutException();
            } else if (process.exitValue() != 0) {
                LOG.error("Generation project fail : command-line " + commandLine);
                throw new IOException("Process failed. Exit code " + process.exitValue() + " command-line : " + commandLine);
            }
        } finally {
            watcher.stop();
        }
    }

    /**
     * Create specified command
     * @param archetypeProperties
     * @return
     * @throws ServerException
     */
    private CommandLine createCommandLine(Map<String, String> archetypeProperties) throws ServerException {
        final CommandLine commandLine = new CommandLine(MavenUtils.getMavenExecCommand());
        commandLine.add("--batch-mode");
        commandLine.add("org.apache.maven.plugins:maven-archetype-plugin:RELEASE:generate");
        commandLine.add(archetypeProperties);
        return commandLine;
    }

}