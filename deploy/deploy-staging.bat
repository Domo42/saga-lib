@ECHO OFF
SET targetVersion=0.1
SET baseDir=%~dp0

REM change version number in project tree
CD ..
CALL mvn versions:set -DgenerateBackupPoms=false -DnewVersion=%targetVersion%


REM change version numbers on artifact deployment poms
CD deploy
CALL mvn --file saga-lib.pom.xml versions:set -DgenerateBackupPoms=false -DnewVersion=%targetVersion%

gpg -u AA9AEC3C --sign --detach-sign -a saga-lib.pom.xml

REM deploy base artifacts
call mvn deploy:deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy ^
                            -DrepositoryId=sonatype-nexus-staging ^
                            -Dfile=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%.jar ^
                            -DpomFile=%baseDir%\saga-lib.pom.xml ^
                            -Djavadoc=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%-javadoc.jar ^
                            -Dsources=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%-sources.jar

REM deploy signatures
call mvn deploy:deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy ^
                            -DrepositoryId=sonatype-nexus-staging ^
                            -Dfile=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%.jar.asc ^
                            -DpomFile=%baseDir%\saga-lib.pom.xml

call mvn deploy:deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy ^
                            -DrepositoryId=sonatype-nexus-staging ^
                            -Dfile=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%-javadoc.jar.asc ^
                            -DpomFile=%baseDir%\saga-lib.pom.xml

call mvn deploy:deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy ^
                            -DrepositoryId=sonatype-nexus-staging ^
                            -Dfile=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%-sources.jar.asc ^
                            -DpomFile=%baseDir%\saga-lib.pom.xml

GOTO:eof

:error
    ECHO.
    ECHO -----------------------------------
    ECHO - Error performing project staging
    ECHO -----------------------------------
    ECHO.
    EXIT /B