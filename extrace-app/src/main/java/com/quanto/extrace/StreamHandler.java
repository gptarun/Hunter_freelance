package com.quanto.extrace;

import java.io.IOException;

public interface StreamHandler {
	void handleStreamBuffer(byte[] buffer, int offset, int length) throws IOException;
}
