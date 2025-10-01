#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass any JVM options to Gradle and Java processes.
DEFAULT_JVM_OPTS=""

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "ERROR: $*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched.
if ${cygwin} ; then
    [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done

APP_HOME=`dirname "$PRG"`

# For Cygwin, switch paths to Windows format before running java
if ${cygwin} ; then
  APP_HOME=`cygpath --path --windows "$APP_HOME"`
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
fi

# Set-up and run the command
#
# This function is the entry point for running the command. It is responsible for
# locating the launcher, resolving the classpath, and launching the command.
#
# It is passed all of the arguments given to the script.
run_command() {
    # Determine the Java command to use to start the JVM.
    if [ -n "$JAVA_HOME" ] ; then
        if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
            # IBM's JDK on AIX uses strange locations for the executables
            JAVACMD="$JAVA_HOME/jre/sh/java"
        else
            JAVACMD="$JAVA_HOME/bin/java"
        fi
        if [ ! -x "$JAVACMD" ] ; then
            die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
        fi
    else
        JAVACMD="java"
        which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi

    # Increase the maximum file descriptors if we can.
    if ! ${cygwin} && ! ${darwin} && ! ${nonstop} ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            # Maximum file descriptors is the current hard limit of the system.
            # Other values are passed to ulimit simply.
            MAX_FD_LIMIT=`ulimit -H -n`
            if [ $? -eq 0 ] ; then
                if [ "$MAX_FD_LIMIT" != 'unlimited' ] ; then
                    ulimit -n $MAX_FD_LIMIT
                fi
            else
                warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
            fi
        else
            ulimit -n $MAX_FD
        fi
    fi

    # Add the launcher to the CLASSPATH. The launcher is responsible for
    # finding the other jars that are required to run the command.
    CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

    # Split up the JVM options only if the FULL_JVM_OPTS variable is not set.
    # This allows ALL of the options to be passed in a single variable, which
    # is required for some cases.
    if [ -z "$FULL_JVM_OPTS" ] ; then
        # The default set of JVM options are specified in the DEFAULT_JVM_OPTS variable.
        # The user can specify additional options in the JAVA_OPTS and GRADLE_OPTS variables.
        #
        # The user's options are placed first so that they can override any of the
        # default options.
        FULL_JVM_OPTS="$JAVA_OPTS $GRADLE_OPTS $DEFAULT_JVM_OPTS"
        FULL_JVM_OPTS=`echo $FULL_JVM_OPTS | tr ' ' '\n' | grep . | tr '\n' ' '`
    fi

    # Launch the command.
    # The following variables are used to launch the command:
    #
    #    JAVACMD         - the Java command to use.
    #    FULL_JVM_OPTS   - the JVM options to use.
    #    CLASSPATH       - the classpath to use.
    #    APP_MAIN_CLASS  - the main class to run.
    #    "$@"            - the arguments to pass to the main class.
    #
    exec "$JAVACMD" $FULL_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
}

run_command "$@"