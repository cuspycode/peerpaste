all:	bin/peerpaste.jar mac/PeerPaste.zip

java/target/peerpaste.jar:
	(cd java && mvn clean install)

bin/peerpaste.jar:	java/target/peerpaste.jar java/peerpaste.sh
	cp java/target/peerpaste.jar bin/peerpaste.jar
	cp java/peerpaste.sh bin/peerpaste.sh

mac/PeerPaste.zip:	java/target/peerpaste.jar
	cp java/target/peerpaste.jar mac/PeerPaste.app/Contents/Java/
	cp mac/peerpaste.icns mac/PeerPaste.app/Contents/Resources/
	(cd mac && zip -r PeerPaste PeerPaste.app)
	rm mac/PeerPaste.app/Contents/Java/peerpaste.jar
	rm mac/PeerPaste.app/Contents/Resources/peerpaste.icns

clean:
	rm -rf java/target bin/peerpaste.jar bin/peerpaste.sh mac/PeerPaste.zip




