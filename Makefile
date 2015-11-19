PACKAGE = de.markusfisch.android.imageviewmatrixprobe
APK = ImageViewMatrixProbe/build/outputs/apk/ImageViewMatrixProbe-debug.apk

all: debug install start

debug:
	./gradlew assembleDebug

lint:
	./gradlew lintDebug

apk:
	./gradlew build

install:
	adb $(TARGET) install -rk $(APK)

start:
	adb $(TARGET) shell 'am start -n $(PACKAGE)/.activity.MainActivity'

uninstall:
	adb $(TARGET) uninstall $(PACKAGE)

clean:
	./gradlew clean
