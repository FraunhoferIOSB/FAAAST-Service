#!/usr/bin/env bash
#

VERSION=$1
NEXTVERSION=$2
if [[ -z "$3" ]]; then
  NEXTBRANCH=`cat pom.xml | sed -n 's/^\s\+<tag>\([^<]\+\)<\/tag>/\1/p'`
else
  NEXTBRANCH=$3
fi

echo "Releasing:  ${VERSION},
tagged:    v${VERSION},
next:       ${NEXTVERSION}-SNAPSHOT
nextBranch: ${NEXTBRANCH}"
echo "Press enter to go"
read -s

echo "Replacing version numbers"
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=${VERSION}
sed -i 's/<tag>HEAD<\/tag>/<tag>v'${VERSION}'<\/tag>/g' pom.xml


echo "Git add ."
git add .

echo "Next: git commit & Tag [enter]"
read -s
git commit -m "Release v${VERSION}"
git tag -m "Release v${VERSION}" -a v${VERSION}

echo "Next: replacing version nubmers [enter]"
read -s
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=${NEXTVERSION}-SNAPSHOT
sed -i 's/<tag>v'${VERSION}'<\/tag>/<tag>'${NEXTBRANCH}'<\/tag>/g' pom.xml

echo "Git add ."
git add .

echo "Next: git commit [enter]"
read -s
git commit -m "Prepare for next development iteration"

echo "Done"
