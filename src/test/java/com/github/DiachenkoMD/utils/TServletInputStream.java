package com.github.DiachenkoMD.utils;

import jakarta.servlet.ServletInputStream;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;

/**
 * Class-wrapped of mocked version of ServletInputStream to use in HttpServletRequest. <br/>
 */
public class TServletInputStream {

    /**
     * Returns mocked version of ServletInputStream to use in HttpServletRequest.
     * @param expectedReturn string to be converted into ServletInputStream and read somewhere in HttpServletRequest as a body of request.
     * @return ServletInputStream
     * @throws IOException
     */
    public static ServletInputStream getMockedInputStream(String expectedReturn) throws IOException {
        byte[] expectedReturnBytes = expectedReturn.getBytes();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(expectedReturnBytes);

        ServletInputStream _servletInputStream = mock(ServletInputStream.class);

        when(_servletInputStream.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(invocationOnMock -> {
                Object[] args = invocationOnMock.getArguments();
                byte[] output = (byte[]) args[0];
                int offset = (int) args[1];
                int length = (int) args[2];
                return byteArrayInputStream.read(output, offset, length);
        });

        return _servletInputStream;
    }
}
