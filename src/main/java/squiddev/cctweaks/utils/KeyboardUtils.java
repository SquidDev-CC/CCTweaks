package squiddev.cctweaks.utils;

import org.lwjgl.input.Keyboard;

/**
 * Why aren't there any keyboard constants?
 */
public final class KeyboardUtils {
	public static boolean isCtrlKeyDown() {
        return Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
    }

    public static boolean isShiftKeyDown() {
        return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
    }
}
