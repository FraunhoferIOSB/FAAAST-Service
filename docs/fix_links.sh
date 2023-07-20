# Fix the links pre-build. In this case, edit the markdown file rather than
  # the resulting HTML
FILE="docs/source/assetconnections/http_assetconnection.md"
if [[ "$1" != "" ]]; then
  FILE="$1"
fi
sed -E 's/\[Python\]\((.*).md\)/\[Python\]\(\1.html\)/g' -i ${FILE}