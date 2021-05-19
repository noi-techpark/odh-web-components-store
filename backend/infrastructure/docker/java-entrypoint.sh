#!/bin/bash

mkdir -p /var/maven/.m2/repository
cat > /var/maven/.m2/settings.xml << EOF
<settings>
    <localRepository>/var/maven/.m2</localRepository>
</settings>
EOF

export MAVEN_CONFIG="$HOME"

chown -R jenkins:jenkins /var/maven

#/bin/bash -c "/usr/local/bin/mvn-entrypoint.sh $@"
/bin/bash -c "$@" 
