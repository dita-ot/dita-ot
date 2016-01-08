# Contribution Guidelines

We welcome contributions to the DITA-OT. 

## Create an Issue

If you find a bug — _and you don’t know how to fix it_, [create an issue][1] to request changes.

Before you do that, [review the open issues][2] to make sure it hasn't already been reported.

A good issue description contains:

*  description of the problem,
*  copy of the error message and/or stack trace,
*  version of DITA-OT, and
*  a self-contained test case.

A test case is simply a set of files that can be used to reproduce the issue. [Gist][3] is an easy way to upload your test files and link them to the issue description.

_or — even better:_

## Create a Pull Request

If you know how to fix the issue yourself, that's great! Here's what to do:

1. [Fork the repository][4],
2. [Create a new branch][5], 
3. Make your changes on the new branch, and 
3. [Send a pull request][6]. 

### Always create a branch for your changes

The DITA-OT project uses the [Git Flow][7] branching strategy. 

In this model, change requests are tracked in feature branches that are created by branching off of the main development baseline in the `develop` branch. This makes it easier to keep track of related changes and merge them back into the development stream later. To find out more about how this works, see the [Gitflow Workflow][8] tutorial.

To send a pull request, create a feature branch in your fork with a name like `feature/my-changes`, make your changes on that branch in your fork and issue the pull request from there. 

**Note:** By default, pull requests are based on the `develop` branch of the parent `dita-ot` repository, which is appropriate for ​*feature enhancement*​ pull requests. When you create a pull request, GitHub allows you to change the base branch if you think your changes should be applied to a different branch. To fix a bug in the current release, set the base branch for your pull request to the `hotfix/` branch for the ​*latest stable version*​. 

For more information, see [Contributing to Open Source on GitHub][9].


[1]:	https://github.com/dita-ot/dita-ot/issues/new
[2]:	https://github.com/dita-ot/dita-ot/issues
[3]:	https://gist.github.com/
[4]:	https://help.github.com/articles/fork-a-repo/
[5]:	https://help.github.com/articles/creating-and-deleting-branches-within-your-repository/
[6]:	https://help.github.com/articles/using-pull-requests/
[7]:	http://nvie.com/posts/a-successful-git-branching-model/
[8]:	https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow
[9]:	https://guides.github.com/activities/contributing-to-open-source/
