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
package org.eclipse.che.plugin.maven.server.projecttype.handler;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.ide.maven.tools.Build;
import org.eclipse.che.ide.maven.tools.Model;

import java.util.Map;

import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_TEST_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.GROUP_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PACKAGING;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_GROUP_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_VERSION;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.SIMPLE_GENERATION_STRATEGY;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.TEST_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.VERSION;

/**
 * Generates simple Maven project.
 *
 * @author Artem Zatsarynnyi
 */
public class SimpleGeneratorStrategy implements GeneratorStrategy {

    @Override
    public String getId() {
        return SIMPLE_GENERATION_STRATEGY;
    }

    @Override
    public void generateProject(FolderEntry baseFolder, ProjectConfig projectConfig, Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {


        String artifactId = projectConfig.getAttributes().get(ARTIFACT_ID).get(0);
        String groupId = projectConfig.getAttributes().get(GROUP_ID).get(0);
        String version = projectConfig.getAttributes().get(VERSION).get(0);
        if (artifactId == null) {
            throw new ConflictException("Missed required attribute artifactId");
        }

        if (groupId == null) {
            throw new ConflictException("Missed required attribute groupId");
        }

        if (version == null) {
            throw new ConflictException("Missed required attribute version");
        }

        Model model = Model.createModel();
        model.setModelVersion("4.0.0");

        if (baseFolder.getChild("pom.xml") == null) {
            baseFolder.createFile("pom.xml", new byte[0]);
        }

        String parentArtifactId = projectConfig.getAttributes().get(PARENT_ARTIFACT_ID).get(0);
        if (parentArtifactId != null) {
            model.setArtifactId(parentArtifactId);
        }
        String parentGroupId = projectConfig.getAttributes().get(PARENT_GROUP_ID).get(0);
        if (parentGroupId != null) {
            model.setGroupId(parentGroupId);
        }
        String parentVersion = projectConfig.getAttributes().get(PARENT_VERSION).get(0);
        if (parentVersion != null) {
            model.setVersion(parentVersion);
        }
        model.setArtifactId(artifactId);
        model.setGroupId(groupId);
        model.setVersion(version);
        String packaging = projectConfig.getAttributes().get(PACKAGING).get(0);

        if (packaging != null) {
            model.setPackaging(packaging);
        }
        String sourceFolder = projectConfig.getAttributes().get(SOURCE_FOLDER).get(0);
        if (sourceFolder != null) {
            baseFolder.createFolder(sourceFolder);
            if (!DEFAULT_SOURCE_FOLDER.equals(sourceFolder)) {
                model.setBuild(new Build().setSourceDirectory(sourceFolder));
            }
        }
        String testSourceFolder = projectConfig.getAttributes().get(TEST_SOURCE_FOLDER).get(0);
        if (testSourceFolder != null) {
            baseFolder.createFolder(testSourceFolder);
            if (!DEFAULT_TEST_SOURCE_FOLDER.equals(testSourceFolder)) {
                Build build = model.getBuild();
                if (build != null) {
                    build.setTestSourceDirectory(testSourceFolder);
                } else {
                    model.setBuild(new Build().setTestSourceDirectory(testSourceFolder));
                }
            }
        }
        model.writeTo(baseFolder.getChild("pom.xml").getVirtualFile());
    }
}
