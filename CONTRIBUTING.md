# Contribution Guidelines

We welcome contributions to the DITA-OT. 

## Create an Issue

If you find a bug — _and you don’t know how to fix it_, [create an issue](https://github.com/dita-ot/dita-ot/issues/new) to request changes.

Before you do that, [review the open issues](https://github.com/dita-ot/dita-ot/issues) to make sure it hasn't already been reported.

A good issue description contains:

*  description of the problem,
*  copy of the error message and/or stack trace,
*  version of DITA-OT, and
*  a self-contained test case.

A test case is simply a set of files that can be used to reproduce the issue. [Gist](https://gist.github.com/) is an easy way to upload your test files and link it to the
issue description.

_or — even better:_

## Create a Pull Request

If you know how to fix the issue yourself, that's great! Here's what to do:

1. [Fork the repository][1],
2. [Create a new branch][3], 
3. Make your changes on the new branch, and 
3. [Send a pull request][2]. 

### Always create a branch for your changes

The DITA-OT project uses the [Git Flow][4] branching strategy. 

In this model, change requests are tracked in feature branches that are created by branching off of the main development baseline in the `develop` branch. This makes it easier to keep track of related changes and merge them back into the development stream later. To find out more about how this works, see the [Gitflow Workflow][5] tutorial.

To send a pull request, create a feature branch in your fork with a name like `feature/my-changes`, make your changes on that branch in your fork and issue the pull request from there. 

For more information, see [Contributing to Open Source on GitHub][6].


[1]:    <https://help.github.com/articles/fork-a-repo/>
[2]:    <https://help.github.com/articles/using-pull-requests/>
[3]:    <https://help.github.com/articles/creating-and-deleting-branches-within-your-repository/>
[4]:    <http://nvie.com/posts/a-successful-git-branching-model/>
[5]:    <https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow>
[6]:    <https://guides.github.com/activities/contributing-to-open-source/>
[7]: https://gist.github.com/
