.PHONY: all clean dbkg eclipse

all: build-openfire

# Can not use 'build' as target name, because there is a
# directory called build
build-openfire:
	cd build && ant

clean:
	cd build && ant clean

dpkg:
	cd build && ant installer.debian

rpm:
	cd build && ant installer.rpm
	cd target/release && sudo rpm -ivh  --replacefiles --force openfire-4.1.0.alpha-1.i386.rpm

plugins:
	cd build && ant plugins

eclipse: .settings .classpath .project

.settings:
	ln -s build/eclipse/settings .settings

.classpath:
	ln -s build/eclipse/classpath .classpath

.project:
	ln -s build/eclipse/project .project
