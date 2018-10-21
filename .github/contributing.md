# CONTRIBUTING

## Contributing Guidelines

This document provides general guidelines about how to contribute to the project. Keep in mind these important things before you start contributing.

## Reporting issues

* Use [github issues](https://github.com/agrosner/DBFlow/issues) to report a bug.
* Before creating a new issue:
  * Make sure you are using the [latest release](https://github.com/agrosner/DBFlow/releases).
  * Check if the issue was [already reported or fixed](https://github.com/agrosner/DBFlow/issues?utf8=✓&q=is%3Aissue). Notice that it may not be released yet.
  * If you found a match add the github "+1" reaction brief comment. This helps prioritize the issues addressing the most common and critical ones first. If possible, add additional information to help us reproduce, and find the issue. Please use your best judgement.    
* Reporting issues:
  * Please include the following information to help maintainers to fix the problem faster:
    * Android version you are targeting.
    * Full console output of stack trace or code compilation error.
    * Any other additional detail you think it would be useful to understand and solve the problem.

## Pull requests

I welcome and encourage all pull requests. It usually will take me within 24-48 hours to respond to any issue or request. Here are some basic rules to follow to ensure timely addition of your request: 1. Match coding style \(braces, spacing, etc.\) This is best achieved using CMD+Option+L \(Reformat code\) on Mac \(not sure for Windows\) with Android Studio defaults. 2. If its a feature, bugfix, or anything please only change code to what you specify. 3. Please keep PR titles easy to read and descriptive of changes, this will make them easier to merge :\) 4. Pull requests _must_ be made against `develop` branch. Any other branch \(unless specified by the maintainers\) will get rejected.

### Suggested git workflow to contribute

1. Fork the repository.
2. Clone your forked project into your developer machine: `git clone git@github.com:<your-github-username>/DBFlow.git`
3. Add the original project repo as upstream repository in your forked project: `git remote add upstream git@github.com:DBFlow/DBFlow.git`
4. Before starting a new feature make sure your forked develop branch is synchronized upstream master branch. Considering you do not mere your pull request into develop you can run: `git checkout master` and then `git pull upstream develop`. Optionally `git push origin develop`.
5. Create a new branch. Note that the starting point is the upstream develop branch HEAD. `git checkout -b my-feature-name`
6. Stage all your changes `git add .` and commit them `git commit -m "Your commit message"`
7. Make sure your branch is up to date with upstream develop, `git pull --rebase upstream develop`, resolve conflicts if necessary. This will move your commit to the top of git stack.
8. Squash your commits into one commit. `git rebase -i HEAD~6` considering you did 6 commits.
9. Push your branch into your forked remote repository.
10. Create a new pull request adding any useful comment.

### Feature proposal

We would love to hear your ideas and make discussions about it.

* Use github issues to make feature proposals.
* We use `type: feature request` label to mark all [feature request issues](https://github.com/agrosner/DBFlow/labels/type%3A%20feature%20request).
* Before submitting your proposal make sure there is no similar feature request. If you find a match, feel free to join the discussion or just or just act with a reaction if you think the feature is worth implementing.
* Be as specific as possible providing a precise explanation of the feature so anyone can understand the problem and the benefits of solving it.

