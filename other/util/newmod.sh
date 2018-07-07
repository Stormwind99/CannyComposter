#!/bin/sh
#
# Create a new Minecraft mod workspace from a template
#

if [ $# -lt 1 ]
then
	echo "Usage: $0 NAME"
	exit 1
fi

# Config
SRC=ExampleMod
MCVER=1.12.2
FORGEVER=14.23.4.2705
MAPPINGVER=snapshot_20180531
GITHUBUSER=Stormwind99
BINTRAYUSER=stormwind
GROUPDOT="com.wumple"
GITLOC="git@github.com"
TEMPLATEREPO="git@github.com:Stormwind99/${SRC}.git"
PRIVATEPROPS=newmod/private.properties
CFG=newmod/newmod.cfg

if [ -f ${CFG} ]
then
	. ${CFG}
fi

# script dir
DIR=`dirname $0`
DIR=`realpath ${DIR}`

# new mod name, caps and no spaces
NAME=$1
# template workspace dir
SRCWORKSPACE=${SRC}Workspace
# dest workspace dir
WORKSPACE=${NAME}Workspace
# lower case version of template name
SRCLOWER=${SRC,,}
# lower case version of dest name
NAMELOWER=${NAME,,}
# template namespace
SRCGROUP=examplens
# dest namespace
GROUPSLASH=`tr . / <<< "${GROUPDOT}"`

# set to echo to test
#TEST=echo
TEST=

# start from the scripts dir
$TEST cd $DIR
if [ -d "${WORKSPACE}" ]
then
	echo "${WORKSPACE} already exists!"
	exit 2
fi

# if template missing, get from remote repo
if [ ! -d "${SRCWORKSPACE}" ]
then	
	echo "Clone template repo ${TEMPLATEREPO}..."
	$TEST mkdir ${SRCWORKSPACE}
	$TEST cd ${SRCWORKSPACE}
	$TEST git clone ${TEMPLATEREPO}
	$TEST cd ..
else
	echo "Using template ${SRCWORKSPACE}..."
fi

# Copy template and change names
echo "Copy template and change names..."

$TEST cp -a ${SRCWORKSPACE} ${WORKSPACE}
$TEST mv ${WORKSPACE}/${SRC} ${WORKSPACE}/${NAME}
# for Curseforge, Github, Bintray API keys/tokens
if [ -f "${PRIVATEPROPS}" ]
then
	$TEST cp "${PRIVATEPROPS}" ${WORKSPACE}/${NAME}
fi
$TEST cd ${WORKSPACE}/${NAME}

$TEST mkdir -p src/main/java/${GROUPSLASH}
$TEST mv src/main/java/${SRCGROUP}/${SRCLOWER}/${SRC}.java  src/main/java/${SRCGROUP}/${SRCLOWER}/${NAME}.java
$TEST mv src/main/java/${SRCGROUP}/${SRCLOWER}  src/main/java/${GROUPSLASH}/${NAMELOWER}
$TEST rmdir src/main/java/${SRCGROUP}

# modify source and other files
$TEST sed \
	-e "s/${SRC}/${NAME}/g" \
	-e "s/${SRCLOWER}/${NAMELOWER}/g" \
	-e "s/${SRCGROUP}/${GROUPDOT}/g" \
	-i README.md update.json build.properties src/main/java/${GROUPSLASH}/${NAMELOWER}/*.java

# modify build.properties
$TEST sed \
       	-e "s/GITHUBUSER/${GITHUBUSER}/g" \
       	-e "s/BINTRAYUSER/${BINTRAYUSER}/g" \
	-e "s/MCVER/${MCVER}/g" \
	-e "s/FORGEVER/${FORGEVER}/g" \
	-e "s/MAPPINGVER/${MAPPINGVER}/g" \
	-e "s/GROUPDOT/${GROUPDOT}/g" \
	-i build.properties

# set up git remote
echo "Set up git remote..."

$TEST git remote rm origin
$TEST git remote add origin ${GITLOC}:${GITHUBUSER}/${NAME}.git

# set up git wiki
echo "Set up git wiki..."
$TEST mkdir ../${NAME}.wiki
$TEST cd ../${NAME}.wiki
$TEST git init .
$TEST git remote add origin ${GITLOC}:${GITHUBUSER}/${NAME}.wiki.git
$TEST touch Home.md
$TEST git add Home.md
$TEST git commit -m "Initial commit"
$TEST cd ../${NAME}

# run forgegradle steps
echo "Run forgegradle steps: setupDecompWorkspace eclipse..."

$TEST ./gradlew.bat setupDecompWorkspace
$TEST ./gradlew.bat eclipse

# Done

echo "Run a test build..."
$TEST ./gradlew.bat build

echo
echo Now change more settings in build.properties and private.properties
echo
