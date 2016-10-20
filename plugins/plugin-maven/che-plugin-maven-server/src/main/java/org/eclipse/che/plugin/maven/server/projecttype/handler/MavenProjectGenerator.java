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
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.handlers.GenerateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author gazarenkov
 */
@Singleton
public class MavenProjectGenerator implements GenerateProjectHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MavenProjectGenerator.class);

    private final Map<String, GeneratorStrategy> strategies = new HashMap<>();

    @Inject
    public MavenProjectGenerator(Set<GeneratorStrategy> generatorStrategies) {
        for (GeneratorStrategy generatorStrategy : generatorStrategies) {
            strategies.put(generatorStrategy.getId(), generatorStrategy);
        }
        if (!strategies.containsKey(MavenAttributes.SIMPLE_GENERATION_STRATEGY)) { //must always be if not added in DI we add it here
            strategies.put(MavenAttributes.SIMPLE_GENERATION_STRATEGY, new SimpleGeneratorStrategy());
        }
    }

    @Override
    public String getProjectType() {
        return MavenAttributes.MAVEN_ID;
    }

    @Override
    public void onCreateProject(FolderEntry baseFolder, ProjectConfig projectConfig,
                                Map<String, String> options) throws ForbiddenException, ConflictException, ServerException {
        if (options == null || options.isEmpty() || !options.containsKey("type")) {
            strategies.get(MavenAttributes.SIMPLE_GENERATION_STRATEGY).generateProject(baseFolder, projectConfig, options);
        } else {
            if (strategies.containsKey(options.get("type"))) {
                strategies.get(options.get("type")).generateProject(baseFolder, projectConfig, options);
            } else {
                String errorMsg = String.format("Generation strategy %s not found", options.get("type"));
                LOG.warn("MavenProjectGenerator", errorMsg);
                throw new ServerException(errorMsg);
            }
        }
    }
}
