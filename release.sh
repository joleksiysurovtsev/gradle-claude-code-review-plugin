#!/bin/bash

# Release script for Claude AI Code Review Plugin
# Usage: ./release.sh [patch|minor|major]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default to patch release
RELEASE_TYPE=${1:-patch}

echo -e "${YELLOW}🚀 Starting release process...${NC}"

# Check if working directory is clean
if [[ -n $(git status -s) ]]; then
    echo -e "${RED}❌ Working directory is not clean. Please commit or stash changes.${NC}"
    exit 1
fi

# Get current version
CURRENT_VERSION=$(./gradlew currentVersion -q | grep "Project version:" | sed 's/Project version: //')
echo -e "${GREEN}📌 Current version: ${CURRENT_VERSION}${NC}"

# Create new version based on release type
case $RELEASE_TYPE in
    patch)
        echo "Creating patch release..."
        ./gradlew markNextVersion -Prelease.incrementer=incrementPatch -q
        ;;
    minor)
        echo "Creating minor release..."
        ./gradlew markNextVersion -Prelease.incrementer=incrementMinor -q
        ;;
    major)
        echo "Creating major release..."
        ./gradlew markNextVersion -Prelease.incrementer=incrementMajor -q
        ;;
    *)
        echo -e "${RED}❌ Invalid release type: $RELEASE_TYPE${NC}"
        echo "Usage: ./release.sh [patch|minor|major]"
        exit 1
        ;;
esac

# Get new version
NEW_VERSION=$(./gradlew currentVersion -q | grep "Project version:" | sed 's/Project version: //')
echo -e "${GREEN}✨ New version: ${NEW_VERSION}${NC}"

# Update CHANGELOG.md
echo -e "${YELLOW}📝 Please update CHANGELOG.md with changes for version ${NEW_VERSION}${NC}"
echo "Press Enter when done..."
read

# Commit changelog
git add CHANGELOG.md
git commit -m "chore: update CHANGELOG for v${NEW_VERSION}"

# Create and push tag
echo -e "${YELLOW}🏷️  Creating tag v${NEW_VERSION}...${NC}"
./gradlew createRelease -Prelease.customUsername="Release Bot" -q

# Push changes
echo -e "${YELLOW}📤 Pushing changes to remote...${NC}"
git push origin main --follow-tags

echo -e "${GREEN}✅ Release v${NEW_VERSION} created successfully!${NC}"
echo -e "${GREEN}🎉 GitHub Actions will now build and publish the plugin.${NC}"
echo ""
echo "Next steps:"
echo "1. Wait for GitHub Actions to complete"
echo "2. Check the release at: https://github.com/joleksiysurovtsev/claude-review-plugin/releases"
echo "3. Verify plugin at: https://plugins.gradle.org/plugin/dev.surovtsev.claude-review"