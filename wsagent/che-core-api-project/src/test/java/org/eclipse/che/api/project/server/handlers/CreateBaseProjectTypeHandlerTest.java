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
package org.eclipse.che.api.project.server.handlers;

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.vfs.Path;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 *  @author Vitalii Parfonov
 */
public class CreateBaseProjectTypeHandlerTest {

    @Test
    public void testCreateProject() throws Exception {
        Path path = mock(Path.class);
        CreateBaseProjectTypeHandler createBaseProjectTypeHandler = mock(CreateBaseProjectTypeHandler.class);
        createBaseProjectTypeHandler.onCreateProject(path, null, null);
        verify(path).toString();
        verify(createBaseProjectTypeHandler).getReadmeContent();
    }
}
