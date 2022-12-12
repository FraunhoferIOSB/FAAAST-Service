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
TAG_DOWNLOAD_RELEASE="download-release"
TAG_DOWNLOAD_SNAPSHOT="download-snapshot"
TAG_CHANGELOG_HEADER="changelog-header"
CHANGELOG_FILE="./docs/source/changelog/changelog.md"
README_FILE="README.md"
GETTING_STARTED_FILE="./docs/source/gettingstarted/gettingstarted.md"
LATEST_RELEASE_VERSION_CONTENT="[Download latest RELEASE version \($VERSION\)]\(https:\/\/repo1.maven.org\/maven2\/de\/fraunhofer\/iosb\/ilt\/faaast\/service\/starter\/${VERSION}\/starter-${VERSION}.jar\)"
LATEST_SNAPSHOT_VERSION_CONTENT="[Download latest SNAPSHOT version \($NEXTVERSION\-SNAPSHOT)]\(https:\/\/oss.sonatype.org\/service\/local\/artifact\/maven\/redirect?r=snapshots\&g=de\.fraunhofer\.iosb\.ilt\.faaast\.service\&a=starter\&v=${NEXTVERSION}-SNAPSHOT\)"

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
replaceValue $README_FILE $TAG_VERSION $VERSION
replaceValue $README_FILE $TAG_DOWNLOAD_SNAPSHOT ""
replaceValue $README_FILE $TAG_DOWNLOAD_RELEASE $LATEST_RELEASE_VERSION_CONTENT
replaceValue $GETTING_STARTED_FILE $TAG_DOWNLOAD_RELEASE $LATEST_RELEASE_VERSION_CONTENT
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
replaceValue $README_FILE $TAG_DOWNLOAD_SNAPSHOT LATEST_SNAPSHOT_VERSION_CONTENT
replaceValue $GETTING_STARTED_FILE $TAG_DOWNLOAD_SNAPSHOT LATEST_SNAPSHOT_VERSION_CONTENT
sed -i "2 i <!--start:${TAG_CHANGELOG_HEADER}--><!--end:${TAG_CHANGELOG_HEADER}-->" $CHANGELOG_FILE
replaceValue $CHANGELOG_FILE $TAG_CHANGELOG_HEADER "## Current development version (${NEXTVERSION}-SNAPSHOT)"
mvn -B spotless:apply

echo "Git add ."
git add .

echo "Next: git commit [enter]"
read -s
git commit -m "Prepare for next development iteration"

echo "Done"
