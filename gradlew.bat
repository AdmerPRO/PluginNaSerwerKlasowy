@ECHO OFF
SETLOCAL

SET APP_HOME=%~dp0

IF "%JAVA_HOME%"=="" (
    SET JAVACMD=java.exe
) ELSE (
    SET JAVACMD=%JAVA_HOME%\bin\java.exe
)

SET CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

"%JAVACMD%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

ENDLOCAL