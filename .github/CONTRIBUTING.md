# Contribution Guidelines

We welcome your contributions to the _DITA Open Toolkit_ project.

## Create an Issue

If you find a bug — _and you don’t know how to fix it_, [create an issue][1] to request changes.

Before you do that, [review the open issues][2] to make sure it hasn't already been reported.

_or — even better:_

## Create a Pull Request

If you know how to fix the issue yourself, submit a pull request with the proposed changes.

Here's what to do:

1. [Fork the repository][3].
2. [Create a new branch][4].
3. Make your changes on the new branch.
4. Commit your changes in logical chunks.
5. Indicate that you agree to the terms of the Apache License Version 2.0 by "[signing off][5]" your contribution with `git commit -s`.

    This adds a line with your name and e-mail address to your Git commit message:

    ```bash
     Signed-off-by: Jane Doe <jane.doe@example.com>
    ```

6. Push your feature branch to your fork.
7. [Send a pull request][6].

### Always create a branch for your changes

The DITA-OT project uses the [Git Flow][7] branching strategy.

In this model, change requests are tracked in feature branches that are created by branching off of the main development baseline in the `develop` branch. This makes it easier to keep track of related changes and merge them back into the development stream later. To find out more about how this works, see the [Gitflow Workflow][8] tutorial.

To send a pull request, create a feature branch in your fork with a name like `feature/my-changes`, make your changes on that branch in your fork and issue the pull request from there.

**Note:** By default, pull requests are based on the `develop` branch of the parent `dita-ot` repository, which is appropriate for ​*feature enhancement*​ pull requests. When you create a pull request, GitHub allows you to change the base branch if you think your changes should be applied to a different branch. To fix a bug in the current release, set the base branch for your pull request to the `hotfix/` branch for the ​*latest stable version*​.

For more information, see [Contributing to Open Source on GitHub][9].

[1]: https://github.com/dita-ot/dita-ot/issues/new/choose
[2]: https://github.com/dita-ot/dita-ot/issues
[3]: https://help.github.com/articles/fork-a-repo/
[4]: https://help.github.com/articles/creating-and-deleting-branches-within-your-repository/
[5]: http://www.dita-ot.org/DCO
[6]: https://help.github.com/articles/using-pull-requests/
[7]: http://nvie.com/posts/a-successful-git-branching-model/
[8]: https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow
[9]: https://guides.github.com/activities/contributing-to-open-source/
