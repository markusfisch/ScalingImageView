PACKAGE = de.markusfisch.android.scalingimageview
APK = app/build/outputs/apk/app-debug.apk

all: debug install start

debug:
	./gradlew assembleDebug

release: lint findbugs
	./gradlew build

lint:
	./gradlew lintDebug

findbugs:
	./gradlew findBugs

install:
	adb $(TARGET) install -r $(APK)

start:
	adb $(TARGET) shell 'am start -n $(PACKAGE)/.activity.MainActivity'

uninstall:
	adb $(TARGET) uninstall $(PACKAGE)

clean:
	./gradlew clean
