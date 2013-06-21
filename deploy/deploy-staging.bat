@ECHO OFF
SET targetVersion=0.1
SET baseDir=%~dp0
SET targetUrl=https://oss.sonatype.org/service/local/staging/deploy/maven2

REM change version number in project tree
CD ..
CALL mvn versions:set -DgenerateBackupPoms=false -DnewVersion=%targetVersion%


REM change version numbers on artifact deployment poms
CD deploy
CALL mvn --file saga-lib.pom.xml versions:set -DgenerateBackupPoms=false -DnewVersion=%targetVersion%

del *.asc

CD ..
call mvn clean install -DperformRelease=true -Dgpg.keyname=AA9AEC3C

CD deploy
SET pomAscFile=saga-lib-%targetVersion%.pom.asc

gpg -u AA9AEC3C --sign --detach-sign -o %pomAscFile% -a saga-lib.pom.xml


REM deploy base artifacts
call mvn deploy:deploy-file -Durl=%targetUrl% ^
                            -DrepositoryId=sonatype-nexus-staging ^
                            -Dfile=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%.jar ^
                            -DpomFile=%baseDir%\saga-lib.pom.xml ^
                            -Djavadoc=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%-javadoc.jar ^
                            -Dsources=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%-sources.jar
                            -Dfiles

REM deploy signatures
call mvn deploy:deploy-file -Durl=%targetUrl% ^
                            -DrepositoryId=sonatype-nexus-staging ^
                            -Dfile=%baseDir%\%pomAscFile% ^
                            -DpomFile=%baseDir%\saga-lib.pom.xml ^
                            -Dpackaging=pom.asc

call mvn deploy:deploy-file -Durl=%targetUrl% ^
                            -DrepositoryId=sonatype-nexus-staging ^
                            -Dfile=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%.jar.asc ^
                            -DpomFile=%baseDir%\saga-lib.pom.xml ^
                            -Dpackaging=jar.asc

call mvn deploy:deploy-file -Durl=%targetUrl% ^
                            -DrepositoryId=sonatype-nexus-staging ^
                            -Dfile=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%-javadoc.jar.asc ^
                            -DpomFile=%baseDir%\saga-lib.pom.xml ^
                            -Dclassifier=javadoc ^
                            -Dpackaging=jar.asc

call mvn deploy:deploy-file -Durl=%targetUrl% ^
                            -DrepositoryId=sonatype-nexus-staging ^
                            -Dfile=%baseDir%\..\saga-lib\target\saga-lib-%targetVersion%-sources.jar.asc ^
                            -DpomFile=%baseDir%\saga-lib.pom.xml ^
                            -Dclassifier=sources ^
                            -Dpackaging=jar.asc

GOTO:eof

:error
    ECHO.
    ECHO -----------------------------------
    ECHO - Error performing project staging
    ECHO -----------------------------------
    ECHO.
    EXIT /B