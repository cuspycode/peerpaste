all:	bin/peerpaste.jar mac/PeerPaste.zip

java/target/peerpaste.jar:
	(cd java && mvn clean install)

bin/peerpaste.jar:	java/target/peerpaste.jar java/peerpaste.sh
	cp java/target/peerpaste.jar bin/peerpaste.jar
	cp java/peerpaste.sh bin/peerpaste.sh

mac/PeerPaste.zip:	java/target/peerpaste.jar
	rm -rf mac/PeerPaste.app
	jar2app java/target/peerpaste.jar -i mac/peerpaste.icns mac/PeerPaste
	(cd mac && zip -r PeerPaste PeerPaste.app)
	rm -rf mac/PeerPaste.app

clean:
	rm -rf java/target bin/peerpaste.jar bin/peerpaste.sh mac/PeerPaste.zip

