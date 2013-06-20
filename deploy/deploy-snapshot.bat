SET targetVersion=0.1-SNAPSHOT
SET baseDir=%~dp0

REM change version number in project tree
CD ..
CALL mvn versions:set -DgenerateBackupPoms=false -DnewVersion=%targetVersion%


REM change version numbers on artifact deployment poms
CD deploy
CALL mvn --file saga-lib.pom.xml versions:set -DgenerateBackupPoms=false -DnewVersion=%targetVersion%


CALL mvn deploy:deploy-file -Durl=https://oss.sonatype.org/content/repositories/snapshots ^
                            -DrepositoryId=sonatype-nexus-snapshots ^
                            -Dfile=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%.jar ^
                            -DpomFile=%baseDir%\saga-lib.pom.xml ^
                            -Djavadoc=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%-javadoc.jar ^
                            -Dsources=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%-sources.jar
