package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.core.apis.IAPIEnvironment;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.util.Iterator;
import java.util.List;

/**
 * Patches the HTTP API
 */
@MergeVisitor.Rename(from = "org/squiddev/cctweaks/core/patch/HTTPRequest_Patch", to = "dan200/computercraft/core/apis/HTTPRequest")
public class HTTPAPI_Patch {
	@MergeVisitor.Stub
	private List<HTTPRequest_Patch> m_httpRequests;
	@MergeVisitor.Stub
	private IAPIEnvironment m_apiEnvironment;

	public void advance(double _dt) {
		synchronized (m_httpRequests) {
			Iterator<HTTPRequest_Patch> it = m_httpRequests.iterator();
			while (it.hasNext()) {
				HTTPRequest_Patch h = it.next();
				if (h.isComplete()) {
					String url = h.getURL();
					if (h.wasSuccessful()) {
						m_apiEnvironment.queueEvent("http_success", new Object[]{url, h.asResponse()});
					} else {
						m_apiEnvironment.queueEvent("http_failure", new Object[]{url, "Could not connect", h.asResponse()});
					}
					it.remove();
				}
			}
		}
	}
}
