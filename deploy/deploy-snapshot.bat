SET targetVersion=0.1-SNAPSHOT
SET baseDir=%~dp0

call mvn deploy:deploy-file -Durl=https://oss.sonatype.org/content/repositories/snapshots ^
                            -DrepositoryId=sonatype-nexus-snapshots ^
                            -Dfile=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%.jar ^
                            -DpomFile=%baseDir%\saga-lib.pom.xml ^
                            -Djavadoc=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%-javadoc.jar ^
                            -Dsources=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%-sources.jar
