package org.squiddev.cctweaks.core.asm;

import org.apache.commons.io.IOUtils;
import org.squiddev.patcher.Logger;
import org.squiddev.patcher.transformer.TransformationChain;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Custom transformation chain with replacing classes in /patch/ directory
 */
public class CustomChain extends TransformationChain {
	protected ArrayList<String> patches = new ArrayList<String>();

	@Override
	public byte[] transform(String className, byte[] bytes) throws Exception {
		for (String patch : patches) {
			if (className.startsWith(patch)) {
				String source = "patch/" + className.replace('.', '/') + ".class";

				InputStream stream = CustomChain.class.getClassLoader().getResourceAsStream(source);
				if (stream != null) {
					bytes = IOUtils.toByteArray(stream);
					break;
				} else {
					Logger.warn("Cannot find custom rewrite " + source);
				}
			}
		}
		return super.transform(className, bytes);
	}

	public void addPatchFile(String file) {
		patches.add(file);
	}
}
