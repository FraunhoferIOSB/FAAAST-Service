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
CHANGELOG_FILE="./docs/source/other/release-notes.md"
README_FILE="README.md"
INSTALLATION_FILE="./docs/source/basics/installation.md"
README_LATEST_RELEASE_VERSION_CONTENT="[Download latest RELEASE version \($VERSION\)]\(https:\/\/repo1.maven.org\/maven2\/de\/fraunhofer\/iosb\/ilt\/faaast\/service\/starter\/${VERSION}\/starter-${VERSION}.jar\)"
README_LATEST_SNAPSHOT_VERSION_CONTENT="[Download latest SNAPSHOT version \($NEXTVERSION\-SNAPSHOT)]\(https:\/\/oss.sonatype.org\/service\/local\/artifact\/maven\/redirect?r=snapshots\&g=de\.fraunhofer\.iosb\.ilt\.faaast\.service\&a=starter\&v=${NEXTVERSION}-SNAPSHOT\)"
INSTALLATION_LATEST_RELEASE_VERSION_CONTENT="{download}\`Latest RELEASE version \($VERSION\) <https:\/\/repo1.maven.org\/maven2\/de\/fraunhofer\/iosb\/ilt\/faaast\/service\/starter\/${VERSION}\/starter-${VERSION}.jar>\`"
INSTALLATION_LATEST_SNAPSHOT_VERSION_CONTENT="{download}\`Latest SNAPSHOT version \($NEXTVERSION\-SNAPSHOT) <https:\/\/oss.sonatype.org\/service\/local\/artifact\/maven\/redirect?r=snapshots\&g=de\.fraunhofer\.iosb\.ilt\.faaast\.service\&a=starter\&v=${NEXTVERSION}-SNAPSHOT>\`"

# arguments: tag
function startTag()
{
	echo "<!--start:$1-->\\n"
}

# arguments: tag
function endTag()
{
	echo "<!--end:$1-->"
}

# arguments: file, tag, newValue, originalValue(optional, default: matches anything)
function replaceValue()
{
	local file=$1
	local tag=$2
	local newValue=$3
	local originalValue=${4:-.*}
	local startTag=$(startTag "$tag")
	local endTag=$(endTag "$tag")
	sed -r -z "s/$startTag($originalValue)$endTag/$startTag$newValue$endTag/g" -i "$file"
}

# arguments: file, tag
function removeTag()
{
	local file=$1
	local tag=$2
	local startTag=$(startTag "$tag")
	local endTag=$(endTag "$tag")
	sed -r -z "s/$startTag(.*)$endTag/\1/g" -i "$file"
}

# arguments: file, newVersion
function replaceVersion()
{
	local file=$1
	local new_version=$2
	local startTag=$(startTag "$tag")
	local endTag=$(endTag "$tag")
	replaceValue "$file" "$TAG_VERSION" "$new_version"
	sed -r -z 's/(<artifactId>starter<\/artifactId>[\r\n]+\s*<version>)[^<]+(<\/version>)/\1'"${new_version}"'\2/g' -i "$file"
	sed -r -z 's/(\x27de.fraunhofer.iosb.ilt.faaast.service:starter:)[^\x27]*\x27/\1'"${new_version}"'\x27/g' -i "$file"
}

# argument: newVersion
function updateServiceProfileUrls()
{
	echo "VERSION=$VERSION"
	echo "NEXTVERSION=$NEXTVERSION"
	echo "ARGUMENT=$1"
	local new_version=$1
	local major=$(echo "$new_version" | sed -E 's/^([0-9]+)\..*/\1/')
	local minor=$(echo "$new_version" | sed -E 's/^[0-9]+\.([0-9]+)\..*/\1/')
	local file="./model/src/main/java/de/fraunhofer/iosb/ilt/faaast/service/model/ServiceSpecificationProfile.java"
	sed -i "s|https://github.com/FraunhoferIOSB/FAAAST-Service/API/[0-9]*\/[0-9]*\/|https://github.com/FraunhoferIOSB/FAAAST-Service/API/$major/$minor/|g" "$file"
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
replaceVersion "$README_FILE" "$VERSION"
replaceValue "$README_FILE" "$TAG_DOWNLOAD_SNAPSHOT" ""
replaceValue "$README_FILE" "$TAG_DOWNLOAD_RELEASE" "$README_LATEST_RELEASE_VERSION_CONTENT"
replaceVersion "$INSTALLATION_FILE" "$VERSION"
replaceValue "$INSTALLATION_FILE" "$TAG_DOWNLOAD_SNAPSHOT" ""
replaceValue "$INSTALLATION_FILE" "$TAG_DOWNLOAD_RELEASE" "$INSTALLATION_LATEST_RELEASE_VERSION_CONTENT"
replaceValue "$CHANGELOG_FILE" "$TAG_CHANGELOG_HEADER" "## ${VERSION}"
removeTag "$CHANGELOG_FILE" "$TAG_CHANGELOG_HEADER"

mvn -B spotless:apply

echo "Updating thrid party license report"
mvn clean install license:aggregate-third-party-report -P build-ci -Dmaven.test.skip=false -B

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
replaceValue "$README_FILE" "$TAG_DOWNLOAD_SNAPSHOT" "$README_LATEST_SNAPSHOT_VERSION_CONTENT"
replaceValue "$INSTALLATION_FILE" "$TAG_DOWNLOAD_SNAPSHOT" "$INSTALLATION_LATEST_SNAPSHOT_VERSION_CONTENT"
sed -i "2 i <!--start:${TAG_CHANGELOG_HEADER}-->\\n<!--end:${TAG_CHANGELOG_HEADER}-->" "$CHANGELOG_FILE"
replaceValue "$CHANGELOG_FILE" "$TAG_CHANGELOG_HEADER" "## ${NEXTVERSION}-SNAPSHOT (current development version)"
updateServiceProfileUrls $NEXTVERSION
mvn -B spotless:apply

echo "Git add ."
git add .

echo "Next: git commit [enter]"
read -s
git commit -m "Prepare for next development iteration"

echo "Done"
