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
 * @ngdoc controller
 * @name workspaces.select-stack.controller:WorkspaceSelectStackController
 * @description This class is handling the controller for the 'select stack' widget.
 * @author Oleksii Orel
 */
export class WorkspaceSelectStackController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout, $scope, lodash, cheStack) {
    this.$timeout = $timeout;
    this.$scope = $scope;
    this.lodash = lodash;

    this.stacks = cheStack.getStacks();
    if (this.stacks.length) {
      this.$scope.$emit('create-project-stacks:initialized');
    } else {
      cheStack.fetchStacks().then(() => {
        this.$scope.$emit('create-project-stacks:initialized');
      });
    }

    $scope.$on('event:selectStackId', (event, data) => {
      event.stopPropagation();
      let findStack = this.lodash.find(this.stacks, (stack) => {
        return stack.id === data.stackId;
      });
      if (findStack) {
        if (data.tabName === 'ready-to-go') {
          this.readyToGoStack = findStack;
        } else if (data.tabName === 'stack-library') {
          this.stackLibraryUser = findStack;
        }
        if (this.tabName === data.tabName) {
          this.onStackSelect(findStack);
        }
      }
    });
  }

  /**
   * Callback when tab has been change
   * @param tabName  the select tab name
   */
  setStackTab(tabName) {
    this.tabName = tabName;
    this.onTabChange({tabName: tabName});

    if (tabName === 'ready-to-go') {
      this.onStackSelect(this.readyToGoStack);
      return;
    } else if (tabName === 'stack-library') {
      this.onStackSelect(this.stackLibraryUser);
      return;
    }
    this.onStackSelect(null);
  }

  /**
   * Callback when stack has been select
   * @param stack
   */
  onStackSelect(stack) {
    this.stack = stack;
    this.onStackChange({stack: stack});
  }
}
