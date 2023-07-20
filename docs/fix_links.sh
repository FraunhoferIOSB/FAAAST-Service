# Fix the links pre-build. In this case, edit the markdown file rather than
  # the resulting HTML
  FILE="docs/source/assetconnections/http_assetconnection.md"
  if [[ "$1" != "" ]]; then
    FILE="$1"
  fi
  sed -E 's/\[Python\]\((.*).md\)/\[Python\]\(\1.html\)/g' -i ${FILE}
else
  # Fix the links post-build: rewrite the HTML after it's been generated. Was
  # not able to get this to work on Read the Docs.
  FILE="_build/html/http_assetconnection.html"
  if [[ "$1" != "" ]]; then
    FILE="$1"
  fi
  sed -E 's/a href="(.*)\.md"/a href="\1\.html"/g' -i ${FILE}
fi