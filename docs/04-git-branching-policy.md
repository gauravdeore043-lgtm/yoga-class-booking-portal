# Task 4 – Git Branching and Commit Policy

## Branches

| Branch | Purpose | Rules |
|---|---|---|
| `main` | Released, deployable code. Jenkins deploys from here. | Protected: changes only through a pull request; no force-push or deletion. Every release is tagged `vMAJOR.MINOR.PATCH`. |
| `develop` | Integration branch for the next release | Protected: changes only through a pull request from a work branch. |
| `feature/<short-name>` | New functionality, e.g. `feature/registration-and-booking` | Branch from `develop`; merge back to `develop` through a PR. |
| `fix/<short-name>` | Bug fixes, e.g. `fix/seat-release-on-cancel` | Branch from `develop`. |
| `ci/<short-name>` | Jenkins, Docker and Ansible changes | Branch from `develop`. |
| `docs/<short-name>` | Documentation-only changes | Branch from `develop`. |
| `release/<version>` (optional) | Stabilise a release | Merge to `main` and tag. |

Branch names use **lower-case kebab-case** and must start with one of the prefixes above.

## Commit messages – Conventional Commits

```
<type>(<optional scope>): <imperative summary, ≤ 72 chars>

<optional body: what and why>
```

Types: `feat`, `fix`, `docs`, `test`, `ci`, `cd`, `build`, `ops`, `refactor`, `chore`.
Examples: `feat(booking): booking request flow with seat hold`, `fix(booking): release seat when a booking is cancelled`.

## Pull requests

* Use the PR template. Link the issue in the description (`Closes #n`).
* At least one review comment before merging. The Jenkins pipeline must be green before promoting to `main`.
* Merge with a merge commit (`--no-ff`) so feature history stays visible.
* Resolve conflicts on the feature branch, never on `develop` or `main`.

## Releases and tags

* Promote `develop` → `main` through a PR titled `Release vX.Y.Z`.
* Tag the merge commit on `main` with an annotated tag `vX.Y.Z` and create a GitHub release.
