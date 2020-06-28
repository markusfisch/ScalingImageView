PACKAGE = de.markusfisch.android.scalingimageviewdemo

all: debug install start

debug:
	./gradlew assembleDebug

release: lint
	./gradlew build

aar:
	./gradlew :scalingimageview:assembleRelease

lint:
	./gradlew lintDebug

infer: clean
	infer -- ./gradlew assembleDebug

install:
	adb $(TARGET) install -r app/build/outputs/apk/debug/app-debug.apk

start:
	adb $(TARGET) shell 'am start -n $(PACKAGE)/.activity.MainActivity'

uninstall:
	adb $(TARGET) uninstall $(PACKAGE)

clean:
	./gradlew clean
