# Introduction

This tool checks a list of url's from stdin and reports http response codes as well as recurses
http and meta redirection.

## Interactive example

    $ java -jar urlcheck.jar
    http://google.com
    http://google.com [200]
    http://gmail.com
    http://gmail.com [200] [REDIR] -> https://mail.google.com/mail/ [200]
    quit

## Batch example

    $ echo 'http://google.com' > urls.dat
    $ java -jar urlcheck.jar < urls.dat
    http://google.com [200]

# Download precompiled jar

[urlcheck.jar](https://db.tt/2PjE3iGZ)

# Building

This project uses the [Gradle](www.gradle.org) build automation tool.

## Artifacts

    src/main/groovy/urlcheck.groovy | Source code
    build/libs/urlcheck.jar         | Standalone executable jar (after build)

## Linux / OS X

### Install Gradle

    $ gradlew

### Build the project

    $ gradle build

## Windows

### Install Gradle

    X:\urlcheck>gradlew.bat

### Build the project

    X:\urlcheck>gradle build
