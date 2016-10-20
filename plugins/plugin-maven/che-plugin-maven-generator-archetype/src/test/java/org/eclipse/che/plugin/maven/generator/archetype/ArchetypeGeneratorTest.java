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

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.plugin.maven.generator.archetype.dto.MavenArchetype;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by vetal on 20.10.16.
 */
public class ArchetypeGeneratorTest {


    @Test
    public void generateFromArchetype() throws Exception {
        MavenArchetype mavenArchetype = mock(MavenArchetype.class);
        when(mavenArchetype.getArtifactId()).thenReturn("tomee-webapp-archetype");
        when(mavenArchetype.getGroupId()).thenReturn("org.apache.openejb.maven");
        when(mavenArchetype.getVersion()).thenReturn("1.7.1");
        File workDir = Files.createTempDirectory("workDir").toFile();
        ArchetypeGenerator archetypeGenerator = new ArchetypeGenerator();
        String artifactId = NameGenerator.generate("artifactId", 5);
        String groupId = NameGenerator.generate("groupId", 5);
        archetypeGenerator.generateFromArchetype(workDir, mavenArchetype, groupId, artifactId, "1.0-SNAPSHOT");
        String[] list = workDir.list();
        List<String> strings = Arrays.asList(list);
        Assert.assertTrue(strings.contains(artifactId));

    }

}