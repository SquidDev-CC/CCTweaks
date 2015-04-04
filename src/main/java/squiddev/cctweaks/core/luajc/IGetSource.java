package squiddev.cctweaks.core.luajc;

import org.luaj.vm2.Prototype;

/**
 * Interface used to get current debugging info
 */
public interface IGetSource {
	String getSource();

	int getLine();

	Prototype getPrototype();
}