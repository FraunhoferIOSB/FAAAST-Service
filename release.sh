#!/usr/bin/env bash
#
VERSION=$1
NEXTVERSION=$2
if [[ -z "$3" ]]; then
  NEXTBRANCH=$(sed -n 's/^\s\+<tag>\([^<]\+\)<\/tag>/\1/p' pom.xml)
else
  NEXTBRANCH=$3
fi
TAG_VERSION="version"
TAG_DOWNLOAD_SNAPSHOT="download-snapshot"
TAG_CHANGELOG_HEADER="changelog-header"
CHANGELOG_FILE="./docs/source/changelog/changelog.md"

# arguments: file, tag, newValue, originalValue(optional, default: matches anything)
function replaceValue()
{
	local file=$1
	local tag=$2
	local newValue=$3
	local originalValue=${4:-[^<]*}
	local startTag="<!--start:${tag}-->"
	local endTag="<!--end:${tag}-->"
	sed -r -z "s/$startTag($originalValue)$endTag/$startTag$newValue$endTag/g" -i $file
}

# arguments: file
function removeTag()
{
	local file=$1
	local tag=$2
	local startTag="<!--start:${tag}-->"
	local endTag="<!--end:${tag}-->"
	sed -r -z "s/$startTag([^<]*)$endTag/\1/g" -i $file
}

echo "Releasing:  ${VERSION},
tagged:    v${VERSION},
next:       ${NEXTVERSION}-SNAPSHOT
nextBranch: ${NEXTBRANCH}"
echo "Press enter to go"
read -s

echo "Replacing version numbers"
mvn -B versions:set -DgenerateBackupPoms=false -DnewVersion="${VERSION}"
sed -i 's/<tag>HEAD<\/tag>/<tag>v'"${VERSION}"'<\/tag>/g' pom.xml
replaceValue README.md $TAG_VERSION $VERSION
replaceValue README.md $TAG_DOWNLOAD_SNAPSHOT ""
replaceValue $CHANGELOG_FILE $TAG_VERSION $VERSION
replaceValue $CHANGELOG_FILE $TAG_CHANGELOG_HEADER "## Release version ${VERSION}"
removeTag $CHANGELOG_FILE $TAG_CHANGELOG_HEADER
mvn -B spotless:apply

echo "Git add ."
git add .

echo "Next: git commit & Tag [enter]"
read -s
git commit -m "Release v${VERSION}"
git tag -m "Release v${VERSION}" -a v"${VERSION}"

echo "Next: replacing version nubmers [enter]"
read -s
mvn versions:set -DgenerateBackupPoms=false -DnewVersion="${NEXTVERSION}"-SNAPSHOT
sed -i 's/<tag>v'"${VERSION}"'<\/tag>/<tag>'"${NEXTBRANCH}"'<\/tag>/g' pom.xml
sed -i "2 i <!--start:${TAG_CHANGELOG_HEADER}--><!--end:${TAG_CHANGELOG_HEADER}-->" $CHANGELOG_FILE
replaceValue $CHANGELOG_FILE $TAG_CHANGELOG_HEADER "## Current development version (${NEXTVERSION}-SNAPSHOT)"
replaceValue README.md $TAG_DOWNLOAD_SNAPSHOT "[Download latest SNAPSHOT version ($NEXTVERSION)]($DOWNLOAD_URL_SNAPHOT)"
mvn -B spotless:apply

echo "Git add ."
git add .

echo "Next: git commit [enter]"
read -s
git commit -m "Prepare for next development iteration"

echo "Done"
