#!/bin/bash

# $PWD gives the current working dir
export MAVEN_CONFIG="$PWD"

mkdir -p "$MAVEN_CONFIG/.m2/repository"
cat > "$MAVEN_CONFIG/.m2/settings.xml" << EOF
<settings>
    <localRepository>$MAVEN_CONFIG/.m2</localRepository>
</settings>
EOF

# chown -R "$USER:$GROUP" "$MAVEN_CONFIG"

/bin/bash -c "$@" 
