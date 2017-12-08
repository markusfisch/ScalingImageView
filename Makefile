PACKAGE = de.markusfisch.android.scalingimageviewdemo
APK = app/build/outputs/apk/debug/app-debug.apk

all: debug install start

debug:
	./gradlew assembleDebug

release: lint findbugs
	./gradlew build

lint:
	./gradlew lintDebug

findbugs:
	./gradlew findBugs

sonarqube:
	./gradlew sonarqube

infer: clean
	infer -- ./gradlew assembleDebug

install:
	adb $(TARGET) install -r $(APK)

start:
	adb $(TARGET) shell 'am start -n $(PACKAGE)/.activity.MainActivity'

uninstall:
	adb $(TARGET) uninstall $(PACKAGE)

clean:
	./gradlew clean
