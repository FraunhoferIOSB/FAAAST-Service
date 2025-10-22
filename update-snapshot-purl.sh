#!/usr/bin/env bash
#
# Script to update static PURL/redirect to latest snapshot URL .
#
# Author: Michael Jacoby <michael.jacoby@iosb.fraunhofer.de>
# Date: August 18, 2025
# License: Apache 2.0

MAVEN_SNAPSHOT_REPOSITORY="https://central.sonatype.com/repository/maven-snapshots"
ACCOUNT_URL="https://archive.org/account"
PURL_URL="https://purl.archive.org"
TOKEN_ERROR_EDITING_PURL="Error editing PURL:"
TOKEN_ERROR_SAVING_PURL="Error saving PURL:"
TOKEN_SUCCESS_SAVING_PURL="Saved: changes to PURL"
TOKEN_PURL_UNCHANGED="There were no changes so the PURL did not save."

ARTIFACT_ID=""
GROUP_ID=""
VERSION=""
PURL_DOMAIN=""
PURL_NAME=""

read -d '' -r USAGE <<- EOF
Usage: update-snapshot-redirect [-g groupId] [-a artifactId] [-v version] [-d PURL domain] [-p PURL name]

Updates a PURL redirect on https://purl.archive.org to point to the latest SNAPSHOT release on https://central.sonatype.com.
Requires $PURL_USER and $PURL_PASSWORD are set in ENV.

Options
  -a <artifactId>   Artifact ID of the maven snapshot.
  -d <domain>       The domain of the PURL to update.
  -g <groupId>      Group ID of the maven snapshot.
  -h                Print this message and exit.
  -p <name>         The name of the PURL to update.
  -v <version>      Version of the maven snapshot.
EOF

fatal() {
	echo '[fatal]' "$@" >&2
	exit 1
}

function toUrlPath() {
	printf "${1//./\/}"
}

urlencode() {
	local LC_ALL=C
	for (( i = 0; i < ${#1}; i++ )); do
		: "${1:i:1}"
		case "$_" in
			[a-zA-Z0-9.~_-])
				printf '%s' "$_"
				;;

			*)
				printf '%%%02X' "'$_"
				;;
		esac
	done
	printf '\n'
}

parseOptions() {
	local OPTIND OPTARG opt
	while getopts 'a:d:g:hp:v:' opt; do
		case "$opt" in
			a) ARTIFACT_ID=$OPTARG;;
			d) PURL_DOMAIN=$OPTARG;;
			g) GROUP_ID=$OPTARG;;
			h) echo "$USAGE"; exit 0;;
			p) PURL_NAME=$OPTARG;;
			v) VERSION="${OPTARG%-SNAPSHOT}";;
			*) echo "$USAGE" >&2; exit 2;;
		esac
	done
}

main() {
	parseOptions "$@"

	[ -z "$PURL_USER" ] && fatal "PURL_USER must be set"
	[ -z "$PURL_PASSWORD" ] && fatal "PURL_PASSWORD must be set"
	[ -z "$GROUP_ID" ] && fatal "GroupId (-g) must be set"
	[ -z "$ARTIFACT_ID" ] && fatal "ArtifactId (-a) must be set"
	[ -z "$VERSION" ] && fatal "Version (-v) must be set"
	[ -z "$PURL_DOMAIN" ] && fatal "PURL domain (-d) must be set"
	[ -z "$PURL_NAME" ] && fatal "PURL name (-p) must be set"
	
	
	echo "Updating PURL redirect for ${PURL_URL}/${PURL_DOMAIN}/${PURL_NAME} to latest SNAPSHOT release of artifact ${GROUP_ID}:${ARTIFACT_ID}:${VERSION}..."	
	# determine new URL by querying maven metadata
	local mvn_baseurl mvn_metadata mvn_timestamp mvn_buildnumber new_url
	mvn_baseurl="${MAVEN_SNAPSHOT_REPOSITORY}/$(toUrlPath "$GROUP_ID")/$(toUrlPath "$ARTIFACT_ID")/${VERSION}-SNAPSHOT"
	mvn_metadata=$(curl --silent --show-error --fail "${mvn_baseurl}/maven-metadata.xml") || fatal "failed to fetch maven metadata"
	mvn_timestamp=$(echo "$mvn_metadata" | grep --only-matching --perl-regexp '(?<=<timestamp>).*?(?=</timestamp>)') || fatal "failed to extract timeStamp"
	mvn_buildnumber=$(echo "$mvn_metadata" | grep --only-matching --perl-regexp '(?<=<buildNumber>).*?(?=</buildNumber>)') || fatal "failed to extract buildNUmber"
	new_url="${mvn_baseurl}/${ARTIFACT_ID}-${VERSION}-${mvn_timestamp}-${mvn_buildnumber}.jar"
	echo "New target URL: ${new_url}"
	
	local cookie csrf_response update_response
	# update PURL entry with new_url
	cookie="cookie"
	echo -e -n "Preparing cookies...\t\t"	
	curl --silent --show-error --fail --cookie-jar "$cookie" "${ACCOUNT_URL}/login" -o /dev/null || fatal "failed to fetch initial set of cookies"
	echo "success"

	echo -e -n "logging in...\t\t\t"
	curl --silent --show-error --fail --cookie "$cookie" --cookie-jar "$cookie" --data "username=${PURL_USER}&password=${PURL_PASSWORD}" "${ACCOUNT_URL}/login" | grep --silent "Successful login." || fatal "failed to log in"
	echo "success"

	echo -e -n "fetching CSRF token...\t\t"
	csrf_response=$(curl \
		--silent \
		--show-error \
		--fail \
		--cookie "$cookie" \
		--cookie-jar "$cookie" \
		--location \
		"${PURL_URL}/edit_purl/${PURL_DOMAIN}/${PURL_NAME}") 
	if [ $? -ne 0 ]; then
		echo "csrf_response"
		fatal "failed to fetch CSRF data"
	fi
	if echo "$csrf_response" | grep --silent "${TOKEN_ERROR_EDITING_PURL}"; then
		reason=$(echo "$csrf_response" | sed -n "s/.*$TOKEN_ERROR_EDITING_PURL \(.*\)/\1/p")
		fatal "failed to fetch CSRF data: $reason"
	fi
	csrf_token=$(echo "$csrf_response" | sed -n 's/.*name="csrf_token" value="\([^"]*\)".*/\1/p') || fatal "failed to extract CSRF token"		
	[ -z "$PURL_USER" ] && fatal "failed to extract CSRF token"
	echo "success"

	echo -e -n "updating URL...\t\t\t"
	new_url_encoded=$(urlencode "${new_url}")	
	update_response=$(curl \
		--silent \
		--show-error \
		--cookie "$cookie" \
		--cookie-jar "$cookie" \
		--write-out "%{http_code} %{redirect_url}" \
		--output /dev/null \
		--data "csrf_token=${csrf_token}&redirect_type=302&target=${new_url_encoded}" \
		"${PURL_URL}/edit_purl/${PURL_DOMAIN}/${PURL_NAME}")

	update_response_code="${update_response%% *}"
	update_response_redirect="${update_response#* }"

	if [ $? -ne 0 ]; then
		echo "$update_response"
		fatal "failed to update PURL"
	elif [ "$update_response_code" -eq 302 ]; then
		update_response=$(curl \
			--show-error \
			--fail \
			--no-progress-meter \
			--cookie "$cookie" \
			--cookie-jar "$cookie" \
			"${update_response_redirect}")
		local reason=""
		if echo "$update_response" | grep --silent "${TOKEN_ERROR_SAVING_PURL}"; then			
			reason=$(echo "$update_response" | sed -n "s/.*$TOKEN_ERROR_SAVING_PURL \(.*\)/\1/p")			
			[ "$reason" = "$TOKEN_PURL_UNCHANGED" ] && echo "success (ULR has not changed)" || fatal "failed to update PURL: $reason"		
		elif echo "$update_response" | grep --silent "${TOKEN_SUCCESS_SAVING_PURL}"; then
			echo "success"
		else
			fatal "failed to update PURL"
		fi		
	elif [ "$update_response_code" -ne 200 ]; then
		fatal "failed to update PURL (HTTP status code ${update_response})"
	else
		echo "success"
	fi
	
	echo -e -n "Logging out...\t\t\t"
	curl --silent --show-error --fail --cookie "$cookie" "${ACCOUNT_URL}/logout" || fatal "failed to log out"
	echo "success"

	[ -f "$cookie" ] && rm --force "$cookie"
	echo "PURL redirect updated successfully."
}

main "$@"