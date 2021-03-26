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

# We need the PR branch, develop branch and master branch to be present in different cases to allow Sonar properly build test coverage diffs and commit annotations in coverage details
if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then # Fetch the PR branch with complete history for Travis PR builds in order to let Sonar properly display annotations in coverage details
  git fetch --no-tags https://github.com/${TRAVIS_PULL_REQUEST_SLUG}.git +refs/heads/${TRAVIS_BRANCH}:refs/remotes/origin/${TRAVIS_BRANCH}
fi

# Perform the build without unit tests
gradle build --exclude-task test
# Separate command to run unit tests with full log at console output
gradle test jacocoTestReport sonarqube

# Install SNYK. SNYK should be used only for scanning master,SB,RC,FB_DEVOPS branches
# SNYK should run only for Travis 'branch' builds, and shouldn't run for Travis 'PR' builds
if [[ "$TRAVIS_PULL_REQUEST" == 'false' ]] && [[ "${TRAVIS_BRANCH}" =~ ^master$|^develop$|^SB_*|^RC_*|^FB_DEVOPS-* ]]; then
  npm install -g snyk
fi

# SNYK dependency scan - should run only for master and RC branches
# Should run only for Travis 'branch' builds. Shouldn't run for Travis 'PR' builds
if [[ "$TRAVIS_PULL_REQUEST" == 'false' ]] && [[ "${TRAVIS_BRANCH}" =~ ^master$|^develop$|^SB_.*|^RC_.* ]]; then
  snyk monitor --org=incountry --prune-repeated-subdependencies --project-name="${APP_NAME}:${TRAVIS_BRANCH}"
else
  log_info "Snyk dependency scan skipped"
fi
