#!/usr/bin/env bash

if [[ "${TRAVIS_BUILD_SCRIPT_DEBUG_ENABLED:-false}" == 'true' ]]; then
  set -x
fi

set -e
set -o pipefail

RED="\033[31;1m"
GREEN="\033[32;1m"
RESET="\033[0m"

log_info() {
  echo -e "${GREEN}$1${RESET}"
}
log_error() {
  echo -e "${RED}$1${RESET}"
}

# Return true if branch matches the grep regexp pattern specified and false otherwise
branch_matches() {
  if grep -qE "$1" <(echo "$TRAVIS_BRANCH"); then return 0; else return 1; fi
}

# We need the PR branch, develop branch and master branch to be present in different cases to allow Sonar properly build test coverage diffs and commit annotations in coverage details
if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then # Fetch the PR branch with complete history for Travis PR builds in order to let Sonar properly display annotations in coverage details
  git fetch --no-tags https://github.com/${TRAVIS_PULL_REQUEST_SLUG}.git +refs/heads/${TRAVIS_BRANCH}:refs/remotes/origin/${TRAVIS_BRANCH}
fi

# Perform the build
gradle build jacocoTestReport sonarqube

if [[ "$TRAVIS_PULL_REQUEST" == 'false' ]] && branch_matches "^master$|^develop$|^SB_*|^RC_*"; then
  npm install -g snyk
  snyk monitor --org=incountry --prune-repeated-subdependencies --remote-repo-url="${APP_NAME}" --project-name="${APP_NAME}:${TRAVIS_BRANCH}"
else
  log_info "Snyk dependency scan skipped"
fi
