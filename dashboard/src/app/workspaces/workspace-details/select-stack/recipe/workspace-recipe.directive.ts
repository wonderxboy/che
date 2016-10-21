/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * Defines a directive for displaying recipe widget.
 * @author Oleksii Orel
 */
export class WorkspaceRecipe {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/workspaces/workspace-details/select-stack/recipe/workspace-recipe.html';
    this.replace = false;

    this.controller = 'WorkspaceRecipeController';
    this.controllerAs = 'workspaceRecipeCtrl';

    this.bindToController = true;

    // scope values
    this.scope = {
      recipeUrl:'=cheRecipeUrl',
      recipeScript:'=cheRecipeScript',
      recipeFormat:'=cheRecipeFormat'
    };

  }

}
